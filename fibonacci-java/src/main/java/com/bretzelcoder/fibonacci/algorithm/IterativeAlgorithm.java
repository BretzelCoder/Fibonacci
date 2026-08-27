package com.bretzelcoder.fibonacci.algorithm;

import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import java.math.BigInteger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Bottom-up iteration — O(n) time, O(1) space.
 * Holds only the last two values: no recursion stack, no cache, no allocation beyond
 * the BigInteger arithmetic itself. Usually the fastest of the four at moderate n.
 */
@Component
@Order(3)
public class IterativeAlgorithm implements FibonacciAlgorithm {

    @Override
    public String name() {
        return "iterative";
    }

    @Override
    public String label() {
        return "Iterative";
    }

    @Override
    public String timeComplexity() {
        return "O(n)";
    }

    @Override
    public String spaceComplexity() {
        return "O(1)";
    }

    @Override
    public int maxN() {
        return FibonacciConstants.MAX_N;
    }

    @Override
    public boolean requiresExplicitInclusion() {
        return false;
    }

    @Override
    public BigInteger compute(int n) {
        requireNonNegative(n);

        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.ONE;
        for (int i = 0; i < n; i++) {
            BigInteger next = a.add(b);
            a = b;
            b = next;
        }
        return a;
    }
}
