package giis.demo.jdbc.models;

import giis.demo.util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryOrderManager {

    private final Database db = new Database();

    // ------------------------------------------------------
    // Insert new order (now includes supplier)
    // ------------------------------------------------------
    public void insertOrder(InventoryOrder order) {
        String sql = "INSERT INTO inventory_orders (" +
                "inventory_item_id, item_name, model_code, " +
                "quantity_in_stock, quantity_ordered, status, " +
                "requested_by, supplier, notes" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order.getInventoryItemId());
            stmt.setString(2, order.getItemName());
            stmt.setString(3, order.getModelCode());
            stmt.setInt(4, order.getQuantityInStock());
            stmt.setInt(5, order.getQuantityOrdered());
            stmt.setString(6, order.getStatus());
            stmt.setString(7, order.getRequestedBy());
            stmt.setString(8, order.getSupplier());
            stmt.setString(9, order.getNotes());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------
    // Read all orders
    // ------------------------------------------------------
    public List<InventoryOrder> getAllOrders() {
        String sql = "SELECT * FROM inventory_orders ORDER BY order_date DESC, id DESC";

        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return mapOrderList(rs);

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ------------------------------------------------------
    // Filter by status
    // ------------------------------------------------------
    public List<InventoryOrder> getOrdersByStatus(String status) {
        String sql = "SELECT * FROM inventory_orders WHERE status = ? ORDER BY order_date DESC, id DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                return mapOrderList(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ------------------------------------------------------
    // Update STATUS only
    // ------------------------------------------------------
    public void updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE inventory_orders SET status = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------
    // NEW: Update quantity, supplier, requestedBy, status
    // ------------------------------------------------------
    public void updateOrderDetails(int orderId,
                                   int qtyOrdered,
                                   String supplier,
                                   String requestedBy,
                                   String status) {

        String sql = "UPDATE inventory_orders SET " +
                "quantity_ordered = ?, " +
                "supplier = ?, " +
                "requested_by = ?, " +
                "status = ? " +
                "WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, qtyOrdered);
            ps.setString(2, supplier);
            ps.setString(3, requestedBy);
            ps.setString(4, status);
            ps.setInt(5, orderId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------
    // Delete
    // ------------------------------------------------------
    public void deleteOrder(int orderId) {
        String sql = "DELETE FROM inventory_orders WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------
    // ResultSet mapping
    // ------------------------------------------------------
    private List<InventoryOrder> mapOrderList(ResultSet rs) throws SQLException {
        List<InventoryOrder> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapOrder(rs));
        }
        return list;
    }

    private InventoryOrder mapOrder(ResultSet rs) throws SQLException {
        LocalDateTime dt = null;
        String date = rs.getString("order_date");
        if (date != null) dt = LocalDateTime.parse(date.replace(" ", "T"));

        InventoryOrder o = new InventoryOrder(
                rs.getInt("id"),
                rs.getInt("inventory_item_id"),
                rs.getString("item_name"),
                rs.getString("model_code"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("quantity_ordered"),
                rs.getString("status"),
                rs.getString("supplier"),        
                rs.getString("requested_by"),
                dt,
                rs.getString("notes")
        );


        o.setSupplier(rs.getString("supplier"));

        return o;
    }
}
