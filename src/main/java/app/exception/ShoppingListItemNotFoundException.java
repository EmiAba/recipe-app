package app.exception;

public class ShoppingListItemNotFoundException extends RuntimeException {

    public ShoppingListItemNotFoundException(String message) {
        super(message);
    }

    public ShoppingListItemNotFoundException() {
    }
}