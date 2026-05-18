package io.github.rozlabs.reins.core;

import java.time.Instant;

/**
 * Outcome of a single eval case execution. Produced by ReinsEvalExtension and
 * consumed by reporters.
 */
public record EvalResult(
    String testClassName,
    String caseId,
    String input,
    String expected,
    String actual,
    double similarity,
    double threshold,
    boolean passed,
    String failureReason,
    long latencyMs,
    Instant timestamp
) {}
