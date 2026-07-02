## Why

The project has no automated tests: correctness of the four Fibonacci
algorithms and of the service layer is currently only checked by eyeballing
CLI output. Adding a pytest suite catches regressions early (especially
important since this project is meant to become a template for future
projects) and gives a concrete, safe target for exercising the new
OpenSpec spec-driven pipeline end-to-end.

## What Changes

- Add `pytest` (and `pytest-cov` for coverage reporting) to `requirements.txt`.
- Add a `tests/` package with:
  - `test_fibonacci_algorithms.py`: unit tests for `fibonacci_recursive`,
    `fibonacci_memoized`, `fibonacci_iterative`, `fibonacci_sympy` and
    `measure_execution` (correctness on known values, agreement between
    implementations, boundary/edge cases, `ValueError` on invalid input,
    `MAX_N_NAIVE` guard on the recursive implementation).
  - `test_fibonacci_service.py`: unit tests for `compute_all` (result
    shape, `all_match` flag, `best_name`/`best_time` selection, the
    `include_naive=False` and `n > MAX_N_NAIVE` skip paths, `ValueError`
    on out-of-range `n`).
- Add a `pytest.ini` (or `[tool.pytest.ini_options]`-equivalent) minimal
  config so `pytest` can be run from the repo root with no extra flags.
- Update `README.md` with a short "Running tests" section.

No changes to `fibonacci_algorithms.py`, `fibonacci_service.py`,
`fibonacci_flet.py` or `MyFibonacciTest.py` behavior — this change is
test-only and additive.

## Capabilities

### New Capabilities
- `automated-testing`: a pytest-based test suite covering the algorithms
  module and the service layer, runnable via `pytest` from the repo root.

### Modified Capabilities
(none — no existing spec-level behavior changes)

## Impact

- Affected files: `requirements.txt`, `README.md` (new section), new
  `tests/` directory, new `pytest.ini`.
- New dev dependency: `pytest` (+ `pytest-cov`).
- No impact on runtime behavior of the CLI or GUI.
