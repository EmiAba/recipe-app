package app.web;

import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.security.AuthenticationMethadata;
import app.shoppinglist.model.ShoppingListItem;
import app.shoppinglist.service.ShoppingListItemService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ShoppingListItemRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/shopping-list")
public class ShoppingListItemController {

    private final ShoppingListItemService shoppingListItemService;
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public ShoppingListItemController(ShoppingListItemService shoppingListItemService,
                                      UserService userService,
                                      RecipeService recipeService) {
        this.shoppingListItemService = shoppingListItemService;
        this.userService = userService;
        this.recipeService = recipeService;
    }



    @GetMapping
    public ModelAndView getShoppingList(@AuthenticationPrincipal AuthenticationMethadata auth) {
        User user = userService.getById(auth.getUserId());

        List<ShoppingListItem> items = shoppingListItemService.getUserShoppingList(user);
        Map<String, List<ShoppingListItem>> categorizedItems =
                shoppingListItemService.getCategorizedItems(user);

        long totalItems = shoppingListItemService.getTotalItemsCount(user);
        long completedItems = shoppingListItemService.getCompletedItemsCount(user);
        long recipeCount = shoppingListItemService.getRecipeCount(user);
        int completionPercentage = shoppingListItemService.getCompletionPercentage(user);

        List<Recipe> userRecipes = recipeService.getRecipesByUser(user, null);

        ModelAndView modelAndView = new ModelAndView("shopping-list");
        modelAndView.addObject("user", user);
        modelAndView.addObject("shoppingListItems", items);
        modelAndView.addObject("categorizedItems", categorizedItems);
        modelAndView.addObject("totalItems", totalItems);
        modelAndView.addObject("completedItems", completedItems);
        modelAndView.addObject("recipeCount", recipeCount);
        modelAndView.addObject("completionPercentage", completionPercentage);
        modelAndView.addObject("userRecipes", userRecipes);
        modelAndView.addObject("itemRequest", new ShoppingListItemRequest());

        return modelAndView;
    }



    @PostMapping("/add")
    public ModelAndView addItem(@Valid @ModelAttribute("itemRequest") ShoppingListItemRequest request,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal AuthenticationMethadata auth) {

        User user = userService.getById(auth.getUserId());

        if (bindingResult.hasErrors()) {
            List<ShoppingListItem> items = shoppingListItemService.getUserShoppingList(user);
            Map<String, List<ShoppingListItem>> categorizedItems =
                    shoppingListItemService.getCategorizedItems(user);
            List<Recipe> userRecipes = recipeService.getRecipesByUser(user, null);

            ModelAndView modelAndView = new ModelAndView("shopping-list");
            modelAndView.addObject("user", user);
            modelAndView.addObject("shoppingListItems", items);
            modelAndView.addObject("categorizedItems", categorizedItems);
            modelAndView.addObject("totalItems", shoppingListItemService.getTotalItemsCount(user));
            modelAndView.addObject("completedItems", shoppingListItemService.getCompletedItemsCount(user));
            modelAndView.addObject("recipeCount", shoppingListItemService.getRecipeCount(user));
            modelAndView.addObject("completionPercentage", shoppingListItemService.getCompletionPercentage(user));
            modelAndView.addObject("userRecipes", userRecipes);
            modelAndView.addObject("itemRequest", request);
            return modelAndView;
        }

        shoppingListItemService.addItem(user,
                request.getName(),
                request.getQuantity(),
                request.getUnit(),
                request.getNotes(),
                request.getCustomCategory());

        return new ModelAndView("redirect:/shopping-list");
    }

   

    @PostMapping("/add-from-recipe")
    public ModelAndView addFromRecipe(@RequestParam("recipeId") UUID recipeId,
                                      @AuthenticationPrincipal AuthenticationMethadata auth) {

        User user = userService.getById(auth.getUserId());

        if (recipeId != null) {
            shoppingListItemService.addIngredientsFromRecipe(user, recipeId);
        }

        return new ModelAndView("redirect:/shopping-list");
    }



