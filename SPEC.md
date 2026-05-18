# Reins v0.1.0 — Technical Specification

## Project Overview

**Reins** is a Spring AI library that provides harness versioning and evaluation capabilities for LLM applications. A "harness" is the code that wraps an LLM and determines what information to store, retrieve, and present (prompts, retrieval config, tool definitions, model parameters). Reins lets developers define versioned harnesses, run them against eval datasets, and compare results.

Inspired by Stanford IRIS Lab's Meta-Harness paper (arXiv:2603.28052).

**Goal of v0.1.0**: A working, production-grade Spring Boot starter library with annotation-based harness definition, JUnit 5 integration for dataset-driven eval, embedding-based similarity scoring, and JSON + console reporting.

---

## Technology Stack — FIXED VERSIONS, DO NOT CHANGE

- **Java**: 21 (LTS)
- **Spring Boot**: 3.3.5
- **Spring AI**: 1.1.6 (latest stable on 1.1 line as of May 2026)
- **JUnit Jupiter**: 5.11.x (provided by Spring Boot BOM)
- **Jackson**: 2.x (provided by Spring Boot BOM)
- **Build tool**: Maven 3.9+

Use Spring AI BOM for version management. Use Spring Boot starter parent or `spring-boot-dependencies` BOM.

---

## Project Structure

Single Maven module: `reins-spring-boot-starter`

```
reins/
├── pom.xml
├── README.md
├── LICENSE (Apache 2.0)
├── .gitignore
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/github/ridvanozcan91/reins/
│   │   │       ├── core/
│   │   │       │   ├── Harness.java               // annotation
│   │   │       │   ├── ReinsHarness.java          // interface
│   │   │       │   ├── HarnessRegistry.java
│   │   │       │   ├── EvalCase.java              // record
│   │   │       │   └── EvalResult.java            // record
│   │   │       ├── eval/
│   │   │       │   ├── ReinsEval.java             // meta-annotation
│   │   │       │   ├── ReinsDatasetProvider.java  // ArgumentsProvider
│   │   │       │   ├── ReinsEvalExtension.java    // TestWatcher + AfterAllCallback
│   │   │       │   ├── EvalContext.java
│   │   │       │   ├── DatasetLoader.java
│   │   │       │   ├── SimilarityCalculator.java  // interface
│   │   │       │   ├── EmbeddingSimilarityCalculator.java
│   │   │       │   └── ExactMatchSimilarityCalculator.java
│   │   │       ├── report/
│   │   │       │   ├── ConsoleReporter.java
│   │   │       │   └── JsonReporter.java
│   │   │       └── autoconfigure/
│   │   │           └── ReinsAutoConfiguration.java
│   │   └── resources/
│   │       └── META-INF/
│   │           └── spring/
│   │               └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── test/
│       ├── java/
│       │   └── io/github/ridvanozcan91/reins/
│       │       ├── core/
│       │       │   └── HarnessRegistryTest.java
│       │       ├── eval/
│       │       │   ├── DatasetLoaderTest.java
│       │       │   ├── ExactMatchSimilarityCalculatorTest.java
│       │       │   ├── EmbeddingSimilarityCalculatorTest.java
│       │       │   └── ReinsEvalIntegrationTest.java
│       │       └── report/
│       │           └── JsonReporterTest.java
│       └── resources/
│           └── datasets/
│               └── sample.json
```

Replace `ridvanozcan91` with the actual GitHub username before publishing. For initial development, use a placeholder like `mycompany` or your own.

---

