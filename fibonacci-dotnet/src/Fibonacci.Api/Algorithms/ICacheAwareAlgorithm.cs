using Fibonacci.Api.Models;

namespace Fibonacci.Api.Algorithms;

/// <summary>
/// Extends IFibonacciAlgorithm for algorithms that maintain a cache and can expose statistics.
/// Interface Segregation: only consumers that care about cache stats depend on this contract.
/// </summary>
public interface ICacheAwareAlgorithm : IFibonacciAlgorithm
{
    void ResetCache();
    CacheStats GetCacheStats();
}
