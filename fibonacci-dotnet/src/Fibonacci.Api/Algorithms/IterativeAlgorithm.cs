using System.Numerics;
using Fibonacci.Api.Constants;

namespace Fibonacci.Api.Algorithms;

/// <summary>
/// Iterative Fibonacci — O(n) time, O(1) space.
/// Optimal for moderate n: no stack overhead, no cache, minimal memory footprint.
/// </summary>
public sealed class IterativeAlgorithm : IFibonacciAlgorithm
{
    public string Name => "iterative";
    public string Label => "Iterative";
    public string TimeComplexity => "O(n)";
    public string SpaceComplexity => "O(1)";
    public int MaxN => FibonacciConstants.MaxN;
    public bool RequiresExplicitInclusion => false;

    public BigInteger Compute(int n)
    {
        ArgumentOutOfRangeException.ThrowIfNegative(n);

        BigInteger a = BigInteger.Zero, b = BigInteger.One;
        for (var i = 0; i < n; i++)
            (a, b) = (b, a + b);

        return a;
    }
}
