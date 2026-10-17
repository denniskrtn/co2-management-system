package de.co2management.backend.emission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EmissionCalculatorTest {
    @Test
    @DisplayName("T-05-01: 1.200 × 0,380 ergibt 456 kg CO₂e")
    void multipliesQuantityByFactor() {

        EmissionCalculator calculator = new EmissionCalculator();
        BigDecimal quantity = new BigDecimal("1200");
        BigDecimal factorValue = new BigDecimal("0.380");

        BigDecimal result = calculator.calculate(quantity, factorValue);

        assertThat(result).isEqualByComparingTo("456");
    }

    @Test
    @DisplayName("T-05-02: 0,001 × 0,123456 ergibt 0,000123456, ungerundet")
    void keepsFullPrecisionWithoutRounding() {

        EmissionCalculator calculator = new EmissionCalculator();
        BigDecimal quantity = new BigDecimal("0.001");
        BigDecimal factorValue = new BigDecimal("0.123456");

        BigDecimal result = calculator.calculate(quantity, factorValue);

        assertThat(result).isEqualByComparingTo("0.000123456");
    }

}
