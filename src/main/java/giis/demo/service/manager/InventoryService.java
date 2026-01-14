package giis.demo.service.manager;

import giis.demo.jdbc.InventoryManager;
import giis.demo.jdbc.models.InventoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for inventory operations.
 */
public class InventoryService {

    private final InventoryManager manager = new InventoryManager();

    // Get all items
    public List<InventoryItem> getAllItems() {
        return manager.getAllItems();
    }

    // Search: item name or model code
    public List<InventoryItem> searchItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            return manager.getAllItems();
        }
        return manager.searchItems(query.trim());
    }

    // Filter by category, location, status
    public List<InventoryItem> filterItems(String category,
                                           String location,
                                           String status) {
        return manager.filterItems(category, location, status);
    }

    // -----------------------------------------------------
    // NEW: return list of all unique suppliers
    // -----------------------------------------------------
    public List<String> getAllSuppliers() {
        return manager.getAllItems()
                .stream()
                .map(InventoryItem::getSupplier)
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------
    // NEW: add a new inventory item (used by Add Item dialog)
    // -----------------------------------------------------
    public void addItem(InventoryItem item) {
        manager.addItem(item);
    }

    // -----------------------------------------------------
    // NEW: find variants (same itemName + modelCode)
    // -----------------------------------------------------
    public List<InventoryItem> findVariants(String itemName) {
        if (itemName == null) return new ArrayList<>();
        return manager.findSimilarItems(itemName);
    }

}
