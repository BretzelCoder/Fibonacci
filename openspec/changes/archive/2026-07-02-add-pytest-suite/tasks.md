## 1. Setup

- [x] 1.1 Add `pytest` and `pytest-cov` to `requirements.txt`
- [x] 1.2 Create the `tests/` package (`tests/__init__.py`)
- [x] 1.3 Add `pytest.ini` at the repo root with `testpaths = tests`

## 2. Algorithm tests (`tests/test_fibonacci_algorithms.py`)

- [x] 2.1 Add autouse fixture that calls `fibonacci_memoized.cache_clear()` before each test
- [x] 2.2 Test known values (n=0, n=1, n=10) for all four algorithms
- [x] 2.3 Test cross-implementation agreement for n in range(0, 31)
- [x] 2.4 Test `ValueError` on negative n for all four algorithms
- [x] 2.5 Test `ValueError` on `fibonacci_recursive(MAX_N_NAIVE + 1)`
- [x] 2.6 Test `fibonacci_memoized` cache hit/miss counts via `cache_info()`
- [x] 2.7 Test `measure_execution` returns correct result and `elapsed >= 0`

## 3. Service layer tests (`tests/test_fibonacci_service.py`)

- [x] 3.1 Add autouse fixture that calls `fibonacci_memoized.cache_clear()` before each test
- [x] 3.2 Test `compute_all(20)` returns `all_match=True` and all four results present
- [x] 3.3 Test `compute_all(20, include_naive=False)` skips recursive with reason "disabled by user"
- [x] 3.4 Test `compute_all(50)` skips recursive with an O(2^n)-related reason
- [x] 3.5 Test `best_name`/`best_time` match the fastest non-skipped result
- [x] 3.6 Test `ValueError` on `compute_all(-1)` and `compute_all(MAX_N + 1)`

## 4. Verification

- [x] 4.1 Run `pytest` from the repo root and confirm all tests pass
- [x] 4.2 Update `README.md` with a "Running tests" section documenting the `pytest` command
