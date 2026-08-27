package com.bretzelcoder.fibonacci.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hit/miss counters for one measurement.
 *
 * <p>{@code total} and {@code hitRatio} are derived, and Jackson serializes only a record's
 * components unless an accessor is explicitly annotated — so {@link JsonProperty} is what
 * keeps them in the payload. The .NET record exposes the same two computed properties, and
 * the shared TypeScript type declares all four fields as required.
 */
public record CacheStats(long hits, long misses) {

    @JsonProperty
    public long total() {
        return hits + misses;
    }

    @JsonProperty
    public double hitRatio() {
        long total = total();
        return total > 0 ? (double) hits / total : 0d;
    }
}
