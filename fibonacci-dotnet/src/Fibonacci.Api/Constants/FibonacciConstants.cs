namespace Fibonacci.Api.Constants;

public static class FibonacciConstants
{
    /// <summary>Exponential cost becomes unacceptable above this value for naive recursion.</summary>
    public const int MaxNNaive = 35;

    /// <summary>Upper bound for all algorithms — keeps response times reasonable.</summary>
    public const int MaxN = 5_000;
}
