package de.co2management.backend.emission;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CreateEntryRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest(name = "{0}: Menge {1}, gültig: {2}")
    @CsvSource({
            "T-05-05, 0, false",
            "T-05-06, -1, false",
            "T-05-07, 0.001, true",
            "T-05-08, 0.0001, false"
    })
    void validatesQuantity(String testCase, String quantity, boolean expectedValid) {
        CreateEntryRequest request = new CreateEntryRequest(
                1L, 1L, new BigDecimal(quantity), LocalDate.of(2026, 3, 15));

        assertThat(validator.validate(request).isEmpty()).isEqualTo(expectedValid);
    }
}