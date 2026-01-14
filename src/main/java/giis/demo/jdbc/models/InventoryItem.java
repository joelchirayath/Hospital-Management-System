package giis.demo.jdbc.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryItem {

    private int id;
    private String itemName;
    private String category;
    private String subcategory;
    private String modelCode;
    private String batchNumber;
    private String location;
    private int quantityOnHand;
    private int minimumQuantity;
    private LocalDate expiryDate;          // null when item has no expiry
    private String supplier;
    private LocalDateTime lastUpdated;     // when the record was last changed

    public InventoryItem() {}

    public InventoryItem(String itemName,
                         String category,
                         String subcategory,
                         String modelCode,
                         String batchNumber,
                         String location,
                         int quantityOnHand,
                         int minimumQuantity,
                         LocalDate expiryDate,
                         String supplier,
                         LocalDateTime lastUpdated) {

        this.itemName = itemName;
        this.category = category;
        this.subcategory = subcategory;
        this.modelCode = modelCode;
        this.batchNumber = batchNumber;
        this.location = location;
        this.quantityOnHand = quantityOnHand;
        this.minimumQuantity = minimumQuantity;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
        this.lastUpdated = lastUpdated;
    }

    public InventoryItem(int id,
                         String itemName,
                         String category,
                         String subcategory,
                         String modelCode,
                         String batchNumber,
                         String location,
                         int quantityOnHand,
                         int minimumQuantity,
                         LocalDate expiryDate,
                         String supplier,
                         LocalDateTime lastUpdated) {

        this(itemName, category, subcategory, modelCode, batchNumber,
             location, quantityOnHand, minimumQuantity, expiryDate,
             supplier, lastUpdated);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }

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

    public String getStockStatus() {
        if (quantityOnHand <= 0) return "Out of stock";
        if (quantityOnHand <= minimumQuantity) return "Low stock";
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

    public boolean isExpired() {
        return "Expired".equalsIgnoreCase(getExpiryStatus());
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
               "id=" + id +
               ", itemName='" + itemName + '\'' +
               ", category='" + category + '\'' +
               ", subcategory='" + subcategory + '\'' +
               ", modelCode='" + modelCode + '\'' +
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
