package app.shoppinglist.service;

import app.exception.ShoppingListItemNotFoundException;
import app.exception.UnauthorizedAccessException;
import app.ingredient.model.Ingredient;
import app.ingredient.service.IngredientService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.shoppinglist.model.ShoppingListItem;
import app.shoppinglist.repository.ShoppingListItemRepository;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ShoppingListItemService {

    private final ShoppingListItemRepository shoppingListRepository;
    private final RecipeService recipeService;
    private final IngredientService ingredientService;

    @Autowired
    public ShoppingListItemService(ShoppingListItemRepository shoppingListRepository,
                                   RecipeService recipeService,
                                   IngredientService ingredientService) {
        this.shoppingListRepository = shoppingListRepository;
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
    }


    public ShoppingListItem addItem(User user, String name, String quantity,
                                    String unit, String notes, String customCategory) {


        Ingredient ingredient = ingredientService.findByNameOptional(name);

        ShoppingListItem item = ShoppingListItem.builder()
                .name(name.trim())
                .quantity(quantity != null ? quantity.trim() : null)
                .unit(unit != null ? unit.trim() : null)
                .notes(notes != null ? notes.trim() : null)
                .customCategory(customCategory != null ? customCategory.trim() : null)
                .ingredient(ingredient)
                .recipe(null)
                .completed(false)
                .user(user)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        ShoppingListItem savedItem = shoppingListRepository.save(item);
        log.info("User [{}] added item [{}] to shopping list", user.getUsername(), name);
        return savedItem;
    }


    public void addIngredientsFromRecipe(User user, UUID recipeId) {
        Recipe recipe = recipeService.getById(recipeId);

        if (recipe.getRecipeIngredients().isEmpty()) {
            log.warn("Recipe [{}] has no ingredients to add", recipe.getTitle());
            return;
        }

        recipe.getRecipeIngredients().forEach(recipeIngredient -> {
            ShoppingListItem item = ShoppingListItem.builder()
                    .name(recipeIngredient.getIngredient().getName())
                    .quantity(recipeIngredient.getQuantity())
                    .unit(recipeIngredient.getUnit())
                    .notes(recipeIngredient.getNotes())
                    .ingredient(recipeIngredient.getIngredient())
                    .recipe(recipe)
                    .customCategory("From Recipe") // или null
                    .completed(false)
                    .user(user)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();

            shoppingListRepository.save(item);
        });

        log.info("User [{}] added {} ingredients from recipe [{}] to shopping list",
                user.getUsername(), recipe.getRecipeIngredients().size(), recipe.getTitle());
    }



    public ShoppingListItem updateItem(UUID itemId, User user, String name,
                                       String quantity, String unit, String notes,
                                       String customCategory) {
        ShoppingListItem item = getById(itemId);
        validateOwnership(item, user);


        Ingredient ingredient = null;
        if (!item.getName().equalsIgnoreCase(name)) {
            ingredient = ingredientService.findByNameOptional(name);
        } else {
            ingredient = item.getIngredient();
        }

        item.setName(name.trim());
        item.setQuantity(quantity != null ? quantity.trim() : null);
        item.setUnit(unit != null ? unit.trim() : null);
        item.setNotes(notes != null ? notes.trim() : null);
        item.setCustomCategory(customCategory != null ? customCategory.trim() : null);
        item.setIngredient(ingredient);
        item.setUpdatedOn(LocalDateTime.now());

        ShoppingListItem updatedItem = shoppingListRepository.save(item);
        log.info("User [{}] updated shopping list item [{}]", user.getUsername(), itemId);
        return updatedItem;
    }


    public ShoppingListItem toggleItemCompletion(UUID itemId, User user) {
        ShoppingListItem item = getById(itemId);
        validateOwnership(item, user);

        item.setCompleted(!item.isCompleted());
        item.setUpdatedOn(LocalDateTime.now());

        ShoppingListItem updatedItem = shoppingListRepository.save(item);
        log.info("User [{}] {} item [{}]", user.getUsername(),
                item.isCompleted() ? "completed" : "uncompleted", item.getName());
        return updatedItem;
    }


    public void markAllCompleted(User user) {
        List<ShoppingListItem> userItems = getUserShoppingList(user);

        userItems.forEach(item -> {
            if (!item.isCompleted()) {
                item.setCompleted(true);
                item.setUpdatedOn(LocalDateTime.now());
            }
        });

        shoppingListRepository.saveAll(userItems);
        log.info("User [{}] marked all shopping list items as completed", user.getUsername());
    }




    public void deleteItem(UUID itemId, User user) {
        ShoppingListItem item = getById(itemId);
        validateOwnership(item, user);

        shoppingListRepository.delete(item);
        log.info("User [{}] deleted shopping list item [{}]", user.getUsername(), item.getName());
    }


    public void removeCompletedItems(User user) {
        List<ShoppingListItem> completedItems =
                shoppingListRepository.findByUserAndCompletedOrderByCreatedOnDesc(user, true);

        shoppingListRepository.deleteAll(completedItems);
        log.info("User [{}] removed {} completed items from shopping list",
                user.getUsername(), completedItems.size());
    }


    public void clearAllItems(User user) {
        List<ShoppingListItem> userItems = getUserShoppingList(user);
        shoppingListRepository.deleteAll(userItems);
        log.info("User [{}] cleared entire shopping list ({} items)",
                user.getUsername(), userItems.size());
    }

    // ========== READ Operations ==========


    public ShoppingListItem getById(UUID id) {
        return shoppingListRepository.findById(id)
                .orElseThrow(() -> new ShoppingListItemNotFoundException(
                        "Shopping list item with ID [%s] not found".formatted(id)));
    }


    public ShoppingListItem getItemForEdit(UUID itemId, User currentUser) {
        ShoppingListItem item = getById(itemId);
        validateOwnership(item, currentUser);
        return item;
    }


    public List<ShoppingListItem> getUserShoppingList(User user) {
        return shoppingListRepository.findByUserOrderByCreatedOnDesc(user);
    }

    /**
     * Само completed items
     */
    public List<ShoppingListItem> getCompletedItems(User user) {
        return shoppingListRepository.findByUserAndCompletedOrderByCreatedOnDesc(user, true);
    }


    public Map<String, List<ShoppingListItem>> getCategorizedItems(User user) {
        List<ShoppingListItem> allItems = getUserShoppingList(user);
        return allItems.stream()
                .collect(Collectors.groupingBy(this::getDisplayCategoryName));
    }

    // ========== Statistics ==========

    /**
     * Общ брой items
     */
    public long getTotalItemsCount(User user) {
        return shoppingListRepository.countByUser(user);
    }

    /**
     * Брой completed items
     */
    public long getCompletedItemsCount(User user) {
        return shoppingListRepository.countByUserAndCompleted(user, true);
    }

    /**
     * Колко различни рецепти има в shopping list-a
     */
    public long getRecipeCount(User user) {
        return getUserShoppingList(user).stream()
                .filter(item -> item.getRecipe() != null)
                .map(item -> item.getRecipe().getId())
                .distinct()
                .count();
    }

    /**
     * Процент завършени items
     */
    public int getCompletionPercentage(User user) {
        long total = getTotalItemsCount(user);
        if (total == 0) {
            return 0;
        }
        long completed = getCompletedItemsCount(user);
        return (int) Math.round((double) completed / total * 100);
    }

    // ========== Helper Methods ==========

    /**
     * Взимане на display име на категория
     */
    public String getDisplayCategoryName(ShoppingListItem item) {
        if (item.getCustomCategory() != null && !item.getCustomCategory().trim().isEmpty()) {
            return item.getCustomCategory();
        }
        return "Uncategorized";
    }

    /**
     * Проверка дали user-a е owner на item-a
     */
    private void validateOwnership(ShoppingListItem item, User user) {
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException(
                    "You can only modify your own shopping list items");
        }
    }
}