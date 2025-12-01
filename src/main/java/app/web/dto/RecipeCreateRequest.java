package app.web.dto;

import app.recipe.model.DifficultyLevel;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeCreateRequest {

    @NotBlank(message = "Recipe title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Preparation time is required")
    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    private Integer prepTimeMinutes;

    @NotNull(message = "Cooking time is required")
    @Min(value = 0, message = "Cooking time cannot be negative")
    private Integer cookTimeMinutes;

    @NotNull(message = "Serving size is required")
    @Min(value = 1, message = "Serving size must be at least 1")
    private Integer servingSize;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    @NotBlank(message = "Instructions are required")
    private String instructions;

    @URL
    @Size(max = 500)
    private String imageUrl;

    private Boolean isPublic = true;

    @NotBlank(message = "Ingredients are required")
    private String ingredients;

    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    private Double sugar;
    private Double sodium;

    private Set<String> categoryNames;
}