"""
Core Fibonacci algorithm implementations — pure computation, no I/O.

Four algorithms of increasing sophistication:
    recursive   O(2^n)   time  O(n)  space  — educational only, n ≤ MAX_N_NAIVE
    memoized    O(n)     time  O(n)  space  — lru_cache demonstration
    iterative   O(n)     time  O(1)  space  — optimal for moderate n
    sympy       O(log n) time  O(1)  space  — fast doubling via Lucas identities
"""

import sys
import time
from functools import lru_cache
from typing import Callable, Tuple

import sympy

MAX_N_NAIVE: int = 35    # Exponential cost becomes unacceptable above this
MAX_N: int = 5_000       # Recursion-depth ceiling for the memoized variant

# Raise the interpreter limit so fibonacci_memoized(MAX_N) never hits it.
# The default CPython limit (~1000) would be exceeded on first call for n > ~994.
sys.setrecursionlimit(max(sys.getrecursionlimit(), MAX_N + 200))


def _fib_recursive(n: int) -> int:
    """Bare recursive kernel — no input guards, only called from fibonacci_recursive."""
    if n <= 1:
        return n
    return _fib_recursive(n - 1) + _fib_recursive(n - 2)


def fibonacci_recursive(n: int) -> int:
    """
    Naive recursive Fibonacci — O(2^n) time, O(n) stack space.

    Raises:
        ValueError: if n < 0 or n > MAX_N_NAIVE.
    """
    if n < 0:
        raise ValueError(f"n must be >= 0, got {n}")
    if n > MAX_N_NAIVE:
        raise ValueError(
            f"n={n} exceeds MAX_N_NAIVE={MAX_N_NAIVE}. "
            "Naive recursion is O(2^n) — use fibonacci_iterative or fibonacci_memoized instead."
        )
    return _fib_recursive(n)


@lru_cache(maxsize=None)
def fibonacci_memoized(n: int) -> int:
    """
    Memoized recursive Fibonacci via lru_cache — O(n) time, O(n) space.

    Each unique value is computed once; subsequent calls are O(1) cache hits.
    The maximum safe n is bounded by MAX_N (recursion depth on first call).

    Raises:
        ValueError: if n < 0.
    """
    if n < 0:
        raise ValueError(f"n must be >= 0, got {n}")
    if n <= 1:
        return n
    return fibonacci_memoized(n - 1) + fibonacci_memoized(n - 2)


def fibonacci_iterative(n: int) -> int:
    """
    Iterative Fibonacci — O(n) time, O(1) space.

    Raises:
        ValueError: if n < 0.
    """
    if n < 0:
        raise ValueError(f"n must be >= 0, got {n}")
    a, b = 0, 1
    for _ in range(n):
        a, b = b, a + b
    return a


def fibonacci_sympy(n: int) -> int:
    """
    Fibonacci via SymPy's fast-doubling (Lucas sequences) — O(log n) time, O(1) space.

    Raises:
        ValueError: if n < 0.
    """
    if n < 0:
        raise ValueError(f"n must be >= 0, got {n}")
    return int(sympy.fibonacci(n))  # type: ignore[arg-type]


def measure_execution(func: Callable[[int], int], n: int) -> Tuple[int, float]:
    """Returns (result, elapsed_seconds) for func(n)."""
    start = time.perf_counter()
    result = func(n)
    return result, time.perf_counter() - start
