package com.bretzelcoder.fibonacci.web;

import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Form-backing object for the Thymeleaf page.
 *
 * <p>A mutable JavaBean rather than the {@code ComputeRequest} record: Thymeleaf's
 * {@code th:field} resolves properties through getters and setters, which records do not
 * provide. Keeping the two apart also stops view concerns from leaking into the JSON
 * contract shared with the .NET stack.
 */
public class ComputeForm {

    @Min(value = 0, message = "n must be between 0 and " + FibonacciConstants.MAX_N + ".")
    @Max(value = FibonacciConstants.MAX_N, message = "n must be between 0 and " + FibonacciConstants.MAX_N + ".")
    private int n = 30;

    private boolean includeNaive = true;

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public boolean isIncludeNaive() {
        return includeNaive;
    }

    public void setIncludeNaive(boolean includeNaive) {
        this.includeNaive = includeNaive;
    }
}
