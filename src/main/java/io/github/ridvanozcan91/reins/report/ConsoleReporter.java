package io.github.ridvanozcan91.reins.report;

import io.github.ridvanozcan91.reins.core.EvalResult;

import java.util.List;

/**
 * Prints a human-readable eval summary to stdout. ASCII-only for cross-platform terminals.
 */
public final class ConsoleReporter {

    private ConsoleReporter() {}

    public static void print(String testClass, List<EvalResult> results) {
        int total = results.size();
        long passed = results.stream().filter(EvalResult::passed).count();
        long failed = total - passed;
        double avgSim = results.stream()
            .mapToDouble(EvalResult::similarity).average().orElse(0.0);
        long totalLatency = results.stream()
            .mapToLong(EvalResult::latencyMs).sum();
        double avgLatency = total > 0 ? (double) totalLatency / total : 0;

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("+- Reins Eval Report: ").append(testClass).append('\n');
        sb.append(String.format("|  Cases:           %d (PASS %d  FAIL %d)%n",
            total, passed, failed));
        sb.append(String.format("|  Pass rate:       %.1f%%%n",
            total > 0 ? 100.0 * passed / total : 0.0));
        sb.append(String.format("|  Avg similarity:  %.3f%n", avgSim));
        sb.append(String.format("|  Avg latency:     %.0f ms%n", avgLatency));
        sb.append(String.format("|  Total time:      %d ms%n", totalLatency));

        if (failed > 0) {
            sb.append("|\n|  Failures:\n");
            results.stream()
                .filter(r -> !r.passed())
                .forEach(r -> sb.append(String.format(
                    "|    - %s (similarity %.3f < %.3f)%n",
                    r.caseId(), r.similarity(), r.threshold()
                )));
        }
        sb.append("+-").append('\n');

        System.out.println(sb);
    }
}
