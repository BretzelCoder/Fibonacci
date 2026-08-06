import pytest

from fibonacci_algorithms import (
    MAX_N_NAIVE,
    fibonacci_iterative,
    fibonacci_memoized,
    fibonacci_recursive,
    fibonacci_sympy,
    measure_execution,
)

ALGORITHMS = [
    fibonacci_recursive,
    fibonacci_memoized,
    fibonacci_iterative,
    fibonacci_sympy,
]

KNOWN_VALUES = [(0, 0), (1, 1), (10, 55)]


@pytest.fixture(autouse=True)
def _clear_memoized_cache():
    fibonacci_memoized.cache_clear()
    yield
    fibonacci_memoized.cache_clear()


@pytest.mark.parametrize("algo", ALGORITHMS)
@pytest.mark.parametrize("n,expected", KNOWN_VALUES)
def test_known_values(algo, n, expected):
    assert algo(n) == expected


@pytest.mark.parametrize("n", range(0, 31))
def test_cross_implementation_agreement(n):
    results = {algo(n) for algo in ALGORITHMS}
    assert len(results) == 1


@pytest.mark.parametrize("algo", ALGORITHMS)
def test_negative_n_raises(algo):
    with pytest.raises(ValueError):
        algo(-1)


def test_recursive_above_max_n_naive_raises():
    with pytest.raises(ValueError):
        fibonacci_recursive(MAX_N_NAIVE + 1)


def test_memoized_cache_hit_after_repeat_call():
    fibonacci_memoized(15)
    info_before = fibonacci_memoized.cache_info()
    fibonacci_memoized(15)
    info_after = fibonacci_memoized.cache_info()
    assert info_after.hits > info_before.hits


def test_memoized_cache_starts_clear_each_test():
    info = fibonacci_memoized.cache_info()
    assert info.hits == 0
    assert info.misses == 0


def test_measure_execution_returns_result_and_nonnegative_time():
    result, elapsed = measure_execution(fibonacci_iterative, 10)
    assert result == 55
    assert elapsed >= 0
