package giis.demo.ui.manager;

import giis.demo.jdbc.models.InventoryItem;
import giis.demo.jdbc.models.InventoryOrder;
import giis.demo.service.manager.InventoryOrderService;
import giis.demo.service.manager.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.swing.table.DefaultTableModel;
import java.io.FileOutputStream;
import java.io.IOException;

public class InventoryAlertWindow extends JFrame {

    private final InventoryService inventoryService = new InventoryService();
    private final InventoryOrderService orderService = new InventoryOrderService();

    // Data
    private List<InventoryItem> allItems = new ArrayList<>();
    private List<InventoryItem> lowStockItems = new ArrayList<>();
    private List<InventoryItem> expiredItems = new ArrayList<>();
    private List<InventoryItem> soonItems = new ArrayList<>();

    // UI
    private JTabbedPane tabbedPane;

    private JTable lowStockTable;
    private JTable expiredTable;
    private JTable soonTable;

    private DefaultTableModel lowStockModel;
    private DefaultTableModel expiredModel;
    private DefaultTableModel soonModel;

    // Thresholds
    private JSpinner daysSpinner;                // threshold for "expiring soon"
    private JSpinner lowStockThresholdSpinner;   // extra low-stock quantity threshold
    private JComboBox<String> categoryFilter;    // filter by category for all tabs

    public InventoryAlertWindow() {
        setTitle("Inventory Alerts");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        loadDataAndRefresh();

        setVisible(true);
    }

    // ------------------------------------------------------
    // Header
    // ------------------------------------------------------
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        header.setBackground(new Color(245, 247, 252));

