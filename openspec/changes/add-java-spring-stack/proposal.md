## Why

The repository already hosts two independent stacks solving the same problem
(comparing caching strategies for F(n)): the original Python CLI/Flet stack at
the repo root, and a .NET 9 + Vue 3 stack in `fibonacci-dotnet/`. Both exist so
the same, well-understood domain can be used to practice a new ecosystem
without spending effort on inventing requirements.

A third implementation in Java 21 + Spring Boot 3 extends that pattern to the
JVM ecosystem. It is the natural next step for two reasons:

- **Caching is the project's stated learning goal.** The JVM's answer
  (Caffeine behind Spring's cache abstraction) has a well-known pitfall that
  neither Python's `lru_cache` nor .NET's `IMemoryCache` exposes: Spring's
  `@Cacheable` is proxy-based, so a *recursive* memoized method never hits the
  cache on its self-calls. Reproducing the memoized algorithm on the JVM forces
  that lesson to be learned explicitly rather than read about.
- **A server-rendered UI is a genuinely different exercise.** The .NET stack
  already covers "REST API + SPA". Pairing the Java API with Thymeleaf covers
  the other half of the web-application spectrum without duplicating the
  ~1000 lines of Vue components that already exist.

## What Changes

- Add a self-contained `fibonacci-java/` sub-project (Maven, Java 21,
  Spring Boot 3.5) containing:
  - Four algorithm implementations behind a `FibonacciAlgorithm` interface —
    naive recursive, memoized (Caffeine), iterative, fast doubling — mirroring
    the bounds already used by the other two stacks (`n <= 35` for the naive
    recursion, `n <= 5000` for the rest).
  - A `FibonacciService` orchestration layer that measures each algorithm,
    resets the cache before each measurement, applies skip rules, verifies
    that all implementations agree, and selects the fastest.
  - A JSON endpoint `POST /api/fibonacci/compute` whose request and response
    shapes are byte-compatible with the existing .NET endpoint, so the Vue
    frontend in `fibonacci-dotnet/frontend/` can be pointed at it unchanged.
  - A server-rendered Thymeleaf UI at `GET /` and `POST /` that submits `n`
    and `includeNaive` as a plain HTML form and renders the comparison table,
    the cache hit/miss statistics, the integrity check and the winner —
    functional with JavaScript disabled.
  - A JUnit 5 + AssertJ + MockMvc test suite covering the algorithms, the
    service orchestration, the JSON contract and the Thymeleaf view.
- Update `TECH_README.md` with a "Stack Java" section (prerequisites,
  installation, run, test, endpoint, troubleshooting) alongside the existing
  Python and .NET sections.
- Update the root `.gitignore` for Maven build output (`target/`).

No change to the Python stack, the .NET stack, or their behavior — this change
is entirely additive.

## Capabilities

### New Capabilities
- `java-fibonacci-stack`: a Java 21 / Spring Boot 3 implementation of the
  Fibonacci comparator, exposing both a JSON endpoint contract-compatible with
  the .NET API and a server-rendered Thymeleaf UI.

### Modified Capabilities
(none — no existing spec-level behavior changes)

## Impact

- New directory `fibonacci-java/` (sources, tests, Maven wrapper, `.gitignore`).
- Affected existing files: `TECH_README.md` (new section + updated stack
  table), `.gitignore` (add `target/`).
- New toolchain prerequisite for contributors who want to build this stack:
  JDK 21. Maven itself is not required — the Maven Wrapper (`mvnw`) is
  committed, as is standard for Spring Boot projects.
- Default port `8080`, chosen so the Java stack can run at the same time as
  the .NET API (`5000`) and the Vite dev server (`5173`).
- No impact on the Python or .NET stacks; each remains independently buildable.
