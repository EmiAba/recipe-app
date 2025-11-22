package app.mealplanning;


import app.mealplanning.client.MealPlanningClient;
import app.mealplanning.client.dto.MealPlanResponse;
import app.mealplanning.service.MealPlanningService;
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
    private  MealPlanningClient mealPlanningClient;

    @InjectMocks
    private MealPlanningService mealPlanningService;

    @Test
    void addRecipeToMealPlan_shouldCallFeignClient() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        mealPlanningService.addRecipeToMealPlan(
                userId, recipeId, "Pasta", 500, "LUNCH", LocalDate.now()
        );

        verify(mealPlanningClient, times(1)).addMealPlan(any());
    }


    @Test
    void getWeeklyMealPlans_shouldReturnMealPlans() {
        UUID userId = UUID.randomUUID();
        LocalDate weekStart = LocalDate.now();
        MealPlanResponse mockMeal = new MealPlanResponse();
        List<MealPlanResponse> mockResponse = List.of(mockMeal);

        when(mealPlanningClient.getWeeklyMealPlans(userId, weekStart))
                .thenReturn(ResponseEntity.ok(mockResponse));

        List<MealPlanResponse> result = mealPlanningService.getWeeklyMealPlans(userId, weekStart);

        assertThat(result).hasSize(1);
        verify(mealPlanningClient, times(1)).getWeeklyMealPlans(userId, weekStart);
    }


    @Test
    void deleteMealPlan_shouldCallFeignClient() {
        UUID mealPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mealPlanningService.deleteMealPlan(mealPlanId, userId);

        verify(mealPlanningClient, times(1)).deleteMealPlan(mealPlanId, userId);
    }
}
