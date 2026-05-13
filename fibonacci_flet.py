"""
Flet frontend — Fibonacci implementation comparator.
POC validating the architecture before migrating to FastAPI + React.
"""

import threading
from typing import List, Optional

import flet as ft

from fibonacci_algorithms import MAX_N, MAX_N_NAIVE
from fibonacci_service import AlgoResult, ComputeResponse, compute_all

_COLORS: dict[str, str] = {
    "recursive": "#EF5350",
    "memoized":  "#42A5F5",
    "iterative": "#66BB6A",
    "sympy":     "#AB47BC",
}
_COLOR_FALLBACK = "#9E9E9E"


def _shorten(value: int, max_len: int = 22) -> str:
    s = str(value)
    return s if len(s) <= max_len else s[: max_len - 1] + "…"


def _build_card(r: AlgoResult) -> ft.Card:
    color = _COLORS.get(r.name, _COLOR_FALLBACK)
    rows: List[ft.Control] = [
        ft.Container(
            content=ft.Text(r.label, weight=ft.FontWeight.BOLD, color=color, size=14),
            border=ft.border.only(left=ft.BorderSide(3, color)),
            padding=ft.padding.only(left=8),
        ),
        ft.Divider(height=10, color=ft.Colors.TRANSPARENT),
    ]

    if r.skipped:
        display = r.skip_reason or "ignorée"
        rows.append(ft.Text(display, italic=True, color=ft.Colors.GREY_600, size=12))
    else:
        assert r.result is not None
        rows += [
            ft.Text(f"F({r.n})", size=11, color=ft.Colors.GREY_400),
            ft.Text(_shorten(r.result), size=13, selectable=True, weight=ft.FontWeight.BOLD),
            ft.Divider(height=4, color=ft.Colors.TRANSPARENT),
            ft.Row(
                [ft.Icon(ft.Icons.TIMER_OUTLINED, size=13, color=ft.Colors.GREY_400),
                 ft.Text(f"{r.time_s:.6f} s", size=12)],
                spacing=4,
            ),
        ]
        if r.cache_hits is not None:
            rows += [
                ft.Divider(height=6),
                ft.Row([ft.Icon(ft.Icons.DONE, size=13, color="#66BB6A"),
                        ft.Text(f"Hits    {r.cache_hits}", size=12)], spacing=4),
                ft.Row([ft.Icon(ft.Icons.CLOSE, size=13, color="#EF5350"),
                        ft.Text(f"Misses  {r.cache_misses}", size=12)], spacing=4),
            ]

    return ft.Card(
        content=ft.Container(
            content=ft.Column(rows, tight=True, spacing=2),
            padding=14,
            width=200,
        ),
        elevation=3,
    )


def _build_perf_bars(resp: ComputeResponse) -> ft.Column:
    computed = [r for r in resp.results if not r.skipped]
    max_t = max((r.time_s for r in computed), default=1) or 1

    bar_rows: List[ft.Control] = []
    for r in computed:
        bar_rows.append(ft.Row(
            [
                ft.Text(r.label, width=175, size=12),
                ft.Container(
                    content=ft.ProgressBar(
                        value=r.time_s / max_t,
                        color=_COLORS.get(r.name, _COLOR_FALLBACK),
                        bgcolor="#22FFFFFF",
                    ),
                    expand=True, height=16,
                ),
                ft.Text(f"{r.time_s:.6f} s", width=110, size=12,
                        text_align=ft.TextAlign.RIGHT),
            ],
            vertical_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=12,
        ))

    return ft.Column(
        [ft.Text("Performance relative", size=15, weight=ft.FontWeight.BOLD),
         ft.Divider(height=8),
         *bar_rows],
        spacing=6,
    )


def _build_cache_vis(r: AlgoResult) -> ft.Column:
    total = (r.cache_hits or 0) + (r.cache_misses or 0)
    if total == 0:
        return ft.Column([])
    hit_ratio = (r.cache_hits or 0) / total

    def bar_row(label: str, color: str, value: float, count: int) -> ft.Row:
        pct = value * 100
        return ft.Row(
            [
                ft.Text(label, width=70, size=12, color=color),
                ft.Container(
                    content=ft.ProgressBar(value=value, color=color, bgcolor="#22FFFFFF"),
                    expand=True, height=16,
                ),
                ft.Text(f"{count}  ({pct:.0f}%)", width=95, size=12,
                        text_align=ft.TextAlign.RIGHT),
            ],
            vertical_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=12,
        )

    return ft.Column(
        [
            ft.Text("Cache lru_cache (mémoïsée)", size=15, weight=ft.FontWeight.BOLD),
            ft.Divider(height=8),
            bar_row("Hits",   "#66BB6A", hit_ratio,     r.cache_hits or 0),
            bar_row("Misses", "#EF5350", 1 - hit_ratio, r.cache_misses or 0),
        ],
        spacing=6,
    )


