package com.bretzelcoder.fibonacci.service;

import com.bretzelcoder.fibonacci.model.ComputeResponse;

public interface FibonacciService {

    /**
     * Runs every registered algorithm for {@code n} and compares them.
     *
     * @param n            value in {@code [0, MAX_N]}
     * @param includeNaive whether to run algorithms that require explicit opt-in
     * @throws IllegalArgumentException if {@code n} is outside {@code [0, MAX_N]}
     */
    ComputeResponse computeAll(int n, boolean includeNaive);
}