        JLabel title = new JLabel("Inventory Alerts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 30, 30));

        JLabel subtitle = new JLabel("Low stock • Expired • Expiring soon");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(subtitle);

        // Right: thresholds + reset
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        right.setOpaque(false);

        JLabel daysLabel = new JLabel("Expiring within (days):");
        daysLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        daysSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        daysSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) daysSpinner.getEditor()).getTextField().setColumns(3);

        JLabel lowLabel = new JLabel("Low stock threshold (qty \u2264):");
        lowLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lowStockThresholdSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        lowStockThresholdSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) lowStockThresholdSpinner.getEditor()).getTextField().setColumns(4);

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        daysSpinner.addChangeListener(e -> loadDataAndRefresh());
        lowStockThresholdSpinner.addChangeListener(e -> loadDataAndRefresh());
        resetBtn.addActionListener(e -> {
            daysSpinner.setValue(30);
            lowStockThresholdSpinner.setValue(0);
            loadDataAndRefresh();
        });

        right.add(daysLabel);
        right.add(daysSpinner);
        right.add(lowLabel);
        right.add(lowStockThresholdSpinner);
        right.add(resetBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ------------------------------------------------------
    // Center: Category filter + tabs
    // ------------------------------------------------------
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));
        center.setBackground(Color.WHITE);

        // Top filter & legend
        JPanel topFilters = new JPanel(new BorderLayout());
        topFilters.setBackground(Color.WHITE);
        topFilters.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Left: category filter
        JPanel catPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        catPanel.setOpaque(false);

        JLabel catLabel = new JLabel("Filter by category:");
        catLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        categoryFilter = new JComboBox<>(new String[]{
                "All", "Medicine", "Equipment", "Consumables", "Others"
        });
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryFilter.addActionListener(e -> refreshTables());

        catPanel.add(catLabel);
        catPanel.add(categoryFilter);

        // Right: legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        legendPanel.setOpaque(false);

        JLabel legendLbl = new JLabel("Legend:");
        legendLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

        legendPanel.add(legendLbl);
        legendPanel.add(createLegendLabel("Low stock", new Color(255, 249, 196)));
        legendPanel.add(createLegendLabel("Expired", new Color(255, 205, 210)));
        legendPanel.add(createLegendLabel("Expiring soon", new Color(255, 224, 178)));

        topFilters.add(catPanel, BorderLayout.WEST);
        topFilters.add(legendPanel, BorderLayout.EAST);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Low stock tab
        lowStockModel = createTableModel();
        lowStockTable = createAlertTable(lowStockModel, new Color(255, 249, 196)); // light yellow
        JScrollPane lowScroll = new JScrollPane(lowStockTable);
        lowScroll.getViewport().setBackground(Color.WHITE);

        JPanel lowPanel = new JPanel(new BorderLayout());
        lowPanel.setBackground(Color.WHITE);
        lowPanel.add(lowScroll, BorderLayout.CENTER);
        lowPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        tabbedPane.addTab("⚠ Low Stock", lowPanel);

        // Expired tab
        expiredModel = createTableModel();
        expiredTable = createAlertTable(expiredModel, new Color(255, 205, 210)); // light red
        JScrollPane expiredScroll = new JScrollPane(expiredTable);
        expiredScroll.getViewport().setBackground(Color.WHITE);

        JPanel expiredPanel = new JPanel(new BorderLayout());
        expiredPanel.setBackground(Color.WHITE);
        expiredPanel.add(expiredScroll, BorderLayout.CENTER);
        expiredPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        tabbedPane.addTab("⛔ Expired", expiredPanel);

        // Expiring soon tab
        soonModel = createTableModel();
        soonTable = createAlertTable(soonModel, new Color(255, 224, 178)); // light orange
        JScrollPane soonScroll = new JScrollPane(soonTable);
        soonScroll.getViewport().setBackground(Color.WHITE);

        JPanel soonPanel = new JPanel(new BorderLayout());
        soonPanel.setBackground(Color.WHITE);
        soonPanel.add(soonScroll, BorderLayout.CENTER);
        soonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        tabbedPane.addTab("⏳ Expiring Soon", soonPanel);

        // Attach double-click details popup for all three tables
        attachItemDoubleClick(lowStockTable);
        attachItemDoubleClick(expiredTable);
        attachItemDoubleClick(soonTable);

        center.add(topFilters, BorderLayout.NORTH);
        center.add(tabbedPane, BorderLayout.CENTER);

        return center;
    }

    private JLabel createLegendLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setOpaque(true);
        lbl.setBackground(color);
        lbl.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        lbl.setPreferredSize(new Dimension(110, 18));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new String[]{
                        "ID", "Name", "Category", "Location",
                        "Qty", "Min", "Expiry", "Stock Status", "Expiry Status"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createAlertTable(DefaultTableModel model, Color baseColor) {
        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Center numeric columns by name (so indices don't matter)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // These columns exist in your model
        table.getColumn("Qty").setCellRenderer(centerRenderer);
        table.getColumn("Min").setCellRenderer(centerRenderer);

        // --- HIDE ID COLUMN FROM VIEW (but keep it in the model) ---
        // This removes the ID from what the user sees, but you can still
        // access it via model.getValueAt(row, 0)
        TableColumn idColumn = table.getColumn("ID");
        table.removeColumn(idColumn);
        // ------------------------------------------------------------

        // Renderer with subtle background color per category
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(baseColor.darker());
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(baseColor);
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        return table;
    }

    

    // Attach double-click on any alert table → show item details
    private void attachItemDoubleClick(JTable table) {
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow == -1) return;

                    int modelRow = table.convertRowIndexToModel(viewRow);
                    DefaultTableModel m = (DefaultTableModel) table.getModel();
                    Object idObj = m.getValueAt(modelRow, 0);
                    if (!(idObj instanceof Integer)) return;

                    int itemId = (Integer) idObj;
                    InventoryItem item = findItemById(itemId);
                    if (item != null) {
                        showItemDetailsDialog(item);
                    }
                }
            }
        });
    }

    private InventoryItem findItemById(int id) {
        for (InventoryItem i : allItems) {
            if (i.getId() == id) {
                return i;
            }
        }
        return null;
    }

    // ------------------------------------------------------
    // Footer: Export / Order / View Orders / Close
    // ------------------------------------------------------
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        footer.setBackground(new Color(245, 247, 252));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        left.setOpaque(false);

        JButton exportCsvButton = new JButton("Export CSV");
        exportCsvButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exportCsvButton.addActionListener(e -> exportCurrentTabToCsv());

        JButton exportPdfButton = new JButton("Export PDF");
        exportPdfButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exportPdfButton.addActionListener(e -> exportCurrentTabToPseudoPdf());

        left.add(exportCsvButton);
        left.add(exportPdfButton);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        right.setOpaque(false);

        JButton orderButton = new JButton("Place Order for Selected");
        orderButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        orderButton.addActionListener(e -> handlePlaceOrder());

        JButton viewOrdersButton = new JButton("View Orders Placed");
        viewOrdersButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        viewOrdersButton.addActionListener(e -> showOrdersPlacedDialog());

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeButton.addActionListener(e -> dispose());

        right.add(orderButton);
        right.add(viewOrdersButton);
        right.add(closeButton);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    // ------------------------------------------------------
    // Data load + classification
    // ------------------------------------------------------
    private void loadDataAndRefresh() {
        allItems = inventoryService.getAllItems();
        classifyItems();
        refreshTables();
    }

    private void classifyItems() {
        lowStockItems.clear();
        expiredItems.clear();
        soonItems.clear();

        int days = (Integer) daysSpinner.getValue();
        int lowThreshold = (lowStockThresholdSpinner != null)
                ? (Integer) lowStockThresholdSpinner.getValue()
                : 0;

        LocalDate today = LocalDate.now();
        LocalDate soonLimit = today.plusDays(days);

        for (InventoryItem item : allItems) {
            String stockStatus = computeStockStatus(item, lowThreshold);
            String expiryStatus = computeExpiryStatus(item, today, soonLimit);

            // Low stock category (includes out of stock)
            if ("Low stock".equalsIgnoreCase(stockStatus) ||
                    "Out of stock".equalsIgnoreCase(stockStatus)) {
                lowStockItems.add(item);
            }

            // Expired items
            if ("Expired".equalsIgnoreCase(expiryStatus)) {
                expiredItems.add(item);
            }

            // Expiring soon
            if ("Expiring soon".equalsIgnoreCase(expiryStatus)) {
                soonItems.add(item);
            }
        }
    }

    private String computeStockStatus(InventoryItem i, int customThreshold) {
        int qty = i.getQuantityOnHand();
        int min = i.getMinimumQuantity();

        // Use manager threshold if > 0, combined with configured minimum
        int effectiveMin = min;
        if (customThreshold > 0) {
            effectiveMin = Math.max(min, customThreshold);
        }

        if (qty <= 0) return "Out of stock";
        if (qty <= effectiveMin) return "Low stock";
        return "In stock";
    }

    private String computeExpiryStatus(InventoryItem i, LocalDate today, LocalDate soonLimit) {
        LocalDate exp = i.getExpiryDate();
        if (exp == null) return "No expiry";
        if (exp.isBefore(today)) return "Expired";
        if (!exp.isAfter(soonLimit)) return "Expiring soon";
        return "OK";
    }

    private void refreshTables() {
        fillTable(lowStockModel, lowStockItems);
        fillTable(expiredModel, expiredItems);
        fillTable(soonModel, soonItems);
    }

    private void fillTable(DefaultTableModel model, List<InventoryItem> items) {
        model.setRowCount(0);

        LocalDate today = LocalDate.now();
        int days = (Integer) daysSpinner.getValue();
        int lowThreshold = (lowStockThresholdSpinner != null)
                ? (Integer) lowStockThresholdSpinner.getValue()
                : 0;
        LocalDate soonLimit = today.plusDays(days);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String selectedCategory = categoryFilter.getSelectedItem().toString();

        for (InventoryItem i : items) {
            // Category filter
            if (!"All".equalsIgnoreCase(selectedCategory)) {
                String cat = i.getCategory() == null ? "" : i.getCategory().trim();
                if (!cat.equalsIgnoreCase(selectedCategory)) {
                    continue;
                }
            }

            LocalDate exp = i.getExpiryDate();
            String expiryStr = (exp == null) ? "—" : fmt.format(exp);

            String stockStatus = computeStockStatus(i, lowThreshold);
            String expiryStatus = computeExpiryStatus(i, today, soonLimit);

            model.addRow(new Object[]{
                    i.getId(),
                    safe(i.getItemName()),
                    safe(i.getCategory()),
                    safe(i.getLocation()),
                    i.getQuantityOnHand(),
                    i.getMinimumQuantity(),
                    expiryStr,
                    stockStatus,
                    expiryStatus
            });
        }
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s.trim();
    }

    // ------------------------------------------------------
    // Export functions
    // ------------------------------------------------------
    private void exportCurrentTabToCsv() {
        JTable table = getCurrentTable();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export CSV", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Alerts as CSV");
        chooser.setSelectedFile(new java.io.File("alerts.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        java.io.File file = chooser.getSelectedFile();

        try (FileWriter writer = new FileWriter(file)) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // Header
            for (int col = 0; col < model.getColumnCount(); col++) {
                writer.write(model.getColumnName(col));
                if (col < model.getColumnCount() - 1)
                    writer.write(",");
            }
            writer.write("\n");

            // Rows
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    String text = (val == null) ? "" : val.toString().replace("\"", "\"\"");
                    writer.write("\"" + text + "\"");
                    if (col < model.getColumnCount() - 1)
                        writer.write(",");
                }
                writer.write("\n");
            }

            writer.flush();
            JOptionPane.showMessageDialog(this,
                    "CSV exported successfully:\n" + file.getAbsolutePath(),
                    "Export CSV",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error exporting CSV:\n" + ex.getMessage(),
                    "Export CSV",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCurrentTabToPseudoPdf() {
        JTable table = getCurrentTable();
        if (table == null) {
            JOptionPane.showMessageDialog(this,
                    "No data to export.",
                    "Export PDF",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Alerts as PDF");
        chooser.setSelectedFile(new java.io.File("alerts.pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        java.io.File file = chooser.getSelectedFile();

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        try (FileOutputStream fos = new FileOutputStream(file)) {

            Document document = new Document();
            PdfWriter.getInstance(document, fos);

            document.open();

            // Title
            document.add(new Paragraph("Inventory Alerts Report"));
            document.add(new Paragraph("========================="));
            document.add(new Paragraph("Tab: "
                    + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex())));
            document.add(new Paragraph(" ")); // empty line

            // Create a table with same number of columns as the model
            PdfPTable pdfTable = new PdfPTable(model.getColumnCount());

            // Header row
            for (int col = 0; col < model.getColumnCount(); col++) {
                pdfTable.addCell(model.getColumnName(col));
            }

            // Data rows
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    String text = (val == null) ? "" : val.toString();
                    pdfTable.addCell(text);
                }
            }

            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(this,
                    "PDF exported successfully:\n" + file.getAbsolutePath(),
                    "Export PDF",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (DocumentException | IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error exporting PDF:\n" + ex.getMessage(),
                    "Export PDF",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private JTable getCurrentTable() {
        int index = tabbedPane.getSelectedIndex();
        if (index == 0) return lowStockTable;
        if (index == 1) return expiredTable;
        if (index == 2) return soonTable;
        return null;
    }

    // ------------------------------------------------------
    // Place Order logic
    // ------------------------------------------------------
    private void handlePlaceOrder() {
        int tabIndex = tabbedPane.getSelectedIndex();
        if (tabIndex != 0) { // Low Stock tab index = 0
            JOptionPane.showMessageDialog(this,
                    "Orders can be placed from the 'Low Stock' tab.\n" +
                            "Please select an item there.",
                    "Place Order",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int viewRow = lowStockTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a low-stock item to order.",
                    "Place Order",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = lowStockTable.convertRowIndexToModel(viewRow);
        int itemId = (int) lowStockModel.getValueAt(modelRow, 0);

        InventoryItem selected = null;
        for (InventoryItem i : lowStockItems) {
            if (i.getId() == itemId) {
                selected = i;
                break;
            }
        }
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Could not find the selected item in memory.",
                    "Place Order",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        showOrderDialog(selected);
    }

    private void showOrderDialog(InventoryItem item) {
        JDialog dialog = new JDialog(this, "Place Order", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 13);

        int row = 0;

        // Row 0: Item Name
        gbc.gridx = 0; gbc.gridy = row;
        JLabel nameLbl = new JLabel("Item:");
        nameLbl.setFont(labelFont);
        panel.add(nameLbl, gbc);

        gbc.gridx = 1;
        JLabel nameLabel = new JLabel(safe(item.getItemName()));
        nameLabel.setFont(valueFont);
        panel.add(nameLabel, gbc);

        // Row 1: Model
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel modelLbl = new JLabel("Model:");
        modelLbl.setFont(labelFont);
        panel.add(modelLbl, gbc);

        gbc.gridx = 1;
        JLabel modelVal = new JLabel(safe(item.getModelCode()));
        modelVal.setFont(valueFont);
        panel.add(modelVal, gbc);

        // Row 2: Quantity in stock
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel stockLbl = new JLabel("In stock:");
        stockLbl.setFont(labelFont);
        panel.add(stockLbl, gbc);

        gbc.gridx = 1;
        JLabel stockVal = new JLabel(item.getQuantityOnHand() + " (min " + item.getMinimumQuantity() + ")");
        stockVal.setFont(valueFont);
        panel.add(stockVal, gbc);

        // Row 3: Quantity to order
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel qtyLbl = new JLabel("Quantity to order:");
        qtyLbl.setFont(labelFont);
        panel.add(qtyLbl, gbc);

        int suggestedQty = Math.max(item.getMinimumQuantity() * 2 - item.getQuantityOnHand(), 1);
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(suggestedQty, 1, 10000, 1));
        qtySpinner.setFont(valueFont);
        gbc.gridx = 1;
        panel.add(qtySpinner, gbc);

        // Row 4: Supplier (scrollable list)  ----------------- FIXED SIZING
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel supplierLbl = new JLabel("Supplier:");
        supplierLbl.setFont(labelFont);
        panel.add(supplierLbl, gbc);

        List<String> allSuppliers = inventoryService.getAllSuppliers();
        DefaultListModel<String> supplierListModel = new DefaultListModel<>();
        for (String s : allSuppliers) {
            supplierListModel.addElement(s);
        }

        JList<String> supplierList = new JList<>(supplierListModel);
        supplierList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierList.setVisibleRowCount(5);
        supplierList.setFont(valueFont);

        // Preselect item's own supplier if present
        if (item.getSupplier() != null && !item.getSupplier().trim().isEmpty()) {
            supplierList.setSelectedValue(item.getSupplier().trim(), true);
        }

        JScrollPane supplierScroll = new JScrollPane(supplierList);
        supplierScroll.setPreferredSize(new Dimension(220, 90));

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(supplierScroll, gbc);
        // reset for following rows
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 5: Requested by
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel reqLbl = new JLabel("Requested by:");
        reqLbl.setFont(labelFont);
        panel.add(reqLbl, gbc);

        gbc.gridx = 1;
        JTextField requestedByField = new JTextField(18);
        requestedByField.setFont(valueFont);
        panel.add(requestedByField, gbc);

        // Row 6: Notes
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel notesLbl = new JLabel("Notes:");
        notesLbl.setFont(labelFont);
        panel.add(notesLbl, gbc);

        gbc.gridx = 1;
        JTextArea notesArea = new JTextArea(4, 18);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(valueFont);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(notesScroll, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setBackground(new Color(245, 247, 252));

        JButton submitBtn = new JButton("Submit Order");
        JButton cancelBtn = new JButton("Cancel");

        submitBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btnPanel.add(submitBtn);
        btnPanel.add(cancelBtn);

        submitBtn.addActionListener(e -> {
            int qty = (Integer) qtySpinner.getValue();
            String selectedSupplier = supplierList.getSelectedValue();
            String requestedBy = requestedByField.getText().trim();
            String notes = notesArea.getText().trim();

            if (qty <= 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Quantity to order must be greater than zero.",
                        "Invalid Quantity",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selectedSupplier == null || selectedSupplier.trim().isEmpty()) {
                int opt = JOptionPane.showConfirmDialog(
                        dialog,
                        "No supplier selected. Proceed without supplier?",
                        "No Supplier Selected",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (opt != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                orderService.placeOrder(item, qty,
                        selectedSupplier == null ? null : selectedSupplier.trim(),
                        requestedBy, notes);

                JOptionPane.showMessageDialog(dialog,
                        "Order submitted successfully.",
                        "Order Placed",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                        "Error placing order:\n" + ex.getMessage(),
                        "Order Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ------------------------------------------------------
    // Orders placed view with status update + double-click edit
    // ------------------------------------------------------
    private void showOrdersPlacedDialog() {
        JDialog dialog = new JDialog(this, "Orders Placed", true);
        dialog.setSize(950, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] columns = {
                "ID", "Item Name", "Model", "Qty Stock",
                "Qty Ordered", "Status", "Supplier", "Requested By", "Order Date"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        JTable table = new JTable(model);
        table.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(table);

        // Top: status filter
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        top.setBackground(new Color(245, 247, 252));

        JLabel filterLabel = new JLabel("Filter by status:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JComboBox<String> statusFilterCombo = new JComboBox<>(new String[]{
                "All", "Pending", "Submitted", "Completed", "Cancelled"
        });
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        top.add(filterLabel);
        top.add(statusFilterCombo);

        dialog.add(top, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);

        // Buttons: status updates + close
        JPanel south = new JPanel(new BorderLayout());
        south.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        south.setBackground(new Color(245, 247, 252));

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        leftButtons.setOpaque(false);

        JButton markSubmitted = new JButton("Mark as Submitted");
        JButton markCompleted = new JButton("Mark as Completed");
        JButton markCancelled = new JButton("Mark as Cancelled");

        leftButtons.add(markSubmitted);
        leftButtons.add(markCompleted);
        leftButtons.add(markCancelled);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        rightButtons.setOpaque(false);

        JButton closeBtn = new JButton("Close");
        rightButtons.add(closeBtn);

        south.add(leftButtons, BorderLayout.WEST);
        south.add(rightButtons, BorderLayout.EAST);

        dialog.add(south, BorderLayout.SOUTH);

        // Helper: refresh orders according to current filter
        Runnable refreshTable = () -> {
            String filter = statusFilterCombo.getSelectedItem().toString();
            List<InventoryOrder> updatedOrders = orderService.getOrdersByStatus(filter);
            model.setRowCount(0);
            for (InventoryOrder o : updatedOrders) {
                String dateStr = (o.getOrderDate() == null)
                        ? "—"
                        : fmt.format(o.getOrderDate());
                model.addRow(new Object[]{
                        o.getId(),
                        safe(o.getItemName()),
                        safe(o.getModelCode()),
                        o.getQuantityInStock(),
                        o.getQuantityOrdered(),
                        safe(o.getStatus()),
                        safe(o.getSupplier()),
                        safe(o.getRequestedBy()),
                        dateStr
                });
            }
        };

        // Initial load
        refreshTable.run();

        // Filter listener
        statusFilterCombo.addActionListener(e -> refreshTable.run());

        // Double-click to edit order
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row == -1) return;
                    int orderId = (int) model.getValueAt(row, 0);
                    showEditOrderDialog(orderId, dialog, refreshTable);
                }
            }
        });

        markSubmitted.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog,
                        "Select an order to update.",
                        "Update Status",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int orderId = (int) model.getValueAt(row, 0);
            orderService.updateOrderStatus(orderId, "Submitted");
            refreshTable.run();
        });

        markCompleted.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog,
                        "Select an order to update.",
                        "Update Status",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int orderId = (int) model.getValueAt(row, 0);
            orderService.updateOrderStatus(orderId, "Completed");
            refreshTable.run();
        });

        markCancelled.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog,
                        "Select an order to update.",
                        "Update Status",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int orderId = (int) model.getValueAt(row, 0);
            orderService.updateOrderStatus(orderId, "Cancelled");
            refreshTable.run();
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // ------------------------------------------------------
    // Edit / Delete single order (double-click)
    // ------------------------------------------------------
    private void showEditOrderDialog(int orderId, JDialog parent, Runnable refreshTable) {
        InventoryOrder order = orderService.getAllOrders()
                .stream()
                .filter(o -> o.getId() == orderId)
                .findFirst()
                .orElse(null);

        if (order == null) {
            JOptionPane.showMessageDialog(parent,
                    "Could not load this order (it may have been deleted).",
                    "Edit Order",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(parent, "Edit Order #" + orderId, true);
        dlg.setSize(560, 380);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 13);

        int row = 0;

        // Item name
        gbc.gridx = 0; gbc.gridy = row;
        JLabel nameLbl = new JLabel("Item:");
        nameLbl.setFont(labelFont);
        panel.add(nameLbl, gbc);

        gbc.gridx = 1;
        JLabel nameVal = new JLabel(safe(order.getItemName()));
        nameVal.setFont(valueFont);
        panel.add(nameVal, gbc);

        // Model
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel modelLbl = new JLabel("Model:");
        modelLbl.setFont(labelFont);
        panel.add(modelLbl, gbc);

        gbc.gridx = 1;
        JLabel modelVal = new JLabel(safe(order.getModelCode()));
        modelVal.setFont(valueFont);
        panel.add(modelVal, gbc);

        // Quantities
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel stockLbl = new JLabel("Qty in stock:");
        stockLbl.setFont(labelFont);
        panel.add(stockLbl, gbc);

        gbc.gridx = 1;
        JLabel stockVal = new JLabel(String.valueOf(order.getQuantityInStock()));
        stockVal.setFont(valueFont);
        panel.add(stockVal, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel orderedLbl = new JLabel("Qty ordered:");
        orderedLbl.setFont(labelFont);
        panel.add(orderedLbl, gbc);

        gbc.gridx = 1;
        JSpinner qtySpinner = new JSpinner(
                new SpinnerNumberModel(order.getQuantityOrdered(), 1, 100000, 1));
        qtySpinner.setFont(valueFont);
        panel.add(qtySpinner, gbc);

        // Supplier (scrollable list) ------------------------ FIXED SIZING
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel supplierLbl = new JLabel("Supplier:");
        supplierLbl.setFont(labelFont);
        panel.add(supplierLbl, gbc);

        List<String> suppliers = inventoryService.getAllSuppliers();
        DefaultListModel<String> supplierModel = new DefaultListModel<>();
        for (String s : suppliers) {
            supplierModel.addElement(s);
        }

        JList<String> supplierList = new JList<>(supplierModel);
        supplierList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierList.setVisibleRowCount(5);
        supplierList.setFont(valueFont);

        if (order.getSupplier() != null && !order.getSupplier().trim().isEmpty()) {
            supplierList.setSelectedValue(order.getSupplier().trim(), true);
        }

        JScrollPane supplierScroll = new JScrollPane(supplierList);
        supplierScroll.setPreferredSize(new Dimension(220, 90));

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(supplierScroll, gbc);
        // reset for following rows
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Status
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(labelFont);
        panel.add(statusLbl, gbc);

        gbc.gridx = 1;
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{
                "Pending", "Submitted", "Completed", "Cancelled"
        });
        statusCombo.setFont(valueFont);
        statusCombo.setSelectedItem(safe(order.getStatus()));
        panel.add(statusCombo, gbc);

        // Requested by
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel reqLbl = new JLabel("Requested by:");
        reqLbl.setFont(labelFont);
        panel.add(reqLbl, gbc);

        gbc.gridx = 1;
        JTextField reqField = new JTextField(20);
        reqField.setFont(valueFont);
        reqField.setText(order.getRequestedBy() == null ? "" : order.getRequestedBy());
        panel.add(reqField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setBackground(new Color(245, 247, 252));

        JButton saveBtn = new JButton("Save");
        JButton deleteBtn = new JButton("Delete");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btnPanel.add(deleteBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        saveBtn.addActionListener(e -> {
            int newQty = (Integer) qtySpinner.getValue();
            String newSupplier = supplierList.getSelectedValue();
            String newStatus = statusCombo.getSelectedItem().toString();
            String newRequestedBy = reqField.getText().trim();

            if (newQty <= 0) {
                JOptionPane.showMessageDialog(dlg,
                        "Quantity ordered must be greater than zero.",
                        "Edit Order",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newSupplier == null || newSupplier.trim().isEmpty()) {
                int opt = JOptionPane.showConfirmDialog(
                        dlg,
                        "No supplier selected. Save without supplier?",
                        "No Supplier Selected",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (opt != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                orderService.updateOrderDetails(orderId,
                        newQty,
                        newSupplier == null ? null : newSupplier.trim(),
                        newRequestedBy,
                        newStatus);
                JOptionPane.showMessageDialog(dlg,
                        "Order updated.",
                        "Edit Order",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshTable.run();
                dlg.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg,
                        "Error updating order:\n" + ex.getMessage(),
                        "Edit Order",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    dlg,
                    "Are you sure you want to delete this order?",
                    "Delete Order",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                orderService.deleteOrder(orderId);
                JOptionPane.showMessageDialog(dlg,
                        "Order deleted.",
                        "Delete Order",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshTable.run();
                dlg.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg,
                        "Error deleting order:\n" + ex.getMessage(),
                        "Delete Order",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dlg.dispose());

        dlg.add(panel, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ------------------------------------------------------
    // Item details popup (for alert tables)
    // ------------------------------------------------------
    private void showItemDetailsDialog(InventoryItem item) {
        LocalDate today = LocalDate.now();
        int days = (Integer) daysSpinner.getValue();
        LocalDate soonLimit = today.plusDays(days);

        int lowThreshold = (lowStockThresholdSpinner != null)
                ? (Integer) lowStockThresholdSpinner.getValue()
                : 0;

        String stockStatus = computeStockStatus(item, lowThreshold);
        String expiryStatus = computeExpiryStatus(item, today, soonLimit);

        String badgeText = expiryStatus.equals("Expired") || expiryStatus.equals("Expiring soon")
                ? expiryStatus
                : stockStatus;

        JDialog dlg = new JDialog(this, "Item Details", true);
        dlg.setSize(520, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(20, 0));
        root.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));
        root.setBackground(Color.WHITE);

        // LEFT: icon + category
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(120, 160));

        JLabel iconLabel = new JLabel(getCategoryIcon(item.getCategory()), SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel catLabel = new JLabel(safe(item.getCategory()));
        catLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        catLabel.setForeground(new Color(70, 70, 80));
        catLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(iconLabel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(catLabel);

        // RIGHT: details
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel(safe(item.getItemName()));
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightPanel.add(title);
        rightPanel.add(Box.createVerticalStrut(10));

        // Status badge row
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusRow.setOpaque(false);

        JLabel statusLabel = new JLabel("Overall status: ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel badge = new JLabel(badgeText);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        badge.setBackground(getStatusBadgeColor(badgeText));
        badge.setForeground(Color.WHITE);

        statusRow.add(statusLabel);
        statusRow.add(Box.createHorizontalStrut(5));
        statusRow.add(badge);

        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(statusRow);
        rightPanel.add(Box.createVerticalStrut(10));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        rightPanel.add(sep);
        rightPanel.add(Box.createVerticalStrut(10));

        // Helper to add rows
        java.util.function.BiConsumer<String, String> addRow = (labelText, value) -> {
            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setOpaque(false);
            rowPanel.setMaximumSize(new Dimension(400, 26));

            JLabel left = new JLabel(labelText + ": ");
            left.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JLabel right = new JLabel(value);
            right.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            right.setForeground(new Color(60, 60, 60));

            rowPanel.add(left, BorderLayout.WEST);
            rowPanel.add(right, BorderLayout.CENTER);

            rightPanel.add(rowPanel);
            rightPanel.add(Box.createVerticalStrut(6));
        };

        addRow.accept("Stock status", stockStatus);
        addRow.accept("Expiry status", expiryStatus);
        addRow.accept("Subcategory", safe(item.getSubcategory()));
        addRow.accept("Model", safe(item.getModelCode()));
        addRow.accept("Batch", safe(item.getBatchNumber()));
        addRow.accept("Location", safe(item.getLocation()));
        addRow.accept("Quantity", item.getQuantityOnHand() + " (min " + item.getMinimumQuantity() + ")");
        addRow.accept("Expiry", item.getExpiryDate() == null ? "—" : item.getExpiryDate().toString());
        addRow.accept("Supplier", safe(item.getSupplier()));

        root.add(leftPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        dlg.add(root, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setBackground(new Color(245, 247, 252));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        bottom.add(close);

        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private Color getStatusBadgeColor(String status) {
        if (status == null) return new Color(120, 120, 120);
        String s = status.trim().toLowerCase();
        switch (s) {
            case "expired":
                return new Color(198, 40, 40);      // red
            case "expiring soon":
                return new Color(251, 192, 45);     // amber
            case "low stock":
                return new Color(245, 124, 0);      // orange
            case "in stock":
                return new Color(46, 125, 50);      // green
            case "out of stock":
                return new Color(97, 97, 97);       // grey
            default:
                return new Color(120, 120, 120);    // default grey
        }
    }

    private String getCategoryIcon(String category) {
        if (category == null) return "📦";
        String c = category.trim().toLowerCase();
        if (c.equals("medicine")) return "💊";
        if (c.equals("equipment")) return "🩺";
        if (c.equals("consumables")) return "📦";
        return "📦";
    }
}
