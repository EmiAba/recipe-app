package app.web;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentCreateRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final RecipeService recipeService;


    @Autowired
    public CommentController(CommentService commentService, UserService userService, RecipeService recipeService
    ) {
        this.commentService = commentService;
        this.userService = userService;
        this.recipeService = recipeService;

    }

    @PostMapping("/recipe/{recipeId}")
    public ModelAndView addComment(@PathVariable UUID recipeId,
                                   @Valid CommentCreateRequest commentCreateRequest,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());

        if (bindingResult.hasErrors()) {
            Recipe recipe = recipeService.getById(recipeId);

            boolean isAuthor = recipeService.isAuthor(recipe, user);
            boolean isFavorite = recipeService.isFavorite(recipe, user);

            Double averageRating = commentService.getAverageRatingForRecipe(recipeId);
            int totalRatings = commentService.getTotalRatingsForRecipe(recipeId);

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("recipe-detail");
            modelAndView.addObject("recipe", recipe);
            modelAndView.addObject("isAuthor", isAuthor);
            modelAndView.addObject("isFavorite", isFavorite);
            modelAndView.addObject("user", user);
            modelAndView.addObject("comments", commentService.getCommentsByRecipe(recipeId));
            modelAndView.addObject("averageRating", averageRating);
            modelAndView.addObject("totalRatings", totalRatings);
            modelAndView.addObject("commentCreateRequest", commentCreateRequest);

            return modelAndView;
        }

        commentService.createComment(commentCreateRequest, recipeId, user);
        return new ModelAndView("redirect:/recipes/" + recipeId + "#comments");
    }



    @DeleteMapping("/{commentId}/delete")
    public ModelAndView deleteComment(@PathVariable UUID commentId,
                                      @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {

        Comment comment = commentService.getById(commentId);
        UUID recipeId = comment.getRecipe().getId();

        User currentUser = userService.getById(authenticationMethadata.getUserId());
        commentService.deleteComment(commentId, currentUser);

        return new ModelAndView("redirect:/recipes/" + recipeId + "#comments");
    }

}