package io.github.rozlabs.reins.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.rozlabs.reins.core.EvalResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonReporterTest {

    private static final ObjectMapper READER = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @Test
    void writesPayloadThatRoundTrips() throws Exception {
        Instant ts = Instant.parse("2026-01-15T10:30:00Z");
        List<EvalResult> results = List.of(
            new EvalResult(
                "DemoEvalTest", "t1", "hello", "hi", "hi",
                1.0, 0.75, true, null, 42L, ts
            ),
            new EvalResult(
                "DemoEvalTest", "t2", "bye", "goodbye", "see you",
                0.55, 0.75, false, "below threshold", 87L, ts
            ),
            new EvalResult(
                "DemoEvalTest", "t3", "thanks", "you're welcome", "yw",
                0.80, 0.75, true, null, 30L, ts
            )
        );

        Path file = JsonReporter.write("DemoEvalTest", results);

        assertThat(file).isNotNull();
        assertThat(Files.exists(file)).isTrue();
        assertThat(file.getFileName().toString())
            .startsWith("DemoEvalTest-")
            .endsWith(".json");

        JsonReporter.ReportPayload payload = READER.readValue(
            file.toFile(), JsonReporter.ReportPayload.class
        );

        assertThat(payload.testClass()).isEqualTo("DemoEvalTest");
        assertThat(payload.totalCases()).isEqualTo(3);
        assertThat(payload.passedCases()).isEqualTo(2);
        assertThat(payload.avgSimilarity())
            .isCloseTo((1.0 + 0.55 + 0.80) / 3.0, org.assertj.core.api.Assertions.offset(1e-9));
        assertThat(payload.generatedAt()).isNotNull();
        assertThat(payload.results()).hasSize(3);

        EvalResult first = payload.results().get(0);
        assertThat(first.caseId()).isEqualTo("t1");
        assertThat(first.input()).isEqualTo("hello");
        assertThat(first.expected()).isEqualTo("hi");
        assertThat(first.actual()).isEqualTo("hi");
        assertThat(first.similarity()).isEqualTo(1.0);
        assertThat(first.threshold()).isEqualTo(0.75);
        assertThat(first.passed()).isTrue();
        assertThat(first.failureReason()).isNull();
        assertThat(first.latencyMs()).isEqualTo(42L);
        assertThat(first.timestamp()).isEqualTo(ts);

        EvalResult failing = payload.results().get(1);
        assertThat(failing.passed()).isFalse();
        assertThat(failing.failureReason()).isEqualTo("below threshold");
    }
}
