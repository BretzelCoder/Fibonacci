package com.bretzelcoder.fibonacci.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bretzelcoder.fibonacci.model.AlgorithmResult;
import com.bretzelcoder.fibonacci.model.CacheStats;
import com.bretzelcoder.fibonacci.model.ComputeResponse;
import com.bretzelcoder.fibonacci.service.FibonacciService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Guards the JSON contract shared with the .NET stack.
 *
 * <p>The field names and casing asserted here are the ones declared in
 * {@code fibonacci-dotnet/frontend/src/types/fibonacci.ts}. A rename on this side must fail
 * a test rather than only the Vue frontend, at runtime, in a browser.
 */
@WebMvcTest(FibonacciApiController.class)
class FibonacciApiControllerTest {

    private static final String ENDPOINT = "/api/fibonacci/compute";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FibonacciService service;

    private static ComputeResponse sampleResponse() {
        AlgorithmResult recursive = new AlgorithmResult(
                "recursive", "Naive Recursive", "O(2^n)", "O(n)",
                30, "832040", 0.0416894, null, false, null);

        AlgorithmResult memoized = new AlgorithmResult(
                "memoized", "Memoized (Caffeine)", "O(n)", "O(n)",
                30, "832040", 0.001054, new CacheStats(28, 31), false, null);

        AlgorithmResult skipped = AlgorithmResult.skipped(
                new com.bretzelcoder.fibonacci.algorithm.IterativeAlgorithm(), 30, "Excluded by user.");

        return new ComputeResponse(30, List.of(recursive, memoized, skipped),
                true, "Memoized (Caffeine)", 0.001054);
    }

    @Test
    @DisplayName("a valid request returns the full contract")
    void returnsTheContract() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(sampleResponse());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":30,\"includeNaive\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.n").value(30))
                .andExpect(jsonPath("$.allMatch").value(true))
                .andExpect(jsonPath("$.bestName").value("Memoized (Caffeine)"))
                .andExpect(jsonPath("$.bestTimeSeconds").value(0.001054))
                .andExpect(jsonPath("$.results.length()").value(3))
                .andExpect(jsonPath("$.results[0].name").value("recursive"))
                .andExpect(jsonPath("$.results[0].label").value("Naive Recursive"))
                .andExpect(jsonPath("$.results[0].timeComplexity").value("O(2^n)"))
                .andExpect(jsonPath("$.results[0].spaceComplexity").value("O(n)"))
                .andExpect(jsonPath("$.results[0].cacheStats").value(nullValue()))
                .andExpect(jsonPath("$.results[1].cacheStats.hits").value(28))
                .andExpect(jsonPath("$.results[1].cacheStats.misses").value(31))
                .andExpect(jsonPath("$.results[1].cacheStats.total").value(59))
                .andExpect(jsonPath("$.results[1].cacheStats.hitRatio").value(28d / 59d))
                .andExpect(jsonPath("$.results[2].skipped").value(true))
                .andExpect(jsonPath("$.results[2].skipReason").value("Excluded by user."));
    }

    @Test
    @DisplayName("the response carries exactly the fields the shared TypeScript type declares")
    void exposesExactlyTheDeclaredFields() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(sampleResponse());

        String body = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":30}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(body);
        assertThat(root.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("n", "results", "allMatch", "bestName", "bestTimeSeconds");

        assertThat(root.path("results").get(0).fieldNames()).toIterable()
                .containsExactlyInAnyOrder("name", "label", "timeComplexity", "spaceComplexity",
                        "n", "result", "timeSeconds", "cacheStats", "skipped", "skipReason");

        assertThat(root.path("results").get(1).path("cacheStats").fieldNames()).toIterable()
                .containsExactlyInAnyOrder("hits", "misses", "total", "hitRatio");
    }

    @Test
    @DisplayName("result is serialized as a string, not a number")
    void serializesResultAsString() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(sampleResponse());

        String body = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":30}"))
                .andReturn().getResponse().getContentAsString();

        JsonNode result = objectMapper.readTree(body).path("results").get(0).path("result");
        assertThat(result.isTextual())
                .describedAs("JavaScript loses precision above 2^53, so result must stay a string")
                .isTrue();
    }

    @Test
    @DisplayName("includeNaive defaults to true when the property is absent")
    void includeNaiveDefaultsToTrue() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(sampleResponse());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":20}"))
                .andExpect(status().isOk());

        verify(service).computeAll(eq(20), eq(true));
    }

    @Test
    @DisplayName("an explicit false is honoured")
    void honoursExplicitFalse() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(sampleResponse());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":20,\"includeNaive\":false}"))
                .andExpect(status().isOk());

        verify(service).computeAll(eq(20), eq(false));
    }

    @Test
    @DisplayName("a negative n is rejected with a readable message")
    void rejectsNegativeN() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("n must be between 0 and 5000."));
    }

    @Test
    @DisplayName("an n above the bound is rejected with a readable message")
    void rejectsTooLargeN() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":5001}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("n must be between 0 and 5000."));
    }

    @Test
    @DisplayName("a service-level rejection is also reported as a 400 with a message")
    void reportsServiceRejections() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean()))
                .willThrow(new IllegalArgumentException("n must be between 0 and 5000, was 9000."));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":30}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("n must be between 0 and 5000, was 9000."));
    }

    @Test
    @DisplayName("an unexpected failure still returns a JSON message")
    void reportsUnexpectedFailuresAsJson() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean()))
                .willThrow(new IllegalStateException("boom"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":30}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }
}
