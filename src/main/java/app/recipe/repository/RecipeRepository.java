package app.recipe.repository;

import app.recipe.model.Recipe;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    List<Recipe> findByAuthorAndDeletedFalseOrderByCreatedOnDesc(User user);

    List<Recipe> findByIsPublicTrue();

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.categories " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r IN (SELECT f FROM User u JOIN u.favorites f WHERE u.id = :userId) " +
            "AND r.deleted = false " +
            "ORDER BY r.createdOn DESC")
    List<Recipe> findUserFavoritesWithCategories(@Param("userId") UUID userId);
}

