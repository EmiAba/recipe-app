package app.mealplanning.service;

import app.mealplanning.client.MealPlanningClient;
import app.mealplanning.client.dto.ApiMealType;
import app.mealplanning.client.dto.MealPlanRequest;
import app.mealplanning.client.dto.MealPlanResponse;
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

    @Autowired
    public MealPlanningService(MealPlanningClient mealPlanningClient) {
        this.mealPlanningClient = mealPlanningClient;
    }


    public void addRecipeToMealPlan(UUID userId, UUID recipeId, String recipeName,
                                    Integer calories, String mealType, LocalDate plannedDate) {


        try {
            ApiMealType apiMealType = ApiMealType.valueOf(mealType.toUpperCase());

            MealPlanRequest request = MealPlanRequest.builder()
                    .userId(userId)
                    .mealName(recipeName)
                    .mealType(apiMealType)
                    .plannedDate(plannedDate)
                    .calories(calories)
                    .recipeId(recipeId)
                    .build();

            mealPlanningClient.addMealPlan(request);


        } catch (FeignException e) {
            log.error("Error adding meal plan: {}", e.getMessage());
            throw new IllegalArgumentException("Unable to add meal to your plan. Please try again.");
        }
    }

    public List<MealPlanResponse> getWeeklyMealPlans(UUID userId, LocalDate weekStart) {
        try {
            return mealPlanningClient.getWeeklyMealPlans(userId, weekStart).getBody();

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
            throw new IllegalArgumentException("Unable to delete meal. Please try again.");
        }
    }

}





