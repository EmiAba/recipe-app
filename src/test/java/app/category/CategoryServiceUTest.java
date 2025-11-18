package app.category;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.category.service.CategoryService;
import app.exception.CategoryNotFoundException;
import app.recipe.model.Recipe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceUTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;


    @Test
    void whenGetAllCategories_thenReturnList() {

        Category categoryOne = Category.builder().name("Dessert").build();
        Category categoryTwo = Category.builder().name("Lunch").build();

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(categoryOne, categoryTwo));

        List<Category> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        verify(categoryRepository).findAll();

    }


    @Test
    public void whenFindCategoriesByNames_thenReturnCategory() {

        Category categoryOne = Category.builder().name("Dessert").build();
        Category categoryTwo = Category.builder().name("Lunch").build();

        when(categoryRepository.findByNameIn(Set.of("Dessert", "Lunch")))
                .thenReturn(Set.of(categoryOne, categoryTwo));


        Set<Category> result = categoryService.findCategoriesByNames(Set.of("Dessert", "Lunch"));


        assertThat(result).hasSize(2);
        assertThat(result).contains(categoryOne, categoryTwo);
    }


    @Test
    public void whenGetCategoryRecipeCounts_thenReturnMapWithAllCategoryCounts() {
        Recipe recipeOne = Recipe.builder().build();
        Recipe recipeTwo = Recipe.builder().build();

        Category dessert = Category.builder()
                .name("Dessert")
                .recipes(new HashSet<>(Arrays.asList(recipeOne, recipeTwo)))
                .build();

        Category lunch = Category.builder()
                .name("Lunch")
                .recipes(new HashSet<>(Arrays.asList(recipeOne, recipeTwo)))
                .build();

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(dessert, lunch));

        Map<String, Long> result = categoryService.getCategoryRecipeCounts();

        assertThat(result).hasSize(2);
        assertThat(result.get("Dessert")).isEqualTo(2L);
        assertThat(result.get("Lunch")).isEqualTo(2L);
    }



    @Test
    public void whenFindCategoryByName_andRepositoryReturnsOptionalEmpty_thenThrowException(){
        when(categoryRepository.findByName("Desert")).thenReturn(Optional.empty());

         assertThrows(CategoryNotFoundException.class,  () -> categoryService.findByName("Desert"));

    }



    @Test
    public void whenFindCategoryByName_thenFindCategory() {

        Category categoryOne = Category.builder().name("Dessert").build();

        when(categoryRepository.findByName("Dessert")).thenReturn(Optional.of(categoryOne));


        Category result = categoryService.findByName("Dessert");


        assertEquals("Dessert", result.getName());
    }

}
