package de.co2management.backend.user;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserService {

    private final AppUserRepository userRepository;

    public AppUserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<UserInfo> findUserInfo(String username) {
        return userRepository.findByUsername(username).map(UserInfo::from);
    }
}