## Maven Configuration

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.github.ridvanozcan91</groupId>
    <artifactId>reins-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Reins</name>
    <description>Harness versioning and evaluation for Spring AI applications</description>
    <url>https://github.com/ridvanozcan91/reins</url>

    <licenses>
        <license>
            <name>Apache License 2.0</name>
            <url>https://www.apache.org/licenses/LICENSE-2.0</url>
        </license>
    </licenses>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <spring-boot.version>3.3.5</spring-boot.version>
        <spring-ai.version>1.1.6</spring-ai.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Spring Boot auto-configuration -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>

        <!-- Spring AI core abstractions (EmbeddingModel, ChatClient, etc.) -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-model</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-client-chat</artifactId>
        </dependency>

        <!-- Spring Test (provides SpringExtension) - exposed to users -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <!-- Compile scope on purpose: users of this library use it in tests -->
        </dependency>

        <!-- JUnit Jupiter params for ArgumentsProvider -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
        </dependency>

        <!-- Jackson for JSON dataset + reports -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>

        <!-- TEST dependencies -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <parameters>true</parameters>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Critical note**: `spring-boot-starter-test` is in `compile` scope (no `<scope>test</scope>`) because consumers of this library will use `SpringExtension`, `@SpringBootTest`, AssertJ etc. in their test code, and our library's eval extension needs `SpringExtension` at runtime.

---

## Implementation — Step by Step

### Step 1: Core annotation, interface, and records

**`Harness.java`** (annotation):

```java
package io.github.ridvanozcan91.reins.core;

import org.springframework.stereotype.Component;
import java.lang.annotation.*;

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
```

**`ReinsHarness.java`** (interface):

```java
package io.github.ridvanozcan91.reins.core;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Contract for a Reins-managed harness. Implementations define how the underlying
 * ChatClient is configured for a particular harness version.
 */
public interface ReinsHarness {

    /**
     * Configure and return a ChatClient for this harness.
     * Implementations typically set system prompts, advisors, tools, and model options.
     *
     * @param builder a fresh ChatClient.Builder provided by Spring AI
     * @return the configured ChatClient
     */
    ChatClient build(ChatClient.Builder builder);

    /**
     * Convenience method to send a user message and return the text response.
     * Default implementation calls build() each time; harnesses can cache the ChatClient
     * if desired.
     */
    default String ask(String input) {
        throw new UnsupportedOperationException(
            "Override ask() or call build() and use the ChatClient directly"
        );
    }
}
```

**`EvalCase.java`** (record):

```java
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
```

**`EvalResult.java`** (record):

```java
package io.github.ridvanozcan91.reins.core;

import java.time.Instant;

/**
 * Outcome of a single eval case execution. Produced by ReinsEvalExtension and
 * consumed by reporters.
 */
public record EvalResult(
    String testClassName,
    String caseId,
    String input,
    String expected,
    String actual,
    double similarity,
    double threshold,
    boolean passed,
    String failureReason,
    long latencyMs,
    Instant timestamp
) {}
```

**`HarnessRegistry.java`**:

```java
package io.github.ridvanozcan91.reins.core;

import org.springframework.context.ApplicationContext;
import java.util.*;

/**
 * Discovers @Harness annotated Spring beans at startup and exposes them by name+version.
 */
public class HarnessRegistry {

    private final Map<String, ReinsHarness> harnesses = new HashMap<>();

    public HarnessRegistry(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(Harness.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            if (!(bean instanceof ReinsHarness harness)) {
                throw new IllegalStateException(
                    "Bean '" + entry.getKey() + "' is annotated with @Harness " +
                    "but does not implement ReinsHarness"
                );
            }
            Harness annotation = bean.getClass().getAnnotation(Harness.class);
            if (annotation == null) {
                // Possibly proxied — try interface route
                annotation = org.springframework.core.annotation.AnnotationUtils
                    .findAnnotation(bean.getClass(), Harness.class);
            }
            String key = key(annotation.name(), annotation.version());
            if (harnesses.containsKey(key)) {
                throw new IllegalStateException(
                    "Duplicate harness registration: " + key
                );
            }
            harnesses.put(key, harness);
        }
    }

    public Optional<ReinsHarness> get(String name, String version) {
        return Optional.ofNullable(harnesses.get(key(name, version)));
    }

    public Collection<ReinsHarness> all() {
        return Collections.unmodifiableCollection(harnesses.values());
    }

    public int size() {
        return harnesses.size();
    }

    private static String key(String name, String version) {
        return name + ":" + version;
    }
}
```

