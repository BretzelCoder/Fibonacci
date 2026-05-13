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
    count = f"{ratio:,.0f}"
    if ratio >= 1:
        return f"~{count}x faster"
    inv = f"{1 / ratio:,.0f}"
    return f"~{inv}x slower"


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
    print(f"{tag:<30}: {pct:.6f}% of the time ({_fmt_ratio(time_ref, time_cmp)})")


def main() -> None:
    user_input = input("Enter n (default: 30): ").strip()

    try:
        n = int(user_input) if user_input else 30
    except ValueError:
        print("Error: please enter a valid integer.", file=sys.stderr)
        sys.exit(1)

    if n < 0:
        print("Error: n must be >= 0.", file=sys.stderr)
        sys.exit(1)

    if n > MAX_N:
        print(
            f"Error: n={n} exceeds MAX_N={MAX_N}. Please choose a smaller value.",
            file=sys.stderr,
        )
        sys.exit(1)

    print(f"\n# Evaluation for n = {n}")

    # -- Recursive -----------------------------------------------------------
    print("\n--- Recursive ---")
    res_naive: Optional[int] = None
    time_naive: Optional[float] = None
    if n > MAX_N_NAIVE:
        print(f"Warning: n > {MAX_N_NAIVE} — skipped (O(2^n) complexity)")
    else:
        res_naive, time_naive = measure_execution(fibonacci_recursive, n)
        print(f"Result : {res_naive}")
        print(f"Time   : {time_naive:.4f} seconds")

    # -- Cache ---------------------------------------------------------------
    print("\n--- Cache ---")
    res_memoized, time_memoized = measure_execution(fibonacci_memoized, n)
    print(f"Result : {res_memoized}")
    print(f"Time   : {time_memoized:.6f} seconds")
    stats = fibonacci_memoized.cache_info()
    print(f"Cache  : Hits={stats.hits}, Misses={stats.misses}")

    # -- Iterative -----------------------------------------------------------
    print("\n--- Iterative ---")
    res_iterative, time_iterative = measure_execution(fibonacci_iterative, n)
    print(f"Result : {res_iterative}")
    print(f"Time   : {time_iterative:.6f} seconds")

    # -- Sympy ---------------------------------------------------------------
    print("\n--- Sympy ---")
    res_sympy, time_sympy = measure_execution(fibonacci_sympy, n)
    print(f"Result : {res_sympy}")
    print(f"Time   : {time_sympy:.6f} seconds")

    # -- Integrity check -----------------------------------------------------
    print("\n--- Integrity Check ---")
    computed: list[Tuple[str, int]] = [
        ("cache",     res_memoized),
        ("iterative", res_iterative),
        ("sympy",     res_sympy),
    ]
    if res_naive is not None:
        computed.insert(0, ("recursive", res_naive))

    if len({v for _, v in computed}) == 1:
        print(f"OK  All {len(computed)} implementations return the same result.")
    else:
        print("ERROR  Inconsistency detected between results!", file=sys.stderr)

    # -- Performance comparison ----------------------------------------------
    print("\n--- Performance Comparison ---")
    if time_naive is not None:
        _print_comparison("Cache",     "Recursive", time_naive,     time_memoized)
        _print_comparison("Iterative", "Recursive", time_naive,     time_iterative)
        _print_comparison("Sympy",     "Recursive", time_naive,     time_sympy)
    _print_comparison("Iterative", "Cache",     time_memoized,  time_iterative)
    _print_comparison("Sympy",     "Cache",     time_memoized,  time_sympy)
    _print_comparison("Sympy",     "Iterative", time_iterative, time_sympy)

    # -- Summary -------------------------------------------------------------
    print("\n--- Summary ---")
    timings: dict[str, float] = {
        "Cache":     time_memoized,
        "Iterative": time_iterative,
        "Sympy":     time_sympy,
    }
    if time_naive is not None:
        timings["Recursive"] = time_naive
    best = min(timings, key=lambda k: timings[k])
    print(f"Fastest for n={n}: {best} ({timings[best]:.6f} s)")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nInterrupted.", file=sys.stderr)
        sys.exit(0)
