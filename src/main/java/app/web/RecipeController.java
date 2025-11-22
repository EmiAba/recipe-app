package app.web;


import app.comment.service.CommentService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.category.service.CategoryService;
import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentCreateRequest;
import app.web.dto.RecipeCreateRequest;
import app.web.dto.RecipeUpdateRequest;
import app.web.mapper.RecipeMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final CommentService commentService;




    @Autowired
    public RecipeController(RecipeService recipeService, CategoryService categoryService,
                            UserService userService,  CommentService commentService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @GetMapping("/add")
    public ModelAndView getRecipeAddPage(@AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("recipe-form");
        modelAndView.addObject("recipeCreateRequest",  new RecipeCreateRequest());
        modelAndView.addObject("categories", categoryService.getAllCategories());
        modelAndView.addObject("user", user);
        return modelAndView;
    }



    @PostMapping("/save")
    public ModelAndView saveRecipe(
            @Valid RecipeCreateRequest recipeCreateRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(authenticationMethadata.getUserId());

            ModelAndView modelAndView = new ModelAndView("recipe-form");
            modelAndView.addObject("categories", categoryService.getAllCategories());
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        User user = userService.getById(authenticationMethadata.getUserId());
        Recipe createdRecipe = recipeService.createRecipe(recipeCreateRequest, user);
        return new ModelAndView("redirect:/recipes/" + createdRecipe.getId());
    }


    @GetMapping("/{recipeId}")
    public ModelAndView viewRecipe(@PathVariable UUID recipeId,
                                   @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());
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
        modelAndView.addObject("comments", commentService.getCommentsByRecipe(recipe.getId()));
        modelAndView.addObject("averageRating", averageRating);
        modelAndView.addObject("totalRatings", totalRatings);
        modelAndView.addObject("commentCreateRequest", new CommentCreateRequest());

        return modelAndView;
    }


    @GetMapping("/mine")
    public ModelAndView getMyRecipes(@AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("recipe-list");
        modelAndView.addObject("recipes", recipeService.getRecipesByUser(user, null));

        modelAndView.addObject("user", user);
        return modelAndView;
    }



    @GetMapping("/edit/{recipeId}")
    public ModelAndView getRecipeEditPage(@PathVariable UUID recipeId,
                                          @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());
        Recipe recipe = recipeService.getById(recipeId);


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("recipe-edit");
        modelAndView.addObject("recipeUpdateRequest",  RecipeMapper.toUpdateRequest(recipe));
        modelAndView.addObject("recipeId", recipeId);
        modelAndView.addObject("categories", categoryService.getAllCategories());
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PutMapping("/{recipeId}")
    public ModelAndView updateRecipe(@PathVariable UUID recipeId,
                                     @Valid RecipeUpdateRequest recipeUpdateRequest,
                                     BindingResult bindingResult,
                                     @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {

        User user = userService.getById(authenticationMethadata.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("recipe-edit");
            modelAndView.addObject("recipeUpdateRequest", recipeUpdateRequest);
            modelAndView.addObject("recipeId", recipeId);
            modelAndView.addObject("categories", categoryService.getAllCategories());
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        Recipe updatedRecipe = recipeService.updateRecipe(recipeId, recipeUpdateRequest, user);
        return new ModelAndView("redirect:/recipes/" + updatedRecipe.getId());
    }


    @DeleteMapping("/{recipeId}/delete")
    public ModelAndView deleteRecipe(@PathVariable UUID recipeId,
                                     @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());
        recipeService.deleteRecipe(recipeId, user);
        return new ModelAndView("redirect:/recipes/mine");
    }


    @GetMapping("/favorites")
    public ModelAndView getMyFavorites(@AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());
        List<Recipe> favorites = recipeService.getUserFavorites(user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("recipe-favorites");
        modelAndView.addObject("recipes", favorites);
        modelAndView.addObject("user", user);

        return modelAndView;
    }


    @PostMapping("/{recipeId}/favorite")
    public ModelAndView addToFavorites(@PathVariable UUID recipeId,
                                       @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());
        recipeService.addToFavorites(user, recipeId);
        return new ModelAndView("redirect:/recipes/" + recipeId +"?success=added");
    }


    @PostMapping("/{recipeId}/unfavorite")
    public ModelAndView removeFromFavorites(@PathVariable UUID recipeId,
                                            @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());
        recipeService.removeFromFavorites(user, recipeId);
        return new ModelAndView("redirect:/recipes/" + recipeId +"?success=removed");
    }



    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        byte[] pdf = recipeService.generateRecipePdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "recipe.pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

}