package com.bretzelcoder.fibonacci.algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The caching behaviour this project exists to practise.
 * The expected counts are the same ones the Python ({@code lru_cache}) and .NET
 * ({@code IMemoryCache}) stacks report for n=30 — that agreement is the point.
 */
class MemoizedAlgorithmTest {

    private MemoizedAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = TestAlgorithms.memoized();
        algorithm.resetCache();
    }

    @AfterEach
    void tearDown() {
        algorithm.shutdown();
    }

    @Test
    @DisplayName("each value below n is computed exactly once")
    void computesEachValueOnce() {
        algorithm.compute(30);

        var stats = algorithm.cacheStats();

        assertThat(stats.misses()).isEqualTo(31);
        assertThat(stats.hits()).isEqualTo(28);
        assertThat(stats.total()).isEqualTo(59);
        assertThat(stats.hitRatio()).isEqualTo(28d / 59d);
    }

    @Test
    @DisplayName("a repeated call is served entirely from the cache")
    void repeatedCallHitsTheCache() {
        algorithm.compute(30);
        long missesAfterFirst = algorithm.cacheStats().misses();
        long hitsAfterFirst = algorithm.cacheStats().hits();

        algorithm.compute(30);

        var stats = algorithm.cacheStats();
        assertThat(stats.hits()).isEqualTo(hitsAfterFirst + 1);
        assertThat(stats.misses()).isEqualTo(missesAfterFirst);
    }

    @Test
    @DisplayName("reset clears both the entries and the statistics")
    void resetClearsEverything() {
        algorithm.compute(30);
        assertThat(algorithm.cacheStats().total()).isPositive();

        algorithm.resetCache();

        assertThat(algorithm.cacheStats().hits()).isZero();
        assertThat(algorithm.cacheStats().misses()).isZero();
        assertThat(algorithm.cacheStats().hitRatio()).isZero();

        // Values are gone too, so the same n misses all over again.
        algorithm.compute(30);
        assertThat(algorithm.cacheStats().misses()).isEqualTo(31);
    }

    @Test
    @DisplayName("recursion at the upper bound does not overflow the stack")
    void handlesMaximumNWithoutStackOverflow() {
        assertThatCode(() -> {
            BigInteger value = algorithm.compute(5_000);
            assertThat(value.toString()).hasSize(1_045);
            assertThat(value).isEqualTo(new IterativeAlgorithm().compute(5_000));
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("statistics are reported per measurement, not cumulatively")
    void statisticsAreScopedToTheLastReset() {
        algorithm.compute(30);
        algorithm.resetCache();
        algorithm.compute(10);

        var stats = algorithm.cacheStats();
        assertThat(stats.misses()).isEqualTo(11);
        assertThat(stats.hits()).isEqualTo(8);
    }
}
