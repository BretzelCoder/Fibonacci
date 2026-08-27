package com.bretzelcoder.fibonacci.web;

import com.bretzelcoder.fibonacci.config.FibonacciConstants;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Server-rendered surface. A plain form POST re-renders the same template with results,
 * so the page is fully usable with JavaScript disabled.
 *
 * <p>Shares {@code FibonacciService} with {@link FibonacciApiController}, which is what
 * guarantees the page and the JSON API can never report different numbers.
 */
@Controller
public class FibonacciViewController {

    private static final String VIEW = "index";

    private final com.bretzelcoder.fibonacci.service.FibonacciService service;

    public FibonacciViewController(com.bretzelcoder.fibonacci.service.FibonacciService service) {
        this.service = service;
    }

    @ModelAttribute("maxN")
    int maxN() {
        return FibonacciConstants.MAX_N;
    }

    @ModelAttribute("maxNNaive")
    int maxNNaive() {
        return FibonacciConstants.MAX_N_NAIVE;
    }

    @GetMapping("/")
    public String index(@ModelAttribute("form") ComputeForm form) {
        return VIEW;
    }

    @PostMapping("/")
    public String compute(@Valid @ModelAttribute("form") ComputeForm form,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return VIEW;
        }

        model.addAttribute("comparison",
                ComparisonView.from(service.computeAll(form.getN(), form.isIncludeNaive())));
        return VIEW;
    }
}
