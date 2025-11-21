package app.web;

import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@ActiveProfiles("test")
public class AdminControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAdminDashboard_shouldReturn200AndAdminView() throws Exception {
        User adminUser = aRandomAdminUser();
        List<User> allUsers = List.of();

        when(userService.getById(adminUser.getId())).thenReturn(adminUser);
        when(userService.getAllUsers()).thenReturn(allUsers);
        when(userService.getTotalUsers()).thenReturn(10L);
        when(userService.getAdminCount()).thenReturn(2L);
        when(userService.getUserCount()).thenReturn(8L);

        AuthenticationMethadata principal = new AuthenticationMethadata(adminUser.getId(), adminUser.getUsername(),
                adminUser.getPassword(), adminUser.getRole(), adminUser.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/admin")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("user", "users", "totalUsers", "adminCount", "userCount"));

        verify(userService, times(1)).getById(adminUser.getId());
        verify(userService, times(1)).getAllUsers();
        verify(userService, times(1)).getTotalUsers();
        verify(userService, times(1)).getAdminCount();
        verify(userService, times(1)).getUserCount();
    }


    @Test
    void changeUserRole_shouldRedirectToAdmin() throws Exception {
        User adminUser = aRandomAdminUser();
        User regularUser = aRandomUser();

        AuthenticationMethadata principal = new AuthenticationMethadata(adminUser.getId(), adminUser.getUsername(),
                adminUser.getPassword(), adminUser.getRole(), adminUser.isActive());

        MockHttpServletRequestBuilder httpRequest = patch("/admin/users/" + regularUser.getId() + "/ADMIN")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, times(1)).changeUserRole(regularUser.getId(), UserRole.ADMIN);
    }




    private User aRandomAdminUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("AdminEmi")
                .password("123123")
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .isActive(true)
                .firstName("Admin")
                .lastName("User")
                .country("Bulgaria")
                .profilePicture("")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .recipes(new ArrayList<>())
                .favorites(new HashSet<>())
                .build();
    }

    public static User aRandomUser() {


        return User.builder()
                .id(UUID.randomUUID())
                .username("Emi123")
                .password("123123")
                .email("test@test.com")
                .role(UserRole.USER)
                .isActive(true)
                .firstName("Emi")
                .lastName("Aba")
                .country("Bulgaria")
                .profilePicture("")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .recipes(new ArrayList<>())
                .favorites(new HashSet<>())
                .build();

    }
}