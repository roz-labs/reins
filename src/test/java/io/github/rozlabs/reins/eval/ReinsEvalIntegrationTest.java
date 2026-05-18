package io.github.rozlabs.reins.eval;

import io.github.rozlabs.reins.core.EvalCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ReinsEvalIntegrationTest.TestConfig.class)
class ReinsEvalIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        SimilarityCalculator similarityCalculator() {
            return new ExactMatchSimilarityCalculator();
        }
    }

    @Test
    void contextLoads(@Autowired SimilarityCalculator calc) {
        assertThat(calc).isNotNull();
    }

    @ReinsEval(dataset = "datasets/sample.json", similarityThreshold = 0.0)
    void evaluatesAllCases(EvalCase testCase, EvalContext ctx) {
        // Echo back the expected — passes threshold 0.0 regardless
        ctx.record(testCase.expected());
        assertThat(ctx.recorded()).isTrue();
    }
}
