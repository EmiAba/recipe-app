package app.mealplanning.service;

import app.exception.MealPlanningException;
import app.mealplanning.client.MealPlanningClient;
import app.mealplanning.client.dto.ApiMealType;
import app.mealplanning.client.dto.MealPlanRequest;
import app.mealplanning.client.dto.MealPlanResponse;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MealPlanningService {
    private final MealPlanningClient mealPlanningClient;
    private final RecipeService recipeService;

    @Autowired
    public MealPlanningService(MealPlanningClient mealPlanningClient, RecipeService recipeService) {
        this.mealPlanningClient = mealPlanningClient;
        this.recipeService = recipeService;
    }


    public void addRecipeToMealPlan(UUID userId, UUID recipeId, String mealType, LocalDate plannedDate) {


        Recipe recipe = recipeService.getById(recipeId);

        try {
            ApiMealType apiMealType = ApiMealType.valueOf(mealType.toUpperCase());

            MealPlanRequest request = MealPlanRequest.builder()
                    .userId(userId)
                    .mealName(recipe.getTitle())
                    .mealType(apiMealType)
                    .plannedDate(plannedDate)
                    .calories(recipe.getCalories())
                    .recipeId(recipeId)
                    .build();

            mealPlanningClient.addMealPlan(request);

        } catch (FeignException e) {
            log.error("Error adding meal plan: {}", e.getMessage());
            throw new MealPlanningException("Unable to add meal to your plan. Please try again.");
        }
    }


    public List<MealPlanResponse> getWeeklyMealPlans(UUID userId, LocalDate weekStart) {
        try {
            LocalDate actualWeekStart = resolveWeekStart(weekStart);
            return mealPlanningClient.getWeeklyMealPlans(userId, actualWeekStart.toString()).getBody();
        } catch (FeignException e) {
            log.error("Error getting weekly meal plans: {}", e.getMessage());
            return List.of();
        }
    }

    public void deleteMealPlan(UUID mealPlanId, UUID userId) {
        try {
            mealPlanningClient.deleteMealPlan(mealPlanId, userId);
        } catch (FeignException e) {
            log.error("Error deleting meal plan: {}", e.getMessage());
            throw new MealPlanningException("Unable to delete meal. Please try again.");
        }
    }


    public LocalDate resolveWeekStart(LocalDate weekStart) {
        return (weekStart != null)
                ? weekStart
                : LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    }


    /**
     * Проверява кои рецепти от meal plans са налични (не са deleted)
     */

    public Map<UUID, Boolean> getRecipeAvailability(List<MealPlanResponse> mealPlans) {

        Set<UUID> recipeIds = mealPlans.stream()
                .map(MealPlanResponse::getRecipeId)
                .collect(Collectors.toSet());


        List<Recipe> recipes = recipeService.getByIds(recipeIds);


        Map<UUID, Boolean> availability = new HashMap<>();
        for (Recipe recipe : recipes) {
            availability.put(recipe.getId(), !recipe.isDeleted());
        }


        for (UUID recipeId : recipeIds) {
            if (!availability.containsKey(recipeId)) {
                availability.put(recipeId, false);
            }
        }

        return availability;
    }

}





