package de.co2management.backend.report;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class DisplayRounding {

    private DisplayRounding() {
    }

    static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}