package io.github.ridvanozcan91.reins.autoconfigure;

import io.github.ridvanozcan91.reins.eval.EmbeddingSimilarityCalculator;
import io.github.ridvanozcan91.reins.eval.ExactMatchSimilarityCalculator;
import io.github.ridvanozcan91.reins.eval.SimilarityCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReinsAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ReinsAutoConfiguration.class));

    @Test
    void fallsBackToExactMatchWhenNoEmbeddingModelIsPresent() {
        runner.run(ctx -> assertThat(ctx.getBean(SimilarityCalculator.class))
            .isInstanceOf(ExactMatchSimilarityCalculator.class));
    }

    @Test
    void prefersEmbeddingCalculatorWhenEmbeddingModelIsPresent() {
        runner
            .withUserConfiguration(EmbeddingModelConfig.class)
            .run(ctx -> assertThat(ctx.getBean(SimilarityCalculator.class))
                .isInstanceOf(EmbeddingSimilarityCalculator.class));
    }

    @Test
    void userDefinedCalculatorOverridesAutoConfiguredOne() {
        runner
            .withUserConfiguration(EmbeddingModelConfig.class, UserCalculatorConfig.class)
            .run(ctx -> assertThat(ctx.getBean(SimilarityCalculator.class))
                .isSameAs(UserCalculatorConfig.USER_BEAN));
    }

    @Configuration
    static class EmbeddingModelConfig {
        @Bean
        EmbeddingModel embeddingModel() {
            return mock(EmbeddingModel.class);
        }
    }

    @Configuration
    static class UserCalculatorConfig {
        static final SimilarityCalculator USER_BEAN = (a, b) -> 0.42;

        @Bean
        SimilarityCalculator similarityCalculator() {
            return USER_BEAN;
        }
    }
}
