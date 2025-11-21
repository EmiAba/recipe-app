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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;


    @Autowired
    private MockMvc mockMvc;


    @Test
    void getProfileMenu_shouldReturnEditProfileView() throws Exception {
        User user = aRandomUser();

        when(userService.getById(user.getId())).thenReturn(user);

        AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder request = get("/users/profile")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attributeExists("user", "userEditRequest"));

        verify(userService, times(1)).getById(user.getId());
    }

    @Test
    void updateUserProfile_shouldRedirectToHome() throws Exception {
        User user = aRandomUser();

        AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder request = put("/users/profile")
                .formField("firstName", "Adrien")
                .formField("lastName", " Brody")
                .formField("country", "Bulgaria")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).editUserDetails(any(), any());
    }

    @Test
    void updateUserProfile_withInvalidData_shouldReturnEditProfileView() throws Exception {
        User user = aRandomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder request = put("/users/profile")
                .formField("firstName", "Adrien")
                .formField("lastName", "")
                .formField("country", "")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"));

        verify(userService, never()).editUserDetails(any(), any());
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
