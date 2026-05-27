using Fibonacci.Api.Algorithms;

namespace Fibonacci.Api.Models;

public record AlgorithmResult(
    string Name,
    string Label,
    string TimeComplexity,
    string SpaceComplexity,
    int N,
    string? Result,
    double TimeSeconds,
    CacheStats? CacheStats,
    bool Skipped,
    string SkipReason
)
{
    public static AlgorithmResult FromSkipped(IFibonacciAlgorithm algo, int n, string reason) =>
        new(
            Name: algo.Name,
            Label: algo.Label,
            TimeComplexity: algo.TimeComplexity,
            SpaceComplexity: algo.SpaceComplexity,
            N: n,
            Result: null,
            TimeSeconds: 0,
            CacheStats: null,
            Skipped: true,
            SkipReason: reason
        );
}
