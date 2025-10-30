package app.comment.repository;

import app.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.author LEFT JOIN FETCH c.recipe WHERE c.id = :id")
    Optional<Comment> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.author WHERE c.recipe.id = :recipeId ORDER BY c.createdOn DESC")
    List<Comment> findByRecipeIdWithAuthor(@Param("recipeId") UUID recipeId);


    @Query("SELECT COUNT(DISTINCT c.author.id) FROM Comment c")
    long countDistinctAuthors();


}
