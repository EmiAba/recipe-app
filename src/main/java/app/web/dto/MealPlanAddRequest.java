package app.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealPlanAddRequest {

    @NotNull(message = "Please select a recipe")
    private UUID recipeId;


    @NotNull(message = "Meal type is required")
    private String mealType;

    @NotNull(message = "Planned date is required")
    @FutureOrPresent(message = "Planned date cannot be in the past")
    private LocalDate plannedDate;
}