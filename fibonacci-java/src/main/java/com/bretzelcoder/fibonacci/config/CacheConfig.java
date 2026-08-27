package com.bretzelcoder.fibonacci.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.math.BigInteger;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caffeine cache backing the memoized algorithm.
 *
 * <p>The settings mirror the .NET stack one for one so the two implementations stay
 * comparable: {@code maximumSize} is the analogue of {@code MemoryCacheOptions.SizeLimit}
 * with {@code Size = 1} per entry, and {@code expireAfterAccess} of {@code SlidingExpiration}.
 *
 * <p>{@code recordStats()} is what makes hit/miss reporting possible without hand-rolled
 * counters — see {@link com.bretzelcoder.fibonacci.algorithm.MemoizedAlgorithm}.
 */
@Configuration
public class CacheConfig {

    /** One entry per distinct n; 10 000 comfortably covers the [0, MAX_N] range. */
    public static final long MAX_CACHE_ENTRIES = 10_000L;

    public static final Duration EXPIRE_AFTER_ACCESS = Duration.ofMinutes(10);

    @Bean
    public Cache<Integer, BigInteger> fibonacciCache() {
        return Caffeine.newBuilder()
                .maximumSize(MAX_CACHE_ENTRIES)
                .expireAfterAccess(EXPIRE_AFTER_ACCESS)
                .recordStats()
                .build();
    }
}
