# ADR 0001: SimilarityCalculator wiring

- **Status:** Accepted
- **Date:** 2026-05-18

## Context

Reins needs a default `SimilarityCalculator` bean. The preference is cosine
similarity over Spring AI embeddings when an `EmbeddingModel` is available, and
exact string match as a fallback otherwise. The bean must also yield to any
user-defined `SimilarityCalculator` (`@ConditionalOnMissingBean`).

The non-obvious constraint: in a Spring Boot app, `EmbeddingModel` is typically
contributed by a third-party auto-configuration shipped with Spring AI (e.g.
`OllamaEmbeddingAutoConfiguration`, `OpenAiEmbeddingAutoConfiguration`).
Reins's auto-config has no a priori knowledge of which provider, if any, is on
the classpath.

## Decision

A single `@Bean` method that resolves the embedding model lazily via
`ObjectProvider<EmbeddingModel>`:

```java
@Bean
@ConditionalOnMissingBean(SimilarityCalculator.class)
public SimilarityCalculator similarityCalculator(ObjectProvider<EmbeddingModel> embeddingModels) {
    EmbeddingModel model = embeddingModels.getIfAvailable();
    return model != null
        ? new EmbeddingSimilarityCalculator(model)
        : new ExactMatchSimilarityCalculator();
}
```

`ObjectProvider.getIfAvailable()` is evaluated at bean instantiation, after
every auto-configuration has had a chance to register its own beans, so the
order in which auto-configs run becomes irrelevant.

## Alternatives considered

### 1. Two beans guarded by `@ConditionalOnBean(EmbeddingModel.class)`

```java
@Bean
@ConditionalOnBean(EmbeddingModel.class)
@ConditionalOnMissingBean(SimilarityCalculator.class)
public SimilarityCalculator embeddingSimilarityCalculator(EmbeddingModel m) { ... }

@Bean
@ConditionalOnMissingBean(SimilarityCalculator.class)
public SimilarityCalculator exactMatchSimilarityCalculator() { ... }
```

**Rejected.** `@ConditionalOnBean` is evaluated during auto-config
*registration*, before Spring AI's embedding auto-configurations have had a
chance to register their `EmbeddingModel`. The condition therefore evaluates
to false in nearly every real application and the embedding branch is silently
pruned — every consumer falls back to exact match without warning. This was
the bug that motivated the present ADR; it's easy to miss because exact match
"works" (returns 0.0 for everything that isn't byte-identical) instead of
failing loudly.

### 2. `@AutoConfigureAfter` enumerating each provider auto-config

```java
@AutoConfiguration(after = {
    OllamaEmbeddingAutoConfiguration.class,
    OpenAiEmbeddingAutoConfiguration.class,
    // …every provider Reins might run behind…
})
```

**Rejected.** Works in principle but couples Reins to the full set of Spring
AI embedding providers. New providers ship regularly; we'd be issuing patch
releases just to add provider classes to the list, and a user picking an
embedding provider we haven't enumerated yet would silently hit the original
bug. `ObjectProvider` avoids the coupling entirely.

## Consequences

- Reins ships one `SimilarityCalculator` bean, not two.
- Provider-agnostic: any current or future Spring AI embedding provider works
  without changes here.
- The condition that picks embedding vs. exact match is a runtime check inside
  the bean method, not a Spring `@Conditional` — so it's not visible in
  `/actuator/conditions` output. If we ever need that visibility, revisit.
- User-defined `SimilarityCalculator` beans still take precedence via
  `@ConditionalOnMissingBean(SimilarityCalculator.class)`.

Test coverage: `ReinsAutoConfigurationTest` covers all three branches
(no embedding model, embedding model present, user override).
