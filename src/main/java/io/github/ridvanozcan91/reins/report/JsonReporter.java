package io.github.ridvanozcan91.reins.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.ridvanozcan91.reins.core.EvalResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes a JSON eval report to {@code target/reins-reports/<testClass>-<timestamp>.json}.
 */
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
