package io.github.rozlabs.reins.autoconfigure;

import io.github.rozlabs.reins.core.HarnessRegistry;
import io.github.rozlabs.reins.eval.EmbeddingSimilarityCalculator;
import io.github.rozlabs.reins.eval.ExactMatchSimilarityCalculator;
import io.github.rozlabs.reins.eval.SimilarityCalculator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
     * Default {@link SimilarityCalculator}. Resolves an {@link EmbeddingModel} lazily via
     * {@link ObjectProvider} so the choice happens at bean instantiation — after every
     * auto-configuration has had a chance to register its embedding model — rather than at
     * auto-config registration time, which is what {@code @ConditionalOnBean} would do.
     *
     * <p>Prefers cosine similarity over Spring AI embeddings when a model is available;
     * falls back to exact-match otherwise.
     */
    @Bean
    @ConditionalOnMissingBean(SimilarityCalculator.class)
    public SimilarityCalculator similarityCalculator(ObjectProvider<EmbeddingModel> embeddingModels) {
        EmbeddingModel model = embeddingModels.getIfAvailable();
        return model != null
            ? new EmbeddingSimilarityCalculator(model)
            : new ExactMatchSimilarityCalculator();
    }
}
