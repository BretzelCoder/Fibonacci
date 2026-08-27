package com.bretzelcoder.fibonacci.algorithm;

import com.bretzelcoder.fibonacci.model.CacheStats;

/**
 * Implemented only by algorithms that maintain a cache and can report on it.
 * Interface segregation: the service depends on this contract solely to reset
 * the cache before a measurement and to read the resulting statistics.
 */
public interface CacheAwareAlgorithm extends FibonacciAlgorithm {

    /** Drops every cached entry and rebases the statistics to zero. */
    void resetCache();

    /** Hits and misses accumulated since the last {@link #resetCache()}. */
    CacheStats cacheStats();
}
