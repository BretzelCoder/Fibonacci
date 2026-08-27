package com.bretzelcoder.fibonacci.web;

import com.bretzelcoder.fibonacci.model.ComputeRequest;
import com.bretzelcoder.fibonacci.model.ComputeResponse;
import com.bretzelcoder.fibonacci.service.FibonacciService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JSON surface. The request and response shapes are deliberately identical to the .NET
 * endpoint's, down to field casing, so {@code fibonacci-dotnet/frontend} can be pointed
 * at this API by changing nothing but the Vite proxy target.
 */
@RestController
@RequestMapping(path = "/api/fibonacci", produces = MediaType.APPLICATION_JSON_VALUE)
public class FibonacciApiController {

    private final FibonacciService service;

    public FibonacciApiController(FibonacciService service) {
        this.service = service;
    }

    @PostMapping(path = "/compute", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ComputeResponse compute(@Valid @RequestBody ComputeRequest request) {
        return service.computeAll(request.n(), request.includeNaive());
    }
}
