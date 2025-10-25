package app.web.mapper;

import app.category.model.Category;
import app.recipe.model.Recipe;
import app.web.dto.RecipeUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RecipeMapper {


    public static RecipeUpdateRequest toUpdateRequest(Recipe recipe) {
        RecipeUpdateRequest request = RecipeUpdateRequest.builder()
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .servingSize(recipe.getServingSize())
                .difficultyLevel(recipe.getDifficultyLevel())
                .instructions(recipe.getInstructions())
                .imageUrl(recipe.getImageUrl())
                .isPublic(recipe.isPublic())
                .ingredients(recipe.getIngredients())
                .calories(recipe.getCalories())
                .protein(recipe.getProtein())
                .carbs(recipe.getCarbs())
                .fat(recipe.getFat())
                .fiber(recipe.getFiber())
                .sugar(recipe.getSugar())
                .sodium(recipe.getSodium())
                .build();


        if (recipe.getCategories() != null) {
            request.setCategoryNames(recipe.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet()));
        }

        return request;
    }

 }