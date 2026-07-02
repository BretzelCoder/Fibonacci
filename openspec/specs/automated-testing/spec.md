# automated-testing Specification

## Purpose
TBD - created by archiving change add-pytest-suite. Update Purpose after archive.
## Requirements
### Requirement: Algorithm correctness is verified by automated tests
The system SHALL provide automated tests verifying that
`fibonacci_recursive`, `fibonacci_memoized`, `fibonacci_iterative`, and
`fibonacci_sympy` return correct, mutually consistent results.

#### Scenario: Known values
- **WHEN** each algorithm is called with n=0, n=1, and n=10
- **THEN** each returns 0, 1, and 55 respectively

#### Scenario: Cross-implementation agreement
- **WHEN** all four algorithms are called with the same n (0 <= n <= 30)
- **THEN** all four return the same result

### Requirement: Invalid input is rejected by automated tests
The system SHALL provide automated tests verifying that each algorithm
raises `ValueError` for invalid input.

#### Scenario: Negative n
- **WHEN** any of the four algorithms is called with n=-1
- **THEN** a `ValueError` is raised

#### Scenario: Recursive n above MAX_N_NAIVE
- **WHEN** `fibonacci_recursive` is called with n = MAX_N_NAIVE + 1
- **THEN** a `ValueError` is raised

### Requirement: Memoized cache behavior is verified by automated tests
The system SHALL provide automated tests verifying `fibonacci_memoized`'s
`lru_cache`-backed hit/miss behavior, with cache state reset between
tests.

#### Scenario: Cache is cleared before each test
- **WHEN** a test in the algorithms test module runs
- **THEN** `fibonacci_memoized.cache_info()` shows zero hits and zero
  misses before that test's own calls

#### Scenario: Repeated call is a cache hit
- **WHEN** `fibonacci_memoized(n)` is called twice with the same n in a
  test (after clearing the cache)
- **THEN** `fibonacci_memoized.cache_info().hits` is at least 1 after the
  second call

### Requirement: measure_execution returns a valid result and timing
The system SHALL provide automated tests verifying that
`measure_execution` returns the wrapped function's result together with
a non-negative elapsed time.

#### Scenario: Result and timing are returned
- **WHEN** `measure_execution(fibonacci_iterative, 10)` is called
- **THEN** it returns a tuple `(55, elapsed)` where `elapsed >= 0`

### Requirement: Service layer orchestration is verified by automated tests
The system SHALL provide automated tests verifying that
`compute_all` aggregates all four algorithms correctly, including its
skip paths and best-result selection.

#### Scenario: All algorithms agree
- **WHEN** `compute_all(20)` is called
- **THEN** the returned `ComputeResponse.all_match` is `True`

#### Scenario: Naive recursion explicitly disabled
- **WHEN** `compute_all(20, include_naive=False)` is called
- **THEN** the `recursive` entry in `results` has `skipped=True` and
  `skip_reason="disabled by user"`

#### Scenario: n exceeds MAX_N_NAIVE
- **WHEN** `compute_all(50)` is called
- **THEN** the `recursive` entry in `results` has `skipped=True` and a
  `skip_reason` mentioning the O(2^n) cost of naive recursion

#### Scenario: Best algorithm is selected by lowest time
- **WHEN** `compute_all(20)` is called
- **THEN** `best_name` and `best_time` correspond to the entry in
  `results` (among non-skipped entries) with the lowest `time_s`

#### Scenario: Out-of-range n is rejected
- **WHEN** `compute_all(-1)` or `compute_all(MAX_N + 1)` is called
- **THEN** a `ValueError` is raised

