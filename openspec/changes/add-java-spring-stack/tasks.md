## 1. Toolchain and project skeleton

- [x] 1.1 Install JDK 21 and verify `java -version` reports 21
- [x] 1.2 Create `fibonacci-java/` with a Spring Boot 3.5 / Java 21 `pom.xml`
      (`web`, `thymeleaf`, `validation`, `cache`, `caffeine`, `test` starters)
- [x] 1.3 Commit the Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/`)
- [x] 1.4 Add `fibonacci-java/.gitignore` for `target/` and IDE files
- [x] 1.5 Add `FibonacciApplication` and confirm `./mvnw test` runs on an empty suite

## 2. Constants and domain model

- [x] 2.1 Add `config/FibonacciConstants` with `MAX_N = 5000` and `MAX_N_NAIVE = 35`
- [x] 2.2 Add `model/CacheStats` record (`hits`, `misses`, derived `total` and `hitRatio`)
- [x] 2.3 Add `model/AlgorithmResult` record with a `skipped(...)` factory
- [x] 2.4 Add `model/ComputeRequest` record with `@Min(0) @Max(5000)` on `n`
      and `includeNaive` defaulting to true
- [x] 2.5 Add `model/ComputeResponse` record
- [x] 2.6 Verify `total` and `hitRatio` are serialized (including `hitRatio = 0`
      when `total = 0`)

## 3. Algorithms

- [x] 3.1 Add `algorithm/FibonacciAlgorithm` interface (metadata + `compute`)
- [x] 3.2 Add `algorithm/CacheAwareAlgorithm` sub-interface (`resetCache`, `cacheStats`)
- [x] 3.3 Implement `NaiveRecursiveAlgorithm` (`@Order(1)`, bound 35, explicit inclusion)
- [x] 3.4 Add `config/CacheConfig` building a Caffeine cache with
      `maximumSize(10_000)`, `expireAfterAccess(10m)` and `recordStats()`
- [x] 3.5 Implement `MemoizedAlgorithm` (`@Order(2)`) over the Caffeine `Cache`,
      with a class comment explaining why `@Cacheable` is not used
- [x] 3.6 Implement hit/miss reporting as a delta against a snapshot taken in `resetCache`
- [x] 3.7 Run the memoized recursion on a dedicated 32 MB-stack thread, created
      outside the timed region
- [x] 3.8 Implement `IterativeAlgorithm` (`@Order(3)`)
- [x] 3.9 Implement `FastDoublingAlgorithm` (`@Order(4)`)
- [x] 3.10 Reject negative `n` with `IllegalArgumentException` in all four

## 4. Service layer

- [x] 4.1 Add `service/FibonacciService` interface (`computeAll(n, includeNaive)`)
- [x] 4.2 Implement `DefaultFibonacciService` injecting `List<FibonacciAlgorithm>`
- [x] 4.3 Validate `n` in `[0, MAX_N]`, throwing `IllegalArgumentException`
- [x] 4.4 Apply the skip rules (explicit exclusion, then `n > algorithm.maxN`)
- [x] 4.5 Reset the cache of cache-aware algorithms before measuring
- [x] 4.6 Measure with `System.nanoTime()` and report seconds as a `double`
- [x] 4.7 Compute `allMatch` over non-skipped results and select the fastest
- [x] 4.8 Log the request at INFO, matching the .NET service

## 5. JSON API

- [x] 5.1 Add `web/FibonacciApiController` with `POST /api/fibonacci/compute`
- [x] 5.2 Bind the body with `@Valid @RequestBody`
- [x] 5.3 Add `web/ApiExceptionHandler` mapping `MethodArgumentNotValidException`
      and `IllegalArgumentException` to 400 with a `message` field
- [x] 5.4 Map unhandled exceptions to a 500 JSON body with a `message` field
- [x] 5.5 Enable CORS for `http://localhost:5173`
- [x] 5.6 Verify the response against `fibonacci-dotnet/frontend/src/types/fibonacci.ts`
      field by field

## 6. Thymeleaf UI

- [x] 6.1 Add `web/FibonacciViewController` with `GET /` and `POST /`
- [x] 6.2 Add `templates/index.html`: form for `n` and `includeNaive`
- [x] 6.3 Render the results table (label, complexities, result, time, cache stats)
- [x] 6.4 Render the winner banner and the integrity check
- [x] 6.5 Render skipped rows with their reason instead of a result
- [x] 6.6 Add a view helper abbreviating results longer than 40 digits, leaving
      `AlgorithmResult` untouched
- [x] 6.7 Render performance bars with a server-computed inline width
- [x] 6.8 Render validation errors inline from `BindingResult`
- [x] 6.9 Add `static/css/app.css` and a note about JIT warm-up affecting timings
- [x] 6.10 Confirm the page is fully usable with JavaScript disabled

## 7. Tests

- [x] 7.1 `FibonacciAlgorithmTest`: known values, cross-implementation agreement
      for 0..30, n=1000 agreement, negative input, declared metadata
- [x] 7.2 `MemoizedAlgorithmTest`: hit/miss counts at n=30, repeated call,
      reset semantics, n=5000 without `StackOverflowError`
- [x] 7.3 `DefaultFibonacciServiceTest`: agreement, both skip paths, skipped
      entry shape, fastest selection, cache stats presence, invalid `n`
- [x] 7.4 `FibonacciApiControllerTest` (`@WebMvcTest`): 200 shape, exact field
      set and casing, `result` as a string, `includeNaive` default, 400 body
- [x] 7.5 `FibonacciViewControllerTest` (`@WebMvcTest`): empty form, rendered
      results, skipped reason, abbreviation, inline validation error
- [x] 7.6 `FibonacciApplicationTest` (`@SpringBootTest`): context loads and
      `List<FibonacciAlgorithm>` is ordered `recursive`, `memoized`,
      `iterative`, `fast-doubling`
- [x] 7.7 Run `./mvnw verify` and confirm the suite is green

## 8. Documentation and verification

- [x] 8.1 Add a "Stack Java" section to `TECH_README.md` (prerequisites,
      install, run, test, endpoint, algorithms table)
- [x] 8.2 Add the Java stack to the stack table at the top of `TECH_README.md`
- [x] 8.3 Document the JIT warm-up caveat on measured timings
- [x] 8.4 Document how to repoint the Vue frontend at the Java API (Vite proxy)
- [x] 8.5 Add Java rows to the troubleshooting table (wrong JDK, port 8080 busy)
- [x] 8.6 Add `target/` to the root `.gitignore`
- [x] 8.7 Start the app, exercise the page for n=30 and n=5000, and call the
      endpoint with curl to confirm both surfaces agree
