package com.bretzelcoder.fibonacci.model;

import com.bretzelcoder.fibonacci.algorithm.FibonacciAlgorithm;

/**
 * One algorithm's outcome for a given n.
 *
 * <p>{@code result} is a {@code String} rather than a {@code BigInteger} on purpose:
 * JavaScript numbers lose precision above 2^53, and F(79) already exceeds that.
 * The .NET stack makes the same choice, so both APIs serialize the value as a
 * JSON string and the shared TypeScript type declares {@code result: string | null}.
 */
public record AlgorithmResult(
        String name,
        String label,
        String timeComplexity,
        String spaceComplexity,
        int n,
        String result,
        double timeSeconds,
        CacheStats cacheStats,
        boolean skipped,
        String skipReason
) {

    public static AlgorithmResult skipped(FibonacciAlgorithm algorithm, int n, String reason) {
        return new AlgorithmResult(
                algorithm.name(),
                algorithm.label(),
                algorithm.timeComplexity(),
                algorithm.spaceComplexity(),
                n,
                null,
                0d,
                null,
                true,
                reason
        );
    }
}
