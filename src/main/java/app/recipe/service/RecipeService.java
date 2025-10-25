package app.recipe.service;

import app.category.model.Category;
import app.exception.RecipeNotFoundException;
import app.user.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
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

    @CacheEvict(cacheNames = "publicRecipes", allEntries = true)
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



      public List<Recipe> getRecentRecipesByUser(User user, int limit) {
        return user.getRecipes().stream()
                .sorted((r1, r2) -> r2.getCreatedOn().compareTo(r1.getCreatedOn()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Recipe> getRecipesByUser(User user) {
        return recipeRepository.findByAuthorOrderByCreatedOnDesc(user);
    }





}