    @PostMapping("/add-from-recipe/{recipeId}")
    public ModelAndView addFromRecipeDetail(@PathVariable UUID recipeId,
                                            @AuthenticationPrincipal AuthenticationMethadata auth) {

        User user = userService.getById(auth.getUserId());
        shoppingListItemService.addIngredientsFromRecipe(user, recipeId);

        return new ModelAndView("redirect:/recipes/" + recipeId);
    }



    @PutMapping("/toggle/{itemId}")
    public ModelAndView toggleItemCompletion(@PathVariable UUID itemId,
                                             @AuthenticationPrincipal AuthenticationMethadata auth) {

        User user = userService.getById(auth.getUserId());
        shoppingListItemService.toggleItemCompletion(itemId, user);

        return new ModelAndView("redirect:/shopping-list");
    }



    @GetMapping("/edit/{itemId}")
    public ModelAndView editItemForm(@PathVariable UUID itemId,
                                     @AuthenticationPrincipal AuthenticationMethadata auth) {

        User user = userService.getById(auth.getUserId());
        ShoppingListItem item = shoppingListItemService.getItemForEdit(itemId, user);

        ShoppingListItemRequest editRequest = ShoppingListItemRequest.builder()
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .notes(item.getNotes())
                .customCategory(item.getCustomCategory())
                .build();

        ModelAndView modelAndView = new ModelAndView("shopping-list-edit");
        modelAndView.addObject("user", user);
        modelAndView.addObject("item", item);
        modelAndView.addObject("itemRequest", editRequest);

        return modelAndView;
    }



    @PutMapping("/edit/{itemId}")
    public ModelAndView updateItem(@PathVariable UUID itemId,
                                   @Valid @ModelAttribute("itemRequest") ShoppingListItemRequest request,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal AuthenticationMethadata auth) {

        User user = userService.getById(auth.getUserId());

        if (bindingResult.hasErrors()) {
            ShoppingListItem item = shoppingListItemService.getItemForEdit(itemId, user);

            ModelAndView modelAndView = new ModelAndView("shopping-list-edit");
            modelAndView.addObject("user", user);
            modelAndView.addObject("item", item);
            modelAndView.addObject("itemRequest", request);
            return modelAndView;
        }

        shoppingListItemService.updateItem(itemId, user,
                request.getName(),
                request.getQuantity(),
                request.getUnit(),
                request.getNotes(),
                request.getCustomCategory());

        return new ModelAndView("redirect:/shopping-list");
    }

 

    @DeleteMapping("/delete/{itemId}")
    public ModelAndView deleteItem(@PathVariable UUID itemId,
                                   @AuthenticationPrincipal AuthenticationMethadata auth) {

        User user = userService.getById(auth.getUserId());
        shoppingListItemService.deleteItem(itemId, user);

        return new ModelAndView("redirect:/shopping-list");
    }

  

    @PutMapping("/mark-all-complete")
    public ModelAndView markAllComplete(@AuthenticationPrincipal AuthenticationMethadata auth) {
        User user = userService.getById(auth.getUserId());
        shoppingListItemService.markAllCompleted(user);

        return new ModelAndView("redirect:/shopping-list");
    }

    @DeleteMapping("/remove-completed")
    public ModelAndView removeCompleted(@AuthenticationPrincipal AuthenticationMethadata auth) {
        User user = userService.getById(auth.getUserId());
        shoppingListItemService.removeCompletedItems(user);

        return new ModelAndView("redirect:/shopping-list");
    }

    @DeleteMapping("/clear-all")
    public ModelAndView clearAll(@AuthenticationPrincipal AuthenticationMethadata auth) {
        User user = userService.getById(auth.getUserId());
        shoppingListItemService.clearAllItems(user);

        return new ModelAndView("redirect:/shopping-list");
    }
}