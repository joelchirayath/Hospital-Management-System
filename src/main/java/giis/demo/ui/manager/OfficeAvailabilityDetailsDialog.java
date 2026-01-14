package giis.demo.ui.manager;

import giis.demo.service.appointment.AppointmentDTO;
import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.OfficeAvailabilitySegment;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class OfficeAvailabilityDetailsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AppointmentModel appointmentModel;
    private final String officeLabel;
    private final LocalDate date;
    private final OfficeAvailabilitySegment segment;

    // Define a modern color palette
    private static final Color HEADER_BG_COLOR = new Color(52, 152, 219); // Blue
    private static final Color HEADER_TEXT_COLOR = Color.WHITE;
    private static final Color CARD_BG_COLOR = Color.WHITE;
    private static final Color CARD_BORDER_COLOR = new Color(220, 220, 220);
    private static final Color HIGHLIGHT_COLOR = new Color(46, 204, 113); // Green for "Urgent"

    // Define consistent font styles
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TITLE_CARD = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_NO_APPOINTMENTS = new Font("Segoe UI", Font.ITALIC, 13);

    public OfficeAvailabilityDetailsDialog(Window owner,
                                           AppointmentModel model,
                                           String officeLabel,
                                           LocalDate date,
                                           OfficeAvailabilitySegment segment) {
        super(owner, "Appointment Details", ModalityType.APPLICATION_MODAL);
        this.appointmentModel = model;
        this.officeLabel = officeLabel;
        this.date = date;
        this.segment = segment;

        initUi();
        setSize(700, 450);
        setLocationRelativeTo(owner);
    }

    private void initUi() {
        List<AppointmentDTO> appts = segment.getAppointments();
        int count = (appts != null) ? appts.size() : 0;

        // Use a clean, consistent look for the dialog
        JPanel root = new JPanel(new BorderLayout(15, 15)); // Increase spacing
        root.setBorder(new EmptyBorder(15, 15, 15, 15));
        root.setBackground(new Color(245, 245, 245)); // Light gray background
        setContentPane(root);

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        String titleHtml = String.format(
                "<html><b style='font-size: 1.2em;'>%s</b><br/>%s, %s – %s (%d appointment%s)</html>",
                officeLabel,
                date,
                segment.getFrom(),
                segment.getTo(),
                count,
                count == 1 ? "" : "s"
        );
        JLabel title = new JLabel(titleHtml);
        title.setFont(FONT_HEADER);
        title.setForeground(HEADER_TEXT_COLOR);
        headerPanel.add(title, BorderLayout.CENTER);
        root.add(headerPanel, BorderLayout.NORTH);

        // ===== CENTER: APPOINTMENT CARDS =====
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(root.getBackground());

        if (count == 0) {
            JLabel none = new JLabel("No appointments in this time range.");
            none.setFont(FONT_NO_APPOINTMENTS);
            none.setForeground(Color.GRAY);
            none.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue()); // Center vertically
            listPanel.add(none);
            listPanel.add(Box.createVerticalGlue());
        } else {
            int index = 1;
            for (AppointmentDTO dto : appts) {
                listPanel.add(createAppointmentCard(index++, dto));
                listPanel.add(Box.createVerticalStrut(10)); // Increased spacing between cards
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder()); // Remove border
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        // ===== FOOTER (CLOSE) =====
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        closeBtn.putClientProperty("JButton.buttonType", "roundRect");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        root.add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createAppointmentCard(int index, AppointmentDTO dto) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG_COLOR);
        card.setBorder(new CompoundBorder(
                new LineBorder(CARD_BORDER_COLOR, 1, true), 
                new EmptyBorder(12, 15, 12, 15)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4); // Increased spacing
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        // Title row: "Appointment #1"
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel heading = new JLabel("Appointment #" + index);
        heading.setFont(FONT_TITLE_CARD);
        card.add(heading, c);

        c.gridwidth = 1;
        int row = 1;

        addRow(card, row++, "Date:", safe(dto.getDate()));
        String timeRange = safe(dto.getStart_time()) + " – " + safe(dto.getEnd_time());
        addRow(card, row++, "Time:", timeRange);
        addRow(card, row++, "Office:", safe(dto.getOffice()));
        addRow(card, row++, "Patient:", dto.getFullPatientName());
        addRow(card, row++, "Patient DNI:", safe(dto.getPatient_dni()));

        String urgentValue = dto.isUrgent() ? "Yes" : "No";
        Color urgentColor = dto.isUrgent() ? HIGHLIGHT_COLOR : Color.GRAY;
        addRow(card, row++, "Urgent:", urgentValue, urgentColor, Font.BOLD);

        addRow(card, row++, "Status:", safe(dto.getStatus()));

        String doctorName = "";
        try {
            if (dto.getDoctor_id() != null) {
                doctorName = appointmentModel.getDoctorNameById(dto.getDoctor_id());
            }
        } catch (Exception ignored) {
        }

        String notes = safe(dto.getNotes());
        if (!notes.isEmpty()) {
            addRow(card, row++, "Notes:", notes);
        }

        return card;
    }

    private void addRow(JPanel card, int row, String label, String value) {
        addRow(card, row, label, value, null, null);
    }

    private void addRow(JPanel card, int row, String label, String value, Color valueColor, Integer valueFontStyle) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 8); // Consistent insets
        c.anchor = GridBagConstraints.WEST;

        // Label
        c.gridx = 0;
        c.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(Color.DARK_GRAY);
        card.add(lbl, c);

        // Value
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Use HTML for values so long notes can wrap
        JLabel val = new JLabel("<html>" + escapeHtml(value) + "</html>");
        Font valFont = FONT_VALUE;
        if (valueFontStyle != null) {
            valFont = valFont.deriveFont(valueFontStyle);
        }
        val.setFont(valFont);
        if (valueColor != null) {
            val.setForeground(valueColor);
        } else {
            val.setForeground(Color.BLACK);
        }
        card.add(val, c);
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "N/A" : s;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
