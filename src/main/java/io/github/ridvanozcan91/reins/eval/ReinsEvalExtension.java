package io.github.ridvanozcan91.reins.eval;

import io.github.ridvanozcan91.reins.core.EvalResult;
import io.github.ridvanozcan91.reins.report.ConsoleReporter;
import io.github.ridvanozcan91.reins.report.JsonReporter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates {@link EvalResult}s for each {@link ReinsEval} invocation and emits
 * a console summary + JSON report when the test class finishes.
 *
 * <p>Because {@link ReinsEval} is a method-scoped meta-annotation, JUnit only
 * registers this extension at method scope — which means class-level callbacks
 * like {@code AfterAllCallback} are never invoked for us. Instead, we register
 * a {@link ExtensionContext.Store.CloseableResource} on the <em>class</em>
 * extension context's store; JUnit closes that store when the class finishes,
 * which is where we emit the report.
 */
public class ReinsEvalExtension implements TestWatcher {

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
        EvalContext evalCtx = context.getRoot()
            .getStore(ReinsDatasetProvider.NS)
            .get("ctx:" + caseId, EvalContext.class);
        if (evalCtx == null) return;

        EvalResult result = new EvalResult(
            evalCtx.testClassName(),
            evalCtx.testCase().id(),
            evalCtx.testCase().input(),
            evalCtx.testCase().expected(),
            evalCtx.actualOutput(),
            evalCtx.similarity() != null ? evalCtx.similarity() : 0.0,
            evalCtx.threshold(),
            passed,
            failureReason,
            evalCtx.latencyMs(),
            Instant.now()
        );

        ExtensionContext classCtx = findClassContext(context);
        String testClassName = evalCtx.testClassName();
        ReportEmitter emitter = classCtx.getStore(ReinsDatasetProvider.NS)
            .getOrComputeIfAbsent(
                "reportEmitter",
                k -> new ReportEmitter(testClassName),
                ReportEmitter.class
            );
        emitter.add(result);
    }

    private static ExtensionContext findClassContext(ExtensionContext context) {
        ExtensionContext c = context;
        while (c.getTestMethod().isPresent() && c.getParent().isPresent()) {
            c = c.getParent().get();
        }
        return c;
    }

    /**
     * Buffers results for one test class and emits the report when the
     * class-level store is closed (i.e., when the class finishes).
     */
    static class ReportEmitter implements ExtensionContext.Store.CloseableResource {
        private final String testClassName;
        private final List<EvalResult> results = new ArrayList<>();

        ReportEmitter(String testClassName) {
            this.testClassName = testClassName;
        }

        void add(EvalResult result) {
            results.add(result);
        }

        @Override
        public void close() {
            if (results.isEmpty()) return;
            ConsoleReporter.print(testClassName, results);
            JsonReporter.write(testClassName, results);
        }
    }
}
