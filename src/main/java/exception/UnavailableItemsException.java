package exception;

import java.util.List;

/**
 * Thrown by OrderService.placeOrder() when one or more cart items reference
 * a soft-deleted product variant.
 *
 * Carries the human-readable product names so the servlet can return them
 * to the client as a structured { success: false, unavailableItems: [...] }
 * response, which checkout.js uses to show a specific message, re-sync the
 * cart, and re-render the summary — all without a page reload.
 */
public class UnavailableItemsException extends RuntimeException {

    private final List<String> itemNames;

    public UnavailableItemsException(List<String> itemNames) {
        super("The following items are no longer available: " + String.join(", ", itemNames));
        this.itemNames = List.copyOf(itemNames);
    }

    public List<String> getItemNames() {
        return itemNames;
    }
}