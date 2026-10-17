package de.co2management.backend.emission;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entries")
public class EmissionEntryController {

    private final EmissionEntryService service;

    public EmissionEntryController(EmissionEntryService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntryInfo create(@Valid @RequestBody CreateEntryRequest request) {
        return service.create(request);
    }
}