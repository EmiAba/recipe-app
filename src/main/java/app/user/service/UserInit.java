package app.user.service;

import app.user.model.User;
import app.user.model.UserRole;
import app.user.property.UserProperties;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserInit implements ApplicationRunner {

    private final UserService userService;
    private final UserProperties userProperties;
    private final UserRepository userRepository;

    @Autowired
    public UserInit(UserService userService, UserProperties userProperties, UserRepository userRepository) {
        this.userService = userService;
        this.userProperties = userProperties;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if(userService.getTotalUsers() > 0){
          return;
        }

        String username = userProperties.getDefaultUser().getUsername();
        String email = userProperties.getDefaultUser().getEmail();
        String password = userProperties.getDefaultUser().getPassword();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();

        User admin = userService.register(registerRequest);


        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);


        System.out.println("\n========================================");
        System.out.println("DEFAULT ADMIN USER CREATED");
        System.out.println("========================================");
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        System.out.println("========================================\n");
    }
}