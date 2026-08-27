package com.bretzelcoder.fibonacci.algorithm;

import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import java.math.BigInteger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Naive recursion — O(2^n) time, O(n) stack.
 * Kept for its educational value: it recomputes the same sub-problems over and over,
 * which is exactly what the memoized variant exists to avoid. Bounded at
 * {@link FibonacciConstants#MAX_N_NAIVE} because the cost past that is prohibitive.
 */
@Component
@Order(1)
public class NaiveRecursiveAlgorithm implements FibonacciAlgorithm {

    @Override
    public String name() {
        return "recursive";
    }

    @Override
    public String label() {
        return "Naive Recursive";
    }

    @Override
    public String timeComplexity() {
        return "O(2^n)";
    }

    @Override
    public String spaceComplexity() {
        return "O(n)";
    }

    @Override
    public int maxN() {
        return FibonacciConstants.MAX_N_NAIVE;
    }

    @Override
    public boolean requiresExplicitInclusion() {
        return true;
    }

    @Override
    public BigInteger compute(int n) {
        requireNonNegative(n);
        return fib(n);
    }

    private static BigInteger fib(int n) {
        return n <= 1
                ? BigInteger.valueOf(n)
                : fib(n - 1).add(fib(n - 2));
    }
}
