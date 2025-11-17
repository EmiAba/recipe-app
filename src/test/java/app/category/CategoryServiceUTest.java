package app.category;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.category.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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




}
