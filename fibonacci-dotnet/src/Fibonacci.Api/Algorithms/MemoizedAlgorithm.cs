using System.Numerics;
using Fibonacci.Api.Constants;
using Fibonacci.Api.Models;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Primitives;

namespace Fibonacci.Api.Algorithms;

/// <summary>
/// Memoized recursive Fibonacci backed by ASP.NET Core's IMemoryCache.
/// Demonstrates the idiomatic .NET approach to in-process caching via dependency injection.
/// O(n) time and O(n) space — each unique n is computed once; subsequent calls are O(1) hits.
/// Hit/miss counters use Interlocked for thread safety without lock contention.
/// Cache invalidation uses a CancellationChangeToken for O(1) eviction instead of looping over keys.
/// </summary>
public sealed class MemoizedAlgorithm : ICacheAwareAlgorithm
{
    private readonly IMemoryCache _cache;
    private int _hits;
    private int _misses;
    private CancellationTokenSource _cts = new();

    public MemoizedAlgorithm(IMemoryCache cache) => _cache = cache;

    public string Name => "memoized";
    public string Label => "Memoized (IMemoryCache)";
    public string TimeComplexity => "O(n)";
    public string SpaceComplexity => "O(n)";
    public int MaxN => FibonacciConstants.MaxN;
    public bool RequiresExplicitInclusion => false;

    public void ResetCache()
    {
        var old = Interlocked.Exchange(ref _cts, new CancellationTokenSource());
        old.Cancel();
        old.Dispose();

        Interlocked.Exchange(ref _hits, 0);
        Interlocked.Exchange(ref _misses, 0);
    }

    public CacheStats GetCacheStats() =>
        new(Volatile.Read(ref _hits), Volatile.Read(ref _misses));

    public BigInteger Compute(int n)
    {
        ArgumentOutOfRangeException.ThrowIfNegative(n);

        if (_cache.TryGetValue<BigInteger>(CacheKey(n), out var cached))
        {
            Interlocked.Increment(ref _hits);
            return cached;
        }

        Interlocked.Increment(ref _misses);
        var result = n <= 1 ? new BigInteger(n) : Compute(n - 1) + Compute(n - 2);

        _cache.Set(CacheKey(n), result, new MemoryCacheEntryOptions
        {
            SlidingExpiration = TimeSpan.FromMinutes(10),
            Size = 1,
        }.AddExpirationToken(new CancellationChangeToken(_cts.Token)));

        return result;
    }

    private static string CacheKey(int n) => $"fib:memoized:{n}";
}
