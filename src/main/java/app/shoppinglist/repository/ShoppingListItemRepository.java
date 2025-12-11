package app.shoppinglist.repository;

import app.recipe.model.Recipe;
import app.shoppinglist.model.ShoppingListItem;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, UUID> {


    List<ShoppingListItem> findByUserOrderByCreatedOnDesc(User user);


    List<ShoppingListItem> findByUserAndCompletedOrderByCreatedOnDesc(User user, boolean completed);


    long countByUser(User user);


    long countByUserAndCompleted(User user, boolean completed);


    List<ShoppingListItem> findByUserAndCustomCategoryOrderByCreatedOnDesc(User user, String category);


    List<ShoppingListItem> findByUserAndRecipeOrderByCreatedOnDesc(User user, Recipe recipe);
}