**`ReinsAutoConfiguration.java`**:

```java
package io.github.ridvanozcan91.reins.autoconfigure;

import io.github.ridvanozcan91.reins.core.HarnessRegistry;
import io.github.ridvanozcan91.reins.eval.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
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
```

**`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`**:

```
io.github.ridvanozcan91.reins.autoconfigure.ReinsAutoConfiguration
```

### Acceptance criteria for Step 1

- `mvn clean compile` succeeds with no warnings about unsupported APIs.
- `HarnessRegistryTest`:
  - Registers a fake `ReinsHarness` bean annotated with `@Harness(name="test", version="v1")` in a Spring test context.
  - Asserts `registry.get("test", "v1")` returns it.
  - Asserts `registry.size() == 1`.
  - Tests that a duplicate registration throws `IllegalStateException`.

---

### Step 2: Dataset loading

**`DatasetLoader.java`**:

```java
package io.github.ridvanozcan91.reins.eval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ridvanozcan91.reins.core.EvalCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class DatasetLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DatasetLoader() {}

    public static List<EvalCase> load(String classpathLocation) {
        try (InputStream is = resolve(classpathLocation)) {
            if (is == null) {
                throw new IllegalArgumentException(
                    "Dataset not found on classpath: " + classpathLocation
                );
            }
            List<EvalCase> cases = MAPPER.readValue(
                is, new TypeReference<List<EvalCase>>() {}
            );
            validate(cases, classpathLocation);
            return cases;
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to load dataset: " + classpathLocation, e
            );
        }
    }

    private static InputStream resolve(String location) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = DatasetLoader.class.getClassLoader();
        }
        return cl.getResourceAsStream(location);
    }

    private static void validate(List<EvalCase> cases, String location) {
        if (cases.isEmpty()) {
            throw new IllegalStateException("Dataset is empty: " + location);
        }
        Set<String> ids = new HashSet<>();
        for (EvalCase c : cases) {
            if (c.id() == null || c.id().isBlank()) {
                throw new IllegalStateException("EvalCase missing id in: " + location);
            }
            if (c.input() == null || c.input().isBlank()) {
                throw new IllegalStateException(
                    "EvalCase '" + c.id() + "' missing input"
                );
            }
            if (!ids.add(c.id())) {
                throw new IllegalStateException(
                    "Duplicate case id '" + c.id() + "' in: " + location
                );
            }
        }
    }
}
```

### Acceptance criteria for Step 2

- `DatasetLoaderTest` covers:
  - Loading a valid dataset from `src/test/resources/datasets/sample.json` (2 cases, one with metadata, one without). Asserts size and field values, including that missing metadata becomes empty map.
  - Loading a missing file throws `IllegalArgumentException`.
  - Loading a dataset with duplicate IDs throws `IllegalStateException`.
  - Loading a dataset with a case missing `id` throws `IllegalStateException`.

Provide `src/test/resources/datasets/sample.json`:
```json
[
  {"id": "t1", "input": "hello", "expected": "hi"},
  {"id": "t2", "input": "bye", "expected": "goodbye", "metadata": {"lang": "en"}}
]
```

---

### Step 3: Similarity calculators

**`SimilarityCalculator.java`**:

```java
package io.github.ridvanozcan91.reins.eval;

public interface SimilarityCalculator {
    /**
     * @return similarity in [0.0, 1.0] where 1.0 is identical
     */
    double calculate(String expected, String actual);
}
```

**`ExactMatchSimilarityCalculator.java`**:

```java
package io.github.ridvanozcan91.reins.eval;

public class ExactMatchSimilarityCalculator implements SimilarityCalculator {

    @Override
    public double calculate(String expected, String actual) {
        if (expected == null || actual == null) return 0.0;
        return expected.trim().equalsIgnoreCase(actual.trim()) ? 1.0 : 0.0;
    }
}
```

