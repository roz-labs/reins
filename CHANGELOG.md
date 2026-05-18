# Changelog

All notable changes to this project are documented in this file. Format loosely
follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Fixed

- `ReinsAutoConfiguration` no longer races with Spring AI's embedding auto-configurations.
  The old design split `SimilarityCalculator` into two beans guarded by
  `@ConditionalOnBean(EmbeddingModel.class)`; that condition is evaluated at auto-config
  registration time, before providers like `OllamaEmbeddingAutoConfiguration` register
  their `EmbeddingModel` bean, so the embedding branch was always pruned and every project
  silently fell back to `ExactMatchSimilarityCalculator`. The bean is now a single method
  that resolves the embedding model lazily via `ObjectProvider<EmbeddingModel>` at
  instantiation time, after every auto-config has had a chance to contribute.

## [0.1.0] - 2026

Initial release.
