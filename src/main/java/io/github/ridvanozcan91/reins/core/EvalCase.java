package io.github.ridvanozcan91.reins.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * A single evaluation case loaded from a dataset.
 */
public record EvalCase(
    String id,
    String input,
    String expected,
    Map<String, Object> metadata
) {
    @JsonCreator
    public EvalCase(
        @JsonProperty("id") String id,
        @JsonProperty("input") String input,
        @JsonProperty("expected") String expected,
        @JsonProperty("metadata") Map<String, Object> metadata
    ) {
        this.id = id;
        this.input = input;
        this.expected = expected;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    /**
     * Used by JUnit @ParameterizedTest as the display name for each invocation.
     */
    @Override
    public String toString() {
        return id;
    }
}
