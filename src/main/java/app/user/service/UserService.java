package app.user.service;


import app.exception.LastAdminException;
import app.exception.UserNotFoundException;
import app.exception.UsernameAlreadyExistException;
import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

    User user = getById(userId);

    user.setFirstName(userEditRequest.getFirstName());
    user.setLastName(userEditRequest.getLastName());
    user.setProfilePicture(userEditRequest.getProfilePicture());
    user.setCountry(userEditRequest.getCountry());
    user.setUpdatedOn(LocalDateTime.now());


    userRepository.save(user);
    }


    @CacheEvict(value = "users", allEntries = true)
    public User register(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());

        if (userOptional.isPresent()) {
            throw new UsernameAlreadyExistException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
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

    @Cacheable("users")
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id[%s] does not exist.".formatted(id)));
    }


    @CacheEvict(value = "users", allEntries = true)
    public void changeUserRole(UUID userId, UserRole newRole) {
        User user = getById(userId);

        if (user.getRole() == UserRole.ADMIN && newRole == UserRole.USER) {
            long adminCount = getAdminCount();
            if (adminCount <= 1) {
                throw new LastAdminException("Cannot remove the last admin user!");
            }
        }

        user.setRole(newRole);
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
    }



    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getAdminCount() {
       return userRepository.countByRole(UserRole.ADMIN);
    }

    public long getUserCount() {
        return  userRepository.countByRole(UserRole.USER);
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       User user= userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with this username does not exist."));

        return new AuthenticationMethadata(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.isActive());
    }
}