## Context

`fibonacci-dotnet/` is the reference this change ports. Its shape is already
close to idiomatic Spring: an `IFibonacciAlgorithm` interface, four singleton
implementations resolved as a collection by the DI container, a stateless
service that measures each one, and a single POST endpoint returning a record
tree. The parts that do *not* map one-to-one are the cache (`IMemoryCache` vs
Caffeine), the collection ordering (explicit registration order vs Spring's
`@Order`), and the UI (Vue SPA vs Thymeleaf, per the chosen scope).

Two hard constraints come from the existing repository:

- The JSON contract is consumed by `fibonacci-dotnet/frontend/src/types/fibonacci.ts`.
  Reusing that frontend against the Java API must require nothing more than
  repointing the Vite proxy, so field names, casing and nullability must match
  exactly — including `result` being a **string** rather than a JSON number.
- The bounds `MAX_N = 5000` and `MAX_N_NAIVE = 35` are shared by both existing
  stacks and must not drift.

## Goals / Non-Goals

**Goals:**
- A `fibonacci-java/` sub-project that builds, tests and runs independently of
  the other two stacks, with no shared build file or cross-directory reference.
- Byte-compatible JSON contract with the .NET endpoint.
- A Thymeleaf UI that works with JavaScript disabled (plain form POST).
- Exercise the JVM caching stack deliberately, including the `@Cacheable`
  self-invocation pitfall, since caching is the project's stated learning goal.
- Test coverage comparable to the .NET suite (algorithms, service, JSON
  contract, view).

**Non-Goals:**
- No Vue frontend for this stack — the scope is explicitly server-rendered.
  The existing Vue app remains the SPA example and can be repointed if desired.
- No persistence layer, no database, no Spring Data. The .NET stack has none
  either, and the Python history file is a GUI concern.
- No Spring Security, no authentication. The endpoint is a local learning tool.
- No distributed cache (Redis). The point is in-process caching strategy
  comparison; a network round-trip would dominate every measurement.
- No CI workflow — consistent with the rest of the repository.
- No Gradle build as an alternative. One build tool, no choice to maintain.

## Decisions

- **Java 21 (LTS) + Spring Boot 3.5, built with Maven.** Java 21 gives records
  and pattern matching, which keep the DTO layer as terse as the C# records
  being ported. Maven over Gradle because Spring Initializr's default and the
  overwhelming majority of Spring documentation use it — this is a learning
  project, so matching the documentation the user will read matters more than
  build performance. The **Maven Wrapper (`mvnw`) is committed**, so JDK 21 is
  the only prerequisite a contributor must install.

- **Package root `com.bretzelcoder.fibonacci`,** with sub-packages
  `algorithm`, `service`, `web`, `model`, `config`. This mirrors the .NET
  folder split (`Algorithms/`, `Services/`, `Controllers/`, `Models/`) so the
  two implementations can be read side by side.

- **Algorithm ordering via `@Order` on each implementation, injected as
  `List<FibonacciAlgorithm>`.** Spring populates a `List<T>` from every bean of
  type `T`, sorted by `@Order`/`Ordered`, exactly as .NET's DI populates
  `IEnumerable<IFibonacciAlgorithm>` from registration order. `@Order` is
  preferred over relying on declaration or classpath-scan order, which is
  explicitly unspecified in Spring. The order is part of the observable
  contract: it determines the order of `results[]` in the response.

- **The memoized algorithm uses the Caffeine `Cache` API directly, NOT
  `@Cacheable`.** This is the central caching decision of the change.
  Spring's cache abstraction is implemented with AOP proxies: an *internal*
  call from `compute(n)` to `compute(n-1)` bypasses the proxy entirely, so a
  recursive `@Cacheable` method caches only its outermost invocation and stays
  O(2^n). Workarounds exist (self-injection, `AopContext.currentProxy()`,
  extracting the recursion into a second bean), but every one of them trades
  clarity for the illusion of using the annotation. Using
  `com.github.benmanes.caffeine.cache.Cache` directly is honest about what is
  happening, is what the .NET version does with `IMemoryCache`, and keeps
  hit/miss accounting in one place. The pitfall is documented in a code
  comment on the class, because avoiding it *is* the lesson.
  Alternative considered: adding a fifth, deliberately broken `@Cacheable`
  algorithm to demonstrate the pitfall empirically — rejected because it would
  break the "same four algorithms in every stack" symmetry that makes the three
  implementations comparable.

- **Cache configuration mirrors the .NET one:** `maximumSize(10_000)`,
  `expireAfterAccess(10 minutes)`. `maximumSize` is the direct analogue of
  `MemoryCacheOptions.SizeLimit` with `Size = 1` per entry, and
  `expireAfterAccess` of `SlidingExpiration`. The cache is invalidated
  (`invalidateAll()`) before each measurement so hit/miss statistics are
  comparable across requests, matching the .NET `ResetCache()` contract.

- **Hit/miss statistics come from Caffeine's own `recordStats()`, exposed as a
  delta against a baseline snapshot taken at reset.** Caffeine's `CacheStats`
  is cumulative for the life of the cache and cannot be zeroed, but it supports
  `minus()`; snapshotting at reset and subtracting at read time yields
  per-request numbers without hand-rolled counters. Alternative considered:
  `AtomicLong` hits/misses incremented manually, as the .NET version does with
  `Interlocked` — rejected because Caffeine already records this correctly and
  duplicating it invites the two numbers to disagree.

