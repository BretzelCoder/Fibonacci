package com.bretzelcoder.fibonacci.service;

import com.bretzelcoder.fibonacci.algorithm.CacheAwareAlgorithm;
import com.bretzelcoder.fibonacci.algorithm.FibonacciAlgorithm;
import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import com.bretzelcoder.fibonacci.model.AlgorithmResult;
import com.bretzelcoder.fibonacci.model.CacheStats;
import com.bretzelcoder.fibonacci.model.ComputeResponse;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the comparison: runs each algorithm, times it, checks that they agree,
 * and reports the fastest.
 *
 * <p>Spring populates the injected {@code List<FibonacciAlgorithm>} from every bean of that
 * type, ordered by {@code @Order} — the direct counterpart of .NET resolving
 * {@code IEnumerable<IFibonacciAlgorithm>} from registration order. That order is observable:
 * it is the order of {@code results[]} in the response.
 */
@Service
public class DefaultFibonacciService implements FibonacciService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFibonacciService.class);

    private final List<FibonacciAlgorithm> algorithms;

    public DefaultFibonacciService(List<FibonacciAlgorithm> algorithms) {
        if (algorithms.isEmpty()) {
            throw new IllegalStateException("No FibonacciAlgorithm beans found — check component scanning.");
        }
        this.algorithms = List.copyOf(algorithms);
    }

    @Override
    public ComputeResponse computeAll(int n, boolean includeNaive) {
        if (n < 0 || n > FibonacciConstants.MAX_N) {
            throw new IllegalArgumentException(
                    "n must be between 0 and " + FibonacciConstants.MAX_N + ", was " + n + ".");
        }

        log.info("Computing Fibonacci for n={}, includeNaive={}", n, includeNaive);

        List<AlgorithmResult> results = algorithms.stream()
                .map(algorithm -> run(algorithm, n, includeNaive))
                .toList();

        List<AlgorithmResult> executed = results.stream()
                .filter(result -> !result.skipped())
                .toList();

        if (executed.isEmpty()) {
            throw new IllegalStateException("Every algorithm was skipped for n=" + n + ".");
        }

        boolean allMatch = executed.stream()
                .map(AlgorithmResult::result)
                .distinct()
                .count() == 1;

        AlgorithmResult best = executed.stream()
                .min(Comparator.comparingDouble(AlgorithmResult::timeSeconds))
                .orElseThrow();

        return new ComputeResponse(n, results, allMatch, best.label(), best.timeSeconds());
    }

    private AlgorithmResult run(FibonacciAlgorithm algorithm, int n, boolean includeNaive) {
        if (algorithm.requiresExplicitInclusion() && !includeNaive) {
            return AlgorithmResult.skipped(algorithm, n, "Excluded by user.");
        }

        if (n > algorithm.maxN()) {
            return AlgorithmResult.skipped(algorithm, n,
                    "n > " + algorithm.maxN() + " — " + algorithm.timeComplexity() + " cost.");
        }

        // Reset before measuring so hit/miss counts describe this run alone.
        if (algorithm instanceof CacheAwareAlgorithm cacheAware) {
            cacheAware.resetCache();
        }

        long startedAt = System.nanoTime();
        BigInteger value = algorithm.compute(n);
        double elapsedSeconds = (System.nanoTime() - startedAt) / 1_000_000_000d;

        CacheStats cacheStats = algorithm instanceof CacheAwareAlgorithm cacheAware
                ? cacheAware.cacheStats()
                : null;

        return new AlgorithmResult(
                algorithm.name(),
                algorithm.label(),
                algorithm.timeComplexity(),
                algorithm.spaceComplexity(),
                n,
                Objects.toString(value, null),
                elapsedSeconds,
                cacheStats,
                false,
                null
        );
    }
}
