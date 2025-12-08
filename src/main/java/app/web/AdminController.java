package app.web;

import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView adminDashboard(@AuthenticationPrincipal AuthenticationMethadata auth) {
        User user = userService.getById(auth.getUserId());
        List<User> allUsers = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin");
        modelAndView.addObject("users", allUsers);
        modelAndView.addObject("user",user);
        modelAndView.addObject("totalUsers", userService.getTotalUsers());
        modelAndView.addObject("adminCount", userService.getAdminCount());
        modelAndView.addObject("userCount", userService.getUserCount());

        return modelAndView;
    }


    @PatchMapping("/users/{userId}/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView changeUserRole(@PathVariable UUID userId, @PathVariable UserRole role) {
        userService.changeUserRole(userId, role);
        return new ModelAndView("redirect:/admin");
    }


    //block users
    @PatchMapping("/users/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserStatus(@PathVariable UUID userId) {
        userService.toggleUserActiveStatus(userId);
        return "redirect:/admin";
    }

}