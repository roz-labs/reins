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

@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
public class ReinsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HarnessRegistry harnessRegistry(ApplicationContext context) {
        return new HarnessRegistry(context);
    }

    @Bean
    @ConditionalOnBean(EmbeddingModel.class)
    @ConditionalOnMissingBean(SimilarityCalculator.class)
    public SimilarityCalculator embeddingSimilarityCalculator(EmbeddingModel embeddingModel) {
        return new EmbeddingSimilarityCalculator(embeddingModel);
    }

    @Bean
    @ConditionalOnMissingBean(SimilarityCalculator.class)
    public SimilarityCalculator exactMatchSimilarityCalculator() {
        return new ExactMatchSimilarityCalculator();
    }
}
