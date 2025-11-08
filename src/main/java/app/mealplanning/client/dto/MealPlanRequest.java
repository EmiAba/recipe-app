package app.mealplanning.client.dto;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class MealPlanRequest {
    private UUID userId;
    private String mealName;
    private ApiMealType mealType;
    private LocalDate plannedDate;
    private Integer calories;
    private UUID recipeId;
}