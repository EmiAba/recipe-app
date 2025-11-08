package app.mealplanning.client;


import app.mealplanning.client.dto.MealPlanRequest;
import app.mealplanning.client.dto.MealPlanResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "meal-planning-svc", url = "http://localhost:8081/api/v1/meal-plans")
public interface MealPlanningClient {


    @GetMapping("/weekly")
    ResponseEntity<List<MealPlanResponse>> getWeeklyMealPlans(
            @RequestParam("userId") UUID userId,
            @RequestParam("weekStart") LocalDate weekStart
    );


    @PostMapping
    ResponseEntity<MealPlanResponse> addMealPlan(
            @RequestBody MealPlanRequest requestBody
    );


    @DeleteMapping("/{mealPlanId}")
    ResponseEntity<Void> deleteMealPlan(
            @PathVariable UUID mealPlanId,
            @RequestParam UUID userId
    );

}