package app.user.service;


import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    public User register(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());

        if (userOptional.isPresent()) {
            throw new RuntimeException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));

        log.info("Successfully created new user account for username [%s] and id [%s]".formatted(user.getUsername(), user.getId()));
        return user;
    }


    private User initializeUser(RegisterRequest registerRequest) {
        return User
                .builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }


    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User with id[%s] does not exist.".formatted(id)));
    }



       public void changeUserRole(UUID userId, UserRole newRole) {
        User user = getById(userId);
        user.setRole(newRole);
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
    }


    public long getTotalUsers() {
        return getAllUsers().size();
    }

    public long getUserCount() {
        return getAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.USER)
                .count();
    }

}