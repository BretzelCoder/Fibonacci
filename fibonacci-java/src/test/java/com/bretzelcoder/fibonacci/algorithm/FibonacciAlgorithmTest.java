package com.bretzelcoder.fibonacci.algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Behaviour every implementation must share, plus the declared metadata each one owns.
 * Plain unit tests — no Spring context, so the suite stays fast.
 */
class FibonacciAlgorithmTest {

    static Stream<FibonacciAlgorithm> allAlgorithms() {
        return Stream.of(
                new NaiveRecursiveAlgorithm(),
                TestAlgorithms.memoized(),
                new IterativeAlgorithm(),
                new FastDoublingAlgorithm()
        );
    }

    @ParameterizedTest(name = "{0} returns the known values")
    @MethodSource("allAlgorithms")
    void returnsKnownValues(FibonacciAlgorithm algorithm) {
        assertThat(algorithm.compute(0)).isEqualTo(BigInteger.ZERO);
        assertThat(algorithm.compute(1)).isEqualTo(BigInteger.ONE);
        assertThat(algorithm.compute(10)).isEqualTo(BigInteger.valueOf(55));
    }

    @ParameterizedTest(name = "{0} rejects negative n")
    @MethodSource("allAlgorithms")
    void rejectsNegativeN(FibonacciAlgorithm algorithm) {
        assertThatIllegalArgumentException().isThrownBy(() -> algorithm.compute(-1));
    }

    @Test
    @DisplayName("all four implementations agree for 0 <= n <= 30")
    void allImplementationsAgree() {
        List<FibonacciAlgorithm> algorithms = allAlgorithms().toList();

        IntStream.rangeClosed(0, 30).forEach(n -> {
            List<BigInteger> values = algorithms.stream()
                    .map(algorithm -> algorithm.compute(n))
                    .distinct()
                    .toList();

            assertThat(values)
                    .describedAs("implementations disagree for n=%d", n)
                    .hasSize(1);
        });
    }

    @Test
    @DisplayName("results beyond the 64-bit range stay exact")
    void agreesBeyondLongRange() {
        BigInteger iterative = new IterativeAlgorithm().compute(1000);
        BigInteger fastDoubling = new FastDoublingAlgorithm().compute(1000);
        BigInteger memoized = TestAlgorithms.memoized().compute(1000);

        assertThat(iterative).isEqualTo(fastDoubling).isEqualTo(memoized);
        assertThat(iterative.toString()).hasSize(209);
        assertThat(iterative).isGreaterThan(BigInteger.valueOf(Long.MAX_VALUE));
    }

    @Nested
    @DisplayName("declared metadata")
    class Metadata {

        @Test
        void naiveRecursive() {
            var algorithm = new NaiveRecursiveAlgorithm();

            assertThat(algorithm.name()).isEqualTo("recursive");
            assertThat(algorithm.label()).isEqualTo("Naive Recursive");
            assertThat(algorithm.timeComplexity()).isEqualTo("O(2^n)");
            assertThat(algorithm.spaceComplexity()).isEqualTo("O(n)");
            assertThat(algorithm.maxN()).isEqualTo(FibonacciConstants.MAX_N_NAIVE).isEqualTo(35);
            assertThat(algorithm.requiresExplicitInclusion()).isTrue();
        }

        @Test
        void memoized() {
            var algorithm = TestAlgorithms.memoized();

            assertThat(algorithm.name()).isEqualTo("memoized");
            assertThat(algorithm.timeComplexity()).isEqualTo("O(n)");
            assertThat(algorithm.spaceComplexity()).isEqualTo("O(n)");
            assertThat(algorithm.maxN()).isEqualTo(FibonacciConstants.MAX_N).isEqualTo(5_000);
            assertThat(algorithm.requiresExplicitInclusion()).isFalse();
        }

        @Test
        void iterative() {
            var algorithm = new IterativeAlgorithm();

            assertThat(algorithm.name()).isEqualTo("iterative");
            assertThat(algorithm.label()).isEqualTo("Iterative");
            assertThat(algorithm.timeComplexity()).isEqualTo("O(n)");
            assertThat(algorithm.spaceComplexity()).isEqualTo("O(1)");
            assertThat(algorithm.maxN()).isEqualTo(5_000);
            assertThat(algorithm.requiresExplicitInclusion()).isFalse();
        }

        @Test
        void fastDoubling() {
            var algorithm = new FastDoublingAlgorithm();

            assertThat(algorithm.name()).isEqualTo("fast-doubling");
            assertThat(algorithm.label()).isEqualTo("Fast Doubling");
            assertThat(algorithm.timeComplexity()).isEqualTo("O(log n)");
            assertThat(algorithm.spaceComplexity()).isEqualTo("O(log n)");
            assertThat(algorithm.maxN()).isEqualTo(5_000);
            assertThat(algorithm.requiresExplicitInclusion()).isFalse();
        }
    }
}
