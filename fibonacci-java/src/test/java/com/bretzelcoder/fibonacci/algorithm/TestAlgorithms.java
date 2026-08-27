package com.bretzelcoder.fibonacci.algorithm;

import com.bretzelcoder.fibonacci.config.CacheConfig;

/**
 * Builds algorithm instances outside a Spring context. Each call returns a
 * {@link MemoizedAlgorithm} over a brand-new cache, so tests never share cached
 * values or statistics.
 */
final class TestAlgorithms {

    static MemoizedAlgorithm memoized() {
        return new MemoizedAlgorithm(new CacheConfig().fibonacciCache());
    }

    private TestAlgorithms() {
    }
}
