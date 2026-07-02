namespace Fibonacci.Api.Models;

public record CacheStats(int Hits, int Misses)
{
    public int Total => Hits + Misses;
    public double HitRatio => Total > 0 ? (double)Hits / Total : 0;
}
