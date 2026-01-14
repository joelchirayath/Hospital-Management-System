package giis.demo.ui.manager;

import giis.demo.jdbc.models.InventoryItem;
import giis.demo.service.manager.InventoryService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class InventoryDashboardWindow extends JFrame {

    private final InventoryService service = new InventoryService();

    // Data
    private List<InventoryItem> allItems = new ArrayList<>();

    // Table
    private JTable table;
    private DefaultTableModel model;
    private JScrollPane tableScroll;

    // Cards (table vs empty state)
    private JPanel cardPanel;
    private JPanel emptyPanel;
    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    // Filters
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> locationFilter;
    private JComboBox<String> statusFilter;

    // NEW — low stock threshold
    private JSpinner lowStockSpinner;
    private JLabel lowStockLabel;

    // NEW — expiry range filter
    private JComboBox<String> expiryFilter;
    private JLabel expiryFromLabel, expiryToLabel;
    private JSpinner expiryFromSpinner, expiryToSpinner;

    // KPI labels
    private JLabel totalItemsValue;
    private JLabel lowStockValue;
    private JLabel expiredValue;
    private JLabel expiringSoonValue;

    // Status bar
    private JLabel lastUpdatedLabel;

    // Auto refresh
    private Timer autoRefreshTimer;

    public InventoryDashboardWindow() {
        setTitle("Inventory Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        // Initial load
        loadAllItems();

        // Auto-refresh every 60s
        autoRefreshTimer = new Timer(60000, e -> loadAllItems());
        autoRefreshTimer.start();
    }

    // ------------------------------------------------------
    // Header: title + hospital subtitle
    // ------------------------------------------------------
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        header.setBackground(new Color(245, 247, 252));

        JLabel title = new JLabel("Inventory Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 30, 30));

        JLabel subtitle = new JLabel("Hospital Central – Supplies, Equipment & Medicines");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(subtitle);

        header.add(left, BorderLayout.WEST);

        return header;
    }

    // ------------------------------------------------------
    // Main: KPIs + filters + table/empty
    // ------------------------------------------------------
    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));
        main.setBackground(Color.WHITE);

        main.add(createKpiPanel(), BorderLayout.NORTH);
        main.add(createCenterPanel(), BorderLayout.CENTER);

        return main;
    }

    // KPI cards row
    private JPanel createKpiPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.setBackground(Color.WHITE);

        // Total: keep default soft blue
        totalItemsValue = createKpiCard(
                panel,
                "Total Items",
                new Color(250, 252, 255)
        );

        // Low stock: orange
        lowStockValue = createKpiCard(
                panel,
                "Low Stock Items",
                new Color(255, 224, 178)   // light orange
        );

        // Expired: red
        expiredValue = createKpiCard(
                panel,
                "Expired Items",
                new Color(255, 205, 210)   // light red/pink
        );

        // Expiring soon: yellow
        expiringSoonValue = createKpiCard(
                panel,
                "Expiring in 30 Days",
                new Color(255, 249, 196)   // light yellow
        );

        return panel;
    }

    private JLabel createKpiCard(JPanel parent, String labelText, Color backgroundColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setBackground(backgroundColor);
        card.setOpaque(true);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14)); // bold & larger
        label.setForeground(new Color(60, 60, 70));

        JLabel value = new JLabel("0");
        value.setFont(new Font("Segoe UI", Font.BOLD, 20));
        value.setForeground(new Color(40, 40, 60));
        value.setOpaque(false);

        card.add(label);
        card.add(Box.createVerticalStrut(5));
        card.add(value);

        parent.add(card);
        return value;
    }

    // Center area: filters + table/empty
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);

        center.add(createFilterBar(), BorderLayout.NORTH);
        center.add(createCardPanel(), BorderLayout.CENTER);

        return center;
    }

    // Filters + search + buttons
    private JPanel createFilterBar() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        Font filterLabelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font filterControlFont = new Font("Segoe UI", Font.BOLD, 13);

        // Search
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(filterLabelFont);

        JTextField sf = new JTextField();
        sf.setColumns(12);                     // smaller width
        sf.setFont(filterControlFont);
        searchField = sf;

        JScrollPane searchScroll = new JScrollPane(sf);
        searchScroll.setPreferredSize(new Dimension(150, 28));
        searchScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        searchScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Auto-suggest popup (based on allItems)
        JPopupMenu suggestPopup = new JPopupMenu();
        suggestPopup.setFocusable(false);

        sf.getDocument().addDocumentListener(new DocumentListener() {
            private void showPopup() {
                suggestPopup.removeAll();
                String text = sf.getText().trim().toLowerCase();
                if (text.isEmpty()) {
                    suggestPopup.setVisible(false);
                    return;
                }

                // Build suggestions dynamically from latest allItems
                List<String> names = new ArrayList<>();
                for (InventoryItem it : allItems) {
                    String name = it.getItemName();
                    if (name != null && !names.contains(name)) {
                        names.add(name);
                    }
                }

                for (String name : names) {
                    if (name.toLowerCase().contains(text)) {
                        JMenuItem mi = new JMenuItem(name);
                        mi.addActionListener(e -> {
                            sf.setText(name);
                            suggestPopup.setVisible(false);
                            applySearchAndFilters();
                        });
                        suggestPopup.add(mi);
                    }
                }
                if (suggestPopup.getComponentCount() > 0) {
                    suggestPopup.show(sf, 0, sf.getHeight());
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { showPopup(); applySearchAndFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { showPopup(); applySearchAndFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { showPopup(); applySearchAndFilters(); }
        });

        // Category
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(filterLabelFont);

        categoryFilter = new JComboBox<>(new String[]{
                "All", "Medicine", "Equipment", "Consumables", "Others"
        });
        categoryFilter.setFont(filterControlFont);

        // Location
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setFont(filterLabelFont);

        locationFilter = new JComboBox<>(new String[]{
                "All", "Main Pharmacy", "Ward A", "Ward B", "ICU Store",
                "OT Store", "Cardiology", "Admin Office"
        });
        locationFilter.setFont(filterControlFont);

        // Status (stock only)
        JLabel statusLabel = new JLabel("Stock Status:");
        statusLabel.setFont(filterLabelFont);

        statusFilter = new JComboBox<>(new String[]{
                "All", "In stock", "Low stock", "Out of stock"
        });
        statusFilter.setFont(filterControlFont);

        // NEW — Low stock threshold controls (hidden by default)
        lowStockLabel = new JLabel("Max Qty(inclusive):");
        lowStockLabel.setFont(filterLabelFont);
        lowStockLabel.setVisible(false);

        lowStockSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 10000, 1));
        lowStockSpinner.setFont(filterControlFont);
        lowStockSpinner.setVisible(false);

        // NEW — Expiry filter
        JLabel expiryLabel = new JLabel("Expiry Filter:");
        expiryLabel.setFont(filterLabelFont);

        expiryFilter = new JComboBox<>(new String[]{
                "All", "Expired", "Expiring ≤ 30 days", "Custom range"
        });
        expiryFilter.setFont(filterControlFont);

        // NEW — date range controls (hidden)
        expiryFromLabel = new JLabel("From:");
        expiryFromLabel.setFont(filterLabelFont);
        expiryFromLabel.setVisible(false);

        expiryFromSpinner = new JSpinner(new SpinnerDateModel());
        expiryFromSpinner.setFont(filterControlFont);
        expiryFromSpinner.setVisible(false);

        expiryToLabel = new JLabel("To:");
        expiryToLabel.setFont(filterLabelFont);
        expiryToLabel.setVisible(false);

        expiryToSpinner = new JSpinner(new SpinnerDateModel());
        expiryToSpinner.setFont(filterControlFont);
        expiryToSpinner.setVisible(false);

        // Format date pickers
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(expiryFromSpinner, "yyyy-MM-dd");
        expiryFromSpinner.setEditor(fromEditor);

        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(expiryToSpinner, "yyyy-MM-dd");
        expiryToSpinner.setEditor(toEditor);

        JButton resetButton = new JButton("Reset Filters");
        resetButton.setFont(filterControlFont);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(filterControlFont);

        // Layout row 0
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(searchLabel, gbc);
        gbc.gridx = 1;
        panel.add(searchScroll, gbc);

        gbc.gridx = 2;
        panel.add(categoryLabel, gbc);
        gbc.gridx = 3;
        panel.add(categoryFilter, gbc);

        gbc.gridx = 4;
        panel.add(locationLabel, gbc);
        gbc.gridx = 5;
        panel.add(locationFilter, gbc);

        gbc.gridx = 6;
        panel.add(statusLabel, gbc);
        gbc.gridx = 7;
        panel.add(statusFilter, gbc);

        gbc.gridx = 8;
        panel.add(resetButton, gbc);

        gbc.gridx = 9;
        panel.add(refreshButton, gbc);

        // Row 2 — Low Stock Threshold (conditional)
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lowStockLabel, gbc);
        gbc.gridx = 1;
        panel.add(lowStockSpinner, gbc);

        // --- Row 3: Expiry Filter + From + To (all in one line) ---
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(expiryLabel, gbc);

        gbc.gridx = 1;
        panel.add(expiryFilter, gbc);

        // From:
        gbc.gridx = 2;
        panel.add(expiryFromLabel, gbc);

        gbc.gridx = 3;
        panel.add(expiryFromSpinner, gbc);

        // To:
        gbc.gridx = 4;
        panel.add(expiryToLabel, gbc);

        gbc.gridx = 5;
        panel.add(expiryToSpinner, gbc);

        // Listeners
        categoryFilter.addActionListener(e -> applySearchAndFilters());
        locationFilter.addActionListener(e -> applySearchAndFilters());

        statusFilter.addActionListener(e -> {
            boolean show = "Low stock".equals(statusFilter.getSelectedItem().toString());
            lowStockLabel.setVisible(show);
            lowStockSpinner.setVisible(show);
            applySearchAndFilters();
        });

        expiryFilter.addActionListener(e -> {
            boolean custom = "Custom range".equals(expiryFilter.getSelectedItem());
            expiryFromLabel.setVisible(custom);
            expiryFromSpinner.setVisible(custom);
            expiryToLabel.setVisible(custom);
            expiryToSpinner.setVisible(custom);
            applySearchAndFilters();
        });

        lowStockSpinner.addChangeListener(e -> applySearchAndFilters());
        expiryFromSpinner.addChangeListener(e -> applySearchAndFilters());
        expiryToSpinner.addChangeListener(e -> applySearchAndFilters());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            locationFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            expiryFilter.setSelectedIndex(0);

            lowStockLabel.setVisible(false);
            lowStockSpinner.setVisible(false);

            expiryFromLabel.setVisible(false);
            expiryFromSpinner.setVisible(false);
            expiryToLabel.setVisible(false);
            expiryToSpinner.setVisible(false);

            applySearchAndFilters();
        });

        refreshButton.addActionListener(e -> loadAllItems());

        return panel;
    }

    // Card panel: table view vs empty view
    private JPanel createCardPanel() {
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(Color.WHITE);

        // Table
        model = new DefaultTableModel(
                new String[]{
                        "Name", "Category", "Subcategory", "Model",
                        "Batch", "Location", "Qty", "Min Qty",
                        "Expiry", "Stock Status", "Expiry Status"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(24);

        // Table header styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Double-click to show details
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int modelRow = table.convertRowIndexToModel(row);
                        showItemDetails(modelRow);
                    }
                }
            }
        });

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Custom cell renderer for highlight rules
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                int modelRow = tbl.convertRowIndexToModel(row);
                String stockStatus = model.getValueAt(modelRow, 9).toString();
                String expiryStatus = model.getValueAt(modelRow, 10).toString();

                Color base = Color.WHITE;

                if ("Expired".equalsIgnoreCase(expiryStatus)) {
                    base = new Color(255, 200, 200); // light red
                } else if (expiryStatus.toLowerCase().startsWith("expiring")) {
                    base = new Color(255, 230, 180); // light orange
                } else if ("Low stock".equalsIgnoreCase(stockStatus)
                        || "Out of stock".equalsIgnoreCase(stockStatus)) {
                    base = new Color(255, 245, 200); // light yellow
                }

                if (isSelected) {
                    c.setBackground(base.darker());
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(base);
                    c.setForeground(Color.BLACK);
                }

                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    // Right-align numeric columns
                    if (column == 6 || column == 7) {
                        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }

                return c;
            }
        });

        tableScroll = new JScrollPane(table);

        // Empty state panel
        emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);

        JLabel icon = new JLabel("\uD83D\uDD0E", SwingConstants.CENTER); // magnifying glass
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JLabel msg = new JLabel("No items found", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel sub = new JLabel("Try adjusting your filters or search term.", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(110, 110, 110));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(icon);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(msg);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(sub);

        emptyPanel.add(textPanel, BorderLayout.CENTER);

        cardPanel.add(tableScroll, CARD_TABLE);
        cardPanel.add(emptyPanel, CARD_EMPTY);

        return cardPanel;
    }

    // Status bar with "Last updated" + Add Item button
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        bar.setBackground(new Color(245, 247, 252));

        lastUpdatedLabel = new JLabel("Last updated: —");
        lastUpdatedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lastUpdatedLabel.setForeground(new Color(90, 90, 90));

        bar.add(lastUpdatedLabel, BorderLayout.WEST);

        // Right side: Add Item button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        JButton addItemButton = new JButton("Add Item");
        addItemButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addItemButton.addActionListener(e -> showAddItemDialog());

        rightPanel.add(addItemButton);
        bar.add(rightPanel, BorderLayout.EAST);

        return bar;
    }

    // ------------------------------------------------------
    // Data loading and filtering
    // ------------------------------------------------------
    private void loadAllItems() {
        allItems = service.getAllItems();
        applySearchAndFilters(); // also updates KPIs + view
        updateLastUpdatedTime();
    }

    private void applySearchAndFilters() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        String selectedCategory = categoryFilter.getSelectedItem().toString();
        String selectedLocation = locationFilter.getSelectedItem().toString();
        String selectedStatus = statusFilter.getSelectedItem().toString();
        String expiryMode = expiryFilter.getSelectedItem().toString();

        boolean filterLowStock = "Low stock".equals(selectedStatus);
        int customLowThreshold = (int) lowStockSpinner.getValue();

        LocalDate fromDate = null;
        LocalDate toDate = null;

        if ("Custom range".equals(expiryMode)) {
            java.util.Date f = (java.util.Date) expiryFromSpinner.getValue();
            java.util.Date t = (java.util.Date) expiryToSpinner.getValue();
            fromDate = LocalDate.parse(new java.sql.Date(f.getTime()).toString());
            toDate = LocalDate.parse(new java.sql.Date(t.getTime()).toString());
        }

        List<InventoryItem> filtered = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate soonLimit = today.plusDays(30);

        for (InventoryItem item : allItems) {
            String itemName = item.getItemName() != null ? item.getItemName().toLowerCase() : "";
            String itemModel = item.getModelCode() != null ? item.getModelCode().toLowerCase() : "";
            String itemBatch = item.getBatchNumber() != null ? item.getBatchNumber().toLowerCase() : "";

            String itemCategory = item.getCategory() != null ? item.getCategory().trim() : "";
            String itemLocation = item.getLocation() != null ? item.getLocation().trim() : "";

            // Search by name / model / batch
            if (!query.isEmpty()) {
                if (!(itemName.contains(query) ||
                        itemModel.contains(query) ||
                        itemBatch.contains(query))) {
                    continue;
                }
            }

            // Category filter (trim + ignore case)
            if (!"All".equals(selectedCategory)) {
                if (!itemCategory.equalsIgnoreCase(selectedCategory.trim())) {
                    continue;
                }
            }

            // Location filter
            if (!"All".equals(selectedLocation)) {
                if (!itemLocation.equalsIgnoreCase(selectedLocation.trim())) {
                    continue;
                }
            }

            // Compute statuses
            String stockStatus = computeStockStatus(item);
            String expiryStatus = computeExpiryStatus(item, today, soonLimit);

            // Status filter / Low stock custom threshold (stock-based only)
            if (filterLowStock) {
                // use quantity <= custom threshold instead of stockStatus text
                if (item.getQuantityOnHand() > customLowThreshold) {
                    continue;
                }
            } else if (!"All".equals(selectedStatus)) {
                if (!stockStatus.equalsIgnoreCase(selectedStatus)) {
                    continue;
                }
            }

            // Expiry filter (separate from status filter)
            LocalDate exp = item.getExpiryDate();
            switch (expiryMode) {
                case "Expired":
                    if (exp == null || !exp.isBefore(today)) {
                        continue;
                    }
                    break;

                case "Expiring ≤ 30 days":
                    if (exp == null ||
                            exp.isBefore(today) ||
                            exp.isAfter(soonLimit)) {
                        continue;
                    }
                    break;

                case "Custom range":
                    if (exp == null || fromDate == null || toDate == null ||
                            exp.isBefore(fromDate) || exp.isAfter(toDate)) {
                        continue;
                    }
                    break;

                case "All":
                default:
                    // no extra filter
                    break;
            }

            filtered.add(item);
        }

        populateTable(filtered);
        updateKpis(filtered);
    }

    private String computeStockStatus(InventoryItem i) {
        int qty = i.getQuantityOnHand();
        int min = i.getMinimumQuantity();
        if (qty <= 0) return "Out of stock";
        if (qty <= min) return "Low stock";
        return "In stock";
    }

    private String computeExpiryStatus(InventoryItem i, LocalDate today, LocalDate soonLimit) {
        LocalDate exp = i.getExpiryDate();
        if (exp == null) return "No expiry";
        if (exp.isBefore(today)) return "Expired";
        if (!exp.isAfter(soonLimit)) return "Expiring soon";
        return "OK";
    }

    private void populateTable(List<InventoryItem> items) {
        model.setRowCount(0);

        if (items.isEmpty()) {
            showEmptyState();
            return;
        }

        showTableState();

        LocalDate today = LocalDate.now();
        LocalDate soonLimit = today.plusDays(30);

        for (InventoryItem i : items) {
            Object expiry = i.getExpiryDate() == null ? "—" : i.getExpiryDate().toString();

            String stockStatus = computeStockStatus(i);
            String expiryStatus = computeExpiryStatus(i, today, soonLimit);

            model.addRow(new Object[]{
                    i.getItemName(),
                    i.getCategory(),
                    i.getSubcategory(),
                    i.getModelCode(),
                    i.getBatchNumber(),
                    i.getLocation(),
                    i.getQuantityOnHand(),
                    i.getMinimumQuantity(),
                    expiry,
                    stockStatus,
                    expiryStatus
            });
        }
    }

    private void showEmptyState() {
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, CARD_EMPTY);
    }

    private void showTableState() {
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, CARD_TABLE);
    }

    private void updateKpis(List<InventoryItem> items) {
        int total = items.size();
        int lowStock = 0;
        int expired = 0;
        int expiringSoon = 0;

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(30);

        for (InventoryItem i : items) {
            int qty = i.getQuantityOnHand();
            int min = i.getMinimumQuantity();

            if (qty <= min) {
                lowStock++;
            }

            LocalDate exp = i.getExpiryDate();
            if (exp != null) {
                if (exp.isBefore(today)) {
                    expired++;
                } else if (!exp.isAfter(limit)) {
                    expiringSoon++;
                }
            }
        }

        totalItemsValue.setText(String.valueOf(total));
        lowStockValue.setText(String.valueOf(lowStock));
        expiredValue.setText(String.valueOf(expired));
        expiringSoonValue.setText(String.valueOf(expiringSoon));
    }

    private void updateLastUpdatedTime() {
        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        lastUpdatedLabel.setText("Last updated: " + formatted);
    }

    // ------------------------------------------------------
    // ADD ITEM DIALOG (with preloaded, type-in combos)
    // ------------------------------------------------------
    private void showAddItemDialog() {
        JDialog dlg = new JDialog(this, "Add Inventory Item", true);
        dlg.setSize(520, 520);
        dlg.setLocationRelativeTo(this);
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
        
        

        RowIndex rowIdx = new RowIndex();

        // Build distinct lists from existing items
        List<String> categories = new ArrayList<>();
        List<String> subcategories = new ArrayList<>();
        List<String> locations = new ArrayList<>();
        List<String> suppliers = new ArrayList<>();

        for (InventoryItem it : allItems) {
            String c = trimOrNull(it.getCategory());
            if (c != null && !categories.contains(c)) {
                categories.add(c);
            }
            String s = trimOrNull(it.getSubcategory());
            if (s != null && !subcategories.contains(s)) {
                subcategories.add(s);
            }
            String loc = trimOrNull(it.getLocation());
            if (loc != null && !locations.contains(loc)) {
                locations.add(loc);
            }
            String sup = trimOrNull(it.getSupplier());
            if (sup != null && !suppliers.contains(sup)) {
                suppliers.add(sup);
            }
        }

        // Helper to add labeled text field
        java.util.function.BiFunction<String, Integer, JTextField> addTextField =
                (labelText, width) -> {
                    JLabel lbl = new JLabel(labelText + ":");
                    lbl.setFont(labelFont);
                    gbc.gridx = 0;
                    gbc.gridy = rowIdx.get();
                    gbc.weightx = 0;
                    gbc.fill = GridBagConstraints.NONE;
                    panel.add(lbl, gbc);

                    JTextField field = new JTextField(width);
                    field.setFont(valueFont);
                    gbc.gridx = 1;
                    gbc.weightx = 1.0;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    panel.add(field, gbc);

                    rowIdx.inc();
                    return field;
                };

        // Name (plain text)
        JTextField nameField = addTextField.apply("Name", 20);

        // Category (editable combo)
        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = rowIdx.get();
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(catLbl, gbc);

        JComboBox<String> categoryCombo = new JComboBox<>(categories.toArray(new String[0]));
        categoryCombo.setEditable(true);
        categoryCombo.setFont(valueFont);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(categoryCombo, gbc);
        rowIdx.inc();

        // Subcategory (editable combo)
        JLabel subcatLbl = new JLabel("Subcategory:");
        subcatLbl.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = rowIdx.get();
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(subcatLbl, gbc);

        JComboBox<String> subcategoryCombo = new JComboBox<>(subcategories.toArray(new String[0]));
        subcategoryCombo.setEditable(true);
        subcategoryCombo.setFont(valueFont);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(subcategoryCombo, gbc);
        rowIdx.inc();

        // Model (text)
        JTextField modelField = addTextField.apply("Model Code", 20);

        // Batch (text)
        JTextField batchField = addTextField.apply("Batch Number", 20);

        // Location (editable combo)
        JLabel locLbl = new JLabel("Location:");
        locLbl.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = rowIdx.get();
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(locLbl, gbc);

        JComboBox<String> locationCombo = new JComboBox<>(locations.toArray(new String[0]));
        locationCombo.setEditable(true);
        locationCombo.setFont(valueFont);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(locationCombo, gbc);
        rowIdx.inc();

        // Qty
        JLabel qtyLbl = new JLabel("Quantity on hand:");
        qtyLbl.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = rowIdx.get();
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(qtyLbl, gbc);

        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        qtySpinner.setFont(valueFont);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(qtySpinner, gbc);
        rowIdx.inc();

        // Min Qty
        JLabel minLbl = new JLabel("Minimum quantity:");
        minLbl.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = rowIdx.get();
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(minLbl, gbc);

        JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        minSpinner.setFont(valueFont);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(minSpinner, gbc);
        rowIdx.inc();

        // Expiry date
        JLabel expLbl = new JLabel("Expiry (yyyy-MM-dd or blank):");
        expLbl.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = rowIdx.get();
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(expLbl, gbc);

        JTextField expiryField = new JTextField(20);
        expiryField.setFont(valueFont);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(expiryField, gbc);
        rowIdx.inc();

        // Supplier (editable combo)
        JLabel supLbl = new JLabel("Supplier:");
        supLbl.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = rowIdx.get();
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(supLbl, gbc);

        JComboBox<String> supplierCombo = new JComboBox<>(suppliers.toArray(new String[0]));
        supplierCombo.setEditable(true);
        supplierCombo.setFont(valueFont);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(supplierCombo, gbc);
        rowIdx.inc();

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setBackground(new Color(245, 247, 252));

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                        "Name is required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate expiry = null;
            String expiryText = expiryField.getText().trim();
            if (!expiryText.isEmpty()) {
                try {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    expiry = LocalDate.parse(expiryText, fmt);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Invalid expiry date format. Use yyyy-MM-dd or leave blank.",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            int qty = (Integer) qtySpinner.getValue();
            int minQty = (Integer) minSpinner.getValue();

            try {
                InventoryItem item = new InventoryItem();
                item.setItemName(name);
                item.setCategory(emptyToNull(getComboText(categoryCombo)));
                item.setSubcategory(emptyToNull(getComboText(subcategoryCombo)));
                item.setModelCode(emptyToNull(modelField.getText()));
                item.setBatchNumber(emptyToNull(batchField.getText()));
                item.setLocation(emptyToNull(getComboText(locationCombo)));
                item.setQuantityOnHand(qty);
                item.setMinimumQuantity(minQty);
                item.setExpiryDate(expiry);
                item.setSupplier(emptyToNull(getComboText(supplierCombo)));

                service.addItem(item);

                JOptionPane.showMessageDialog(dlg,
                        "Item added successfully.",
                        "Add Item",
                        JOptionPane.INFORMATION_MESSAGE);

                dlg.dispose();
                loadAllItems();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg,
                        "Error adding item:\n" + ex.getMessage(),
                        "Add Item Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dlg.dispose());

        dlg.add(panel, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String getComboText(JComboBox<String> combo) {
        Object sel = combo.getEditor().getItem();
        if (sel == null) return null;
        return sel.toString();
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // ------------------------------------------------------
    // Row detail popup (side-card style) with stock + expiry status
    // + "View Variants" button
    // ------------------------------------------------------
    private void showItemDetails(int modelRow) {

        String rowName = safeCell(modelRow, 0);
        String rowModel = safeCell(modelRow, 3);

        InventoryItem item = allItems.stream()
                .filter(i ->
                        rowName.equals(i.getItemName() == null ? "" : i.getItemName()) &&
                        rowModel.equals(i.getModelCode() == null ? "" : i.getModelCode()))
                .findFirst()
                .orElse(null);

        if (item == null)
            return;

        LocalDate today = LocalDate.now();
        LocalDate soonLimit = today.plusDays(30);

        String stockStatus = computeStockStatus(item);
        String expiryStatus = computeExpiryStatus(item, today, soonLimit);

        // Decide which status to show on badge (priority to expiry)
        String badgeText = expiryStatus.equals("Expired") || expiryStatus.equals("Expiring soon")
                ? expiryStatus
                : stockStatus;

        // Build dialog (instead of JOptionPane) so we can add buttons
        JDialog dlg = new JDialog(this, "Item Details", true);
        dlg.setSize(520, 360);
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

        JLabel catLabel = new JLabel(empty(item.getCategory()));
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
        JLabel title = new JLabel(empty(item.getItemName()));
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
        Color badgeColor = getStatusBadgeColor(badgeText);
        badge.setBackground(badgeColor);
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
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(400, 26));

            JLabel left = new JLabel(labelText + ": ");
            left.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JLabel right = new JLabel(value);
            right.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            right.setForeground(new Color(60, 60, 60));

            row.add(left, BorderLayout.WEST);
            row.add(right, BorderLayout.CENTER);

            rightPanel.add(row);
            rightPanel.add(Box.createVerticalStrut(6));
        };

        // Supplier
        String supplier = empty(item.getSupplier());
        // Fields
        addRow.accept("Stock status", stockStatus);
        addRow.accept("Expiry status", expiryStatus);
        addRow.accept("Subcategory", empty(item.getSubcategory()));
        addRow.accept("Model", empty(item.getModelCode()));
        addRow.accept("Batch", empty(item.getBatchNumber()));
        addRow.accept("Location", empty(item.getLocation()));
        addRow.accept("Quantity", item.getQuantityOnHand() + " (min " + item.getMinimumQuantity() + ")");
        addRow.accept("Expiry", item.getExpiryDate() == null ? "—" : item.getExpiryDate().toString());
        addRow.accept("Supplier", supplier);

        root.add(leftPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        dlg.add(root, BorderLayout.CENTER);

        // Bottom buttons: View Variants + Close
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setBackground(new Color(245, 247, 252));

        JButton variantsBtn = new JButton("View Variants");
        JButton closeBtn = new JButton("Close");

        variantsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        bottom.add(variantsBtn);
        bottom.add(closeBtn);

        dlg.add(bottom, BorderLayout.SOUTH);

        variantsBtn.addActionListener(e -> showVariantsDialog(item));
        closeBtn.addActionListener(e -> dlg.dispose());

        dlg.setVisible(true);
    }

    // ------------------------------------------------------
    // NEW: Variants dialog (same itemName + modelCode)
    // ------------------------------------------------------
    private void showVariantsDialog(InventoryItem baseItem) {
        // Get variants by SAME NAME (case-insensitive handled in service)
        List<InventoryItem> variants = service.findVariants(baseItem.getItemName());

        if (variants == null || variants.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No variants found for this item.",
                    "Variants",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Sort variants: Location (ignore case) → Supplier (ignore case) → Expiry → Batch
        variants.sort((a, b) -> {
            String locA = (a.getLocation() == null ? "" : a.getLocation().trim().toLowerCase());
            String locB = (b.getLocation() == null ? "" : b.getLocation().trim().toLowerCase());
            int cmp = locA.compareTo(locB);
            if (cmp != 0) return cmp;

            String supA = (a.getSupplier() == null ? "" : a.getSupplier().trim().toLowerCase());
            String supB = (b.getSupplier() == null ? "" : b.getSupplier().trim().toLowerCase());
            cmp = supA.compareTo(supB);
            if (cmp != 0) return cmp;

            LocalDate expA = a.getExpiryDate();
            LocalDate expB = b.getExpiryDate();
            LocalDate normA = (expA == null ? LocalDate.MAX : expA);
            LocalDate normB = (expB == null ? LocalDate.MAX : expB);
            cmp = normA.compareTo(normB);
            if (cmp != 0) return cmp;

            String batchA = (a.getBatchNumber() == null ? "" : a.getBatchNumber().trim().toLowerCase());
            String batchB = (b.getBatchNumber() == null ? "" : b.getBatchNumber().trim().toLowerCase());
            return batchA.compareTo(batchB);
        });

        // Global summary: totals across all variants
        LocalDate today = LocalDate.now();
        LocalDate soonLimit = today.plusDays(30);

        int totalQty = 0;
        int totalMinQty = 0;
        int expiredCount = 0;
        int soonCount = 0;
        LocalDate earliestExpiry = null;

        for (InventoryItem i : variants) {
            totalQty += i.getQuantityOnHand();
            totalMinQty += i.getMinimumQuantity();

            LocalDate exp = i.getExpiryDate();
            if (exp != null) {
                if (exp.isBefore(today)) {
                    expiredCount++;
                } else if (!exp.isAfter(soonLimit)) {
                    soonCount++;
                }
                if (earliestExpiry == null || exp.isBefore(earliestExpiry)) {
                    earliestExpiry = exp;
                }
            }
        }

        JDialog dlg = new JDialog(this,
                "Variants of " + empty(baseItem.getItemName()),
                true);
        dlg.setSize(900, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        // ---------------------------
        // NORTH: Global summary panel
        // ---------------------------
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        summaryPanel.setBackground(new Color(245, 247, 252));

        JLabel lblCount = new JLabel("Variants: " + variants.size());
        JLabel lblQty = new JLabel("Total Qty: " + totalQty + " (min " + totalMinQty + ")");
        JLabel lblEarliest = new JLabel("Earliest expiry: " +
                (earliestExpiry == null ? "—" : earliestExpiry.toString()));
        JLabel lblExpSummary = new JLabel("Expired: " + expiredCount +
                " | Expiring ≤30d: " + soonCount);

        Font summaryFont = new Font("Segoe UI", Font.PLAIN, 12);
        lblCount.setFont(summaryFont);
        lblQty.setFont(summaryFont);
        lblEarliest.setFont(summaryFont);
        lblExpSummary.setFont(summaryFont);

        summaryPanel.add(lblCount);
        summaryPanel.add(lblQty);
        summaryPanel.add(lblEarliest);
        summaryPanel.add(lblExpSummary);

        dlg.add(summaryPanel, BorderLayout.NORTH);

        // ---------------------------
        // CENTER: Table with groups
        // ---------------------------
        String[] cols = {
                "ID", "Batch", "Location", "Supplier",
                "Expiry", "Qty", "Min Qty", "Stock Status", "Expiry Status"
        };

        DefaultTableModel variantsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable variantsTable = new JTable(variantsModel);
        variantsTable.setRowHeight(22);
        variantsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scroll = new JScrollPane(variantsTable);

        // Row type info for styling
        final java.util.List<RowKind> rowKinds = new ArrayList<>();

        // Build grouped rows: Location + Supplier
        String lastLocKey = null;
        String lastSupKey = null;

        IntRef groupQty = new IntRef(0);
        IntRef groupMinQty = new IntRef(0);
        IntRef groupCount = new IntRef(0);


        java.util.function.BiConsumer<String, String> flushGroupSummary = (locKey, supKey) -> {

            if (groupCount.value == 0) return;

            String summaryLabel = "Subtotal (" + groupCount.value +
                    " variant" + (groupCount.value > 1 ? "s" : "") + ")";

            variantsModel.addRow(new Object[]{
                    "",
                    summaryLabel,
                    "",
                    "",
                    "",
                    groupQty.value,
                    groupMinQty.value,
                    "",
                    ""
            });

            rowKinds.add(RowKind.SUMMARY);

            // Reset values (NOT the references)
            groupQty.value = 0;
            groupMinQty.value = 0;
            groupCount.value = 0;
        };


        for (InventoryItem i : variants) {
            String locKey = (i.getLocation() == null ? "" : i.getLocation().trim().toLowerCase());
            String supKey = (i.getSupplier() == null ? "" : i.getSupplier().trim().toLowerCase());

            boolean newGroup = false;
            if (lastLocKey == null) {
                newGroup = true;
            } else if (!locKey.equals(lastLocKey) || !supKey.equals(lastSupKey)) {
                // new group → flush previous summary
                flushGroupSummary.accept(lastLocKey, lastSupKey);
                newGroup = true;
            }

            if (newGroup) {
                String headerLabel = "Location: " + empty(i.getLocation())
                        + " | Supplier: " + empty(i.getSupplier());
                variantsModel.addRow(new Object[]{
                        "", headerLabel, "", "", "", "", "", "", ""
                });
                rowKinds.add(RowKind.HEADER);

                lastLocKey = locKey;
                lastSupKey = supKey;
            }

            String expiryStr = (i.getExpiryDate() == null)
                    ? "—"
                    : i.getExpiryDate().toString();

            String stockStatus = computeStockStatus(i);
            String expiryStatus = computeExpiryStatus(i, today, soonLimit);

            variantsModel.addRow(new Object[]{
                    i.getId(),
                    empty(i.getBatchNumber()),
                    empty(i.getLocation()),
                    empty(i.getSupplier()),
                    expiryStr,
                    i.getQuantityOnHand(),
                    i.getMinimumQuantity(),
                    stockStatus,
                    expiryStatus
            });
            rowKinds.add(RowKind.DATA);

            groupQty.value += i.getQuantityOnHand();
            groupMinQty.value += i.getMinimumQuantity();
            groupCount.value++;
        }

        // Flush last group's summary
        flushGroupSummary.accept(lastLocKey, lastSupKey);

        // Custom renderer to style headers, summaries, and data rows
        variantsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                int modelRow = tbl.convertRowIndexToModel(row);
                RowKind kind = rowKinds.get(modelRow);

                // Default font
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }

                Color bg = Color.WHITE;
                Color fg = Color.BLACK;

                if (kind == RowKind.DATA) {
                    String stockStatus = String.valueOf(variantsModel.getValueAt(modelRow, 7));
                    String expiryStatus = String.valueOf(variantsModel.getValueAt(modelRow, 8));

                    if ("Expired".equalsIgnoreCase(expiryStatus)) {
                        bg = new Color(255, 200, 200); // light red
                    } else if (expiryStatus.toLowerCase().startsWith("expiring")) {
                        bg = new Color(255, 230, 180); // light orange
                    } else if ("Low stock".equalsIgnoreCase(stockStatus)
                            || "Out of stock".equalsIgnoreCase(stockStatus)) {
                        bg = new Color(255, 245, 200); // light yellow
                    }

                    // Right-align numeric columns
                    if (c instanceof JLabel && (column == 5 || column == 6)) {
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                    }

                } else if (kind == RowKind.HEADER) {
                    bg = new Color(232, 240, 254); // light blue/gray
                    fg = new Color(40, 40, 80);
                    if (c instanceof JLabel) {
                        JLabel lbl = (JLabel) c;
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        if (column == 1) {
                            lbl.setHorizontalAlignment(SwingConstants.LEFT);
                        }
                    }
                } else if (kind == RowKind.SUMMARY) {
                    bg = new Color(240, 240, 240);
                    fg = new Color(60, 60, 60);
                    if (c instanceof JLabel) {
                        JLabel lbl = (JLabel) c;
                        lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
                        if (column == 1) {
                            lbl.setHorizontalAlignment(SwingConstants.LEFT);
                        }
                        if (column == 5 || column == 6) {
                            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                        }
                    }
                }

                if (isSelected) {
                    c.setBackground(bg.darker());
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(bg);
                    c.setForeground(fg);
                }

                return c;
            }
        });

        dlg.add(scroll, BorderLayout.CENTER);

        // ---------------------------
        // SOUTH: Close button
        // ---------------------------
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setBackground(new Color(245, 247, 252));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        bottom.add(closeBtn);

        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }


    private String empty(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s.trim();
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

    private String safeCell(int modelRow, int col) {
        Object val = model.getValueAt(modelRow, col);
        return val == null ? "" : val.toString();
    }
    
 // Row types used in the variants table renderer
    private enum RowKind {
        HEADER,
        DATA,
        SUMMARY
    }

    // Simple holder so "row" is effectively final for lambdas
    private static class RowIndex {
        private int value = 0;
        int get() { return value; }
        void inc() { value++; }
    }
    
    private static class IntRef {
        int value;
        IntRef(int v) { value = v; }
    }

}
