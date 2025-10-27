package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.exception.CommentNotFoundException;
import app.exception.UnauthorizedAccessException;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.web.dto.CommentCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecipeService recipeService;



    @Autowired
    public CommentService(CommentRepository commentRepository,
                          RecipeService recipeService ) {
        this.commentRepository = commentRepository;
        this.recipeService = recipeService;


    }


    public Comment createComment(CommentCreateRequest commentCreateRequest, UUID recipeId, User author) {
        Recipe recipe = recipeService.getById(recipeId);

        Comment comment = Comment.builder()
                .content(commentCreateRequest.getContent())
                .rating(commentCreateRequest.getRating())
                .author(author)
                .recipe(recipe)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);



        log.info("User [{}] added comment to recipe [{}] with rating [{}]",
                author.getUsername(), recipe.getTitle(), commentCreateRequest.getRating());

        return savedComment;
    }


    public List<Comment> getCommentsByRecipe(UUID recipeId) {
        return commentRepository.findByRecipeIdWithAuthor(recipeId);
    }


    public Double getAverageRatingForRecipe(UUID recipeId) {
        List<Comment> comments = getCommentsByRecipe(recipeId);
        if (comments.isEmpty()) {
            return null;
        }

        double sum = comments.stream()
                .mapToInt(Comment::getRating)
                .sum();

        return sum / comments.size();
    }


    public int getTotalRatingsForRecipe(UUID recipeId) {
        List<Comment> comments = getCommentsByRecipe(recipeId);
        return comments.size();
    }

    public Comment getById(UUID id) {
        return commentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment with id [%s] does not exist.".formatted(id)));
    }


    public void deleteComment(UUID commentId, User currentUser) {
        Comment comment = getById(commentId);


        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only delete your own comments.");
        }

        commentRepository.delete(comment);

        log.info("User [{}] deleted comment [{}]", currentUser.getUsername(), commentId);
    }



}