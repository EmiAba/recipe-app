package app.mealplanning.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {
    private UUID id;
    private UUID userId;
    private String mealName;
    private String mealType;
    private LocalDate plannedDate;
    private Integer calories;
    private UUID recipeId;

}
