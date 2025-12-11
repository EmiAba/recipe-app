package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 100, message = "Item name must be less than 100 characters")
    private String name;

    @Size(max = 50, message = "Quantity must be less than 50 characters")
    private String quantity;

    @Size(max = 50, message = "Unit must be less than 50 characters")
    private String unit;

    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;

    @Size(max = 100, message = "Category must be less than 100 characters")
    private String customCategory;
}