package com.bretzelcoder.fibonacci.algorithm;

import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import java.math.BigInteger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Fast doubling — O(log n) time, O(log n) stack.
 * The JVM counterpart of Python's {@code sympy.fibonacci()}, based on the Lucas identities:
 * <pre>
 *   F(2k)   = F(k) * [2*F(k+1) - F(k)]
 *   F(2k+1) = F(k)^2 + F(k+1)^2
 * </pre>
 * Each recursive step halves n, so the depth is logarithmic and needs no special stack.
 */
@Component
@Order(4)
public class FastDoublingAlgorithm implements FibonacciAlgorithm {

    @Override
    public String name() {
        return "fast-doubling";
    }

    @Override
    public String label() {
        return "Fast Doubling";
    }

    @Override
    public String timeComplexity() {
        return "O(log n)";
    }

    @Override
    public String spaceComplexity() {
        return "O(log n)";
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
        return fastDouble(n).current();
    }

    /** F(k) and F(k+1) computed together — the pair is what makes the doubling step possible. */
    private record Pair(BigInteger current, BigInteger next) {
    }

    private static Pair fastDouble(int n) {
        if (n == 0) {
            return new Pair(BigInteger.ZERO, BigInteger.ONE);
        }

        Pair half = fastDouble(n >> 1);
        BigInteger f = half.current();
        BigInteger fNext = half.next();

        BigInteger c = f.multiply(fNext.shiftLeft(1).subtract(f));
        BigInteger d = f.multiply(f).add(fNext.multiply(fNext));

        return (n & 1) == 0 ? new Pair(c, d) : new Pair(d, c.add(d));
    }
}
