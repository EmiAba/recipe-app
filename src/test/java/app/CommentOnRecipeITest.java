package app;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.category.model.Category;
import app.category.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class CommentOnRecipeITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void postCommentToRecipe_shouldSaveCommentInDatabase() throws Exception {

        User testUser = User.builder()
                .username("Emi123")
                .email("test@example.com")
                .password(passwordEncoder.encode("123123"))
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .recipes(new ArrayList<>())
                .favorites(new HashSet<>())
                .comments(new ArrayList<>())
                .build();
        testUser = userRepository.save(testUser);


        Category testCategory = Category.builder()
                                         .name("Italian")
                                        .build();

        testCategory = categoryRepository.save(testCategory);


        Recipe testRecipe = Recipe.builder()
                .title("Spaghetti Carbonara")
                .description("Classic Italian pasta")
                .ingredients("Pasta, eggs, bacon, cheese")
                .instructions("Cook pasta, mix ingredients")
                .prepTimeMinutes(30)
                .servingSize(4)
                .author(testUser)
                .categories(Set.of(testCategory))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .comments(new ArrayList<>())
                .build();
        testRecipe = recipeRepository.save(testRecipe);


        AuthenticationMethadata principal = new AuthenticationMethadata(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getPassword(),
                testUser.getRole(),
                testUser.isActive()
        );


        MockHttpServletRequestBuilder httpRequest = post("/comments/recipe/" + testRecipe.getId())
                .with(user(principal))
                .with(csrf())
                .param("content", "This recipe is amazing!")
                .param("rating", "5");

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + testRecipe.getId() + "#comments"));


        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("This recipe is amazing!");
        assertThat(comments.get(0).getRating()).isEqualTo(5);
        assertThat(comments.get(0).getAuthor().getId()).isEqualTo(testUser.getId());
        assertThat(comments.get(0).getRecipe().getId()).isEqualTo(testRecipe.getId());
    }
}

