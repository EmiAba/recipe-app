package app.category.service;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;



@Slf4j
@Component
public class CategoryInit implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryInit(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (categoryRepository.count() == 0) {
            createCategory("Breakfast", "Breakfast and morning meals");
            createCategory("Lunch", "Lunch meals");
            createCategory("Dinner", "Dinner and main courses");
            createCategory("Appetizer", "Appetizers and starters");
            createCategory("Dessert", "Desserts and sweets");
            createCategory("Snack", "Snacks and quick bites");
            createCategory("Drink", "Drinks and cocktails");
            createCategory("Soup", "Soups");
            createCategory("Salad", "Fresh Salads");

            log.info("- Categories initialized successfully!");
        } else {
            log.info("* Categories already exist - skipping initialization");
        }
    }

    private void createCategory(String name, String description) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();

        categoryRepository.save(category);
        log.info("Created category: {}", name);
    }
}