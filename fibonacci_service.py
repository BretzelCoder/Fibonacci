"""
Service layer wrapping the Fibonacci algorithms.
Structured to mirror future FastAPI route handlers + Pydantic models.
"""

from dataclasses import dataclass
from typing import List, Optional

from fibonacci_algorithms import (
    MAX_N,
    MAX_N_NAIVE,
    fibonacci_iterative,
    fibonacci_memoized,
    fibonacci_recursive,
    fibonacci_sympy,
    measure_execution,
)


@dataclass
class AlgoResult:
    """Future Pydantic model for one algorithm's output."""
    name: str
    label: str
    n: int
    result: Optional[int]
    time_s: float
    cache_hits: Optional[int] = None
    cache_misses: Optional[int] = None
    skipped: bool = False
    skip_reason: str = ""


@dataclass
class ComputeResponse:
    """Future Pydantic response model for POST /compute."""
    n: int
    results: List[AlgoResult]
    all_match: bool
    best_name: str
    best_time: float


def compute_all(n: int, include_naive: bool = True) -> ComputeResponse:
    """
    Run all Fibonacci implementations and return a unified response.

    Future FastAPI route: POST /compute  { n, include_naive }

    Raises:
        ValueError: if n is outside [0, MAX_N].
    """
    if n < 0 or n > MAX_N:
        raise ValueError(f"n must be in [0, {MAX_N}], got {n}")

    results: List[AlgoResult] = []

    # Naive recursion ──────────────────────────────────────────────────────
    if not include_naive:
        results.append(AlgoResult(
            name="recursive", label="Récursive Naïve",
            n=n, result=None, time_s=0.0,
            skipped=True, skip_reason="désactivée par l'utilisateur",
        ))
    elif n > MAX_N_NAIVE:
        results.append(AlgoResult(
            name="recursive", label="Récursive Naïve",
            n=n, result=None, time_s=0.0,
            skipped=True, skip_reason=f"n > {MAX_N_NAIVE} (coût O(2^n))",
        ))
    else:
        res, t = measure_execution(fibonacci_recursive, n)
        results.append(AlgoResult(
            name="recursive", label="Récursive Naïve",
            n=n, result=res, time_s=t,
        ))

    # Memoized recursion ───────────────────────────────────────────────────
    fibonacci_memoized.cache_clear()
    res, t = measure_execution(fibonacci_memoized, n)
    stats = fibonacci_memoized.cache_info()
    results.append(AlgoResult(
        name="memoized", label="Mémoïsée (Cache)",
        n=n, result=res, time_s=t,
        cache_hits=stats.hits, cache_misses=stats.misses,
    ))

    # Iterative ────────────────────────────────────────────────────────────
    res, t = measure_execution(fibonacci_iterative, n)
    results.append(AlgoResult(name="iterative", label="Itérative", n=n, result=res, time_s=t))

    # SymPy ────────────────────────────────────────────────────────────────
    res, t = measure_execution(fibonacci_sympy, n)
    results.append(AlgoResult(name="sympy", label="SymPy", n=n, result=res, time_s=t))

    computed = [r for r in results if not r.skipped]
    all_match = len({r.result for r in computed}) == 1
    best = min(computed, key=lambda r: r.time_s)

    return ComputeResponse(
        n=n,
        results=results,
        all_match=all_match,
        best_name=best.label,
        best_time=best.time_s,
    )
