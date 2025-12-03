package app.mealplanning;

import app.mealplanning.client.MealPlanningClient;
import app.mealplanning.client.dto.MealPlanResponse;
import app.mealplanning.service.MealPlanningService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MealPlanningServiceUTest {

    @Mock
    private MealPlanningClient mealPlanningClient;

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private MealPlanningService mealPlanningService;

    @Test
    void addRecipeToMealPlan_shouldCallFeignClient() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();


        Recipe recipe1 = Recipe.builder()
                .title("Pasta")
                .calories(500)
                .build();



        when(recipeService.getById(recipeId)).thenReturn(recipe1);


        mealPlanningService.addRecipeToMealPlan(
                userId, recipeId, "LUNCH", LocalDate.now()
        );


        verify(recipeService, times(1)).getById(recipeId);

        verify(mealPlanningClient, times(1)).addMealPlan(any());
    }

    @Test
    void getWeeklyMealPlans_shouldReturnMealPlans() {
        UUID userId = UUID.randomUUID();

        LocalDate weekStart = LocalDate.now();
        String weekStartString = weekStart.toString();

        MealPlanResponse mockMeal = new MealPlanResponse();
        List<MealPlanResponse> mockResponse = List.of(mockMeal);

        when(mealPlanningClient.getWeeklyMealPlans(userId, weekStartString))
                .thenReturn(ResponseEntity.ok(mockResponse));


        List<MealPlanResponse> result =
                mealPlanningService.getWeeklyMealPlans(userId, weekStart);


        assertThat(result).hasSize(1);

        verify(mealPlanningClient, times(1))
                .getWeeklyMealPlans(userId, weekStartString);
    }


    @Test
    void deleteMealPlan_shouldCallFeignClient() {
        UUID mealPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mealPlanningService.deleteMealPlan(mealPlanId, userId);

        verify(mealPlanningClient, times(1)).deleteMealPlan(mealPlanId, userId);
    }

    @Test
    void resolveWeekStart_shouldReturnProvidedDate() {
        LocalDate providedDate = LocalDate.of(2024, 12, 5);

        LocalDate result = mealPlanningService.resolveWeekStart(providedDate);

        assertThat(result).isEqualTo(providedDate);
    }

    @Test
    void resolveWeekStart_shouldReturnMondayWhenNull() {
        LocalDate result = mealPlanningService.resolveWeekStart(null);

        assertThat(result.getDayOfWeek()).isEqualTo(java.time.DayOfWeek.MONDAY);
    }
}