package giis.demo.ui.manager;

import giis.demo.util.Database;

import javax.swing.*;
import java.awt.*;

/**
 * Clean and compact Manager Dashboard UI.
 */
public class ManagerDashboardWindow extends JFrame {

    public ManagerDashboardWindow() {
        setTitle("Manager Dashboard");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false); // fixed-size, but computed nicely by pack()

        initUi();

        pack();                      // let Swing compute best size
        setMinimumSize(new Dimension(440, 340));
        setLocationRelativeTo(null); // center on screen
    }

    private void initUi() {
        // ===== ROOT =====
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(246, 248, 252));
        setContentPane(root);

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Manager Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(40, 40, 40));

        JLabel subtitle = new JLabel("Overview of appointments and inventory");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 120, 120));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);

        // ===== CENTER: CARD IN THE MIDDLE =====
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel card = new JPanel(new GridLayout(7, 1, 8, 8)); 
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 233)),
                BorderFactory.createEmptyBorder(14, 22, 14, 22)
        ));

        // ----- Buttons -----
        JButton btnDiseaseTrend = createPrimaryButton("Appointments by Disease");
        btnDiseaseTrend.addActionListener(e -> new DiseaseTrendWindow().setVisible(true));

        JButton btnManagerWindow = createPrimaryButton("Overall Appointments");
        btnManagerWindow.addActionListener(e -> new ManagerWindow().setVisible(true));

        JButton btnAppointmentsOverTime = createPrimaryButton("Appointments Over Time");
        btnAppointmentsOverTime.addActionListener(e -> new AppointmentVolumeWindow().setVisible(true));

        JButton btnInventoryDashboard = createPrimaryButton("Inventory Dashboard");
        btnInventoryDashboard.addActionListener(e -> new InventoryDashboardWindow().setVisible(true));

        JButton btnInventoryAlerts = createPrimaryButton("Inventory Alerts");
        btnInventoryAlerts.addActionListener(e -> new InventoryAlertWindow().setVisible(true));
        
        JButton btnOfficeAvailability = createPrimaryButton("Office Availability");
        btnOfficeAvailability.addActionListener(e -> new OfficeAvailabilityWindow().setVisible(true));


        JButton btnExit = createSecondaryButton("Exit");
        btnExit.addActionListener(e -> dispose());

        // Add in order
        card.add(btnDiseaseTrend);
        card.add(btnManagerWindow);
        card.add(btnAppointmentsOverTime);
        card.add(btnInventoryDashboard);
        card.add(btnInventoryAlerts);
        card.add(btnOfficeAvailability);
        card.add(btnExit);

        centerWrapper.add(card);
        root.add(centerWrapper, BorderLayout.CENTER);

        // ❌ No footer/status bar anymore (Removed "Ready")
    }

    // ---------- UI HELPERS ----------

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        button.setFocusPainted(false);
        button.setBackground(new Color(236, 240, 250));
        button.setForeground(new Color(35, 40, 55));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 225)),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        button.setPreferredSize(new Dimension(280, 38));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(245, 245, 245));
        button.setForeground(new Color(90, 90, 90));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215)),
                BorderFactory.createEmptyBorder(7, 18, 7, 18)
        ));
        button.setPreferredSize(new Dimension(280, 34));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ---------- ENTRY POINT ----------

    public static void main(String[] args) {
        // Initialize database for this entry point
        Database db = new Database();
        db.createDatabase(true);
        db.loadDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new ManagerDashboardWindow().setVisible(true);
        });
    }
}
