package ch.admin.bit.jeap.testorchestrator.metrics;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MetricsViewController {

    public MetricsViewController(TestCaseMetricsService testCaseMetricsService) {
        this.testCaseMetricsService = testCaseMetricsService;
    }

    private static final String VIEW_TEMPLATE_NAME = "metrics";

    private final TestCaseMetricsService testCaseMetricsService;

    @GetMapping("/metrics")
    public String renderMetrics(Model model) {
        List<TestCaseMetricsDto> allTestCases = new ArrayList<>(testCaseMetricsService.getAllTestCaseMetrics());
        model.addAttribute(VIEW_TEMPLATE_NAME, allTestCases);
        return VIEW_TEMPLATE_NAME;
    }

    @RequestMapping("/")
    public String redirectRoot() {
        return "redirect:/metrics";
    }

}
