package app.recipe;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.exception.RecipeNotFoundException;
import app.exception.UnauthorizedAccessException;
import app.recipe.model.DifficultyLevel;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RecipeCreateRequest;
import app.web.dto.RecipeUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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


    @Test
    public void whenCreateRecipe_thenRecipeSavedWithAuthorAndCategories(){

        User author = User.builder()
                .id(UUID.randomUUID())
                .build();

        Category category = Category.builder()
                .name("Italian")
                .build();

        RecipeCreateRequest recipeRequest = RecipeCreateRequest.builder()
                .title("Pizza")
                .description("stove cooked")
                .instructions("bake 30 minutes")
                .cookTimeMinutes(5)
                .prepTimeMinutes(4)
                .servingSize(6)
                .difficultyLevel(DifficultyLevel.EASY)
                .ingredients("salt")
                .imageUrl("www.image.jpg")
                .calories(100)
                .protein(3.0)
                .fiber(3.0)
                .sugar(25.0)
                .carbs(45.0)
                .isPublic(true)
                .categoryNames(Set.of("Italian"))
                .build();

        Recipe savedRecipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Pizza")
                .author(author)
                .categories(Set.of(category))
                .build();


        when(categoryService.findCategoriesByNames(recipeRequest.getCategoryNames()))
                .thenReturn(Set.of(category));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);


        Recipe result = recipeService.createRecipe(recipeRequest, author);


        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Pizza");
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCategories()).contains(category);
        verify(categoryService).findCategoriesByNames(recipeRequest.getCategoryNames());
        verify(recipeRepository).save(any(Recipe.class));
    }

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


     @Test
     public void givenNonAuthorUser_whenUpdateRecipe_thenThrowException(){

         UUID recipeId = UUID.randomUUID();
         User author = User.builder().id(UUID.randomUUID()).build();
         User differentUser = User.builder().id(UUID.randomUUID()).build();

         Recipe recipe = Recipe.builder()
                 .id(recipeId)
                 .title("Pizza")
                 .author(author)
                 .build();

         RecipeUpdateRequest request = RecipeUpdateRequest.builder()
                 .title("New Title")
                 .build();


         when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));


         assertThrows(UnauthorizedAccessException.class,
                 () -> recipeService.updateRecipe(recipeId, request, differentUser));
     }

    @Test
    public void whenAuthorUpdatesRecipe_thenRecipeUpdatedSuccessfully(){

        UUID recipeId = UUID.randomUUID();
        User author = User.builder().id(UUID.randomUUID()).build();

        Category category = Category.builder().name("Dessert").build();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .title("Pizza")
                .author(author)
                .isPublic(true)
                .categories(new HashSet<>())
                .build();

        RecipeUpdateRequest request = RecipeUpdateRequest.builder()
                .title("cake")
                .description("Delicious cake")
                .instructions("Bake it")
                .prepTimeMinutes(10)
                .cookTimeMinutes(30)
                .servingSize(4)
                .difficultyLevel(DifficultyLevel.EASY)
                .imageUrl("cake.jpg")
                .ingredients("flour, sugar")
                .calories(300)
                .protein(5.0)
                .carbs(50.0)
                .fat(10.0)
                .fiber(2.0)
                .sugar(20.0)
                .sodium(100.0)
                .isPublic(true)
                .categoryNames(Set.of("Dessert"))
                .build();


        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(categoryService.findCategoriesByNames(request.getCategoryNames()))
                .thenReturn(Set.of(category));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);


        Recipe result = recipeService.updateRecipe(recipeId, request, author);


        assertThat(result.getTitle()).isEqualTo("cake");
        verify(recipeRepository).save(recipe);
        verify(categoryService).findCategoriesByNames(request.getCategoryNames());
    }


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


    @Test
    public void whenGetRecipesByUserWithLimit_thenReturnLimitedRecipes() {
        User user = User.builder().build();
        Recipe recipe1 = Recipe.builder().build();
        Recipe recipe2 = Recipe.builder().build();
        Recipe recipe3 = Recipe.builder().build();

        when(recipeRepository.findByAuthorAndDeletedFalseOrderByCreatedOnDesc(user))
                .thenReturn(List.of(recipe1, recipe2, recipe3));

        List<Recipe> result = recipeService.getRecipesByUser(user, 2);

        assertThat(result).hasSize(2);
        verify(recipeRepository).findByAuthorAndDeletedFalseOrderByCreatedOnDesc(user);
    }

    @Test
    public void whenGetRecipesByUserWithNoLimit_thenReturnAllUserRecipes(){

        User user= User.builder().build();
        Recipe recipe1= Recipe.builder().build();
        Recipe recipe2= Recipe.builder().build();


        when(recipeRepository.findByAuthorAndDeletedFalseOrderByCreatedOnDesc(user)).thenReturn(List.of(recipe1, recipe2));
        List<Recipe> result = recipeService.getRecipesByUser(user, null);

        assertThat(result.size()).isEqualTo(2);
        verify(recipeRepository).findByAuthorAndDeletedFalseOrderByCreatedOnDesc(user);


    }

    @Test
    public void whenGetRecipesByUserWithZeroLimit_thenReturnAllRecipes() {
        User user = User.builder().build();
        Recipe recipe1 = Recipe.builder().build();
        Recipe recipe2 = Recipe.builder().build();

        when(recipeRepository.findByAuthorAndDeletedFalseOrderByCreatedOnDesc(user)).thenReturn(List.of(recipe1, recipe2));

        List<Recipe> result = recipeService.getRecipesByUser(user, 0);

        assertThat(result).hasSize(2);
    }


    @Test
    public void whenGetPublicRecipes_thenReturnOnlyPublicRecipesFromCategory(){

        Recipe publicRecipe1 = Recipe.builder()
                .title("Public 1")
                .isPublic(true)
                .build();

        Recipe publicRecipe2 = Recipe.builder()
                .title("Public 2")
                .isPublic(true)
                .build();

        Recipe privateRecipe = Recipe.builder()
                .title("Private")
                .isPublic(false)
                .build();

        Category category = Category.builder()
                .recipes(Set.of(publicRecipe1, publicRecipe2, privateRecipe))
                .build();


        List<Recipe> result = recipeService.getPublicRecipes(category);


        assertThat(result).hasSize(2);
        assertThat(result).contains(publicRecipe1, publicRecipe2);
        assertThat(result).doesNotContain(privateRecipe);
    }


    @Test
    public void whenAddToFavorites_thenRecipeAddedToUserFavorites(){
        UUID recipeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .favorites(new HashSet<>())
                .build();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .build();

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        recipeService.addToFavorites(user, recipeId);

        assertThat(user.getFavorites()).contains(recipe);
        verify(recipeRepository).save(recipe);

    }


    @Test
    public void whenGetUserFavorites_thenReturnSortedFavoritesByCreatedDate(){
        UUID userId = UUID.randomUUID();

        Recipe oldRecipe = Recipe.builder()
                .createdOn(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();

        Recipe newRecipe = Recipe.builder()
                .createdOn(LocalDateTime.of(2025, 12, 1, 10, 0))
                .build();

        User user = User.builder()
                .id(userId)
                .favorites(new HashSet<>(Set.of(oldRecipe, newRecipe)))
                .build();

        when(userService.getById(userId)).thenReturn(user);


        List<Recipe> result = recipeService.getUserFavorites(userId);


        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(newRecipe);
        assertThat(result.get(1)).isEqualTo(oldRecipe);

    }


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

    @Test
    public void whenRemoveFromFavorites_thenRecipeRemovedFromUserFavorites(){
        UUID recipeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .build();

        User user = User.builder()
                .id(userId)
                .favorites(new HashSet<>(Set.of(recipe)))
                .build();

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        recipeService.removeFromFavorites(user, recipeId);

        assertThat(user.getFavorites()).doesNotContain(recipe);
        verify(recipeRepository).save(recipe);

    }


}
