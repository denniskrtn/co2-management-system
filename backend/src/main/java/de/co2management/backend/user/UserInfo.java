package de.co2management.backend.user;

public record UserInfo(String username, String name, Role role, Long siteId, String siteName) {

    public static UserInfo from(AppUser user) {
        return new UserInfo(
                user.getUsername(),
                user.getName(),
                user.getRole(),
                user.getSite() != null ? user.getSite().getId() : null,
                user.getSite() != null ? user.getSite().getName() : null);
    }
}