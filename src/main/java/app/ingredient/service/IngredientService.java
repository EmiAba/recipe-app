package app.ingredient.service;

import app.ingredient.model.Ingredient;
import app.ingredient.repository.IngredientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Autowired
    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    /**
     * Finds existing ingredient by name or creates a new one
     */
    public Ingredient findOrCreateIngredient(String name) {
        String normalizedName = name.trim().toLowerCase();

        return ingredientRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> {
                    Ingredient newIngredient = Ingredient.builder()
                            .name(normalizedName)
                            .createdOn(LocalDateTime.now())
                            .updatedOn(LocalDateTime.now())
                            .build();

                    Ingredient saved = ingredientRepository.save(newIngredient);
                    log.info("Created new ingredient: {}", normalizedName);
                    return saved;
                });
    }
    /**

     * Finds ingredient by name, returns null if not found
     * Used for shopping list - we don't want to create ingredients for non-food items
     */
    public Ingredient findByNameOptional(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String normalizedName = name.trim().toLowerCase();
        return ingredientRepository.findByNameIgnoreCase(normalizedName).orElse(null);
    }



}