package io.github.rozlabs.reins.eval;

import org.springframework.ai.embedding.EmbeddingModel;

/**
 * Computes similarity as the cosine similarity of two embeddings produced
 * by a Spring AI {@link EmbeddingModel}.
 */
public class EmbeddingSimilarityCalculator implements SimilarityCalculator {

    private final EmbeddingModel embeddingModel;

    /** Construct a calculator backed by the given embedding model. */
    public EmbeddingSimilarityCalculator(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public double calculate(String expected, String actual) {
        if (expected == null || actual == null) return 0.0;
        if (expected.equals(actual)) return 1.0;

        float[] e1 = embeddingModel.embed(expected);
        float[] e2 = embeddingModel.embed(actual);
        return cosineSimilarity(e1, e2);
    }

    static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalStateException(
                "Embedding dimensions differ: " + a.length + " vs " + b.length
            );
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, dot / (Math.sqrt(normA) * Math.sqrt(normB))));
    }
}
