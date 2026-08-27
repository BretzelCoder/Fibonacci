package com.bretzelcoder.fibonacci.config;

/**
 * Bounds shared with the Python and .NET stacks. Keep the three in sync:
 * the whole point of the repository is that the same n produces comparable
 * results across implementations.
 */
public final class FibonacciConstants {

    /** Exponential cost becomes unacceptable above this value for naive recursion. */
    public static final int MAX_N_NAIVE = 35;

    /** Upper bound for all algorithms — keeps response times reasonable. */
    public static final int MAX_N = 5_000;

    private FibonacciConstants() {
    }
}