**`EmbeddingSimilarityCalculator.java`**:

```java
package io.github.ridvanozcan91.reins.eval;

import org.springframework.ai.embedding.EmbeddingModel;

public class EmbeddingSimilarityCalculator implements SimilarityCalculator {

    private final EmbeddingModel embeddingModel;

    public EmbeddingSimilarityCalculator(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public double calculate(String expected, String actual) {
        if (expected == null || actual == null) return 0.0;
        if (expected.equals(actual)) return 1.0;

        float[] e1 = embeddingModel.embed(expected);
        float[] e2 = embeddingModel.embed(actual);
        return cosineSimilarity(e1, e2);
    }

    static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalStateException(
                "Embedding dimensions differ: " + a.length + " vs " + b.length
            );
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, dot / (Math.sqrt(normA) * Math.sqrt(normB))));
    }
}
```

Note: `EmbeddingModel.embed(String)` returns `float[]` in Spring AI 1.1.x — this is the verified signature.

### Acceptance criteria for Step 3

- `ExactMatchSimilarityCalculatorTest`:
  - Identical strings return 1.0
  - Case differences are ignored
  - Whitespace at edges is trimmed
  - Null inputs return 0.0
  - Different strings return 0.0

- `EmbeddingSimilarityCalculatorTest`:
  - Uses a mocked `EmbeddingModel` (Mockito or hand-rolled stub returning fixed vectors)
  - Identical strings short-circuit to 1.0 without calling the model
  - Different strings invoke the model and produce expected cosine result
  - Vector dimension mismatch throws `IllegalStateException`
  - Static `cosineSimilarity` test: known vectors produce known results (e.g. orthogonal → 0, identical → 1)

---

### Step 4: Eval annotation and provider

**`ReinsEval.java`** (meta-annotation):

```java
package io.github.ridvanozcan91.reins.eval;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

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
```

**`EvalContext.java`**:

```java
package io.github.ridvanozcan91.reins.eval;

import io.github.ridvanozcan91.reins.core.EvalCase;

public class EvalContext {

    private final EvalCase testCase;
    private final double similarityThreshold;
    private final String testClassName;
    private final SimilarityCalculator similarityCalculator;
    private final long startedAt;

    private String actualOutput;
    private Double similarity;
    private boolean recorded = false;

    public EvalContext(
            EvalCase testCase,
            double similarityThreshold,
            String testClassName,
            SimilarityCalculator similarityCalculator) {
        this.testCase = testCase;
        this.similarityThreshold = similarityThreshold;
        this.testClassName = testClassName;
        this.similarityCalculator = similarityCalculator;
        this.startedAt = System.currentTimeMillis();
    }

    /**
     * Record the actual output from the harness. Computes similarity and throws
     * AssertionError if below threshold (which JUnit converts to test failure).
     */
    public void record(String actual) {
        if (recorded) {
            throw new IllegalStateException(
                "record() called twice for case: " + testCase.id()
            );
        }
        this.actualOutput = actual;
        this.similarity = similarityCalculator.calculate(testCase.expected(), actual);
        this.recorded = true;

        if (similarity < similarityThreshold) {
            throw new AssertionError(String.format(
                "Case '%s': similarity %.3f below threshold %.3f%n" +
                "Expected: %s%nActual:   %s",
                testCase.id(), similarity, similarityThreshold,
                testCase.expected(), actual
            ));
        }
    }

    public EvalCase testCase() { return testCase; }
    public String actualOutput() { return actualOutput; }
    public Double similarity() { return similarity; }
    public double threshold() { return similarityThreshold; }
    public long latencyMs() { return System.currentTimeMillis() - startedAt; }
    public boolean recorded() { return recorded; }
    public String testClassName() { return testClassName; }
}
```

**`ReinsDatasetProvider.java`**:

```java
package io.github.ridvanozcan91.reins.eval;

import io.github.ridvanozcan91.reins.core.EvalCase;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Stream;

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
```

