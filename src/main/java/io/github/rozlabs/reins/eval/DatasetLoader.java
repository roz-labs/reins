package io.github.rozlabs.reins.eval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rozlabs.reins.core.EvalCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads eval datasets (JSON arrays of EvalCase) from the classpath.
 */
public final class DatasetLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DatasetLoader() {}

    /**
     * Load and validate a dataset from a classpath location.
     *
     * @param classpathLocation e.g. "datasets/sample.json"
     * @return validated list of cases (never empty)
     * @throws IllegalArgumentException if the file is not found
     * @throws IllegalStateException    if the dataset is empty, has duplicate ids,
     *                                  or has a case with missing id/input
     */
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
