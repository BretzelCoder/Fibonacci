"""
CLI for comparing Fibonacci algorithm performance.

Usage:
    python MyFibonacciTest.py
"""

import sys
from typing import Optional, Tuple

from fibonacci_algorithms import (
    MAX_N,
    MAX_N_NAIVE,
    fibonacci_iterative,
    fibonacci_memoized,
    fibonacci_recursive,
    fibonacci_sympy,
    measure_execution,
)


def _fmt_ratio(time_ref: float, time_cmp: float) -> str:
    """Returns a human-readable speedup or slowdown label."""
    ratio = time_ref / time_cmp
    count = f"{ratio:,.0f}".replace(",", " ")  # narrow no-break space
    if ratio >= 1:
        return f"≈ {count}× plus rapide"
    inv = f"{1 / ratio:,.0f}".replace(",", " ")
    return f"≈ {inv}× plus lent"


def _print_comparison(
    label_cmp: str,
    label_ref: str,
    time_ref: float,
    time_cmp: float,
) -> None:
    if time_ref <= 0 or time_cmp <= 0:
        return
    pct = (time_cmp / time_ref) * 100
    tag = f"{label_cmp} vs {label_ref}"
    print(f"{tag:<30}: {pct:.6f}% du temps ({_fmt_ratio(time_ref, time_cmp)})")


def main() -> None:
    user_input = input("Entrez la valeur de n (défaut : 30) : ").strip()

    try:
        n = int(user_input) if user_input else 30
    except ValueError:
        print("Erreur : veuillez entrer un entier valide.", file=sys.stderr)
        sys.exit(1)

    if n < 0:
        print("Erreur : n doit être >= 0.", file=sys.stderr)
        sys.exit(1)

    if n > MAX_N:
        print(
            f"Erreur : n={n} dépasse le plafond MAX_N={MAX_N}. "
            "Choisissez une valeur plus petite.",
            file=sys.stderr,
        )
        sys.exit(1)

    print(f"\n# Évaluation pour n = {n}")

    # ── Naive recursion ──────────────────────────────────────────────────────
    print("\n--- Version Récursive Naïve ---")
    res_naive: Optional[int] = None
    time_naive: Optional[float] = None
    if n > MAX_N_NAIVE:
        print(f"⚠️  n > {MAX_N_NAIVE} — ignorée (complexité O(2^n))")
    else:
        res_naive, time_naive = measure_execution(fibonacci_recursive, n)
        print(f"Résultat : {res_naive}")
        print(f"Temps    : {time_naive:.4f} secondes")

    # ── Memoized recursion ───────────────────────────────────────────────────
    print("\n--- Version avec Cache (Mémoïsation) ---")
    res_memoized, time_memoized = measure_execution(fibonacci_memoized, n)
    print(f"Résultat : {res_memoized}")
    print(f"Temps    : {time_memoized:.6f} secondes")
    stats = fibonacci_memoized.cache_info()
    print(f"Cache    : Hits={stats.hits}, Misses={stats.misses}")

    # ── Iterative ────────────────────────────────────────────────────────────
    print("\n--- Version Itérative Ultra Optimisée ---")
    res_iterative, time_iterative = measure_execution(fibonacci_iterative, n)
    print(f"Résultat : {res_iterative}")
    print(f"Temps    : {time_iterative:.6f} secondes")

    # ── SymPy ────────────────────────────────────────────────────────────────
    print("\n--- Version SymPy ---")
    res_sympy, time_sympy = measure_execution(fibonacci_sympy, n)
    print(f"Résultat : {res_sympy}")
    print(f"Temps    : {time_sympy:.6f} secondes")

    # ── Integrity check ──────────────────────────────────────────────────────
    print("\n--- Vérification de l'intégrité ---")
    computed: list[Tuple[str, int]] = [
        ("mémoïsée", res_memoized),
        ("itérative", res_iterative),
        ("sympy", res_sympy),
    ]
    if res_naive is not None:
        computed.insert(0, ("naïve", res_naive))

    if len({v for _, v in computed}) == 1:
        print(f"✅ Succès : les {len(computed)} implémentations retournent le même résultat.")
    else:
        print("❌ Erreur : incohérence détectée entre les résultats !", file=sys.stderr)

    # ── Performance comparison ───────────────────────────────────────────────
    print("\n--- Comparaison des performances ---")
    if time_naive is not None:
        _print_comparison("Cache",     "Naïve",     time_naive,     time_memoized)
        _print_comparison("Itérative", "Naïve",     time_naive,     time_iterative)
        _print_comparison("SymPy",     "Naïve",     time_naive,     time_sympy)
    _print_comparison("Itérative", "Cache",     time_memoized,  time_iterative)
    _print_comparison("SymPy",     "Cache",     time_memoized,  time_sympy)
    _print_comparison("SymPy",     "Itérative", time_iterative, time_sympy)

    # ── Winner ───────────────────────────────────────────────────────────────
    print("\n--- Conclusion ---")
    timings: dict[str, float] = {
        "Mémoïsée (Cache)": time_memoized,
        "Itérative":        time_iterative,
        "SymPy":            time_sympy,
    }
    if time_naive is not None:
        timings["Récursive Naïve"] = time_naive
    best = min(timings, key=lambda k: timings[k])
    print(f"🏆 La plus efficace pour n={n} : {best} ({timings[best]:.6f} s)")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nProgramme interrompu.", file=sys.stderr)
        sys.exit(0)
