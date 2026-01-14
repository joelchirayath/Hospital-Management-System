package giis.demo.jdbc;

import giis.demo.jdbc.models.InventoryItem;
import giis.demo.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    private final Database db = new Database();

    // ------------------------------------------------------
    //  Get ALL items
    // ------------------------------------------------------
    public List<InventoryItem> getAllItems() {
        String sql = "SELECT id, item_name, category, subcategory, model_code, batch_number, "
                   + "location, quantity_on_hand, minimum_quantity, expiry_date, supplier, last_updated "
                   + "FROM inventory_items ORDER BY item_name ASC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return mapInventoryList(rs);

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ------------------------------------------------------
    //  Search (name or model code)
    // ------------------------------------------------------
    public List<InventoryItem> searchItems(String query) {
        String sql =
            "SELECT id, item_name, category, subcategory, model_code, batch_number, "
          + "location, quantity_on_hand, minimum_quantity, expiry_date, supplier, last_updated "
          + "FROM inventory_items "
          + "WHERE item_name LIKE ? OR model_code LIKE ? "
          + "ORDER BY item_name ASC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String like = "%" + query + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapInventoryList(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ------------------------------------------------------
    //  Filter using category & location only
    //  (status is now computed in UI, not stored in DB)
    // ------------------------------------------------------
    public List<InventoryItem> filterItems(String category,
                                           String location,
                                           String ignoredStatus) {

        StringBuilder sql = new StringBuilder(
            "SELECT id, item_name, category, subcategory, model_code, batch_number, "
          + "location, quantity_on_hand, minimum_quantity, expiry_date, supplier, last_updated "
          + "FROM inventory_items WHERE 1=1 "
        );

        List<String> params = new ArrayList<>();

        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            sql.append(" AND category = ? ");
            params.add(category);
        }

        if (location != null && !location.isEmpty() && !"All".equalsIgnoreCase(location)) {
            sql.append(" AND location = ? ");
            params.add(location);
        }

        sql.append(" ORDER BY item_name ASC");

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (String p : params) {
                stmt.setString(idx++, p);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return mapInventoryList(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ------------------------------------------------------
    //  INSERT new inventory item
    // ------------------------------------------------------
    public void insertItem(InventoryItem item) {
        String sql = "INSERT INTO inventory_items ("
                   + "item_name, category, subcategory, model_code, batch_number, "
                   + "location, quantity_on_hand, minimum_quantity, expiry_date, supplier"
                   + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // item_name is NOT NULL
            stmt.setString(1, item.getItemName());

            // category is NOT NULL → make it safe
            String safeCategory = item.getCategory();
            if (safeCategory == null || safeCategory.trim().isEmpty()) {
                safeCategory = "Others";
            }
            stmt.setString(2, safeCategory);

            stmt.setString(3, item.getSubcategory());
            stmt.setString(4, item.getModelCode());
            stmt.setString(5, item.getBatchNumber());
            stmt.setString(6, item.getLocation());
            stmt.setInt(7, item.getQuantityOnHand());
            stmt.setInt(8, item.getMinimumQuantity());

            if (item.getExpiryDate() != null) {
                stmt.setString(9, item.getExpiryDate().toString());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }

            stmt.setString(10, item.getSupplier());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------
    //  ADD new inventory item (alias for insertItem)
    // ------------------------------------------------------
    public void addItem(InventoryItem item) {
        insertItem(item);
    }

    // ------------------------------------------------------
    //  UPDATE existing inventory item
    // ------------------------------------------------------
    public void updateItem(InventoryItem item) {
        if (item.getId() <= 0) {
            throw new IllegalArgumentException("Item ID must be > 0 to update");
        }

        String sql = "UPDATE inventory_items SET "
                   + "item_name = ?, "
                   + "category = ?, "
                   + "subcategory = ?, "
                   + "model_code = ?, "
                   + "batch_number = ?, "
                   + "location = ?, "
                   + "quantity_on_hand = ?, "
                   + "minimum_quantity = ?, "
                   + "expiry_date = ?, "
                   + "supplier = ?, "
                   + "last_updated = CURRENT_TIMESTAMP "
                   + "WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getCategory());
            stmt.setString(3, item.getSubcategory());
            stmt.setString(4, item.getModelCode());
            stmt.setString(5, item.getBatchNumber());
            stmt.setString(6, item.getLocation());
            stmt.setInt(7, item.getQuantityOnHand());
            stmt.setInt(8, item.getMinimumQuantity());

            if (item.getExpiryDate() != null) {
                stmt.setString(9, item.getExpiryDate().toString());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }

            stmt.setString(10, item.getSupplier());
            stmt.setInt(11, item.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------
    //  Utility: map ResultSet → List<InventoryItem>
    // ------------------------------------------------------
    private List<InventoryItem> mapInventoryList(ResultSet rs) throws SQLException {
        List<InventoryItem> list = new ArrayList<>();

        while (rs.next()) {
            list.add(mapInventoryItem(rs));
        }
        return list;
    }

    // ------------------------------------------------------
    //  Utility: convert a single row into an InventoryItem
    // ------------------------------------------------------
    private InventoryItem mapInventoryItem(ResultSet rs) throws SQLException {

        LocalDate expiry = null;
        String expiryStr = rs.getString("expiry_date");
        if (expiryStr != null && !expiryStr.isEmpty()) {
            expiry = LocalDate.parse(expiryStr);
        }

        LocalDateTime updated = null;
        String updatedStr = rs.getString("last_updated");
        if (updatedStr != null && !updatedStr.isEmpty()) {
            // SQLite DATETIME stored as "yyyy-MM-dd HH:mm:ss"
            updated = LocalDateTime.parse(updatedStr.replace(" ", "T"));
        }

        return new InventoryItem(
            rs.getInt("id"),
            rs.getString("item_name"),
            rs.getString("category"),
            rs.getString("subcategory"),
            rs.getString("model_code"),
            rs.getString("batch_number"),
            rs.getString("location"),
            rs.getInt("quantity_on_hand"),
            rs.getInt("minimum_quantity"),
            expiry,
            rs.getString("supplier"),
            updated
        );
    }

    // ------------------------------------------------------
    //  NEW: find variants (same name, case-insensitive)
    //  Variants differ by model, expiry, location, supplier, batch, etc.
    // ------------------------------------------------------
    public List<InventoryItem> findSimilarItems(String itemName) {

        String sql =
            "SELECT id, item_name, category, subcategory, model_code, batch_number, " +
            "location, quantity_on_hand, minimum_quantity, expiry_date, supplier, last_updated " +
            "FROM inventory_items " +
            "WHERE LOWER(item_name) = LOWER(?) " +
            "ORDER BY model_code, expiry_date IS NULL, expiry_date, location, supplier";

        List<InventoryItem> result = new ArrayList<>();

        if (itemName == null || itemName.trim().isEmpty()) {
            return result;
        }

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemName.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapInventoryItem(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

}
