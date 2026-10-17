package de.co2management.backend.site;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public List<SiteInfo> findAll() {
        return siteService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SiteInfo create(@Valid @RequestBody CreateSiteRequest request) {
        return siteService.create(request);
    }

    @PutMapping("/{id}")
    public SiteInfo rename(@PathVariable Long id, @Valid @RequestBody RenameSiteRequest request) {
        return siteService.rename(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        siteService.delete(id);
    }
}