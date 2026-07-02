using System.Diagnostics;
using System.Numerics;
using Fibonacci.Api.Algorithms;
using Fibonacci.Api.Constants;
using Fibonacci.Api.Models;

namespace Fibonacci.Api.Services;

public sealed class FibonacciService : IFibonacciService
{
    private readonly IEnumerable<IFibonacciAlgorithm> _algorithms;
    private readonly ILogger<FibonacciService> _logger;

    public FibonacciService(
        IEnumerable<IFibonacciAlgorithm> algorithms,
        ILogger<FibonacciService> logger)
    {
        _algorithms = algorithms;
        _logger = logger;
    }

    public ComputeResponse ComputeAll(int n, bool includeNaive = true)
    {
        if (n < 0 || n > FibonacciConstants.MaxN)
            throw new ArgumentOutOfRangeException(nameof(n), $"n must be in [0, {FibonacciConstants.MaxN}].");

        _logger.LogInformation("Computing Fibonacci for n={N}, includeNaive={IncludeNaive}", n, includeNaive);

        var results = _algorithms
            .Select(algo => RunAlgorithm(algo, n, includeNaive))
            .ToList();

        var computed = results.Where(r => !r.Skipped).ToList();
        var allMatch = computed.Select(r => r.Result).Distinct().Count() == 1;
        var best = computed.MinBy(r => r.TimeSeconds)
            ?? throw new InvalidOperationException("No algorithms produced a result — check DI registration.");

        return new ComputeResponse(
            N: n,
            Results: results.AsReadOnly(),
            AllMatch: allMatch,
            BestName: best.Label,
            BestTimeSeconds: best.TimeSeconds
        );
    }

    private AlgorithmResult RunAlgorithm(IFibonacciAlgorithm algo, int n, bool includeNaive)
    {
        if (algo.RequiresExplicitInclusion && !includeNaive)
            return AlgorithmResult.FromSkipped(algo, n, "Excluded by user.");

        if (n > algo.MaxN)
            return AlgorithmResult.FromSkipped(algo, n, $"n > {algo.MaxN} — {algo.TimeComplexity} cost.");

        if (algo is ICacheAwareAlgorithm cacheAware)
            cacheAware.ResetCache();

        var (value, elapsed) = Measure(algo, n);

        CacheStats? cacheStats = algo is ICacheAwareAlgorithm withStats
            ? withStats.GetCacheStats()
            : null;

        return new AlgorithmResult(
            Name: algo.Name,
            Label: algo.Label,
            TimeComplexity: algo.TimeComplexity,
            SpaceComplexity: algo.SpaceComplexity,
            N: n,
            Result: value.ToString(),
            TimeSeconds: elapsed,
            CacheStats: cacheStats,
            Skipped: false,
            SkipReason: null
        );
    }

    private static (BigInteger Value, double ElapsedSeconds) Measure(IFibonacciAlgorithm algo, int n)
    {
        var sw = Stopwatch.StartNew();
        var value = algo.Compute(n);
        sw.Stop();
        return (value, sw.Elapsed.TotalSeconds);
    }
}
