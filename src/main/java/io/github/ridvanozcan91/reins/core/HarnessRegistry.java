package io.github.ridvanozcan91.reins.core;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Discovers @Harness annotated Spring beans at startup and exposes them by name+version.
 */
public class HarnessRegistry {

    private final Map<String, ReinsHarness> harnesses = new HashMap<>();

    /**
     * Scans the given {@link ApplicationContext} for beans annotated with {@link Harness}
     * and indexes them by (name, version).
     *
     * @throws IllegalStateException if a {@code @Harness} bean does not implement
     *                               {@link ReinsHarness}, or if two beans share the same
     *                               (name, version) pair
     */
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
                annotation = AnnotationUtils.findAnnotation(bean.getClass(), Harness.class);
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

    /** Look up a harness by its (name, version) pair. */
    public Optional<ReinsHarness> get(String name, String version) {
        return Optional.ofNullable(harnesses.get(key(name, version)));
    }

    /** All registered harnesses, in no particular order. */
    public Collection<ReinsHarness> all() {
        return Collections.unmodifiableCollection(harnesses.values());
    }

    /** Number of registered harnesses. */
    public int size() {
        return harnesses.size();
    }

    private static String key(String name, String version) {
        return name + ":" + version;
    }
}
