## Context

The project currently has no test framework, no `tests/` directory, and
no pytest config. `fibonacci_algorithms.py` exposes four pure functions
plus `measure_execution`; `fibonacci_service.py` exposes `compute_all`,
which orchestrates all four and skips the recursive one when `n` is too
large or naive computation is disabled. Both modules already raise
`ValueError` on invalid `n`, which gives clean, testable contracts.

`fibonacci_memoized` uses a module-level `lru_cache`, so its cache state
persists across test calls unless explicitly cleared — this is exactly
the kind of caching behavior the project exists to practice, so tests
should exercise cache hit/miss counts, not just the return value.

## Goals / Non-Goals

**Goals:**
- Deterministic, fast unit tests for both modules, runnable with a bare
  `pytest` from the repo root.
- Cover correctness, edge cases (`n=0`, `n=1`), invalid input, and the
  `MAX_N_NAIVE` / `include_naive` skip paths.
- Keep the suite fast: no test computes Fibonacci numbers large enough
  to be slow, even for the recursive implementation.

**Non-Goals:**
- No GUI (`fibonacci_flet.py`) or CLI (`MyFibonacciTest.py`) testing —
  out of scope for this change; both are I/O-heavy and better suited to
  a later, separate change if ever needed.
- No CI workflow (e.g. GitHub Actions) setup — local `pytest` only for
  this iteration.
- No property-based testing (e.g. Hypothesis) — plain example-based
  tests are sufficient for this small, well-understood domain.

## Decisions

- **Test runner: pytest**, per the user's explicit choice — no
  alternative considered.
- **Layout: a `tests/` package with one test module per source module**
  (`test_fibonacci_algorithms.py`, `test_fibonacci_service.py`), mirroring
  the existing `fibonacci_algorithms.py` / `fibonacci_service.py` split.
  Alternative considered: a single `test_fibonacci.py` — rejected because
  it would mix pure-algorithm tests with orchestration tests, making
  failures harder to localize as the suite grows.
- **Reference values via `fibonacci_iterative`, not hardcoded lists**,
  except for a small set of hardcoded known values (F(0)=0, F(1)=1,
  F(10)=55) used as a sanity anchor independent of any implementation.
  Cross-checking all four algorithms against each other for a range of
  `n` catches divergence bugs without duplicating a Fibonacci table.
- **`fibonacci_memoized.cache_clear()` called in a per-test fixture**
  (autouse) so cache-hit/miss assertions and cache state are not
  order-dependent between tests.
- **No `pytest-cov` usage enforced by default** — added to
  `requirements.txt` as an optional dev tool; `pytest.ini` does not force
  `--cov` so plain `pytest` stays fast and dependency-light.

## Risks / Trade-offs

- [Recursive tests are slow for large n] → Mitigation: keep all recursive
  test inputs small (n <= 30), well under `MAX_N_NAIVE` (35).
- [`sys.setrecursionlimit` side effect from `fibonacci_algorithms` import]
  → Mitigation: no test relies on the interpreter's default recursion
  limit; this is documented as existing module behavior, not something
  tests need to guard against.
- [lru_cache global state leaking between tests] → Mitigation: autouse
  fixture clears the cache before each test in
  `test_fibonacci_algorithms.py` and `test_fibonacci_service.py`.

## Migration Plan

Additive only — no existing code changes, no rollback needed beyond
reverting the new files if the suite turns out to need rework.
