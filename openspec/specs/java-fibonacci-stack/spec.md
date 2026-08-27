# java-fibonacci-stack Specification

## Purpose
A Java 21 / Spring Boot 3 implementation of the Fibonacci caching-strategy
comparator, living in `fibonacci-java/`. It exposes the same four algorithms
as the Python and .NET stacks through two surfaces: a JSON endpoint whose
contract is byte-compatible with the .NET API, and a server-rendered
Thymeleaf page that works without JavaScript.

## Requirements
### Requirement: Four Fibonacci algorithms are exposed behind a common contract
The Java stack SHALL provide four implementations of `FibonacciAlgorithm` —
naive recursive, memoized, iterative and fast doubling — each declaring a
name, a label, a time complexity, a space complexity, an upper bound on `n`,
and whether it requires explicit inclusion.

#### Scenario: Known values
- **WHEN** any of the four algorithms is called with n=0, n=1 and n=10
- **THEN** it returns 0, 1 and 55 respectively

#### Scenario: Cross-implementation agreement
- **WHEN** all four algorithms are called with the same n (0 <= n <= 30)
- **THEN** all four return the same `BigInteger`

#### Scenario: Large n exceeds 64-bit range
- **WHEN** the iterative and fast-doubling algorithms are called with n=1000
- **THEN** both return the same 209-digit value, without overflow

#### Scenario: Negative n is rejected
- **WHEN** any of the four algorithms is called with n=-1
- **THEN** an `IllegalArgumentException` is thrown

#### Scenario: Declared metadata
- **WHEN** the algorithm metadata is read
- **THEN** the naive recursive algorithm declares name `recursive`,
  complexity `O(2^n)` / `O(n)`, a maximum n of 35, and requires explicit
  inclusion
- **AND** the memoized algorithm declares name `memoized`, complexity `O(n)` /
  `O(n)` and a maximum n of 5000
- **AND** the iterative algorithm declares name `iterative`, complexity `O(n)` /
  `O(1)` and a maximum n of 5000
- **AND** the fast-doubling algorithm declares name `fast-doubling`, complexity
  `O(log n)` / `O(log n)` and a maximum n of 5000

### Requirement: The memoized algorithm caches with Caffeine and reports statistics
The memoized algorithm SHALL store each computed value in a Caffeine cache
bounded to 10 000 entries with a 10-minute expire-after-access policy, and
SHALL expose per-measurement hit and miss counts that can be reset.

#### Scenario: Each value is computed once
- **WHEN** the cache is reset and the memoized algorithm is called with n=30
- **THEN** the reported miss count is 31 and the reported hit count is 28

#### Scenario: Repeated call is served from the cache
- **WHEN** the memoized algorithm is called twice with the same n after a reset
- **THEN** the second call adds at least one hit and no miss

#### Scenario: Reset clears both values and statistics
- **WHEN** the cache is reset after a computation
- **THEN** the reported hit and miss counts are both 0
- **AND** the next computation of the same n reports misses again

#### Scenario: Deep recursion does not overflow the stack
- **WHEN** the memoized algorithm is called with n=5000 on an empty cache
- **THEN** it returns the correct value without throwing `StackOverflowError`

### Requirement: The service orchestrates, measures and compares all algorithms
The service SHALL run every registered algorithm for a given `n`, reset any
cache before measuring it, record its elapsed time, verify that all executed
algorithms agree, and report the fastest one.

#### Scenario: All algorithms are executed and agree
- **WHEN** the service computes n=20 with the naive recursion included
- **THEN** the response contains four results, none skipped, all with the same
  `result` value, and `allMatch` is true

#### Scenario: Naive recursion explicitly excluded
- **WHEN** the service computes n=20 with `includeNaive` false
- **THEN** the `recursive` entry has `skipped` true and a non-empty
  `skipReason`, and the other three entries are executed

#### Scenario: n exceeds the naive recursion bound
- **WHEN** the service computes n=50 with the naive recursion included
- **THEN** the `recursive` entry has `skipped` true and a `skipReason`
  mentioning its bound of 35

#### Scenario: Skipped entries carry no result
- **WHEN** an entry is skipped
- **THEN** its `result` is null, its `timeSeconds` is 0 and its `cacheStats`
  is null

#### Scenario: Fastest algorithm is reported
- **WHEN** the service computes n=20
- **THEN** `bestName` is the label of the non-skipped entry with the lowest
  `timeSeconds`, and `bestTimeSeconds` is that value

#### Scenario: Cache statistics are attached to cache-aware algorithms only
- **WHEN** the service computes any n
- **THEN** the `memoized` entry carries non-null `cacheStats`, and the
  `recursive`, `iterative` and `fast-doubling` entries carry null `cacheStats`