def main(page: ft.Page) -> None:
    page.title = "Fibonacci — Comparaison d'implémentations"
    page.theme_mode = ft.ThemeMode.DARK
    page.padding = ft.padding.symmetric(horizontal=28, vertical=20)
    page.scroll = ft.ScrollMode.AUTO
    try:
        page.window.width = 920
        page.window.min_width = 700
    except Exception:
        pass

    # ── Input controls ───────────────────────────────────────────────────────
    n_field = ft.TextField(
        value="30", width=90,
        keyboard_type=ft.KeyboardType.NUMBER,
        text_align=ft.TextAlign.CENTER,
        border_radius=8,
        label="n",
    )
    n_slider = ft.Slider(min=0, max=50, divisions=50, value=30,
                         label="{value}", expand=True)
    warning_text = ft.Text(
        f"⚠️  n > {MAX_N_NAIVE} — la version naïve sera très lente !",
        color="#FFB300", visible=False, size=13,
    )
    include_naive_cb = ft.Checkbox(label="Inclure la récursive naïve", value=True)
    loading_ring = ft.ProgressRing(width=22, height=22, stroke_width=3, visible=False)
    compute_btn = ft.ElevatedButton(
        "Calculer", icon=ft.Icons.PLAY_ARROW,
        style=ft.ButtonStyle(bgcolor=ft.Colors.BLUE_700),
    )

    # ── Dynamic sections ─────────────────────────────────────────────────────
    cards_row   = ft.Row(wrap=True, spacing=12, run_spacing=12)
    integrity_row = ft.Row(visible=False, spacing=8)
    perf_col    = ft.Column(visible=False)
    cache_col   = ft.Column(visible=False)
    winner_box  = ft.Container(visible=False)

    # ── Slider ↔ field sync ──────────────────────────────────────────────────
    def refresh_warning() -> None:
        try:
            n_val = int(n_field.value or "0")
            warning_text.visible = n_val > MAX_N_NAIVE and bool(include_naive_cb.value)
        except ValueError:
            warning_text.visible = False

    def on_slider(e: ft.ControlEvent) -> None:
        n_field.value = str(int(n_slider.value))
        refresh_warning()
        page.update()

    def on_field(e: ft.ControlEvent) -> None:
        try:
            v = max(0, int(n_field.value or "0"))
            n_slider.value = float(min(v, 50))
        except ValueError:
            pass
        refresh_warning()
        page.update()

    def on_naive_cb_change(e: ft.ControlEvent) -> None:
        refresh_warning()
        page.update()

    n_slider.on_change = on_slider
    n_field.on_change  = on_field
    include_naive_cb.on_change = on_naive_cb_change

    # ── Compute ──────────────────────────────────────────────────────────────
    def on_compute(e: Optional[ft.ControlEvent] = None) -> None:
        try:
            n = int(n_field.value or "")
            if not (0 <= n <= MAX_N):
                raise ValueError
        except ValueError:
            page.open(ft.SnackBar(
                content=ft.Text(f"Entrez un entier entre 0 et {MAX_N}."),
            ))
            page.update()
            return

        compute_btn.disabled = True
        loading_ring.visible = True
        cards_row.controls.clear()
        integrity_row.visible = False
        perf_col.visible      = False
        cache_col.visible     = False
        winner_box.visible    = False
        page.update()

        def run() -> None:
            try:
                resp = compute_all(n, include_naive=bool(include_naive_cb.value))

                cards_row.controls = [_build_card(r) for r in resp.results]

                ok = resp.all_match
                integrity_row.controls = [
                    ft.Icon(
                        ft.Icons.CHECK_CIRCLE if ok else ft.Icons.ERROR,
                        color=ft.Colors.GREEN_400 if ok else ft.Colors.RED_400,
                        size=18,
                    ),
                    ft.Text(
                        "Tous les résultats sont identiques." if ok
                        else "Incohérence détectée !",
                        size=13,
                    ),
                ]
                integrity_row.visible = True

                perf_col.controls = [_build_perf_bars(resp)]
                perf_col.visible  = True

                memo: Optional[AlgoResult] = next(
                    (r for r in resp.results if r.name == "memoized" and not r.skipped), None
                )
                if memo and memo.cache_hits is not None:
                    cache_col.controls = [_build_cache_vis(memo)]
                    cache_col.visible  = True

                winner_box.content = ft.Container(
                    content=ft.Row(
                        [
                            ft.Icon(ft.Icons.EMOJI_EVENTS, color=ft.Colors.AMBER_400, size=26),
                            ft.Text(
                                f"Plus rapide : {resp.best_name}  —  {resp.best_time:.6f} s",
                                size=15, weight=ft.FontWeight.BOLD,
                            ),
                        ],
                        spacing=10,
                    ),
                    padding=16,
                    bgcolor="#22FFB300",
                    border_radius=8,
                    border=ft.border.all(1, "#44FFB300"),
                )
                winner_box.visible = True

            except Exception as exc:
                page.open(ft.SnackBar(content=ft.Text(f"Erreur de calcul : {exc}")))
            finally:
                compute_btn.disabled = False
                loading_ring.visible = False
                page.update()

        threading.Thread(target=run, daemon=True).start()

    compute_btn.on_click = on_compute

    # ── Layout ───────────────────────────────────────────────────────────────
    page.add(
        ft.Text("Fibonacci", size=28, weight=ft.FontWeight.BOLD),
        ft.Text(
            "Comparaison d'implémentations Python — Cache · Récursion · Itération · SymPy",
            size=13, color=ft.Colors.GREY_400,
        ),
        ft.Divider(height=20),

        ft.Row([n_slider, n_field],
               vertical_alignment=ft.CrossAxisAlignment.CENTER, spacing=12),
        warning_text,
        ft.Row(
            [include_naive_cb, ft.Container(expand=True), loading_ring, compute_btn],
            vertical_alignment=ft.CrossAxisAlignment.CENTER, spacing=10,
        ),
        ft.Divider(height=20),

        cards_row,
        integrity_row,
        ft.Divider(height=16),
        perf_col,
        ft.Divider(height=16),
        cache_col,
        ft.Divider(height=16),
        winner_box,
    )


if __name__ == "__main__":
    ft.app(target=main)
