package de.co2management.backend.user;

import de.co2management.backend.site.Site;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    protected AppUser() {
    }

    public AppUser(String name, String username, String passwordHash, Role role, Site site) {
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.site = site;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Site getSite() { return site; }

    public void changeRoleAndSite(Role role, Site site) {
        this.role = role;
        this.site = site;
    }
}