#### Scenario: Out-of-range n is rejected
- **WHEN** the service is asked to compute n=-1 or n=5001
- **THEN** an `IllegalArgumentException` is thrown

#### Scenario: Results are ordered deterministically
- **WHEN** the service computes any n
- **THEN** the entries appear in the order `recursive`, `memoized`,
  `iterative`, `fast-doubling`

### Requirement: A JSON endpoint exposes the comparison contract-compatibly with the .NET API
The Java stack SHALL expose `POST /api/fibonacci/compute` accepting
`{ "n": <int>, "includeNaive": <bool> }` and returning the same JSON shape,
field names and camelCase casing as the existing .NET endpoint.

#### Scenario: Successful computation
- **WHEN** `POST /api/fibonacci/compute` is called with `{"n":30,"includeNaive":true}`
- **THEN** the response status is 200
- **AND** the body contains `n`, `results`, `allMatch`, `bestName` and
  `bestTimeSeconds` at the top level
- **AND** each entry of `results` contains `name`, `label`, `timeComplexity`,
  `spaceComplexity`, `n`, `result`, `timeSeconds`, `cacheStats`, `skipped` and
  `skipReason`
- **AND** each non-null `cacheStats` contains `hits`, `misses`, `total` and
  `hitRatio`

#### Scenario: Result is serialized as a string
- **WHEN** the endpoint is called with n=5000
- **THEN** the `result` field of each executed entry is a JSON string of 1045
  digits, not a JSON number

#### Scenario: includeNaive defaults to true
- **WHEN** the endpoint is called with a body containing only `{"n":20}`
- **THEN** the naive recursive algorithm is executed

#### Scenario: Out-of-range n is rejected with a readable error
- **WHEN** the endpoint is called with n=-1 or n=5001
- **THEN** the response status is 400
- **AND** the body contains a `message` field describing the bound

#### Scenario: Unhandled failures return JSON
- **WHEN** the request handler throws an unexpected exception
- **THEN** the response status is 500 and the body is JSON containing a
  `message` field

#### Scenario: The Vue development origin is allowed
- **WHEN** a cross-origin request is made from `http://localhost:5173`
- **THEN** the response permits that origin

### Requirement: A server-rendered page runs the comparison without JavaScript
The Java stack SHALL serve an HTML page at `GET /` with a form for `n` and
`includeNaive`, and SHALL render the comparison results on `POST /` using
Thymeleaf, with no client-side scripting required.

#### Scenario: Empty form on first visit
- **WHEN** `GET /` is requested
- **THEN** the response status is 200 and the page contains a form with an `n`
  field and an `includeNaive` checkbox, and no results table

#### Scenario: Results rendered after submission
- **WHEN** the form is submitted with n=30
- **THEN** the response status is 200 and the page contains one row per
  algorithm with its label, complexities, result, elapsed time and — for the
  memoized algorithm — its hit and miss counts

#### Scenario: Winner and integrity check are displayed
- **WHEN** the form is submitted with a valid n
- **THEN** the page states which algorithm was fastest and whether all
  executed algorithms returned the same value

#### Scenario: Skipped algorithms show their reason
- **WHEN** the form is submitted with n=50
- **THEN** the naive recursion row is marked skipped and displays its reason
  instead of a result

#### Scenario: Long results are abbreviated
- **WHEN** the form is submitted with n=5000
- **THEN** the rendered result is abbreviated to its leading and trailing
  digits together with its total digit count

#### Scenario: Invalid input is reported inline
- **WHEN** the form is submitted with n=-1 or n=5001
- **THEN** the page is re-rendered with an inline validation message and no
  results table

### Requirement: The stack builds, tests and runs independently
The Java stack SHALL live in `fibonacci-java/` and SHALL be buildable and
testable without the Python or .NET stacks, requiring only a JDK 21
installation.

#### Scenario: Build from a clean clone
- **WHEN** `./mvnw verify` is run in `fibonacci-java/`
- **THEN** the project compiles and the whole test suite passes

#### Scenario: Maven itself is not required
- **WHEN** a contributor has JDK 21 but no Maven installation
- **THEN** the committed Maven Wrapper (`mvnw` / `mvnw.cmd`) builds the project

#### Scenario: Default port avoids the other stacks
- **WHEN** the application starts with no configuration override
- **THEN** it listens on port 8080, leaving 5000 and 5173 free for the .NET
  API and the Vite dev server

#### Scenario: Build output is not versioned
- **WHEN** the project has been built
- **THEN** `target/` is ignored by Git
