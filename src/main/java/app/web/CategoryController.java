package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final RecipeService recipeService;



    @Autowired
    public CategoryController(CategoryService categoryService, UserService userService, RecipeService recipeService) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.recipeService = recipeService;


    }


    @GetMapping("")
    public ModelAndView getAllCategories(@AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());


        List<Category> allCategories = categoryService.getAllCategories();

        Map<String, Long> categoryCounts = categoryService.getCategoryRecipeCounts();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("categories");
        modelAndView.addObject("categories", allCategories);
        modelAndView.addObject("categoryCounts", categoryCounts);
        modelAndView.addObject("user", user);


        return modelAndView;
    }


    @GetMapping("/{categoryName}")
    public ModelAndView getCategoryRecipes(@AuthenticationPrincipal AuthenticationMethadata authenticationMethadata, @PathVariable String categoryName) {
        User user = userService.getById(authenticationMethadata.getUserId());

        Category category = categoryService.findByName(categoryName);

        List<Recipe> publicRecipes = recipeService.getPublicRecipes(category);

        ModelAndView modelAndView = new ModelAndView("category-detail");
        modelAndView.addObject("category", category);
        modelAndView.addObject("recipes", publicRecipes);
        modelAndView.addObject("recipeCount", publicRecipes.size());
        modelAndView.addObject("user", user);

        log.info("Loading category [{}] page - found {} public recipes",
                category.getName(), publicRecipes.size());

        return modelAndView;
    }
}