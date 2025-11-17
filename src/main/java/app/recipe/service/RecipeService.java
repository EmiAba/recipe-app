package app.recipe.service;

import app.category.model.Category;
import app.exception.RecipeNotFoundException;
import app.exception.UnauthorizedAccessException;
import app.user.service.UserService;
import app.web.dto.RecipeUpdateRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.recipe.model.Recipe;
import app.recipe.repository.RecipeRepository;
import app.user.model.User;
import app.category.service.CategoryService;
import app.web.dto.RecipeCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryService categoryService;

    private final UserService userService;



    public RecipeService(RecipeRepository recipeRepository, CategoryService categoryService,
                         UserService userService) {
        this.recipeRepository = recipeRepository;
        this.categoryService = categoryService;


        this.userService = userService;

    }


    public Recipe createRecipe(RecipeCreateRequest recipeCreateRequest, User author) {
        Set<Category> categories = categoryService.findCategoriesByNames(recipeCreateRequest.getCategoryNames());

        Recipe recipe = Recipe.builder()
                .title(recipeCreateRequest.getTitle())
                .description(recipeCreateRequest.getDescription())
                .instructions(recipeCreateRequest.getInstructions())
                .prepTimeMinutes(recipeCreateRequest.getPrepTimeMinutes())
                .cookTimeMinutes(recipeCreateRequest.getCookTimeMinutes())
                .servingSize(recipeCreateRequest.getServingSize())
                .difficultyLevel(recipeCreateRequest.getDifficultyLevel())
                .imageUrl(recipeCreateRequest.getImageUrl())
                .isPublic(recipeCreateRequest.getIsPublic())
                .author(author)
                .ingredients(recipeCreateRequest.getIngredients())
                .calories(recipeCreateRequest.getCalories())
                .protein(recipeCreateRequest.getProtein())
                .carbs(recipeCreateRequest.getCarbs())
                .fat(recipeCreateRequest.getFat())
                .fiber(recipeCreateRequest.getFiber())
                .sugar(recipeCreateRequest.getSugar())
                .sodium(recipeCreateRequest.getSodium())
                .categories(categories)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return recipeRepository.save(recipe);
    }

    public Recipe getById(UUID id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe with id[%s] does not exist.".formatted(id)));
    }


    public Recipe updateRecipe(UUID recipeId, RecipeUpdateRequest recipeUpdateRequest, User currentUser) {
        Recipe recipe = getById(recipeId);

        if (!recipe.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only edit your own recipes.");
        }

        Set<Category> categories = categoryService.findCategoriesByNames(recipeUpdateRequest.getCategoryNames());

        recipe.setTitle(recipeUpdateRequest.getTitle());
        recipe.setDescription(recipeUpdateRequest.getDescription());
        recipe.setInstructions(recipeUpdateRequest.getInstructions());
        recipe.setPrepTimeMinutes(recipeUpdateRequest.getPrepTimeMinutes());
        recipe.setCookTimeMinutes(recipeUpdateRequest.getCookTimeMinutes());
        recipe.setServingSize(recipeUpdateRequest.getServingSize());
        recipe.setDifficultyLevel(recipeUpdateRequest.getDifficultyLevel());
        recipe.setImageUrl(recipeUpdateRequest.getImageUrl());
        recipe.setPublic(recipeUpdateRequest.getIsPublic());
        recipe.setIngredients(recipeUpdateRequest.getIngredients());
        recipe.setCalories(recipeUpdateRequest.getCalories());
        recipe.setProtein(recipeUpdateRequest.getProtein());
        recipe.setCarbs(recipeUpdateRequest.getCarbs());
        recipe.setFat(recipeUpdateRequest.getFat());
        recipe.setFiber(recipeUpdateRequest.getFiber());
        recipe.setSugar(recipeUpdateRequest.getSugar());
        recipe.setSodium(recipeUpdateRequest.getSodium());
        recipe.getCategories().clear();
        recipe.getCategories().addAll(categories);
        recipe.setUpdatedOn(LocalDateTime.now());

        return recipeRepository.save(recipe);
    }


    public void deleteRecipe(UUID recipeId, User currentUser) {
        Recipe recipe = getById(recipeId);

        if (!recipe.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only delete your own recipes.");
        }

        recipe.setDeleted(true);
        recipeRepository.save(recipe);
    }




    public boolean isAuthor(Recipe recipe, User user) {
        return recipe.getAuthor().getId().equals(user.getId());
    }



    public List<Recipe> getRecipesByUser(User user, Integer limit) {
        List<Recipe> recipes = recipeRepository.findByAuthorAndDeletedFalseOrderByCreatedOnDesc(user);

        if (limit != null && limit > 0) {
            return recipes.stream().limit(limit).toList();
        }

        return recipes;
    }

    public List<Recipe> getPublicRecipes(Category category) {
        return category.getRecipes().stream()
                .filter(Recipe::isPublic)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "userFavorites", key = "#user.id")
    public void addToFavorites(User user, UUID recipeId) {
        Recipe recipe = getById(recipeId);
        user.getFavorites().add(recipe);
        recipeRepository.save(recipe);
    }


    @Cacheable(value = "userFavorites", key = "#userId")
    public List<Recipe> getUserFavorites(UUID userId) {
        User user = userService.getById(userId);

        return user.getFavorites().stream()
                .sorted((r1, r2) -> r2.getCreatedOn().compareTo(r1.getCreatedOn()))
                .collect(Collectors.toList());
    }

    public boolean isFavorite(Recipe recipe, User user) {
        return user.getFavorites().contains(recipe);
    }

    @CacheEvict(value = "userFavorites", key = "#user.id")
    public void removeFromFavorites(User user, UUID recipeId) {
        Recipe recipe = getById(recipeId);
        user.getFavorites().remove(recipe);
        recipeRepository.save(recipe);
    }


    public byte[] generateRecipePdf(UUID recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(output);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);


        document.add(new Paragraph(recipe.getTitle())
                .setFontSize(24)
                .setBold());


        document.add(new Paragraph("by " + recipe.getAuthor().getUsername())
                .setFontSize(12)
                .setItalic());

        document.add(new Paragraph(" "));


        StringBuilder info = new StringBuilder();
        if (recipe.getPrepTimeMinutes() != null) {
            info.append("Prep: ").append(recipe.getPrepTimeMinutes()).append(" min  |  ");
        }
        if (recipe.getCookTimeMinutes() != null) {
            info.append("Cook: ").append(recipe.getCookTimeMinutes()).append(" min  |  ");
        }
        if (recipe.getServingSize() != null) {
            info.append("Servings: ").append(recipe.getServingSize()).append("  |  ");
        }
        if (recipe.getDifficultyLevel() != null) {
            info.append("Difficulty: ").append(recipe.getDifficultyLevel());
        }
        document.add(new Paragraph(info.toString()).setFontSize(11));

        document.add(new Paragraph(" "));


        if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            document.add(new Paragraph("Description").setFontSize(16).setBold());
            document.add(new Paragraph(recipe.getDescription()));
            document.add(new Paragraph(" "));
        }


        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            document.add(new Paragraph("Ingredients").setFontSize(16).setBold());
            document.add(new Paragraph(recipe.getIngredients()));
            document.add(new Paragraph(" "));
        }


        document.add(new Paragraph("Instructions").setFontSize(16).setBold());
        document.add(new Paragraph(recipe.getInstructions()));
        document.add(new Paragraph(" "));


        if (recipe.getCalories() != null) {
            document.add(new Paragraph("Nutrition Facts").setFontSize(16).setBold());
            StringBuilder nutrition = new StringBuilder();
            if (recipe.getCalories() != null) nutrition.append("Calories: ").append(recipe.getCalories()).append(" kcal\n");
            if (recipe.getProtein() != null) nutrition.append("Protein: ").append(recipe.getProtein()).append(" g\n");
            if (recipe.getCarbs() != null) nutrition.append("Carbs: ").append(recipe.getCarbs()).append(" g\n");
            if (recipe.getFat() != null) nutrition.append("Fat: ").append(recipe.getFat()).append(" g\n");
            if (recipe.getFiber() != null) nutrition.append("Fiber: ").append(recipe.getFiber()).append(" g\n");
            if (recipe.getSugar() != null) nutrition.append("Sugar: ").append(recipe.getSugar()).append(" g\n");
            if (recipe.getSodium() != null) nutrition.append("Sodium: ").append(recipe.getSodium()).append(" mg\n");
            document.add(new Paragraph(nutrition.toString()));
        }

        document.close();
        return output.toByteArray();
    }
}