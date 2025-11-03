package app.recipe.repository;

import app.recipe.model.Recipe;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    List<Recipe> findByAuthorAndDeletedFalseOrderByCreatedOnDesc(User user);

    List<Recipe> findByIsPublicTrue();
}

