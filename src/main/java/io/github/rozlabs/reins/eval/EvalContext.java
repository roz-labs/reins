package io.github.rozlabs.reins.eval;

import io.github.rozlabs.reins.core.EvalCase;

/**
 * Per-invocation state for a single eval case: the case itself, the similarity calculator,
 * the recorded actual output, and the resulting similarity score. Passed to the user's
 * test method so they can call {@link #record(String)} with the harness output.
 */
public class EvalContext {

    private final EvalCase testCase;
    private final double similarityThreshold;
    private final String testClassName;
    private final SimilarityCalculator similarityCalculator;
    private final long startedAt;

    private String actualOutput;
    private Double similarity;
    private boolean recorded = false;

    /**
     * Construct a context for one eval case. Captures the start time for latency reporting.
     */
    public EvalContext(
            EvalCase testCase,
            double similarityThreshold,
            String testClassName,
            SimilarityCalculator similarityCalculator) {
        this.testCase = testCase;
        this.similarityThreshold = similarityThreshold;
        this.testClassName = testClassName;
        this.similarityCalculator = similarityCalculator;
        this.startedAt = System.currentTimeMillis();
    }

    /**
     * Record the actual output from the harness. Computes similarity and throws
     * AssertionError if below threshold (which JUnit converts to test failure).
     */
    public void record(String actual) {
        if (recorded) {
            throw new IllegalStateException(
                "record() called twice for case: " + testCase.id()
            );
        }
        this.actualOutput = actual;
        this.similarity = similarityCalculator.calculate(testCase.expected(), actual);
        this.recorded = true;

        if (similarity < similarityThreshold) {
            throw new AssertionError(String.format(
                "Case '%s': similarity %.3f below threshold %.3f%n" +
                "Expected: %s%nActual:   %s",
                testCase.id(), similarity, similarityThreshold,
                testCase.expected(), actual
            ));
        }
    }

    /** The eval case being executed. */
    public EvalCase testCase() { return testCase; }

    /** The actual output recorded via {@link #record(String)}, or {@code null} if not yet recorded. */
    public String actualOutput() { return actualOutput; }

    /** Similarity score in [0, 1], or {@code null} if {@link #record(String)} has not been called. */
    public Double similarity() { return similarity; }

    /** Minimum similarity required for the case to pass. */
    public double threshold() { return similarityThreshold; }

    /** Elapsed wall-clock time since this context was created, in milliseconds. */
    public long latencyMs() { return System.currentTimeMillis() - startedAt; }

    /** Whether {@link #record(String)} has been called for this case. */
    public boolean recorded() { return recorded; }

    /** Simple name of the test class that owns this case, used to scope reports. */
    public String testClassName() { return testClassName; }
}
