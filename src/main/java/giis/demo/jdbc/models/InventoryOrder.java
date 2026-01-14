package giis.demo.jdbc.models;

import java.time.LocalDateTime;

public class InventoryOrder {

    private int id;
    private int inventoryItemId;

    private String itemName;
    private String modelCode;

    private int quantityInStock;
    private int quantityOrdered;

    private String status;          // Pending / Submitted / Cancelled / Completed
    private String supplier;        // NEW
    private String requestedBy;

    private LocalDateTime orderDate;
    private String notes;

    // ----------------------------------------------------
    // Constructors
    // ----------------------------------------------------

    public InventoryOrder() {
        // no-arg constructor
    }

    /**
     * Constructor without id (for new orders before insert).
     */
    public InventoryOrder(int inventoryItemId,
                          String itemName,
                          String modelCode,
                          int quantityInStock,
                          int quantityOrdered,
                          String status,
                          String supplier,       // NEW
                          String requestedBy,
                          LocalDateTime orderDate,
                          String notes) {

        this.inventoryItemId = inventoryItemId;
        this.itemName = itemName;
        this.modelCode = modelCode;
        this.quantityInStock = quantityInStock;
        this.quantityOrdered = quantityOrdered;
        this.status = status;
        this.supplier = supplier;          // NEW
        this.requestedBy = requestedBy;
        this.orderDate = orderDate;
        this.notes = notes;
    }

    /**
     * Full constructor including id (for reading from DB).
     */
    public InventoryOrder(int id,
                          int inventoryItemId,
                          String itemName,
                          String modelCode,
                          int quantityInStock,
                          int quantityOrdered,
                          String status,
                          String supplier,        // NEW
                          String requestedBy,
                          LocalDateTime orderDate,
                          String notes) {

        this(inventoryItemId, itemName, modelCode,
             quantityInStock, quantityOrdered,
             status, supplier, requestedBy, orderDate, notes);
        this.id = id;
    }

    // ----------------------------------------------------
    // Getters / Setters
    // ----------------------------------------------------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(int inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    public int getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(int quantityOrdered) { this.quantityOrdered = quantityOrdered; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSupplier() { return supplier; }       // NEW
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ----------------------------------------------------
    // Helper status checks
    // ----------------------------------------------------

    public boolean isPending() { return "Pending".equalsIgnoreCase(status); }
    public boolean isSubmitted() { return "Submitted".equalsIgnoreCase(status); }
    public boolean isCancelled() { return "Cancelled".equalsIgnoreCase(status); }
    public boolean isCompleted() { return "Completed".equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return "InventoryOrder{" +
                "id=" + id +
                ", inventoryItemId=" + inventoryItemId +
                ", itemName='" + itemName + '\'' +
                ", modelCode='" + modelCode + '\'' +
                ", quantityInStock=" + quantityInStock +
                ", quantityOrdered=" + quantityOrdered +
                ", status='" + status + '\'' +
                ", supplier='" + supplier + '\'' +
                ", requestedBy='" + requestedBy + '\'' +
                ", orderDate=" + orderDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}
