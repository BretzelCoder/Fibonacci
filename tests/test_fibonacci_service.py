import pytest

from fibonacci_algorithms import MAX_N, fibonacci_memoized
from fibonacci_service import compute_all


@pytest.fixture(autouse=True)
def _clear_memoized_cache():
    fibonacci_memoized.cache_clear()
    yield
    fibonacci_memoized.cache_clear()


def test_compute_all_matches_and_has_all_algorithms():
    response = compute_all(20)
    assert response.all_match is True
    assert {r.name for r in response.results} == {
        "recursive", "cache", "iterative", "sympy",
    }


def test_compute_all_include_naive_false_skips_recursive():
    response = compute_all(20, include_naive=False)
    recursive = next(r for r in response.results if r.name == "recursive")
    assert recursive.skipped is True
    assert recursive.skip_reason == "disabled by user"


def test_compute_all_skips_recursive_above_max_n_naive():
    response = compute_all(50)
    recursive = next(r for r in response.results if r.name == "recursive")
    assert recursive.skipped is True
    assert "O(2^n)" in recursive.skip_reason


def test_compute_all_best_matches_fastest_non_skipped():
    response = compute_all(20)
    non_skipped = [r for r in response.results if not r.skipped]
    fastest = min(non_skipped, key=lambda r: r.time_s)
    assert response.best_name == fastest.label
    assert response.best_time == fastest.time_s


@pytest.mark.parametrize("n", [-1, MAX_N + 1])
def test_compute_all_out_of_range_raises(n):
    with pytest.raises(ValueError):
        compute_all(n)
