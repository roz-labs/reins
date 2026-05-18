# Reins

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Harness versioning and evaluation for Spring AI applications.

## Why

LLM applications depend not just on the model but on the **harness** around it:
prompts, retrieval config, tools, and memory. Reins lets you version harnesses,
run them against eval datasets, and compare results — natively in Spring AI.

Inspired by Stanford IRIS Lab's [Meta-Harness](https://arxiv.org/abs/2603.28052).

## Status

Early development (v0.1.0). API may change.

## Example project

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
    @Override
    public ChatClient build(ChatClient.Builder builder) {
        return builder
            .defaultSystem("You are a helpful support agent.")
            .build();
    }

    @Override
    public String ask(String input) {
        return build(/* injected builder */).prompt(input).call().content();
    }
}
```

### 3. Create a dataset

`src/test/resources/evals/support-cases.json`:

```json
[
  {"id": "refund", "input": "What's your refund policy?",
   "expected": "Refunds within 30 days."}
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
