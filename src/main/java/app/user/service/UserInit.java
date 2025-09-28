package app.user.service;

import app.user.property.UserProperties;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserInit implements ApplicationRunner {

 private final UserService userService;
 private final UserProperties userProperties;

    @Autowired
    public UserInit(UserService userService, UserProperties userProperties) {
        this.userService = userService;
        this.userProperties = userProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if(!userService.getAllUsers().isEmpty()){
            return;
        }


     RegisterRequest registerRequest=RegisterRequest.builder()
                    .username(userProperties.getDefaultUser().getUsername())
                    .email(userProperties.getDefaultUser().getEmail())
                    .password(userProperties.getDefaultUser().getPassword())
                    .build();

     userService.register(registerRequest);
    }


}



