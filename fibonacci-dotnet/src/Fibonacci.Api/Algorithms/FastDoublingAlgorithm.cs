using System.Numerics;
using Fibonacci.Api.Constants;

namespace Fibonacci.Api.Algorithms;

/// <summary>
/// Fast-doubling Fibonacci — O(log n) time, O(log n) space (recursive stack).
/// Mirrors Python's sympy.fibonacci() using Lucas sequence identities:
///   F(2k)   = F(k) * [2·F(k+1) − F(k)]
///   F(2k+1) = F(k)² + F(k+1)²
/// Each recursive call halves n, yielding logarithmic depth.
/// </summary>
public sealed class FastDoublingAlgorithm : IFibonacciAlgorithm
{
    public string Name => "fast-doubling";
    public string Label => "Fast Doubling";
    public string TimeComplexity => "O(log n)";
    public string SpaceComplexity => "O(log n)";
    public int MaxN => FibonacciConstants.MaxN;
    public bool RequiresExplicitInclusion => false;

    public BigInteger Compute(int n)
    {
        ArgumentOutOfRangeException.ThrowIfNegative(n);
        return FastDouble(n).F;
    }

    private static (BigInteger F, BigInteger FNext) FastDouble(int n)
    {
        if (n == 0)
            return (BigInteger.Zero, BigInteger.One);

        var (f, fNext) = FastDouble(n >> 1);
        var c = f * (2 * fNext - f);
        var d = f * f + fNext * fNext;

        return (n & 1) == 0 ? (c, d) : (d, c + d);
    }
}
