package de.co2management.backend.emission;

public record EmissionSourceInfo(Long id, String name, String unit, Scope scope) {

    public static EmissionSourceInfo from(EmissionSource source) {
        return new EmissionSourceInfo(source.getId(), source.getName(),
                source.getUnit(), source.getScope());
    }
}