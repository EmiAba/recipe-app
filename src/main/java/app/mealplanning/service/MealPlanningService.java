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
import java.util.List;
import java.util.UUID;

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
            return mealPlanningClient.getWeeklyMealPlans(userId, actualWeekStart).getBody();
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
}





