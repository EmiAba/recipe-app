package app.recipe;

import app.category.service.CategoryService;
import app.exception.RecipeNotFoundException;
import app.exception.UnauthorizedAccessException;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceUTest {

    @Mock
    private  RecipeRepository recipeRepository;

    @Mock
    private  CategoryService categoryService;

    @Mock
    private  UserService userService;

    @InjectMocks
    private RecipeService recipeService;

    //create



     @Test
    public void givenNonExistingRecipeId_whenGetById_thenThrowException(){
         UUID recipeId = UUID.randomUUID();

         when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

         assertThrows(RecipeNotFoundException.class, () -> recipeService.getById(recipeId));
     }


     @Test
    public void givenExistingRecipeId_whenGetById_thenReturnRecipe(){
         UUID recipeId = UUID.randomUUID();
         Recipe recipe = Recipe.builder().id(recipeId) .build();

         when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

         Recipe returnedRecipe = recipeService.getById(recipeId);

         assertThat(returnedRecipe).isNotNull();
         assertThat(returnedRecipe).isEqualTo(recipe);
         verify(recipeRepository).findById(recipeId);


     }


     //update


    //delete

    @Test
    public void whenAuthorDeletesRecipe_thenSetDeletedToTrue(){
         User author = User.builder().id(UUID.randomUUID()).build();

         UUID recipeId = UUID.randomUUID();

         Recipe recipe = Recipe.builder().id(recipeId)
                 .author(author)
                 .build();


         when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe(recipeId, author);


        assertThat(recipe.isDeleted()).isTrue();
        verify(recipeRepository).save(recipe);
    }

    @Test
    public void whenNonAuthorDeletesRecipe_thenThrowException() {
        User author = User.builder().id(UUID.randomUUID()).build();
        User notAuthor = User.builder().id(UUID.randomUUID()).build();
        UUID recipeId = UUID.randomUUID();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .author(author)
                .build();

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        assertThrows(UnauthorizedAccessException.class, () -> recipeService.deleteRecipe(recipeId, notAuthor));
    }


    @Test
    public void whenUserIsAuthor_thenReturnTrue(){
        UUID userId = UUID.randomUUID();

        User author = User.builder()
                .id(userId)
                .build();

        Recipe recipe = Recipe.builder()
                .author(author)
                .build();

        boolean result = recipeService.isAuthor(recipe, author);

        assertThat(result).isTrue();


    }

    @Test
    public void whenUserIsNotAuthor_thenReturnFalse() {
        UUID authorId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();

        User author = User.builder()
                .id(authorId)
                .build();

        User notAuthor = User.builder()
                .id(differentUserId)
                .build();

        Recipe recipe = Recipe.builder()
                .author(author)
                .build();

        boolean result = recipeService.isAuthor(recipe, notAuthor);

        assertThat(result).isFalse();
    }

    //get recipe by user

    //get public recipes

    //user favourite

    @Test
    public void whenRecipeIsInFavorites_thenReturnTrue(){

        Recipe recipe = Recipe.builder().build();

        User user = User.builder()
                .favorites(new HashSet<>(Set.of(recipe)))
                .build();

        boolean result=recipeService.isFavorite(recipe, user);


        assertThat(result).isTrue();

    }

    @Test
    public void whenRecipeIsNotInFavorites_thenReturnFalse(){
        Recipe recipe = Recipe.builder().build();

        User user = User.builder()
                .favorites(new HashSet<>())
                .build();

        boolean result=recipeService.isFavorite(recipe, user);


        assertThat(result).isFalse();

    }

    //remove from favourite




}
