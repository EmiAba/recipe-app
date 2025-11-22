package app.web;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import app.exception.UsernameAlreadyExistException;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
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
import java.util.*;

@WebMvcTest(IndexController.class)
@ActiveProfiles("test")
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private RecipeService recipeService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void getIndexEndPoint_shouldReturn200OkAndIndexView() throws Exception {

        MockHttpServletRequestBuilder httpRequest = get("/");

        mockMvc.perform(httpRequest)
                .andExpect(view().name("index"))
                .andExpect(status().isOk());

    }


    @Test
    void getRequestToLoginEndPoint_withErrorParameter_shouldReturnLogoutView_andErrorMessageAttribute() throws Exception {

        MockHttpServletRequestBuilder httpRequest = get("/login").param("error", "");

        mockMvc.perform(httpRequest)
                .andExpect(view().name("login"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }


    @Test
    void getRequestToLoginEndPoint_withoutErrorParameter_shouldReturnLoginView_withoutErrorMessage() throws Exception {

        MockHttpServletRequestBuilder httpRequest = get("/login");

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    void getRequestToRegisterEndPoint_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder httpRequest = get("/register");

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));

    }

    @Test
    void postRegister_shouldReturn200OkAndRedirectToLoginAndInvokeRegisterServiceMethod() throws Exception {

        MockHttpServletRequestBuilder httpRequest = post("/register")
                .formField("username", "Emi123")
                .formField("email", "aba@gmail.com")
                .formField("password", "123456")
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).register(any());
    }


    @Test
    void postRegisterWithInvalidFormData_shouldReturn200kAndShowRegisterView_andRegisterServiceMethodNeverInvoked() throws Exception {

        MockHttpServletRequestBuilder httpRequest = post("/register")
                .formField("username", "E")
                .formField("email", "aba@gmail.com")
                .formField("password", " ")
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("register")) ;

        verify(userService, never()).register(any());
    }


    @Test
    void getHomePage_shouldReturnHomeView_andStatusCodeIs200() throws Exception {
        User user = aRandomUser();
        List<Recipe> recentRecipes = List.of();

        when(userService.getById(user.getId())).thenReturn(user);
        when(recipeService.getRecipesByUser(user, 3)).thenReturn(recentRecipes);

        AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());


        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("recentRecipes"));


        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).getRecipesByUser(user, 3);

    }

        @Test
        void postRequestToRegisterEndpointWhenUsernameAlreadyExist_thenRedirectToRegisterWithFlashParameter() throws Exception {

            when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("Username already exist!"));

            MockHttpServletRequestBuilder httpRequest= post("/register")
                    .formField("username", "Emi123")
                    .formField("password", "123456")
                    .formField("email", "test@test.com")
                    .with(csrf());



            mockMvc.perform(httpRequest)
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/register"))
                    .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));
            verify(userService, times(1)).register(any());
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
