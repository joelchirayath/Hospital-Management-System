package giis.demo.service.manager;

import giis.demo.jdbc.models.InventoryOrderManager;
import giis.demo.jdbc.models.InventoryItem;
import giis.demo.jdbc.models.InventoryOrder;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryOrderService {

    private final InventoryOrderManager orderManager = new InventoryOrderManager();

    // ------------------------------------------------------
    //  Create a new order (NOW includes supplier)
    // ------------------------------------------------------
    public void placeOrder(
            InventoryItem item,
            int quantityOrdered,
            String supplier,
            String requestedBy,
            String notes
    ) {

        if (item == null)
            throw new IllegalArgumentException("Item cannot be null.");

        if (item.getId() <= 0)
            throw new IllegalArgumentException("Item must have valid ID.");

        if (quantityOrdered <= 0)
            throw new IllegalArgumentException("Quantity must be > 0.");

        InventoryOrder order = new InventoryOrder(
                item.getId(),
                item.getItemName(),
                item.getModelCode(),
                item.getQuantityOnHand(),
                quantityOrdered,
                "Pending",
                supplier,
                requestedBy,
                LocalDateTime.now(),
                notes
        );

        orderManager.insertOrder(order);
    }

    // ------------------------------------------------------
    //  Queries
    // ------------------------------------------------------
    public List<InventoryOrder> getAllOrders() {
        return orderManager.getAllOrders();
    }

    public List<InventoryOrder> getOrdersByStatus(String status) {
        if (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status))
            return getAllOrders();
        return orderManager.getOrdersByStatus(status.trim());
    }

    // ------------------------------------------------------
    //  Status update
    // ------------------------------------------------------
    public void updateOrderStatus(int orderId, String status) {
        if (orderId <= 0)
            throw new IllegalArgumentException("Invalid ID");
        orderManager.updateOrderStatus(orderId, status);
    }

    // ------------------------------------------------------
    //  Edit order
    // ------------------------------------------------------
    public void updateOrderDetails(
            int orderId,
            int qtyOrdered,
            String supplier,
            String requestedBy,
            String status
    ) {
        if (orderId <= 0)
            throw new IllegalArgumentException("Invalid ID");

        orderManager.updateOrderDetails(orderId, qtyOrdered, supplier, requestedBy, status);
    }

    // ------------------------------------------------------
    //  Delete order
    // ------------------------------------------------------
    public void deleteOrder(int orderId) {
        if (orderId <= 0)
            throw new IllegalArgumentException("Invalid ID");
        orderManager.deleteOrder(orderId);
    }
}
