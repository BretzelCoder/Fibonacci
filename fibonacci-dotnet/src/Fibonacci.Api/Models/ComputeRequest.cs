using System.ComponentModel.DataAnnotations;
using Fibonacci.Api.Constants;

namespace Fibonacci.Api.Models;

public record ComputeRequest(
    [Range(0, FibonacciConstants.MaxN, ErrorMessage = "n must be between 0 and 5000.")]
    int N,
    bool IncludeNaive = true
);
