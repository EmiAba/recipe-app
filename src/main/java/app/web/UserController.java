package app.web;



import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;



    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;

    }

    @GetMapping("/profile")
    public ModelAndView getProfileMenu(@AuthenticationPrincipal AuthenticationMethadata authenticationMethadata){
        User user = userService.getById(authenticationMethadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));

        return modelAndView;

    }



    @PutMapping("/profile")
    public ModelAndView updateUserProfile(@AuthenticationPrincipal AuthenticationMethadata authenticationMethadata,
                                          @Valid UserEditRequest userEditRequest,
                                          BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            User user = userService.getById(authenticationMethadata.getUserId());
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("edit-profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);
            return modelAndView;
        }

        userService.editUserDetails(authenticationMethadata.getUserId(), userEditRequest);

        return new ModelAndView("redirect:/home");
    }
}