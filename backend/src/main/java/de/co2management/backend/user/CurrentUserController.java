package de.co2management.backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CurrentUserController {

    private final AppUserService userService;

    public CurrentUserController(AppUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserInfo me(@AuthenticationPrincipal Jwt jwt) {
        return userService.findUserInfo(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}