package app.web.mapper;

import app.recipe.model.Recipe;
import app.category.model.Category;
import app.web.dto.RecipeCreateRequest;
import app.web.dto.RecipeIngredientRequest;
import app.web.dto.RecipeUpdateRequest;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RecipeMapper {

    public static RecipeUpdateRequest toUpdateRequest(Recipe recipe) {
        List<RecipeIngredientRequest> ingredientRequests = recipe.getRecipeIngredients()
                .stream()
                .map(ri -> RecipeIngredientRequest.builder()
                        .ingredientName(ri.getIngredient().getName())
                        .quantity(ri.getQuantity())
                        .unit(ri.getUnit())
                        .notes(ri.getNotes())
                        .build())
                .collect(Collectors.toList());

        return RecipeUpdateRequest.builder()
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .instructions(recipe.getInstructions())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .servingSize(recipe.getServingSize())
                .difficultyLevel(recipe.getDifficultyLevel())
                .imageUrl(recipe.getImageUrl())
                .isPublic(recipe.isPublic())
                .calories(recipe.getCalories())
                .protein(recipe.getProtein())
                .carbs(recipe.getCarbs())
                .fat(recipe.getFat())
                .fiber(recipe.getFiber())
                .sugar(recipe.getSugar())
                .sodium(recipe.getSodium())
                .categoryNames(recipe.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toSet()))
                .recipeIngredients(ingredientRequests)
                .build();
    }

    public static RecipeCreateRequest createEmptyRecipeRequest(int ingredientCount) {
        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setRecipeIngredients(createEmptyIngredientsList(ingredientCount));
        return request;
    }

    public static void ensureMinimumIngredients(RecipeCreateRequest request, int minCount) {
        if (request.getRecipeIngredients() == null || request.getRecipeIngredients().isEmpty()) {
            request.setRecipeIngredients(createEmptyIngredientsList(minCount));
        }
    }

    public static void ensureMinimumIngredients(RecipeUpdateRequest request, int minCount) {
        if (request.getRecipeIngredients() == null || request.getRecipeIngredients().isEmpty()) {
            request.setRecipeIngredients(createEmptyIngredientsList(minCount));
        }
    }

    private static List<RecipeIngredientRequest> createEmptyIngredientsList(int count) {
        List<RecipeIngredientRequest> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new RecipeIngredientRequest());
        }
        return list;
    }
}