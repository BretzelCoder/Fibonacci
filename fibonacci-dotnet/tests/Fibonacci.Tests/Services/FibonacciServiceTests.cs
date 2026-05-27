using Fibonacci.Api.Algorithms;
using Fibonacci.Api.Constants;
using Fibonacci.Api.Services;
using FluentAssertions;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Logging.Abstractions;
using Microsoft.Extensions.Options;
using Xunit;

namespace Fibonacci.Tests.Services;

public sealed class FibonacciServiceTests
{
    private static IMemoryCache CreateCache() =>
        new MemoryCache(Options.Create(new MemoryCacheOptions { SizeLimit = 10_000 }));

    private static FibonacciService CreateService()
    {
        var cache = CreateCache();
        IFibonacciAlgorithm[] algorithms =
        [
            new NaiveRecursiveAlgorithm(),
            new MemoizedAlgorithm(cache),
            new IterativeAlgorithm(),
            new FastDoublingAlgorithm(),
        ];

        return new FibonacciService(algorithms, NullLogger<FibonacciService>.Instance);
    }

    // ── Response structure ────────────────────────────────────────────────────

    [Fact]
    public void ComputeAll_ReturnsFourResults()
    {
        var response = CreateService().ComputeAll(10);
        response.Results.Should().HaveCount(4);
    }

    [Fact]
    public void ComputeAll_ResultsHaveExpectedAlgorithmNames()
    {
        var response = CreateService().ComputeAll(10);
        response.Results.Select(r => r.Name)
            .Should().BeEquivalentTo(["recursive", "memoized", "iterative", "fast-doubling"]);
    }

    // ── Integrity ─────────────────────────────────────────────────────────────

    [Fact]
    public void ComputeAll_AllResultsMatch_ForSmallN()
    {
        var response = CreateService().ComputeAll(20);
        response.AllMatch.Should().BeTrue();
    }

    [Fact]
    public void ComputeAll_AllResultsMatch_WhenNaiveIsExcluded()
    {
        var response = CreateService().ComputeAll(20, includeNaive: false);
        response.AllMatch.Should().BeTrue();
    }

    // ── Naive recursion skipping ──────────────────────────────────────────────

    [Fact]
    public void ComputeAll_NaiveSkipped_WhenIncludeNaiveIsFalse()
    {
        var response = CreateService().ComputeAll(10, includeNaive: false);
        response.Results.Single(r => r.Name == "recursive").Skipped.Should().BeTrue();
    }

    [Fact]
    public void ComputeAll_NaiveSkipped_WhenNExceedsMaxNNaive()
    {
        var response = CreateService().ComputeAll(FibonacciConstants.MaxNNaive + 1, includeNaive: true);
        response.Results.Single(r => r.Name == "recursive").Skipped.Should().BeTrue();
    }

    [Fact]
    public void ComputeAll_NaiveNotSkipped_WhenNWithinLimit()
    {
        var response = CreateService().ComputeAll(FibonacciConstants.MaxNNaive, includeNaive: true);
        response.Results.Single(r => r.Name == "recursive").Skipped.Should().BeFalse();
    }

    // ── Cache stats ───────────────────────────────────────────────────────────

    [Fact]
    public void ComputeAll_MemoizedResult_HasCacheStats()
    {
        var response = CreateService().ComputeAll(10);
        var memoized = response.Results.Single(r => r.Name == "memoized");

        memoized.CacheStats.Should().NotBeNull();
        memoized.CacheStats!.Misses.Should().BeGreaterThan(0);
    }

    [Fact]
    public void ComputeAll_NonCacheAlgorithms_HaveNullCacheStats()
    {
        var response = CreateService().ComputeAll(10);

        response.Results
            .Where(r => r.Name != "memoized" && !r.Skipped)
            .Should().AllSatisfy(r => r.CacheStats.Should().BeNull());
    }

    // ── Best performer ────────────────────────────────────────────────────────

    [Fact]
    public void ComputeAll_BestNameIsNonEmpty()
    {
        var response = CreateService().ComputeAll(15);
        response.BestName.Should().NotBeNullOrWhiteSpace();
    }

    [Fact]
    public void ComputeAll_BestTimeSeconds_MatchesAtLeastOneResult()
    {
        var response = CreateService().ComputeAll(15);
        response.Results
            .Where(r => !r.Skipped)
            .Should().Contain(r => r.TimeSeconds == response.BestTimeSeconds);
    }

    // ── Guard conditions ──────────────────────────────────────────────────────

    [Theory]
    [InlineData(-1)]
    [InlineData(FibonacciConstants.MaxN + 1)]
    public void ComputeAll_ThrowsArgumentOutOfRangeException_ForInvalidN(int n)
    {
        CreateService()
            .Invoking(s => s.ComputeAll(n))
            .Should().Throw<ArgumentOutOfRangeException>();
    }
}
