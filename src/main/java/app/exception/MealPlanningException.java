package app.exception;

public class MealPlanningException extends RuntimeException {
    public MealPlanningException(String message) {
        super(message);
    }

    public MealPlanningException() {
    }

}
