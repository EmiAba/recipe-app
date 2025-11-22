package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.exception.UnauthorizedAccessException;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
@ActiveProfiles("test")

public class RecipeControllerApiTest {
    @MockitoBean
    private RecipeService recipeService;
    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void getRecipeAddPage_shouldReturnRecipeFormView() throws Exception {

        User user = aRandomUser();
        Category category = createCategory("Dessert");


        when(userService.getById(user.getId())).thenReturn(user);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/recipes/add")
                .with(user(principal));


        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("recipeCreateRequest"));


        verify(userService, times(1)).getById(user.getId());
        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void saveRecipe_withInvalidData_shouldReturnRecipeForm() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");

        when(userService.getById(user.getId())).thenReturn(user);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = post("/recipes/save")
                .formField("title", "")
                .formField("description", "")
                .with(user(principal))
                .with(csrf());


        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("categories"));


        verify(userService, times(1)).getById(user.getId());
        verify(categoryService, times(1)).getAllCategories();
        verify(recipeService, never()).createRecipe(any(), eq(user));

    }


    @Test
    void saveRecipe_withValidData_shouldRedirectToRecipeDetail() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);

        when(userService.getById(user.getId())).thenReturn(user);
        when(recipeService.createRecipe(any(), eq(user))).thenReturn(recipe);

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = post("/recipes/save")
                .formField("title", "Choco Cake")
                .formField("description", "Yummy")
                .formField("instructions", "Mix and bake")
                .formField("ingredients", "Flour, sugar, cocoa")
                .formField("prepTimeMinutes", "15")
                .formField("cookTimeMinutes", "30")
                .formField("servingSize", "4")
                .formField("difficultyLevel", "EASY")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipe.getId()));

        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).createRecipe(any(), eq(user));
    }


    @Test
    void viewRecipe_shouldReturnRecipeDetailView() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);

        when(userService.getById(user.getId())).thenReturn(user);
        when(recipeService.getById(recipe.getId())).thenReturn(recipe);
        when(recipeService.isAuthor(recipe, user)).thenReturn(true);
        when(recipeService.isFavorite(recipe, user)).thenReturn(true);
        when(commentService.getAverageRatingForRecipe(recipe.getId())).thenReturn(6.0);
        when(commentService.getTotalRatingsForRecipe(recipe.getId())).thenReturn(10);
        when(commentService.getCommentsByRecipe(recipe.getId())).thenReturn(new ArrayList<>());

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/recipes/" + recipe.getId())
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-detail"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attributeExists("isAuthor"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("isFavorite"))
                .andExpect(model().attributeExists("averageRating"))
                .andExpect(model().attributeExists("totalRatings"))
                .andExpect(model().attributeExists("commentCreateRequest"));

        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).getById(recipe.getId());
        verify(recipeService, times(1)).isAuthor(recipe, user);
        verify(recipeService, times(1)).isFavorite(recipe, user);
        verify(commentService, times(1)).getAverageRatingForRecipe(recipe.getId());
        verify(commentService, times(1)).getTotalRatingsForRecipe(recipe.getId());
        verify(commentService, times(1)).getCommentsByRecipe(recipe.getId());
    }

    @Test
    void getMyRecipes_shouldReturnUserRecipesList() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);

        when(userService.getById(user.getId())).thenReturn(user);
        when(recipeService.getRecipesByUser(user, null)).thenReturn(List.of(recipe));

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/recipes/mine")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-list"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("recipes"));

        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).getRecipesByUser(user, null);
    }


    @Test
    void getRecipeEditPage_shouldReturnRecipeEditView() throws Exception {

        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);

        when(userService.getById(user.getId())).thenReturn(user);
        when(recipeService.getById(recipe.getId())).thenReturn(recipe);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));


        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/recipes/edit/" + recipe.getId())
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-edit"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("recipeId"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("recipeUpdateRequest"));


        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).getById(recipe.getId());
        verify(categoryService, times(1)).getAllCategories();

    }


    @Test
    void updateRecipe_withInvalidData_shouldReturnRecipeEditView() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);

        when(userService.getById(user.getId())).thenReturn(user);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = put("/recipes/" + recipe.getId())
                .formField("title", "")
                .formField("description", "")
                .with(user(principal))
                .with(csrf());


        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-edit"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("recipeId"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("recipeUpdateRequest"));


        verify(userService, times(1)).getById(user.getId());
        verify(categoryService, times(1)).getAllCategories();
        verify(recipeService, never()).updateRecipe(any(), any(), eq(user));

    }


    @Test
    void updateRecipe_withValidData_shouldRedirectToRecipeDetail() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco choco", user, category);

        when(userService.getById(user.getId())).thenReturn(user);
        when(recipeService.updateRecipe(eq(recipe.getId()), any(), eq(user))).thenReturn(recipe);

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = put("/recipes/" + recipe.getId())
                .formField("title", "Choco Cake")
                .formField("description", "Yummy")
                .formField("instructions", "Mix and bake")
                .formField("ingredients", "Flour, sugar, cocoa")
                .formField("prepTimeMinutes", "15")
                .formField("cookTimeMinutes", "30")
                .formField("servingSize", "4")
                .formField("difficultyLevel", "EASY")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipe.getId()));

        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).updateRecipe(eq(recipe.getId()), any(), eq(user));
    }
    @Test
    void deleteRecipe_shouldRedirectToMyRecipes() throws Exception{
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);

        when(userService.getById(user.getId())).thenReturn(user);

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = delete("/recipes/"+ recipe.getId()+"/delete")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/mine"));


        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).deleteRecipe(recipe.getId(), user);

    }


    @Test
    void getMyFavorites_shouldReturnUserFavoritesList() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);

        when(userService.getById(user.getId())).thenReturn(user);
        when(recipeService.getUserFavorites(user.getId())).thenReturn(List.of(recipe));

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/recipes/favorites")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-favorites"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("favorites"));

        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).getUserFavorites(user.getId());
    }

    @Test
    void addToFavorites_shouldRedirectToRecipeWithSuccessMessage() throws Exception{
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);

        when(userService.getById(user.getId())).thenReturn(user);

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = post("/recipes/"+ recipe.getId()+"/favorite")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipe.getId() +"?success=added"));


        verify(userService, times(1)).getById(user.getId());
        verify(recipeService, times(1)).addToFavorites(user,recipe.getId());

    }

    @Test
    void downloadRecipePdf_shouldReturnPdfFile() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);
        byte[] pdfBytes = "fake pdf content".getBytes();

        when(recipeService.generateRecipePdf(recipe.getId())).thenReturn(pdfBytes);

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/recipes/" + recipe.getId() + "/pdf")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes(pdfBytes));

        verify(recipeService, times(1)).generateRecipePdf(recipe.getId());
    }




    @Test
    void deleteRecipe_whenNotAuthor_thenReturn403() throws Exception {
        User author = aRandomUser();
        User otherUser = aRandomUser();
        Recipe recipe = createRecipe("Choco cake", author, createCategory("Dessert"));

        when(userService.getById(otherUser.getId())).thenReturn(otherUser);
        doThrow(new UnauthorizedAccessException("You can only delete your own recipes."))
                .when(recipeService).deleteRecipe(recipe.getId(), otherUser);

        AuthenticationMethadata principal = new AuthenticationMethadata(otherUser.getId(),
                otherUser.getUsername(), otherUser.getPassword(), otherUser.getRole(), otherUser.isActive());

        mockMvc.perform(delete("/recipes/" + recipe.getId() + "/delete")
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("access-denied"));

        verify(recipeService, times(1)).deleteRecipe(recipe.getId(), otherUser);
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

    public static Recipe createRecipe(String title, User author, Category category) {
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


    private Category createCategory(String name) {
        return Category.builder().name(name).build();
    }
    private Comment createComment(String content, Integer rating, User author, Recipe recipe) {
        return Comment.builder()
                .id(UUID.randomUUID())
                .content(content)
                .rating(rating)
                .author(author)
                .recipe(recipe)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

}
