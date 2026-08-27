package com.bretzelcoder.fibonacci.algorithm;

import java.math.BigInteger;

/**
 * One strategy for computing F(n), together with the metadata the comparison UI needs.
 *
 * <p>Implementations are discovered by Spring and injected as a {@code List<FibonacciAlgorithm>}.
 * Their {@code @Order} value fixes the order of {@code results[]} in the API response, which is
 * part of the observable contract — see {@code FibonacciApplicationTest}.
 */
public interface FibonacciAlgorithm {

    /** Stable machine-readable identifier, e.g. {@code "fast-doubling"}. */
    String name();

    /** Human-readable name shown in the UI. */
    String label();

    String timeComplexity();

    String spaceComplexity();

    /** Largest n this algorithm may be asked to compute. */
    int maxN();

    /** Whether the caller must opt in explicitly (true only for the naive recursion). */
    boolean requiresExplicitInclusion();

    BigInteger compute(int n);

    /** Shared guard: every implementation rejects negative input identically. */
    default void requireNonNegative(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative, was " + n + ".");
        }
    }
}
