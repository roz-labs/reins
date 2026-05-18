# Reins

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Harness versioning and evaluation for Spring AI applications.

## Why

Prompt changes silently regress LLM applications. Reins helps you catch this:

- Mark a prompt + retrieval + tool config as a versioned unit with `@Harness`
- Run automated evals on every PR via JUnit 5 — embedding similarity, pass/fail
- Persist JSON reports as CI artifacts and diff results across harness versions

Inspired by Stanford IRIS Lab's [Meta-Harness](https://arxiv.org/abs/2603.28052).

## Status

v0.1.0 — early but functional. A production-shaped reference demo lives at
[roz-labs/reins-demo](https://github.com/roz-labs/reins-demo). The API may
evolve based on feedback before v1.0.

## Requirements

- Java 21+
- Spring Boot 3.3+
- Spring AI 1.1+

## Installation

Not yet published to Maven Central. For now, install locally:

```bash
git clone https://github.com/roz-labs/reins.git
cd reins
mvn clean install
```

Then add the dependency to your project as shown in Quick Start below.

## Example project

Reins produces case-level comparisons between harness versions, like this:

```
Case                       v1-baseline        v2-detailed        Delta
out-of-scope-phone         FAIL (0.587)       PASS (1.000)       +0.413
ambiguous-2fa              PASS (0.756)       FAIL (0.539)       -0.217
refund-window              PASS (0.701)       PASS (0.831)       +0.131
...
Pass rate:                 v1=7/10 (70%)      v2=7/10 (70%)
Avg similarity:            v1=0.742           v2=0.770
```

A reference RAG demo using Reins lives at
[roz-labs/reins-demo](https://github.com/roz-labs/reins-demo). It
defines two versions of the same retrieval-augmented chatbot harness — a
minimal baseline prompt and a detailed few-shot-grounded prompt — runs the
same 10-case eval dataset against both, and reports the pass-rate delta.
Runs locally on Ollama; no API keys, no cloud dependency.

## Quick start

### 1. Add the dependency

```xml
<dependency>
    <groupId>io.github.roz-labs</groupId>
    <artifactId>reins-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Define a harness

```java
@Harness(name = "support-agent", version = "v1")
public class SupportHarness implements ReinsHarness {

    private final ChatClient chatClient;

    public SupportHarness(ChatClient.Builder builder) {
        this.chatClient = build(builder);
    }

    @Override
    public ChatClient build(ChatClient.Builder builder) {
        return builder
            .defaultSystem("You are a helpful support agent.")
            .build();
    }

    @Override
    public String ask(String input) {
        return chatClient.prompt().user(input).call().content();
    }
}
```

### 3. Create a dataset

`src/test/resources/evals/support-cases.json`:

```json
[
  {
    "id": "refund",
    "input": "What's your refund policy?",
    "expected": "Refunds within 30 days."
  }
]
```

### 4. Write an eval test

```java
@SpringBootTest
class SupportHarnessEvalTest {

    @Autowired SupportHarness harness;

    @ReinsEval(dataset = "evals/support-cases.json",
               similarityThreshold = 0.75)
    void evaluate(EvalCase testCase, EvalContext ctx) {
        ctx.record(harness.ask(testCase.input()));
    }
}
```

### 5. Run

```bash
mvn test
```

Reports are written to `target/reins-reports/`.

## License

Apache 2.0
