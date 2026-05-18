package io.github.rozlabs.reins.eval;

import io.github.rozlabs.reins.core.EvalCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatasetLoaderTest {

    @Test
    void loadsValidDataset() {
        List<EvalCase> cases = DatasetLoader.load("datasets/sample.json");

        assertThat(cases).hasSize(2);

        EvalCase t1 = cases.get(0);
        assertThat(t1.id()).isEqualTo("t1");
        assertThat(t1.input()).isEqualTo("hello");
        assertThat(t1.expected()).isEqualTo("hi");
        assertThat(t1.metadata()).isEmpty();

        EvalCase t2 = cases.get(1);
        assertThat(t2.id()).isEqualTo("t2");
        assertThat(t2.input()).isEqualTo("bye");
        assertThat(t2.expected()).isEqualTo("goodbye");
        assertThat(t2.metadata()).containsEntry("lang", "en");
    }

    @Test
    void missingFileThrowsIllegalArgument() {
        assertThatThrownBy(() -> DatasetLoader.load("datasets/does-not-exist.json"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Dataset not found");
    }

    @Test
    void duplicateIdsThrowsIllegalState() {
        assertThatThrownBy(() -> DatasetLoader.load("datasets/duplicates.json"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Duplicate case id")
            .hasMessageContaining("dup");
    }

    @Test
    void missingIdThrowsIllegalState() {
        assertThatThrownBy(() -> DatasetLoader.load("datasets/missing-id.json"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("missing id");
    }
}
