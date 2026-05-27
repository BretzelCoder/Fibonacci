using Fibonacci.Api.Models;

namespace Fibonacci.Api.Services;

public interface IFibonacciService
{
    ComputeResponse ComputeAll(int n, bool includeNaive = true);
}
