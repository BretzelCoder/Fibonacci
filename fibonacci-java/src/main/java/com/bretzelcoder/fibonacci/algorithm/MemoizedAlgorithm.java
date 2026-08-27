package com.bretzelcoder.fibonacci.algorithm;

import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import com.bretzelcoder.fibonacci.model.CacheStats;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PreDestroy;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Memoized recursion backed by Caffeine — O(n) time, O(n) space.
 * Each distinct n is computed once; every later reference to it is a cache hit.
 *
 * <h2>Why not {@code @Cacheable}?</h2>
 * Spring's cache abstraction is implemented with AOP proxies, and a proxy only
 * intercepts calls that arrive from <em>outside</em> the bean. The recursive call from
 * {@code memoized(n)} to {@code memoized(n - 1)} is a plain {@code this} invocation, so it
 * never crosses the proxy and never consults the cache. Annotating this method
 * {@code @Cacheable} would therefore cache only the outermost call and leave the algorithm
 * at O(2^n) — indistinguishable from {@link NaiveRecursiveAlgorithm}, but far more
 * confusing because the annotation suggests otherwise.
 *
 * <p>Self-injection or {@code AopContext.currentProxy()} would restore the caching, at the
 * cost of a proxy hop per recursive step and a construct that is harder to read than the
 * cache call it hides. Talking to the Caffeine {@link Cache} directly is honest about what
 * happens, mirrors what the .NET stack does with {@code IMemoryCache}, and keeps hit/miss
 * accounting in one place.
 *
 * <h2>Why an executor?</h2>
 * At n = 5000 the first (all-miss) pass recurses 5000 frames deep. Tomcat worker threads do
 * not have the stack for that, and a {@code StackOverflowError} inside a request thread is a
 * miserable failure to diagnose. The recursion therefore runs on a dedicated thread built
 * with a 32 MB stack. The thread is created once, at construction, so thread startup never
 * lands inside the service's timed region. This is the JVM counterpart of the Python
 * stack's {@code sys.setrecursionlimit} call.
 *
 * <p>The single-threaded executor also serializes concurrent measurements, which is what we
 * want: hit/miss statistics are reset per measurement, so two overlapping computations would
 * otherwise report each other's numbers.
 */
@Component
@Order(2)
public class MemoizedAlgorithm implements CacheAwareAlgorithm {

    /** 5000 frames of BigInteger recursion fit comfortably; the default 512 KB–1 MB does not. */
    private static final long STACK_SIZE_BYTES = 32L * 1024 * 1024;

    private final Cache<Integer, BigInteger> cache;
    private final ExecutorService deepStackExecutor;

    /**
     * Caffeine's statistics are cumulative for the life of the cache and cannot be zeroed,
     * but they can be subtracted. Snapshotting at reset and taking the difference at read
     * time yields per-measurement counts. Volatile because the value is written by the
     * request thread and read after work done on the executor thread.
     */
    private volatile com.github.benmanes.caffeine.cache.stats.CacheStats baseline;

    public MemoizedAlgorithm(Cache<Integer, BigInteger> cache) {
        this.cache = cache;
        this.baseline = cache.stats();
        this.deepStackExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(null, runnable, "fib-memoized", STACK_SIZE_BYTES);
            thread.setDaemon(true);
            return thread;
        });
    }

    @PreDestroy
    void shutdown() {
        deepStackExecutor.shutdownNow();
    }

    @Override
    public String name() {
        return "memoized";
    }

    @Override
    public String label() {
        return "Memoized (Caffeine)";
    }

    @Override
    public String timeComplexity() {
        return "O(n)";
    }

    @Override
    public String spaceComplexity() {
        return "O(n)";
    }

    @Override
    public int maxN() {
        return FibonacciConstants.MAX_N;
    }

    @Override
    public boolean requiresExplicitInclusion() {
        return false;
    }

    @Override
    public void resetCache() {
        cache.invalidateAll();
        cache.cleanUp();
        baseline = cache.stats();
    }

    @Override
    public CacheStats cacheStats() {
        var delta = cache.stats().minus(baseline);
        return new CacheStats(delta.hitCount(), delta.missCount());
    }

    @Override
    public BigInteger compute(int n) {
        requireNonNegative(n);

        Future<BigInteger> task = deepStackExecutor.submit(() -> memoized(n));
        try {
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Memoized computation was interrupted.", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Memoized computation failed.", cause);
        }
    }

    /**
     * Deliberately uses {@code getIfPresent} + {@code put} rather than
     * {@code cache.get(key, mappingFunction)}: the mapping function of a Caffeine (and
     * {@code ConcurrentHashMap}) computation must not recursively update the same map, so
     * the one-liner would risk an {@code IllegalStateException} or a livelock. The explicit
     * lookup also keeps the hit/miss accounting obvious, which is the point of the exercise.
     */
    private BigInteger memoized(int n) {
        BigInteger cached = cache.getIfPresent(n);
        if (cached != null) {
            return cached;
        }

        BigInteger result = n <= 1
                ? BigInteger.valueOf(n)
                : memoized(n - 1).add(memoized(n - 2));

        cache.put(n, result);
        return result;
    }
}
