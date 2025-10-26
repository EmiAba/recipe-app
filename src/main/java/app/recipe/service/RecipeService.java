package app.recipe.service;

import app.category.model.Category;
import app.exception.RecipeNotFoundException;
import app.exception.UnauthorizedAccessException;
import app.user.service.UserService;
import app.web.dto.RecipeUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.user.model.User;
import app.category.service.CategoryService;
import app.web.dto.RecipeCreateRequest;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryService categoryService;

    private final UserService userService;


    public RecipeService(RecipeRepository recipeRepository, CategoryService categoryService,
                         UserService userService) {
        this.recipeRepository = recipeRepository;
        this.categoryService = categoryService;


        this.userService = userService;
    }


    public Recipe createRecipe(RecipeCreateRequest recipeCreateRequest, User author) {
        Set<Category> categories = categoryService.findCategoriesByNames(recipeCreateRequest.getCategoryNames());

        Recipe recipe = Recipe.builder()
                .title(recipeCreateRequest.getTitle())
                .description(recipeCreateRequest.getDescription())
                .instructions(recipeCreateRequest.getInstructions())
                .prepTimeMinutes(recipeCreateRequest.getPrepTimeMinutes())
                .cookTimeMinutes(recipeCreateRequest.getCookTimeMinutes())
                .servingSize(recipeCreateRequest.getServingSize())
                .difficultyLevel(recipeCreateRequest.getDifficultyLevel())
                .imageUrl(recipeCreateRequest.getImageUrl())
                .isPublic(recipeCreateRequest.getIsPublic())
                .author(author)
                .ingredients(recipeCreateRequest.getIngredients())
                .calories(recipeCreateRequest.getCalories())
                .protein(recipeCreateRequest.getProtein())
                .carbs(recipeCreateRequest.getCarbs())
                .fat(recipeCreateRequest.getFat())
                .fiber(recipeCreateRequest.getFiber())
                .sugar(recipeCreateRequest.getSugar())
                .sodium(recipeCreateRequest.getSodium())
                .categories(categories)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return recipeRepository.save(recipe);
    }

    public Recipe getById(UUID id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe with id[%s] does not exist.".formatted(id)));
    }


    public Recipe updateRecipe(UUID recipeId, RecipeUpdateRequest recipeUpdateRequest, User currentUser) {
        Recipe recipe = getById(recipeId);

        if (!recipe.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only edit your own recipes.");
        }

        Set<Category> categories = categoryService.findCategoriesByNames(recipeUpdateRequest.getCategoryNames());

        recipe.setTitle(recipeUpdateRequest.getTitle());
        recipe.setDescription(recipeUpdateRequest.getDescription());
        recipe.setInstructions(recipeUpdateRequest.getInstructions());
        recipe.setPrepTimeMinutes(recipeUpdateRequest.getPrepTimeMinutes());
        recipe.setCookTimeMinutes(recipeUpdateRequest.getCookTimeMinutes());
        recipe.setServingSize(recipeUpdateRequest.getServingSize());
        recipe.setDifficultyLevel(recipeUpdateRequest.getDifficultyLevel());
        recipe.setImageUrl(recipeUpdateRequest.getImageUrl());
        recipe.setPublic(recipeUpdateRequest.getIsPublic());
        recipe.setIngredients(recipeUpdateRequest.getIngredients());
        recipe.setCalories(recipeUpdateRequest.getCalories());
        recipe.setProtein(recipeUpdateRequest.getProtein());
        recipe.setCarbs(recipeUpdateRequest.getCarbs());
        recipe.setFat(recipeUpdateRequest.getFat());
        recipe.setFiber(recipeUpdateRequest.getFiber());
        recipe.setSugar(recipeUpdateRequest.getSugar());
        recipe.setSodium(recipeUpdateRequest.getSodium());
        recipe.getCategories().clear();
        recipe.getCategories().addAll(categories);
        recipe.setUpdatedOn(LocalDateTime.now());

        return recipeRepository.save(recipe);
    }


    public void deleteRecipe(UUID recipeId, User currentUser) {
        Recipe recipe = getById(recipeId);

        if (!recipe.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only edit your own recipes.");
        }

        recipe.getCategories().clear();
        recipe.getFavoriteBy().clear();
        recipe.getComments().clear();

        recipeRepository.saveAndFlush(recipe);

        recipeRepository.delete(recipe);
    }




    public boolean isAuthor(Recipe recipe, User user) {
        return recipe.getAuthor().getId().equals(user.getId());
    }



    public List<Recipe> getRecentRecipesByUser(User user, int limit) {
        return user.getRecipes().stream()
                .sorted((r1, r2) -> r2.getCreatedOn().compareTo(r1.getCreatedOn()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Recipe> getRecipesByUser(User user) {
        return recipeRepository.findByAuthorOrderByCreatedOnDesc(user);
    }


    public List<Recipe> getPublicRecipes(Category category) {
        return category.getRecipes().stream()
                .filter(Recipe::isPublic)
                .collect(Collectors.toList());
    }


    public void addToFavorites(User user, UUID recipeId) {
        Recipe recipe = getById(recipeId);

        if (!recipe.getFavoriteBy().contains(user)) {
            recipe.getFavoriteBy().add(user);
            user.getFavorites().add(recipe);
            recipeRepository.save(recipe);
        }
    }

    public List<Recipe> getUserFavorites(UUID userId) {
        User user = userService.getById(userId);

        return new ArrayList<>(user.getFavorites());
    }

    public boolean isFavorite(Recipe recipe, User user) {
        return user.getFavorites().contains(recipe);
    }
    public void removeFromFavorites(User user, UUID recipeId) {
        Recipe recipe = getById(recipeId);
        recipe.getFavoriteBy().remove(user);
        user.getFavorites().remove(recipe);
        recipeRepository.save(recipe);
    }


}