package io.github.ridvanozcan91.reins.eval;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmbeddingSimilarityCalculatorTest {

    @Test
    void identicalStringsShortCircuitToOneWithoutCallingModel() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        EmbeddingSimilarityCalculator calc = new EmbeddingSimilarityCalculator(model);

        double similarity = calc.calculate("same", "same");

        assertThat(similarity).isEqualTo(1.0);
        verifyNoInteractions(model);
    }

    @Test
    void nullInputsReturnZeroWithoutCallingModel() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        EmbeddingSimilarityCalculator calc = new EmbeddingSimilarityCalculator(model);

        assertThat(calc.calculate(null, "actual")).isEqualTo(0.0);
        assertThat(calc.calculate("expected", null)).isEqualTo(0.0);
        verifyNoInteractions(model);
    }

    @Test
    void differentStringsInvokeModelAndProduceExpectedCosine() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed("a")).thenReturn(new float[] {1.0f, 0.0f});
        when(model.embed("b")).thenReturn(new float[] {1.0f, 1.0f});
        EmbeddingSimilarityCalculator calc = new EmbeddingSimilarityCalculator(model);

        double similarity = calc.calculate("a", "b");

        // cosine([1,0], [1,1]) = 1 / sqrt(2) ≈ 0.7071
        assertThat(similarity).isCloseTo(1.0 / Math.sqrt(2.0), offset(1e-9));
        verify(model).embed("a");
        verify(model).embed("b");
    }

    @Test
    void dimensionMismatchThrowsIllegalState() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed("a")).thenReturn(new float[] {1.0f, 2.0f});
        when(model.embed("b")).thenReturn(new float[] {1.0f, 2.0f, 3.0f});
        EmbeddingSimilarityCalculator calc = new EmbeddingSimilarityCalculator(model);

        assertThatThrownBy(() -> calc.calculate("a", "b"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Embedding dimensions differ")
            .hasMessageContaining("2")
            .hasMessageContaining("3");
    }

    @Test
    void cosineSimilarityIdenticalVectorsReturnsOne() {
        float[] v = {1.0f, 2.0f, 3.0f};
        assertThat(EmbeddingSimilarityCalculator.cosineSimilarity(v, v))
            .isCloseTo(1.0, offset(1e-9));
    }

    @Test
    void cosineSimilarityOrthogonalVectorsReturnsZero() {
        float[] a = {1.0f, 0.0f, 0.0f};
        float[] b = {0.0f, 1.0f, 0.0f};
        assertThat(EmbeddingSimilarityCalculator.cosineSimilarity(a, b)).isEqualTo(0.0);
    }

    @Test
    void cosineSimilarityZeroVectorReturnsZero() {
        float[] zero = {0.0f, 0.0f};
        float[] nonZero = {1.0f, 1.0f};
        assertThat(EmbeddingSimilarityCalculator.cosineSimilarity(zero, nonZero)).isEqualTo(0.0);
        assertThat(EmbeddingSimilarityCalculator.cosineSimilarity(nonZero, zero)).isEqualTo(0.0);
    }
}
