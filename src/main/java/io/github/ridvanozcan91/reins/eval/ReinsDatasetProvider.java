package io.github.ridvanozcan91.reins.eval;

import io.github.ridvanozcan91.reins.core.EvalCase;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Stream;

/**
 * JUnit Jupiter {@link ArgumentsProvider} that loads cases from the dataset declared on
 * {@link ReinsEval}, builds an {@link EvalContext} for each, and exposes them to the
 * parameterized test method as {@code (EvalCase, EvalContext)} arguments.
 */
public class ReinsDatasetProvider
        implements ArgumentsProvider, AnnotationConsumer<ReinsEval> {

    static final ExtensionContext.Namespace NS =
        ExtensionContext.Namespace.create("reins");

    private ReinsEval config;

    @Override
    public void accept(ReinsEval annotation) {
        this.config = annotation;
    }

    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
        List<EvalCase> cases = DatasetLoader.load(config.dataset());
        String testClass = context.getRequiredTestClass().getSimpleName();

        SimilarityCalculator calculator = resolveCalculator(context);

        return cases.stream().map(c -> {
            EvalContext ctx = new EvalContext(
                c, config.similarityThreshold(), testClass, calculator
            );
            context.getRoot().getStore(NS).put("ctx:" + c.id(), ctx);
            return Arguments.of(c, ctx);
        });
    }

    private SimilarityCalculator resolveCalculator(ExtensionContext context) {
        try {
            return SpringExtension.getApplicationContext(context)
                .getBean(SimilarityCalculator.class);
        } catch (IllegalStateException e) {
            throw new IllegalStateException(
                "Reins requires a Spring test context. " +
                "Add @SpringBootTest (or another Spring test annotation) to your test class.",
                e
            );
        }
    }
}
