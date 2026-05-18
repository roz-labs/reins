package io.github.rozlabs.reins.eval;

/**
 * Scores the similarity between an expected and actual string.
 */
public interface SimilarityCalculator {
    /**
     * @return similarity in [0.0, 1.0] where 1.0 is identical
     */
    double calculate(String expected, String actual);
}
