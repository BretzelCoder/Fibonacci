package com.bretzelcoder.fibonacci.model;

import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * JSON request body for the compute endpoint.
 *
 * <p>{@code includeNaive} is a boxed {@code Boolean} so an absent JSON property can be
 * told apart from an explicit {@code false}: Jackson binds a missing property to
 * {@code false} on a primitive, whereas the .NET contract defaults it to {@code true}.
 * The compact constructor performs that normalization, so the accessor never returns null.
 */
public record ComputeRequest(

        @Min(value = 0, message = "n must be between 0 and " + FibonacciConstants.MAX_N + ".")
        @Max(value = FibonacciConstants.MAX_N, message = "n must be between 0 and " + FibonacciConstants.MAX_N + ".")
        int n,

        Boolean includeNaive
) {
    public ComputeRequest {
        if (includeNaive == null) {
            includeNaive = Boolean.TRUE;
        }
    }
}
