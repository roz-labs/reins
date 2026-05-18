package io.github.rozlabs.reins.core;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Spring bean as a versioned LLM harness.
 * Spring will discover and register beans annotated with @Harness via component scanning.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Harness {
    /** Logical name of the harness (e.g. "support-agent"). Used in reports and lookups. */
    String name();

    /** Version identifier (e.g. "v1", "v1.2", "experiment-rag-rerank"). */
    String version();
}
