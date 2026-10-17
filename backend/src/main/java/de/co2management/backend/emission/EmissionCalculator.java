package de.co2management.backend.emission;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class EmissionCalculator {

    public BigDecimal calculate (BigDecimal quantity, BigDecimal factorValue){
        return quantity.multiply(factorValue);
    }
}
