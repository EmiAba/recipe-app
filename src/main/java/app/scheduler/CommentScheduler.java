package app.scheduler;

import app.comment.repository.CommentRepository;
import app.recipe.repository.RecipeRepository;
import app.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class CommentScheduler {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public CommentScheduler(RecipeRepository recipeRepository,
                            UserRepository userRepository,
                            CommentRepository commentRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }


     @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(readOnly = true)
    public void dailyCommentReport() {
        log.info("Generating daily comment report");

        long totalUsers = userRepository.count();
        long totalComments = commentRepository.count();


        long usersWithComments = commentRepository.countDistinctAuthors();

        log.info("Comment report: {} total comments from {} active users out of {} total users",
                totalComments, usersWithComments, totalUsers);
    }


   @Scheduled(fixedRate = 1800000)
    @Transactional(readOnly = true)
    public void recipeCommentAnalysis() {
        log.info("Analyzing recipe comment coverage");

        long totalPublicRecipes = recipeRepository.findByIsPublicTrue().size();
        long totalComments = commentRepository.count();


        long recipesWithComments = commentRepository.findAll().stream()
                .map(comment -> comment.getRecipe().getId())
                .distinct()
                .count();

        log.info("Recipe analysis: {}/{} recipes have comments, {} total comments",
                recipesWithComments, totalPublicRecipes, totalComments);
    }
}