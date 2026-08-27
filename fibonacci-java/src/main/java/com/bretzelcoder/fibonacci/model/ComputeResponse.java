package com.bretzelcoder.fibonacci.model;

import java.util.List;

public record ComputeResponse(
        int n,
        List<AlgorithmResult> results,
        boolean allMatch,
        String bestName,
        double bestTimeSeconds
) {
}