### Acceptance criteria for Step 4

Provider compiles and the meta-annotation properly chains `@ParameterizedTest`, `@ArgumentsSource`, and `@ExtendWith`. Test in Step 6 (integration test) will exercise this end-to-end.

---

### Step 5: Reporters

**`ConsoleReporter.java`**:

```java
package io.github.ridvanozcan91.reins.report;

import io.github.ridvanozcan91.reins.core.EvalResult;
import java.util.List;

public final class ConsoleReporter {

    private ConsoleReporter() {}

    public static void print(String testClass, List<EvalResult> results) {
        int total = results.size();
        long passed = results.stream().filter(EvalResult::passed).count();
        long failed = total - passed;
        double avgSim = results.stream()
            .mapToDouble(EvalResult::similarity).average().orElse(0.0);
        long totalLatency = results.stream()
            .mapToLong(EvalResult::latencyMs).sum();
        double avgLatency = total > 0 ? (double) totalLatency / total : 0;

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("+- Reins Eval Report: ").append(testClass).append('\n');
        sb.append(String.format("|  Cases:           %d (PASS %d  FAIL %d)%n",
            total, passed, failed));
        sb.append(String.format("|  Pass rate:       %.1f%%%n",
            total > 0 ? 100.0 * passed / total : 0.0));
        sb.append(String.format("|  Avg similarity:  %.3f%n", avgSim));
        sb.append(String.format("|  Avg latency:     %.0f ms%n", avgLatency));
        sb.append(String.format("|  Total time:      %d ms%n", totalLatency));

        if (failed > 0) {
            sb.append("|\n|  Failures:\n");
            results.stream()
                .filter(r -> !r.passed())
                .forEach(r -> sb.append(String.format(
                    "|    - %s (similarity %.3f < %.3f)%n",
                    r.caseId(), r.similarity(), r.threshold()
                )));
        }
        sb.append("+-").append('\n');

        System.out.println(sb);
    }
}
```

Using ASCII box characters (not Unicode) for cross-platform terminal compatibility.

**`JsonReporter.java`**:

```java
package io.github.ridvanozcan91.reins.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.ridvanozcan91.reins.core.EvalResult;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class JsonReporter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Path REPORT_DIR = Paths.get("target", "reins-reports");

    private JsonReporter() {}

    public static Path write(String testClass, List<EvalResult> results) {
        try {
            Files.createDirectories(REPORT_DIR);
            String timestamp = DateTimeFormatter
                .ofPattern("yyyyMMdd-HHmmss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
            Path file = REPORT_DIR.resolve(testClass + "-" + timestamp + ".json");

            ReportPayload payload = new ReportPayload(
                testClass,
                Instant.now(),
                results.size(),
                (int) results.stream().filter(EvalResult::passed).count(),
                results.stream().mapToDouble(EvalResult::similarity).average().orElse(0),
                results
            );
            MAPPER.writeValue(file.toFile(), payload);
            System.out.println("   Reins report saved: " + file.toAbsolutePath());
            return file;
        } catch (IOException e) {
            System.err.println("Failed to write Reins report: " + e.getMessage());
            return null;
        }
    }

    public record ReportPayload(
        String testClass,
        Instant generatedAt,
        int totalCases,
        int passedCases,
        double avgSimilarity,
        List<EvalResult> results
    ) {}
}
```

### Acceptance criteria for Step 5

- `JsonReporterTest`:
  - Writes a list of 2-3 fake `EvalResult` objects, asserts the file exists.
  - Reads it back via `ObjectMapper`, asserts fields match.
  - Uses a temporary directory (override `REPORT_DIR` strategy or accept that test writes to `target/reins-reports`).

---

### Step 6: ReinsEvalExtension and end-to-end integration

**`ReinsEvalExtension.java`**:

