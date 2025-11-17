package app.web.mapper;

import app.category.model.Category;
import app.recipe.model.DifficultyLevel;
import app.recipe.model.Recipe;
import app.web.dto.RecipeUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RecipeMapperUTest {


    @Test
    void testMapRecipeToUpdateRequest() {


        Category category = Category.builder()
                .name("Desserts")
                .build();

        Recipe recipe= Recipe.builder()
                .title("Chocolate Cake")
                .description("Delicious cake")
                .prepTimeMinutes(15)
                .cookTimeMinutes(30)
                .servingSize(8)
                .difficultyLevel(DifficultyLevel.MEDIUM)
                .instructions("Mix and bake")
                .imageUrl("www.image.com")
                .isPublic(true)
                .ingredients("flour, sugar, eggs")
                .calories(100)
                .protein(2.0)
                .carbs(20.0)
                .fat(1.0)
                .fiber(10.0)
                .sugar(5.0)
                .sodium(50.0)
                .categories(Set.of(category))
                .build();


        RecipeUpdateRequest result = RecipeMapper.toUpdateRequest(recipe);

        assertNotNull(result);
        assertEquals("Chocolate Cake", result.getTitle());
        assertEquals("Delicious cake", result.getDescription());
        assertEquals(15, result.getPrepTimeMinutes());
        assertEquals(30, result.getCookTimeMinutes());
        assertEquals(8, result.getServingSize());
        assertEquals(DifficultyLevel.MEDIUM, result.getDifficultyLevel());
        assertEquals("Mix and bake", result.getInstructions());
        assertEquals("www.image.com", result.getImageUrl());
        assertEquals(true, result.getIsPublic());
        assertEquals("flour, sugar, eggs", result.getIngredients());
        assertEquals(100, result.getCalories());
        assertEquals(2.0, result.getProtein());
        assertEquals(20.0, result.getCarbs());
        assertEquals(1.0, result.getFat());
        assertEquals(10.0, result.getFiber());
        assertEquals(5.0, result.getSugar());
        assertEquals(50.0, result.getSodium());


        assertNotNull(result.getCategoryNames());
        assertEquals(1, result.getCategoryNames().size());
        assertTrue(result.getCategoryNames().contains("Desserts"));
    }



    @Test
    void testMapRecipeToUpdateRequest_WithNullCategories() {
        Recipe recipe = Recipe.builder()
                .title("Chocolate Cake")
                .categories(null)
                .build();

        RecipeUpdateRequest result = RecipeMapper.toUpdateRequest(recipe);

        assertNull(result.getCategoryNames());
    }



}
