# Fibonacci Sequence — Python Implementation Comparison

A Python learning project focused on caching strategies and algorithmic optimization, comparing four approaches to computing the nth Fibonacci number.

> Prerequisites, setup and usage for the whole repository (Python **and** the .NET 9 / Vue 3 stack): see [TECH_README.md](TECH_README.md).

**Definition:** F(n) = F(n-1) + F(n-2), with F(0) = 0 and F(1) = 1

---

## Architecture

```
fibonacci_algorithms.py   — pure functions (4 implementations + measure_execution)
fibonacci_service.py      — service layer (data models, orchestration)
fibonacci_flet.py         — graphical interface (Flet)
MyFibonacciTest.py        — command-line interface (CLI)
```

---

## Implementations

| Strategy | Time complexity | Space complexity | Limit |
|----------|----------------|-----------------|-------|
| Recursive | O(2^n) | O(n) | n <= 35 |
| Cache (`lru_cache`) | O(n) | O(n) | n <= 5,000 |
| Iterative | O(n) | O(1) | n <= 5,000 |
| Sympy (`sympy.fibonacci`) | O(log n) | O(1) | n <= 5,000 |

### Recursive
Direct implementation of the mathematical definition. Every call recreates the same subtrees, leading to an exponential explosion in operations. **Limited to n <= 35 to avoid prohibitive computation time.**

### Cache
Uses the `@lru_cache` decorator from the standard library to cache each intermediate result. Each value is computed only once.

### Iterative
Keeps only the last two values in memory — O(1) space complexity, no recursion stack or cache.

### Sympy
Delegates to `sympy.fibonacci(n)`, which uses the **fast doubling** algorithm based on Lucas identities. Its O(log n) complexity makes it particularly efficient for large values of n. Returns a native Python integer via `int()`.

---

## Usage

### Command-line interface

```bash
python MyFibonacciTest.py
```

The program prompts for a value of `n` (default: 30) then displays for each implementation:
- the computed result
- execution time measured with `time.perf_counter()`
- cache statistics (hits / misses) for the Cache implementation
- a performance comparison (speedup factor vs Recursive)

### Graphical interface

```bash
python fibonacci_flet.py
```

### Running tests

```bash
pip install -r requirements.txt
pytest
```

The suite covers `fibonacci_algorithms.py` (correctness, cross-implementation
agreement, invalid input, cache behavior) and `fibonacci_service.py`
(orchestration, skip paths, best-result selection).

### Sample CLI output

```
Enter n (default: 30): 30

# Evaluation for n = 30

--- Recursive ---
Result : 832040
Time   : 0.2315 seconds

--- Cache ---
Result : 832040
Time   : 0.000012 seconds
Cache  : Hits=28, Misses=31

--- Iterative ---
Result : 832040
Time   : 0.000003 seconds

--- Sympy ---
Result : 832040
Time   : 0.000021 seconds

--- Integrity Check ---
OK  All 4 implementations return the same result.

--- Performance Comparison ---
Cache vs Recursive            : 0.005183% of the time (~19,294x faster)
Iterative vs Recursive        : 0.001295% of the time (~77,222x faster)
Sympy vs Recursive            : 0.009071% of the time (~11,025x faster)
Iterative vs Cache            : 24.983740% of the time (~4x faster)
Sympy vs Cache                : 175.000000% of the time (~2x slower)
Sympy vs Iterative            : 700.000000% of the time (~7x slower)

--- Summary ---
Fastest for n=30: Iterative (0.000003 s)
```

---

## Requirements

- Python 3.10+
- Dependencies: `pip install -r requirements.txt`
  - [sympy](https://www.sympy.org/) `>=1.12`
  - [flet](https://flet.dev/) `>=0.24.0` (graphical interface only)
