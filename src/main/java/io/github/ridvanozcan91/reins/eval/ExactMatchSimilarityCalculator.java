package io.github.ridvanozcan91.reins.eval;

/**
 * Trivial similarity calculator: 1.0 if the strings match (trimmed, case-insensitive),
 * 0.0 otherwise. Useful as the default fallback when no EmbeddingModel is on the classpath.
 */
public class ExactMatchSimilarityCalculator implements SimilarityCalculator {

    @Override
    public double calculate(String expected, String actual) {
        if (expected == null || actual == null) return 0.0;
        return expected.trim().equalsIgnoreCase(actual.trim()) ? 1.0 : 0.0;
    }
}
