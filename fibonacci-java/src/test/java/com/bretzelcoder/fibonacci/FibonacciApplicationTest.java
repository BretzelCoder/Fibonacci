package com.bretzelcoder.fibonacci;

import static org.assertj.core.api.Assertions.assertThat;

import com.bretzelcoder.fibonacci.algorithm.FibonacciAlgorithm;
import com.bretzelcoder.fibonacci.model.AlgorithmResult;
import com.bretzelcoder.fibonacci.model.ComputeResponse;
import com.bretzelcoder.fibonacci.service.FibonacciService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * The wiring no unit test can check: that Spring finds every algorithm and hands them to the
 * service in the declared order, and that the two surfaces agree end to end.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FibonacciApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private List<FibonacciAlgorithm> algorithms;

    @Autowired
    private FibonacciService service;

    @Test
    @DisplayName("every algorithm is discovered, in the order its @Order declares")
    void algorithmsAreWiredInOrder() {
        assertThat(algorithms)
                .extracting(FibonacciAlgorithm::name)
                .containsExactly("recursive", "memoized", "iterative", "fast-doubling");
    }

    @Test
    @DisplayName("the real stack computes and compares end to end")
    void computesEndToEnd() {
        ComputeResponse response = service.computeAll(30, true);

        assertThat(response.results()).hasSize(4).noneMatch(AlgorithmResult::skipped);
        assertThat(response.results()).extracting(AlgorithmResult::result).containsOnly("832040");
        assertThat(response.allMatch()).isTrue();
    }

    @Test
    @DisplayName("the JSON endpoint answers over HTTP")
    void jsonEndpointAnswers() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/fibonacci/compute",
                java.util.Map.of("n", 30, "includeNaive", true),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"832040\"")
                .contains("\"allMatch\":true")
                .contains("\"fast-doubling\"");
    }

    @Test
    @DisplayName("the page and the JSON API report the same value")
    void bothSurfacesAgree() {
        String json = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/fibonacci/compute",
                java.util.Map.of("n", 40, "includeNaive", false),
                String.class).getBody();

        org.springframework.util.LinkedMultiValueMap<String, String> form =
                new org.springframework.util.LinkedMultiValueMap<>();
        form.add("n", "40");

        String html = restTemplate.postForEntity(
                "http://localhost:" + port + "/", form, String.class).getBody();

        String expected = "102334155";
        assertThat(json).contains("\"" + expected + "\"");
        assertThat(html).contains(expected);
    }
}
