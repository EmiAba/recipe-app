package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredientRequest {


    private String ingredientName; // "Tomato", "Chicken"


    private String quantity; // "2", "1/2", "3.5"


    private String unit; // "cup", "tbsp", "g"

    private String notes; // Optional - "chopped", "diced"
}
