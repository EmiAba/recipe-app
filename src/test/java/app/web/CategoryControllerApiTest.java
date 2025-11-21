package app.web;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.exception.CategoryNotFoundException;
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

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
public class CategoryControllerApiTest {

    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private RecipeService recipeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllCategories_shouldReturn200AndCategoriesView() throws Exception {
        User user = aRandomUser();
        List<Category> categories = List.of(createCategory("Desserts"));
        Map<String, Long> categoryCounts = Map.of("Desserts", 3L);

        when(userService.getById(user.getId())).thenReturn(user);
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryService.getCategoryRecipeCounts()).thenReturn(categoryCounts);

        AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/categories")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("categories", "categoryCounts", "user"));

        verify(userService, times(1)).getById(user.getId());
        verify(categoryService, times(1)).getAllCategories();
        verify(categoryService, times(1)).getCategoryRecipeCounts();
    }

    @Test
    void getCategoryRecipes_shouldReturn200AndCategoryDetailView() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Desserts");
        List<Recipe> publicRecipes = List.of(
                createRecipe("Cake", user, category),
                createRecipe("Cookies", user, category)
        );

        when(userService.getById(user.getId())).thenReturn(user);
        when(categoryService.findByName("Desserts")).thenReturn(category);
        when(recipeService.getPublicRecipes(category)).thenReturn(publicRecipes);

        AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/categories/Desserts")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("category-detail"))
                .andExpect(model().attributeExists("category", "recipes", "recipeCount", "user"))
                .andExpect(model().attribute("recipeCount", 2));

        verify(userService, times(1)).getById(user.getId());
        verify(categoryService, times(1)).findByName("Desserts");
        verify(recipeService, times(1)).getPublicRecipes(category);
    }

    @Test
    void getCategoryRecipes_shouldReturn404_whenCategoryNotFound() throws Exception {
        User user = aRandomUser();

        when(userService.getById(user.getId())).thenReturn(user);
        when(categoryService.findByName("NonExistent"))
                .thenThrow(new CategoryNotFoundException("Category not found"));

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/categories/NonExistent")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
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

    private Category createCategory(String name) {
        return Category.builder().name(name).build();
    }

    private Recipe createRecipe(String title, User author, Category category) {
        return Recipe.builder()
                .id(UUID.randomUUID())
                .title(title)
                .instructions("Test instructions")
                .prepTimeMinutes(10)
                .cookTimeMinutes(20)
                .author(author)
                .categories(Set.of(category))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .isPublic(true)
                .deleted(false)
                .build();
    }
}