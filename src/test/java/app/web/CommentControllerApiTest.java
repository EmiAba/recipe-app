package app.web;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
public class CommentControllerApiTest {


    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;


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

         MockHttpServletRequestBuilder request = delete("/comments/" + comment.getId() + "/delete")
                 .with(user(principal))
                 .with(csrf());

         mockMvc.perform(request)
                 .andExpect(status().is3xxRedirection())
                 .andExpect(redirectedUrl("/recipes/" + recipe.getId() + "#comments"));

         verify(commentService, times(1)).getById(comment.getId());
         verify(userService, times(1)).getById(user.getId());
         verify(commentService, times(1)).deleteComment(comment.getId(), user);


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
