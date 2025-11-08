package app.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MealPlanAddRequest {

    @NotNull(message = "Please select a recipe")
    private UUID recipeId;


    private String mealType;

    @NotNull(message = "Planned date is required")
    @FutureOrPresent(message = "Planned date cannot be in the past")
    private LocalDate plannedDate;
}