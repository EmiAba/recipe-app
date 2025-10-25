package app.web;


import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.category.service.CategoryService;
import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RecipeCreateRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;


@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final CategoryService categoryService;
    private final UserService userService;


    @Autowired
    public RecipeController(RecipeService recipeService, CategoryService categoryService,
                            UserService userService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
        this.userService = userService;

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

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("recipe-detail");
        modelAndView.addObject("recipe", recipe);
        modelAndView.addObject("user", user);



        return modelAndView;
    }


}