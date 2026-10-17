package de.co2management.backend.site;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUSTAINABILITY_OFFICER')")
    @Transactional(readOnly = true)
    public List<SiteInfo> findAll() {
        return siteRepository.findAll(Sort.by("code")).stream()
                .map(SiteInfo::from)
                .toList();
    }

    @Transactional
    public SiteInfo create(CreateSiteRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (siteRepository.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Kennung bereits vergeben");
        }
        Site site = siteRepository.save(new Site(code, request.name().trim()));
        return SiteInfo.from(site);
    }

    @Transactional
    public SiteInfo rename(Long id, RenameSiteRequest request) {
        Site site = findSite(id);
        site.rename(request.name().trim());
        return SiteInfo.from(site);
    }

    @Transactional
    public void delete(Long id) {
        Site site = findSite(id);
        try {
            siteRepository.delete(site);
            siteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Standort hat zugeordnete Benutzer oder Einträge");
        }
    }

    private Site findSite(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Standort nicht gefunden"));
    }
}