- **The memoized recursion runs on a dedicated thread with a 32 MB stack.**
  At `n = 5000` the memoized algorithm recurses 5000 frames deep on the first
  (all-misses) pass. The default JVM thread stack (typically 512 KB–1 MB, and
  smaller still on Tomcat worker threads) makes that a plausible
  `StackOverflowError`, and a stack overflow inside a request thread is an
  ugly, hard-to-diagnose failure. Constructing the worker via
  `new Thread(group, task, name, 32L * 1024 * 1024)` bounds the risk locally,
  with no global JVM flag that a contributor could forget to pass. This is the
  JVM counterpart to the Python stack's `sys.setrecursionlimit` call.
  Alternative considered: filling the cache iteratively from 0 to n — rejected,
  it would silently turn the "memoized recursion" algorithm into the iterative
  one and destroy the comparison the project exists to make.

- **DTOs are Java `record`s, and `AlgorithmResult.result` is a `String`.**
  The .NET version stringifies `BigInteger` in the service layer rather than
  relying on the global JSON converter for the DTO, and the TypeScript type
  declares `result: string | null`. Keeping the field a `String` reproduces
  that exactly and removes any need for a Jackson serializer, so there is no
  `BigIntegerJsonConverter` equivalent in this stack. `BigInteger` remains the
  computation type throughout the `algorithm` package.

- **Validation with Jakarta Bean Validation (`@Min(0) @Max(5000)`) on the
  request record, plus `@Valid` on the controller parameter.** Out-of-range `n`
  therefore fails before reaching the service. A `@RestControllerAdvice`
  translates `MethodArgumentNotValidException` into an RFC 9457 `ProblemDetail`
  with a `message` field, so the existing Vue error handler — which reads
  `payload.message` — works unchanged against this API.
  The service *also* revalidates `n` and throws `IllegalArgumentException`,
  because it is a public API in its own right and is called directly by the
  Thymeleaf controller and by tests.

- **Two controllers, one service.** `FibonacciApiController`
  (`@RestController`, `/api/fibonacci`) and `FibonacciViewController`
  (`@Controller`, `/`) both delegate to `FibonacciService`. Splitting them
  keeps the JSON contract free of view concerns and the view free of
  `ResponseEntity` plumbing; sharing the service guarantees both surfaces
  report identical numbers.

- **The Thymeleaf UI is a plain form POST, no JavaScript.** `GET /` renders the
  empty form; `POST /` computes and re-renders the same template with results.
  Validation errors come back through `BindingResult` and are rendered inline.
  Performance bars are sized with an inline `style="width: N%"` computed
  server-side. Alternative considered: htmx for partial updates — rejected as
  an extra dependency that adds nothing to the learning goal of this change.

- **Long results are truncated in the view, not in the model.** F(5000) is 1045
  digits; rendering it in full breaks the table layout. The template shows the
  first and last 20 digits with the total digit count, while the JSON API
  always returns the full value. Truncation is a presentation concern and must
  not leak into `AlgorithmResult`.

- **Port 8080 (Spring Boot's default), left unconfigured.** The .NET API is
  wired to 5000 and Vite to 5173, so the default is already conflict-free and
  needs no `application.yml` entry — one less thing to keep in sync with the
  documentation. CORS is opened for `http://localhost:5173` so the existing Vue
  frontend can be repointed at this API without touching its source.

- **Testing with JUnit 5 + AssertJ + MockMvc,** all supplied by
  `spring-boot-starter-test`. AssertJ is the closest analogue to the .NET
  suite's FluentAssertions, keeping assertions readable side by side. Algorithm
  and service tests are plain unit tests with no Spring context (fast);
  the contract and view tests use `@WebMvcTest` with the service mocked, and
  one `@SpringBootTest` end-to-end test asserts the real wiring — in
  particular that `List<FibonacciAlgorithm>` is populated in the expected
  order, which is the one behavior no unit test can verify.

## Risks / Trade-offs

- [Wall-clock timings on the JVM are dominated by JIT warm-up, so the first
  measured run of an algorithm can be an order of magnitude slower than the
  steady state — the comparison risks being misleading, and `bestName` may
  differ from the .NET/Python answer for the same `n`.] → Mitigation: this is a
  real and instructive property of the platform, not a defect to hide. It is
  documented in `TECH_README.md` and surfaced as a note in the UI. A proper fix
  (JMH microbenchmarks) is out of scope: it would replace the request/response
  model the other two stacks use and make the three incomparable.
- [The dedicated 32 MB-stack thread adds a thread creation per memoized
  measurement, inflating that algorithm's measured time by a fixed overhead of
  roughly a tenth of a millisecond.] → Mitigation: the thread is created
  *outside* the timed region; only the recursion itself is measured.
- [Duplicating the JSON contract across two stacks means it can silently drift.]
  → Mitigation: the contract test asserts the exact field set and casing
  against a literal expectation, so a rename fails a test rather than only the
  Vue frontend at runtime.
- [`@Order` values are a coordination point: a new algorithm added with a
  duplicate order makes the response order unspecified again.] → Mitigation:
  the `@SpringBootTest` wiring test asserts the exact expected name sequence,
  so a collision or a forgotten `@Order` fails the build.
- [Java 21 is a new prerequisite for anyone cloning the repo who wants to build
  everything.] → Mitigation: the three stacks are independent; the Java stack
  is skippable, and this is stated in `TECH_README.md`.

## Migration Plan

Not applicable — the change is purely additive. `fibonacci-java/` does not
exist yet, nothing depends on it, and the Python and .NET stacks are untouched.
Reverting is `git rm -r fibonacci-java/` plus the `TECH_README.md` and
`.gitignore` edits.

## Open Questions

(none — scope, UI technology and toolchain were settled before this design;
the JIT warm-up caveat is a documented trade-off rather than an open question)
