package de.co2management.backend.report;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DisplayRoundingTest {

    @ParameterizedTest(name = "{0} wird zu {1}")
    @CsvSource({
            "1.005, 1.01",
            "1.004999999, 1.00",
            "0.012, 0.01",
            "0, 0.00"
    })
    void roundsHalfUpToTwoDecimals(String value, String expected) {
        assertThat(DisplayRounding.round(new BigDecimal(value))).isEqualByComparingTo(expected);
    }
}