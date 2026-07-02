using System.Numerics;
using Fibonacci.Api.Constants;

namespace Fibonacci.Api.Algorithms;

/// <summary>
/// Naive recursive Fibonacci — O(2^n) time, O(n) stack space.
/// Educational: demonstrates the exponential cost of recomputing overlapping sub-problems.
/// Capped at MaxNNaive to prevent prohibitive computation times.
/// </summary>
public sealed class NaiveRecursiveAlgorithm : IFibonacciAlgorithm
{
    public string Name => "recursive";
    public string Label => "Naive Recursive";
    public string TimeComplexity => "O(2^n)";
    public string SpaceComplexity => "O(n)";
    public int MaxN => FibonacciConstants.MaxNNaive;
    public bool RequiresExplicitInclusion => true;

    public BigInteger Compute(int n)
    {
        ArgumentOutOfRangeException.ThrowIfNegative(n);
        return Fib(n);
    }

    private static BigInteger Fib(int n) =>
        n <= 1 ? n : Fib(n - 1) + Fib(n - 2);
}
