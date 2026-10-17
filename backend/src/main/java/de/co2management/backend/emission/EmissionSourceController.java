package de.co2management.backend.emission;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emission-sources")
public class EmissionSourceController {

    private final EmissionSourceService service;

    public EmissionSourceController(EmissionSourceService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmissionSourceInfo> findAll() {
        return service.findAllSources();
    }
}