```java
package io.github.ridvanozcan91.reins.eval;

import io.github.ridvanozcan91.reins.core.EvalResult;
import io.github.ridvanozcan91.reins.report.ConsoleReporter;
import io.github.ridvanozcan91.reins.report.JsonReporter;
import org.junit.jupiter.api.extension.*;

import java.time.Instant;
import java.util.*;

public class ReinsEvalExtension implements TestWatcher, AfterAllCallback {

    @Override
    public void testSuccessful(ExtensionContext context) {
        recordResult(context, true, null);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        recordResult(context, false,
            cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName()
        );
    }

    private void recordResult(ExtensionContext context, boolean passed, String failureReason) {
        String caseId = context.getDisplayName();
        EvalContext ctx = context.getRoot()
            .getStore(ReinsDatasetProvider.NS)
            .get("ctx:" + caseId, EvalContext.class);
        if (ctx == null) return;

        EvalResult result = new EvalResult(
            ctx.testClassName(),
            ctx.testCase().id(),
            ctx.testCase().input(),
            ctx.testCase().expected(),
            ctx.actualOutput(),
            ctx.similarity() != null ? ctx.similarity() : 0.0,
            ctx.threshold(),
            passed,
            failureReason,
            ctx.latencyMs(),
            Instant.now()
        );
        getResults(context).add(result);
    }

    @SuppressWarnings("unchecked")
    private List<EvalResult> getResults(ExtensionContext context) {
        return context.getRoot()
            .getStore(ReinsDatasetProvider.NS)
            .getOrComputeIfAbsent(
                "results",
                k -> new ArrayList<EvalResult>(),
                List.class
            );
    }

    @Override
    public void afterAll(ExtensionContext context) {
        String testClass = context.getRequiredTestClass().getSimpleName();
        List<EvalResult> all = getResults(context);
        List<EvalResult> classResults = all.stream()
            .filter(r -> r.testClassName().equals(testClass))
            .toList();
        if (classResults.isEmpty()) return;

        ConsoleReporter.print(testClass, classResults);
        JsonReporter.write(testClass, classResults);
    }
}
```

### Acceptance criteria for Step 6

`ReinsEvalIntegrationTest` exercises the full flow:

```java
package io.github.ridvanozcan91.reins.eval;

import io.github.ridvanozcan91.reins.core.EvalCase;
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
```

When `mvn test` runs:
- The Spring test context starts with the test-scoped `SimilarityCalculator` bean
- `@ReinsEval` causes two test invocations (`t1` and `t2`), both pass
- After all, a console report prints and a JSON file is written to `target/reins-reports/`

---

## README.md (initial version — to be polished after launch)

```markdown
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

## Quick start

### 1. Add the dependency

```xml
<dependency>
    <groupId>io.github.YOUR_USERNAME</groupId>
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
```

---

## LICENSE

Use the standard Apache 2.0 license text. Claude Code can generate this file from the standard template.

## .gitignore

Standard Java/Maven gitignore:

```
target/
.idea/
*.iml
.vscode/
.DS_Store
*.log
```

---

## Quality Bar

For every implementation step:

1. **Compiles cleanly**: `mvn clean compile` with zero warnings (except suppressible Spring/JUnit reflection ones).
2. **Tests pass**: `mvn test` is green.
3. **No TODOs left in code**: every method has a real implementation.
4. **Javadoc on public APIs**: at minimum one-line description on every public class and method.
5. **Null safety**: defensive checks where it matters (loaders, public APIs).

If you (Claude Code) cannot satisfy any of these for a step, STOP and report the issue rather than producing broken code.

---

## Out of Scope for v0.1.0

These will be addressed in v0.2 or later — do NOT implement now:

- HTML reports
- A/B comparison between harness versions
- CLI runner
- LLM-as-judge scoring
- Automatic harness evolution
- Multi-module Maven structure
- Custom dataset formats (YAML, CSV)
- Web dashboard
- Authentication / multi-tenancy

If you find yourself wanting to add any of these, resist. Ship v0.1.0 first.
