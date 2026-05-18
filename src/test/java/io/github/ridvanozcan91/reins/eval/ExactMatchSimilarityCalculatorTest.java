package io.github.ridvanozcan91.reins.eval;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExactMatchSimilarityCalculatorTest {

    private final ExactMatchSimilarityCalculator calc = new ExactMatchSimilarityCalculator();

    @Test
    void identicalStringsReturnOne() {
        assertThat(calc.calculate("hello", "hello")).isEqualTo(1.0);
    }

    @Test
    void caseDifferencesAreIgnored() {
        assertThat(calc.calculate("Hello", "HELLO")).isEqualTo(1.0);
    }

    @Test
    void edgeWhitespaceIsTrimmed() {
        assertThat(calc.calculate("  hello  ", "hello")).isEqualTo(1.0);
        assertThat(calc.calculate("hello", "\thello\n")).isEqualTo(1.0);
    }

    @Test
    void nullInputsReturnZero() {
        assertThat(calc.calculate(null, "hello")).isEqualTo(0.0);
        assertThat(calc.calculate("hello", null)).isEqualTo(0.0);
        assertThat(calc.calculate(null, null)).isEqualTo(0.0);
    }

    @Test
    void differentStringsReturnZero() {
        assertThat(calc.calculate("hello", "world")).isEqualTo(0.0);
    }
}
