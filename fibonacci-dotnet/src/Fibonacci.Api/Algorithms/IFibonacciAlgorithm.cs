using System.Numerics;

namespace Fibonacci.Api.Algorithms;

public interface IFibonacciAlgorithm
{
    string Name { get; }
    string Label { get; }
    string TimeComplexity { get; }
    string SpaceComplexity { get; }
    int MaxN { get; }

    BigInteger Compute(int n);
}
