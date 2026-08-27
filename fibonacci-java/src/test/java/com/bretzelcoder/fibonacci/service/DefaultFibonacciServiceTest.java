package com.bretzelcoder.fibonacci.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.bretzelcoder.fibonacci.algorithm.FastDoublingAlgorithm;
import com.bretzelcoder.fibonacci.algorithm.IterativeAlgorithm;
import com.bretzelcoder.fibonacci.algorithm.MemoizedAlgorithm;
import com.bretzelcoder.fibonacci.algorithm.NaiveRecursiveAlgorithm;
import com.bretzelcoder.fibonacci.config.CacheConfig;
import com.bretzelcoder.fibonacci.model.AlgorithmResult;
import com.bretzelcoder.fibonacci.model.ComputeResponse;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultFibonacciServiceTest {

    private DefaultFibonacciService service;

    @BeforeEach
    void setUp() {
        service = new DefaultFibonacciService(List.of(
                new NaiveRecursiveAlgorithm(),
                new MemoizedAlgorithm(new CacheConfig().fibonacciCache()),
                new IterativeAlgorithm(),
                new FastDoublingAlgorithm()
        ));
    }

    @Test
    @DisplayName("every algorithm runs and they all agree")
    void allAlgorithmsAgree() {
        ComputeResponse response = service.computeAll(20, true);

        assertThat(response.n()).isEqualTo(20);
        assertThat(response.results()).hasSize(4).noneMatch(AlgorithmResult::skipped);
        assertThat(response.results()).extracting(AlgorithmResult::result).containsOnly("6765");
        assertThat(response.allMatch()).isTrue();
    }

    @Test
    @DisplayName("results keep the declared algorithm order")
    void keepsAlgorithmOrder() {
        ComputeResponse response = service.computeAll(20, true);

        assertThat(response.results())
                .extracting(AlgorithmResult::name)
                .containsExactly("recursive", "memoized", "iterative", "fast-doubling");
    }

    @Test
    @DisplayName("naive recursion is skipped when the caller opts out")
    void skipsNaiveWhenExcluded() {
        ComputeResponse response = service.computeAll(20, false);

        AlgorithmResult recursive = entry(response, "recursive");
        assertThat(recursive.skipped()).isTrue();
        assertThat(recursive.skipReason()).isEqualTo("Excluded by user.");

        assertThat(response.results()).filteredOn(result -> !result.skipped()).hasSize(3);
        assertThat(response.allMatch()).isTrue();
    }

    @Test
    @DisplayName("naive recursion is skipped above its own bound")
    void skipsNaiveAboveItsBound() {
        ComputeResponse response = service.computeAll(50, true);

        AlgorithmResult recursive = entry(response, "recursive");
        assertThat(recursive.skipped()).isTrue();
        assertThat(recursive.skipReason()).contains("35").contains("O(2^n)");
    }

    @Test
    @DisplayName("a skipped entry carries no result, no time and no cache statistics")
    void skippedEntriesAreEmpty() {
        AlgorithmResult recursive = entry(service.computeAll(20, false), "recursive");

        assertThat(recursive.result()).isNull();
        assertThat(recursive.timeSeconds()).isZero();
        assertThat(recursive.cacheStats()).isNull();
        assertThat(recursive.n()).isEqualTo(20);
        assertThat(recursive.label()).isEqualTo("Naive Recursive");
    }

    @Test
    @DisplayName("the fastest executed algorithm is reported")
    void reportsTheFastest() {
        ComputeResponse response = service.computeAll(20, true);

        AlgorithmResult expected = response.results().stream()
                .filter(result -> !result.skipped())
                .min(Comparator.comparingDouble(AlgorithmResult::timeSeconds))
                .orElseThrow();

        assertThat(response.bestName()).isEqualTo(expected.label());
        assertThat(response.bestTimeSeconds()).isEqualTo(expected.timeSeconds());
    }

    @Test
    @DisplayName("only cache-aware algorithms report statistics")
    void onlyCacheAwareAlgorithmsReportStatistics() {
        ComputeResponse response = service.computeAll(20, true);

        assertThat(entry(response, "memoized").cacheStats()).isNotNull();
        assertThat(entry(response, "recursive").cacheStats()).isNull();
        assertThat(entry(response, "iterative").cacheStats()).isNull();
        assertThat(entry(response, "fast-doubling").cacheStats()).isNull();
    }

    @Test
    @DisplayName("the cache is reset before each measurement, so statistics are reproducible")
    void resetsTheCacheBetweenRequests() {
        var first = entry(service.computeAll(30, true), "memoized").cacheStats();
        var second = entry(service.computeAll(30, true), "memoized").cacheStats();

        assertThat(second).isEqualTo(first);
        assertThat(second.misses()).isEqualTo(31);
        assertThat(second.hits()).isEqualTo(28);
    }

    @Test
    @DisplayName("timings are non-negative")
    void timingsAreNonNegative() {
        assertThat(service.computeAll(20, true).results())
                .filteredOn(result -> !result.skipped())
                .allSatisfy(result -> assertThat(result.timeSeconds()).isNotNegative());
    }

    @Test
    @DisplayName("n outside [0, 5000] is rejected")
    void rejectsOutOfRangeN() {
        assertThatIllegalArgumentException().isThrownBy(() -> service.computeAll(-1, true));
        assertThatIllegalArgumentException().isThrownBy(() -> service.computeAll(5_001, true));
    }

    @Test
    @DisplayName("the bounds themselves are accepted")
    void acceptsTheBounds() {
        assertThat(service.computeAll(0, true).results())
                .filteredOn(result -> !result.skipped())
                .extracting(AlgorithmResult::result)
                .containsOnly("0");

        assertThat(service.computeAll(5_000, false).allMatch()).isTrue();
    }

    @Test
    @DisplayName("an empty algorithm list is a wiring error, not a runtime surprise")
    void rejectsAnEmptyAlgorithmList() {
        assertThatIllegalStateException()
                .isThrownBy(() -> new DefaultFibonacciService(List.of()))
                .withMessageContaining("No FibonacciAlgorithm beans");
    }

    private static AlgorithmResult entry(ComputeResponse response, String name) {
        return response.results().stream()
                .filter(result -> result.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no result named " + name));
    }
}
