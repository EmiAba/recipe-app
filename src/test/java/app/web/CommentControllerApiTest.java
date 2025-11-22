package app.web;
import app.web.dto.CommentEditRequest;
import app.category.model.Category;
import app.category.service.CategoryService;
import app.comment.model.Comment;
import app.comment.service.CommentService;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
public class CommentControllerApiTest {


    @MockitoBean
    private UserService userService;
    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void addComment_withValidData_shouldRedirectToRecipe() throws Exception {
        User user = aRandomUser();
        UUID recipeId = UUID.randomUUID();

        when(userService.getById(user.getId())).thenReturn(user);

        AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = post("/comments/recipe/" + recipeId)
                .formField("content", "Great recipe!")
                .formField("rating", "5")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipeId + "#comments"));

        verify(userService, times(1)).getById(user.getId());
        verify(commentService, times(1)).createComment(any(), eq(recipeId), eq(user));
    }

    @Test
    void addComment_withInvalidData_shouldReturnRecipeDetailView() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);
        UUID recipeId = recipe.getId();


        when(userService.getById(user.getId())).thenReturn(user);
        when(recipeService.getById(recipeId)).thenReturn(recipe);
        when(recipeService.isAuthor(recipe, user)).thenReturn(true);
        when(recipeService.isFavorite(recipe, user)).thenReturn(false);
        when(commentService.getAverageRatingForRecipe(recipeId)).thenReturn(4.5);
        when(commentService.getTotalRatingsForRecipe(recipeId)).thenReturn(10);
        when(commentService.getCommentsByRecipe(recipeId)).thenReturn(new ArrayList<>());

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());


        MockHttpServletRequestBuilder httpRequest = post("/comments/recipe/" + recipeId)
                .formField("content", "")
                .formField("rating", "5")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-detail"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attributeExists("isAuthor"))
                .andExpect(model().attributeExists("isFavorite"))
                .andExpect(model().attributeExists("averageRating"))
                .andExpect(model().attributeExists("totalRatings"))
                .andExpect(model().attributeExists("commentCreateRequest"))
                .andExpect(model().attributeExists("comments"));
    }

     @Test
     void deleteComment_shouldRedirectToRecipe()  throws Exception {
         User user = aRandomUser();
         Category category=createCategory("Dessert");
         Recipe recipe = createRecipe("Choco cake", user, category);
         Comment comment=createComment("text", 5, user, recipe);

         when(userService.getById(user.getId())).thenReturn(user);
         when(commentService.getById(comment.getId())).thenReturn(comment);


         AuthenticationMethadata principal = new AuthenticationMethadata(user.getId(), user.getUsername(),
                 user.getPassword(), user.getRole(), user.isActive());

         MockHttpServletRequestBuilder httpRequest = delete("/comments/" + comment.getId() + "/delete")
                 .with(user(principal))
                 .with(csrf());

         mockMvc.perform(httpRequest)
                 .andExpect(status().is3xxRedirection())
                 .andExpect(redirectedUrl("/recipes/" + recipe.getId() + "#comments"));

         verify(commentService, times(1)).getById(comment.getId());
         verify(userService, times(1)).getById(user.getId());
         verify(commentService, times(1)).deleteComment(comment.getId(), user);


     }


    @Test
    void editCommentForm_whenUserIsAuthor_shouldReturnEditView() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);
        Comment comment = createComment("text", 5, user, recipe);

        when(userService.getById(user.getId())).thenReturn(user);
        when(commentService.getById(comment.getId())).thenReturn(comment);

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/comments/" + comment.getId() + "/edit")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("comment-edit"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attributeExists("commentEditRequest"));

        verify(commentService, times(1)).getById(comment.getId());
        verify(userService, times(1)).getById(user.getId());

    }
    @Test
    void editCommentForm_whenUserIsNotAuthor_shouldRedirectToRecipe() throws Exception {
        User user = aRandomUser();
        User differentUser = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);
        Comment comment = createComment("text", 5, differentUser, recipe);

        when(userService.getById(user.getId())).thenReturn(user);
        when(commentService.getById(comment.getId())).thenReturn(comment);


        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/comments/" + comment.getId() + "/edit")
                .with(user(principal));

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + comment.getRecipe().getId()));

        verify(commentService, times(1)).getById(comment.getId());
        verify(userService, times(1)).getById(user.getId());
    }


    @Test
    void updateComment_withValidData_shouldRedirectToRecipe() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);
        Comment comment = createComment("text", 5, user, recipe);

        when(userService.getById(user.getId())).thenReturn(user);
        when(commentService.getById(comment.getId())).thenReturn(comment);

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = put("/comments/" + comment.getId() + "/edit")
                .formField("content", "Super!")
                .formField("rating", "4")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + comment.getRecipe().getId() + "#comments"));


        verify(commentService, times(1)).getById(comment.getId());
        verify(userService, times(1)).getById(user.getId());
        verify(commentService, times(1)).updateComment(eq(comment.getId()), any(CommentEditRequest.class), eq(user));

    }

    @Test
    void updateComment_withInvalidData_shouldReturnEditView() throws Exception {
        User user = aRandomUser();
        Category category = createCategory("Dessert");
        Recipe recipe = createRecipe("Choco cake", user, category);
        Comment comment = createComment("text", 5, user, recipe);

        when(userService.getById(user.getId())).thenReturn(user);
        when(commentService.getById(comment.getId())).thenReturn(comment);

        AuthenticationMethadata principal = new AuthenticationMethadata(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = put("/comments/" + comment.getId() + "/edit")
                .formField("content", "")
                .formField("rating", "4")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("comment-edit"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attributeExists("commentEditRequest"));


        verify(commentService, times(1)).getById(comment.getId());
        verify(userService, times(1)).getById(user.getId());
        verify(commentService, never()).updateComment(eq(comment.getId()), any(CommentEditRequest.class), eq(user));


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

    private Category createCategory(String name) {
        return Category.builder().name(name).build();
    }


}
