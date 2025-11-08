package app.web;

import app.mealplanning.client.dto.MealPlanResponse;
import app.mealplanning.service.MealPlanningService;
import app.recipe.service.RecipeService;
import app.security.AuthenticationMethadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.MealPlanAddRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/meal-planning")
public class MealPlanningController {

    private final MealPlanningService mealPlanningService;
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public MealPlanningController(MealPlanningService mealPlanningService,
                                  UserService userService,
                                  RecipeService recipeService) {
        this.mealPlanningService = mealPlanningService;
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping
    public ModelAndView getMealPlanningPage(
            @RequestParam(required = false) LocalDate weekStart,
            @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {

        User user = userService.getById(authenticationMethadata.getUserId());


        LocalDate currentWeekStart = (weekStart != null)
                ? weekStart
                : LocalDate.now().with(java.time.DayOfWeek.MONDAY);

        List<MealPlanResponse> weeklyMealPlans = mealPlanningService.getWeeklyMealPlans(user.getId(), currentWeekStart);
        List<app.recipe.model.Recipe> userRecipes = recipeService.getRecipesByUser(user, null);

        ModelAndView modelAndView = new ModelAndView("meal-planning");
        modelAndView.addObject("user", user);
        modelAndView.addObject("weeklyMealPlans", weeklyMealPlans);
        modelAndView.addObject("userRecipes", userRecipes);
        modelAndView.addObject("weekStart", currentWeekStart);
        modelAndView.addObject("mealPlanAddRequest", new MealPlanAddRequest());

        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addRecipeToMealPlan(
            @Valid @ModelAttribute MealPlanAddRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false) LocalDate weekStart,
            @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {


        LocalDate currentWeekStart = (weekStart != null)
                ? weekStart
                : LocalDate.now().with(java.time.DayOfWeek.MONDAY);

        if (bindingResult.hasErrors()) {

            User user = userService.getById(authenticationMethadata.getUserId());
            List<MealPlanResponse> weeklyMealPlans = mealPlanningService.getWeeklyMealPlans(user.getId(), currentWeekStart);
            List<app.recipe.model.Recipe> userRecipes = recipeService.getRecipesByUser(user, null);

            ModelAndView modelAndView = new ModelAndView("meal-planning");
            modelAndView.addObject("user", user);
            modelAndView.addObject("weeklyMealPlans", weeklyMealPlans);
            modelAndView.addObject("userRecipes", userRecipes);
            modelAndView.addObject("weekStart", currentWeekStart);
            modelAndView.addObject("mealPlanAddRequest", request);
            modelAndView.addObject("selectedDate", request.getPlannedDate());
            modelAndView.addObject("selectedMealType", request.getMealType());

            return modelAndView;
        }

        app.recipe.model.Recipe recipe = recipeService.getById(request.getRecipeId());

        mealPlanningService.addRecipeToMealPlan(
                authenticationMethadata.getUserId(),
                request.getRecipeId(),
                recipe.getTitle(),
                recipe.getCalories(),
                request.getMealType(),
                request.getPlannedDate()
        );


        return new ModelAndView("redirect:/meal-planning/week?weekStart=" + currentWeekStart);
    }

    @GetMapping("/week")
    public ModelAndView getWeeklyView(@RequestParam(name = "weekStart") LocalDate weekStart,
                                      @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        User user = userService.getById(authenticationMethadata.getUserId());

        List<MealPlanResponse> weeklyMealPlans = mealPlanningService.getWeeklyMealPlans(user.getId(), weekStart);
        List<app.recipe.model.Recipe> userRecipes = recipeService.getRecipesByUser(user, null);

        ModelAndView modelAndView = new ModelAndView("meal-planning");
        modelAndView.addObject("user", user);
        modelAndView.addObject("weeklyMealPlans", weeklyMealPlans);
        modelAndView.addObject("userRecipes", userRecipes);
        modelAndView.addObject("weekStart", weekStart);
        modelAndView.addObject("mealPlanAddRequest", new MealPlanAddRequest());

        return modelAndView;
    }

    @DeleteMapping("/{mealPlanId}/delete")
    public String deleteMealPlan(@PathVariable UUID mealPlanId,
                                 @AuthenticationPrincipal AuthenticationMethadata authenticationMethadata) {
        mealPlanningService.deleteMealPlan(mealPlanId, authenticationMethadata.getUserId());
        return "redirect:/meal-planning";
    }




}
