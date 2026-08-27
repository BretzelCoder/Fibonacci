package com.bretzelcoder.fibonacci.web;

import com.bretzelcoder.fibonacci.model.AlgorithmResult;
import com.bretzelcoder.fibonacci.model.ComputeResponse;
import java.util.List;

/**
 * Presentation model for the Thymeleaf page.
 *
 * <p>Everything the template needs is computed here rather than in the template or in
 * {@link AlgorithmResult}: abbreviating a 1045-digit number and sizing a bar are display
 * concerns, and the JSON API must keep returning the full value.
 */
public record ComparisonView(
        int n,
        List<Row> rows,
        boolean allMatch,
        String bestName,
        double bestTimeSeconds
) {

    /** Results longer than this are abbreviated; F(200) already exceeds it. */
    private static final int MAX_DISPLAYED_DIGITS = 40;

    /** Digits kept at each end when abbreviating. */
    private static final int EDGE_DIGITS = 20;

    /** Floor so that a bar for a sub-millisecond algorithm stays visible next to a slow one. */
    private static final double MIN_BAR_PERCENT = 2d;

    public record Row(AlgorithmResult result, String displayResult, int digits, double barPercent) {

        public String name() {
            return result.name();
        }

        public boolean skipped() {
            return result.skipped();
        }

        public boolean abbreviated() {
            return digits > MAX_DISPLAYED_DIGITS;
        }
    }

    public static ComparisonView from(ComputeResponse response) {
        double slowest = response.results().stream()
                .filter(result -> !result.skipped())
                .mapToDouble(AlgorithmResult::timeSeconds)
                .max()
                .orElse(0d);

        List<Row> rows = response.results().stream()
                .map(result -> toRow(result, slowest))
                .toList();

        return new ComparisonView(
                response.n(),
                rows,
                response.allMatch(),
                response.bestName(),
                response.bestTimeSeconds()
        );
    }

    private static Row toRow(AlgorithmResult result, double slowest) {
        String value = result.result();
        int digits = value == null ? 0 : value.length();
        return new Row(result, abbreviate(value), digits, barPercent(result, slowest));
    }

    /** Bars are linear against the slowest executed algorithm, so a 100% bar is the worst time. */
    private static double barPercent(AlgorithmResult result, double slowest) {
        if (result.skipped() || slowest <= 0d) {
            return 0d;
        }
        return Math.max(MIN_BAR_PERCENT, result.timeSeconds() / slowest * 100d);
    }

    private static String abbreviate(String value) {
        if (value == null || value.length() <= MAX_DISPLAYED_DIGITS) {
            return value;
        }
        return value.substring(0, EDGE_DIGITS)
                + "…"
                + value.substring(value.length() - EDGE_DIGITS);
    }
}
