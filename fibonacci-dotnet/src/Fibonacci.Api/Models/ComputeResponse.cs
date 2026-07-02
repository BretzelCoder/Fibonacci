namespace Fibonacci.Api.Models;

public record ComputeResponse(
    int N,
    IReadOnlyList<AlgorithmResult> Results,
    bool AllMatch,
    string BestName,
    double BestTimeSeconds
);
