package de.co2management.backend.emission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EmissionCalculatorTest {
    @Test
    @DisplayName("T-05-01: 1.200 x 0,380 ergibt 456 kg  CO₂e")
    void multipliesQuantityByFactor() {

        EmissionCalculator calculator = new EmissionCalculator();
        BigDecimal quantity = new BigDecimal("1.200");
        BigDecimal factorValue = new BigDecimal("0.380");

        BigDecimal result = calculator.calculate(quantity, factorValue);

        assertThat(result).isEqualByComparingTo("456");
    }

}
