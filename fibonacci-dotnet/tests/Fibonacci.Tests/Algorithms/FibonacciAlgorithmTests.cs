using System.Numerics;
using Fibonacci.Api.Algorithms;
using Fibonacci.Api.Constants;
using FluentAssertions;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Options;
using Xunit;

namespace Fibonacci.Tests.Algorithms;

public sealed class FibonacciAlgorithmTests
{
    // Reference values verified against the mathematical definition.
    private static readonly (int N, BigInteger Expected)[] KnownValues =
    [
        (0,  new BigInteger(0)),
        (1,  new BigInteger(1)),
        (2,  new BigInteger(1)),
        (3,  new BigInteger(2)),
        (5,  new BigInteger(5)),
        (10, new BigInteger(55)),
        (20, new BigInteger(6_765)),
        (30, new BigInteger(832_040)),
    ];

    private static IMemoryCache CreateCache() =>
        new MemoryCache(Options.Create(new MemoryCacheOptions { SizeLimit = 10_000 }));

    // ── Correctness (parametrised across all four algorithms) ─────────────────

    [Theory]
    [MemberData(nameof(AlgorithmsWithKnownValues))]
    public void Compute_ReturnsCorrectValue(IFibonacciAlgorithm algo, int n, BigInteger expected)
    {
        algo.Compute(n).Should().Be(expected);
    }

    public static IEnumerable<object[]> AlgorithmsWithKnownValues()
    {
        var cache = CreateCache();
        IFibonacciAlgorithm[] algorithms =
        [
            new NaiveRecursiveAlgorithm(),
            new MemoizedAlgorithm(cache),
            new IterativeAlgorithm(),
            new FastDoublingAlgorithm(),
        ];

        foreach (var algo in algorithms)
        foreach (var (n, expected) in KnownValues)
            if (n <= algo.MaxN)
                yield return [algo, n, expected];
    }

    // ── Integrity: all four algorithms agree ──────────────────────────────────

    [Fact]
    public void AllAlgorithms_ProduceSameResultForN30()
    {
        var cache = CreateCache();
        BigInteger reference = new NaiveRecursiveAlgorithm().Compute(30);

        new MemoizedAlgorithm(cache).Compute(30).Should().Be(reference);
        new IterativeAlgorithm().Compute(30).Should().Be(reference);
        new FastDoublingAlgorithm().Compute(30).Should().Be(reference);
    }

    // ── Guard conditions ──────────────────────────────────────────────────────

    [Theory]
    [InlineData(-1)]
    [InlineData(-100)]
    public void AllAlgorithms_ThrowForNegativeN(int n)
    {
        var cache = CreateCache();
        IFibonacciAlgorithm[] algorithms =
        [
            new NaiveRecursiveAlgorithm(),
            new MemoizedAlgorithm(cache),
            new IterativeAlgorithm(),
            new FastDoublingAlgorithm(),
        ];

        foreach (var algo in algorithms)
            algo.Invoking(a => a.Compute(n)).Should().Throw<ArgumentOutOfRangeException>();
    }

    [Fact]
    public void NaiveRecursive_MaxNIs35()
    {
        new NaiveRecursiveAlgorithm().MaxN.Should().Be(FibonacciConstants.MaxNNaive);
    }

    // ── Memoized cache behaviour ──────────────────────────────────────────────

    [Fact]
    public void MemoizedAlgorithm_ResetCacheClearsStatsAndEvictsEntries()
    {
        var algo = new MemoizedAlgorithm(CreateCache());
        algo.Compute(10);
        algo.ResetCache();

        var stats = algo.GetCacheStats();
        stats.Hits.Should().Be(0);
        stats.Misses.Should().Be(0);
    }

    [Fact]
    public void MemoizedAlgorithm_FirstCompute_HasBothHitsAndMisses_FromOverlappingSubproblems()
    {
        // The recursive structure means overlapping sub-problems are hit even during the
        // very first computation. e.g. Compute(7) is needed by both Compute(9) and Compute(8).
        var algo = new MemoizedAlgorithm(CreateCache());
        algo.ResetCache();

        algo.Compute(10);
        var stats = algo.GetCacheStats();

        stats.Hits.Should().BeGreaterThan(0, because: "overlapping sub-problems are cache-hit within the same traversal");
        stats.Misses.Should().BeGreaterThan(0, because: "unique values not yet seen require computation");
    }

    [Fact]
    public void MemoizedAlgorithm_RepeatCall_AddsExactlyOneHit_WithNoNewMisses()
    {
        var algo = new MemoizedAlgorithm(CreateCache());
        algo.ResetCache();
        algo.Compute(10);
        var statsAfterFirst = algo.GetCacheStats();

        algo.Compute(10); // the top-level entry is now cached
        var statsAfterSecond = algo.GetCacheStats();

        statsAfterSecond.Misses.Should().Be(statsAfterFirst.Misses, because: "every sub-result is already cached");
        statsAfterSecond.Hits.Should().Be(statsAfterFirst.Hits + 1, because: "only the top-level cache entry is hit");
    }

    [Fact]
    public void MemoizedAlgorithm_HitRatio_IsBetweenZeroAndOne()
    {
        var algo = new MemoizedAlgorithm(CreateCache());
        algo.ResetCache();
        algo.Compute(15);
        algo.Compute(15); // second call on the warm cache

        var stats = algo.GetCacheStats();
        stats.HitRatio.Should().BeInRange(0, 1);
    }
}
