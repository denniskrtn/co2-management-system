package de.co2management.backend.site;

public record SiteInfo(Long id, String code, String name) {

    public static SiteInfo from(Site site) {
        return new SiteInfo(site.getId(), site.getCode(), site.getName());
    }
}