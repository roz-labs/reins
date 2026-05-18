package io.github.ridvanozcan91.reins.autoconfigure;

import io.github.ridvanozcan91.reins.core.HarnessRegistry;
import io.github.ridvanozcan91.reins.eval.EmbeddingSimilarityCalculator;
import io.github.ridvanozcan91.reins.eval.ExactMatchSimilarityCalculator;
import io.github.ridvanozcan91.reins.eval.SimilarityCalculator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for Reins. Activates whenever Spring AI's
 * {@code ChatClient} is on the classpath and wires up a {@link HarnessRegistry}
 * plus a default {@link SimilarityCalculator}.
 */
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
public class ReinsAutoConfiguration {

    /** Scans the context for {@code @Harness} beans and exposes them via {@link HarnessRegistry}. */
    @Bean
    @ConditionalOnMissingBean
    public HarnessRegistry harnessRegistry(ApplicationContext context) {
        return new HarnessRegistry(context);
    }

    /**
     * Preferred similarity calculator: cosine similarity of Spring AI embeddings.
     * Active when an {@code EmbeddingModel} bean is available and no other
     * {@link SimilarityCalculator} has been defined.
     */
    @Bean
    @ConditionalOnBean(EmbeddingModel.class)
    @ConditionalOnMissingBean(SimilarityCalculator.class)
    public SimilarityCalculator embeddingSimilarityCalculator(EmbeddingModel embeddingModel) {
        return new EmbeddingSimilarityCalculator(embeddingModel);
    }

    /** Fallback similarity calculator used when no embedding model is available. */
    @Bean
    @ConditionalOnMissingBean(SimilarityCalculator.class)
    public SimilarityCalculator exactMatchSimilarityCalculator() {
        return new ExactMatchSimilarityCalculator();
    }
}
