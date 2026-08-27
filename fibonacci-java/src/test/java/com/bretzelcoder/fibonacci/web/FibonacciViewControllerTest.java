package com.bretzelcoder.fibonacci.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.bretzelcoder.fibonacci.model.AlgorithmResult;
import com.bretzelcoder.fibonacci.model.CacheStats;
import com.bretzelcoder.fibonacci.model.ComputeResponse;
import com.bretzelcoder.fibonacci.service.FibonacciService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The server-rendered surface. Every assertion goes through a real Thymeleaf render, so a
 * broken expression in the template fails here rather than in a browser.
 */
@WebMvcTest(FibonacciViewController.class)
class FibonacciViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FibonacciService service;

    private static ComputeResponse response(List<AlgorithmResult> results, boolean allMatch) {
        return new ComputeResponse(results.get(0).n(), results, allMatch, "Iterative", 0.000003);
    }

    private static AlgorithmResult executed(String name, String label, int n, String value, double seconds) {
        return new AlgorithmResult(name, label, "O(n)", "O(1)", n, value, seconds, null, false, null);
    }

    @Test
    @DisplayName("the first visit shows an empty form and no results")
    void showsTheFormOnFirstVisit() throws Exception {
        String html = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("form"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("id=\"n\"").contains("id=\"includeNaive\"");
        assertThat(html).doesNotContain("class=\"results\"");
        verify(service, never()).computeAll(anyInt(), anyBoolean());
    }

    @Test
    @DisplayName("submitting renders one row per algorithm with its numbers")
    void rendersResults() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(response(List.of(
                new AlgorithmResult("recursive", "Naive Recursive", "O(2^n)", "O(n)",
                        30, "832040", 0.041, null, false, null),
                new AlgorithmResult("memoized", "Memoized (Caffeine)", "O(n)", "O(n)",
                        30, "832040", 0.001, new CacheStats(28, 31), false, null),
                executed("iterative", "Iterative", 30, "832040", 0.000003)
        ), true));

        String html = mockMvc.perform(post("/").param("n", "30").param("includeNaive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("comparison"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains("Naive Recursive")
                .contains("Memoized (Caffeine)")
                .contains("Iterative")
                .contains("O(2^n)")
                .contains("832040")
                .contains("hits 28 / misses 31");
    }

    @Test
    @DisplayName("the winner and the integrity check are displayed")
    void rendersWinnerAndIntegrityCheck() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(response(
                List.of(executed("iterative", "Iterative", 30, "832040", 0.000003)), true));

        String html = mockMvc.perform(post("/").param("n", "30"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains("Iterative")
                .contains("All executed implementations returned the same value.");
    }

    @Test
    @DisplayName("a disagreement between implementations is called out")
    void rendersDisagreement() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(response(List.of(
                executed("iterative", "Iterative", 30, "832040", 0.000003),
                executed("fast-doubling", "Fast Doubling", 30, "832041", 0.000004)
        ), false));

        String html = mockMvc.perform(post("/").param("n", "30"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("Implementations disagree");
    }

    @Test
    @DisplayName("a skipped algorithm shows its reason instead of a result")
    void rendersSkipReason() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(response(List.of(
                new AlgorithmResult("recursive", "Naive Recursive", "O(2^n)", "O(n)",
                        50, null, 0d, null, true, "n > 35 — O(2^n) cost."),
                executed("iterative", "Iterative", 50, "12586269025", 0.000004)
        ), true));

        String html = mockMvc.perform(post("/").param("n", "50"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("n &gt; 35").contains("O(2^n) cost.");
    }

    @Test
    @DisplayName("a very long result is abbreviated with its digit count")
    void abbreviatesLongResults() throws Exception {
        String huge = java.math.BigInteger.ONE.shiftLeft(4_000).toString();
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(response(
                List.of(executed("iterative", "Iterative", 5_000, huge, 0.001)), true));

        String html = mockMvc.perform(post("/").param("n", "5000"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains(huge.substring(0, 20))
                .contains(huge.substring(huge.length() - 20))
                .contains(huge.length() + " digits")
                .doesNotContain(huge);
    }

    @Test
    @DisplayName("a negative n is reported inline and nothing is computed")
    void rejectsNegativeNInline() throws Exception {
        String html = mockMvc.perform(post("/").param("n", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("form", "n"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("n must be between 0 and 5000.");
        assertThat(html).doesNotContain("class=\"results\"");
        verify(service, never()).computeAll(anyInt(), anyBoolean());
    }

    @Test
    @DisplayName("an n above the bound is reported inline and nothing is computed")
    void rejectsTooLargeNInline() throws Exception {
        mockMvc.perform(post("/").param("n", "5001"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "n"));

        verify(service, never()).computeAll(anyInt(), anyBoolean());
    }

    @Test
    @DisplayName("an unchecked box means the naive recursion is excluded")
    void unsubmittedCheckboxMeansFalse() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(response(
                List.of(executed("iterative", "Iterative", 30, "832040", 0.000003)), true));

        // A browser sends nothing for an unchecked checkbox; th:field emits a hidden
        // marker field so Spring can tell "unchecked" from "not part of the form".
        mockMvc.perform(post("/").param("n", "30").param("_includeNaive", "on"));

        verify(service).computeAll(30, false);
    }

    @Test
    @DisplayName("the page carries no script tag")
    void servesNoJavaScript() throws Exception {
        given(service.computeAll(anyInt(), anyBoolean())).willReturn(response(
                List.of(executed("iterative", "Iterative", 30, "832040", 0.000003)), true));

        String html = mockMvc.perform(post("/").param("n", "30"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .describedAs("the comparison must work with JavaScript disabled")
                .doesNotContain("<script");
    }
}
