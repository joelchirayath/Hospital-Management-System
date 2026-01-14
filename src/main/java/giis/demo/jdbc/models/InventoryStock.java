package giis.demo.jdbc.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryStock {

    private int id;
    private int productId;             // FK → product table

    private String batchNumber;
    private String location;
    private int quantityOnHand;
    private int minimumQuantity;
    private LocalDate expiryDate;      // null if no expiry
    private String supplier;

    private LocalDateTime lastUpdated;

    // --- Constructors ---
    public InventoryStock() {}

    public InventoryStock(
            int productId,
            String batchNumber,
            String location,
            int quantityOnHand,
            int minimumQuantity,
            LocalDate expiryDate,
            String supplier,
            LocalDateTime lastUpdated) {

        this.productId = productId;
        this.batchNumber = batchNumber;
        this.location = location;
        this.quantityOnHand = quantityOnHand;
        this.minimumQuantity = minimumQuantity;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
        this.lastUpdated = lastUpdated;
    }

    public InventoryStock(
            int id,
            int productId,
            String batchNumber,
            String location,
            int quantityOnHand,
            int minimumQuantity,
            LocalDate expiryDate,
            String supplier,
            LocalDateTime lastUpdated) {

        this(productId, batchNumber, location, quantityOnHand, minimumQuantity,
             expiryDate, supplier, lastUpdated);
        this.id = id;
    }

    // --- Getters / Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }

    public int getMinimumQuantity() { return minimumQuantity; }
    public void setMinimumQuantity(int minimumQuantity) { this.minimumQuantity = minimumQuantity; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    // --- Derived statuses ---
    public String getStockStatus() {
        if (quantityOnHand <= 0)
            return "Out of stock";
        if (quantityOnHand <= minimumQuantity)
            return "Low stock";
        return "In stock";
    }

    public String getExpiryStatus() {
        if (expiryDate == null) return "No expiry";

        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(30);

        if (expiryDate.isBefore(today)) return "Expired";
        if (!expiryDate.isAfter(soon)) return "Expiring soon";
        return "OK";
    }

    @Override
    public String toString() {
        return "InventoryStock{" +
                "id=" + id +
                ", productId=" + productId +
                ", batchNumber='" + batchNumber + '\'' +
                ", location='" + location + '\'' +
                ", quantityOnHand=" + quantityOnHand +
                ", minimumQuantity=" + minimumQuantity +
                ", expiryDate=" + expiryDate +
                ", supplier='" + supplier + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
