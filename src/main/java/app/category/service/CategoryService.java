package app.category.service;


import app.exception.DomainException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import app.category.model.Category;
import app.category.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable("categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Set<Category> findCategoriesByNames(Set<String> names) {
        log.info("Looking for categories: {}", names);

        Set<Category> categories = categoryRepository.findByNameIn(names);

        log.info("Found {} categories out of {} requested", categories.size(), names.size());
        categories.forEach(cat -> log.info("Found category: {}", cat.getName()));

        return categories;
    }

    public Map<String, Long> getCategoryRecipeCounts() {
        List<Category> categories = getAllCategories();
        return categories.stream()
                .collect(Collectors.toMap(
                        Category::getName,
                        category -> (long) category.getRecipes().size()
                ));

    }



    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new DomainException("Category '" + name + "' not found"));
    }









}