package io.github.rozlabs.reins.eval;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Drives a dataset-based evaluation. Replaces {@code @ParameterizedTest} on a test method:
 * each row in the dataset becomes one test invocation, similarity is scored against the
 * expected output, and results are aggregated by {@link ReinsEvalExtension}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ParameterizedTest(name = "{0}")
@ArgumentsSource(ReinsDatasetProvider.class)
@ExtendWith(ReinsEvalExtension.class)
public @interface ReinsEval {
    /** Classpath location of the dataset JSON (e.g. "evals/support.json"). */
    String dataset();

    /** Minimum similarity required to pass. Below this, the test fails. */
    double similarityThreshold() default 0.75;
}
