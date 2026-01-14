package giis.demo.ui.receptionist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

import giis.demo.service.appointment.AppointmentDTO;
import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.doctorAppt.DoctorApptController;
import giis.demo.service.appointment.doctorAppt.DoctorApptControllerImpl;
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.service.patient.PatientDTO;

public class NewApptWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private JButton addPatientButton;
    private JTextField txtPatient;
    private JButton removePatientButton;
    private JButton addDoctorButton;
    private JComboBox<DoctorDTO> doctorsComboBox;
    private JButton removeDoctorButton;
    private JSpinner dateSpinner;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    private JComboBox<String> roomBox;
    private JTextArea reasonField;
    private JButton bookButton;
    private JLabel labelPatient;
    private JLabel labelDoctor;
    private JCheckBox urgentCheckBox;

    // Shared AppointmentModel (no new DB instance here)
    private final AppointmentModel model;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color PANEL_BACKGROUND = Color.WHITE;
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private DoctorApptController DAcontroller;
    private int apptId;

    private PatientDTO patientDTO;

    public PatientDTO getPatientDTO() {
        return patientDTO;
    }

    public void setPatientDTO(PatientDTO patientDTO) {
        this.patientDTO = patientDTO;
        getTxtPatient().setText(getstringPatient());
    }

    private String getstringPatient() {
        return patientDTO.getFullName() + " (" + patientDTO.getDni() + ") ";
    }

    // Main constructor using shared AppointmentModel
    public NewApptWindow(AppointmentModel model) {
        this.model = model;
        DAcontroller = new DoctorApptControllerImpl();

        setTitle("Receptionist - Book Appointment");
        setMinimumSize(new Dimension(900, 520));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        mainPanel.setBackground(PANEL_BACKGROUND);

        JLabel header = new JLabel("New Medical Appointment");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_BACKGROUND);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        addSectionLabel("Patient & Doctor", formPanel, gbc, row++);

        addPatientRow(formPanel, gbc, row++);
        addDoctorRow(formPanel, gbc, row++);

        addSectionLabel("Schedule & Room", formPanel, gbc, row++);

        dateSpinner = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));
        addFormRow(formPanel, gbc, row++, "Date:", dateSpinner);

        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));

        startTimeSpinner.addChangeListener(e -> autoAdjustEndTime());

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timePanel.setBackground(PANEL_BACKGROUND);
        timePanel.add(new JLabel("Start:"));
        timePanel.add(startTimeSpinner);
        timePanel.add(new JLabel("End:"));
        timePanel.add(endTimeSpinner);
        addFormRow(formPanel, gbc, row++, "Time:", timePanel);

        roomBox = new JComboBox<>();
        loadRooms();
        addFormRow(formPanel, gbc, row++, "Room:", roomBox);

        addSectionLabel("Reason & Urgency", formPanel, gbc, row++);

        reasonField = new JTextArea(3, 20);
        reasonField.setLineWrap(true);
        reasonField.setWrapStyleWord(true);
        reasonField.setFont(LABEL_FONT);
        JScrollPane reasonScroll = new JScrollPane(reasonField);
        reasonScroll.setBorder(
                BorderFactory.createTitledBorder("Reason for visit (required)"));
        addFormRow(formPanel, gbc, row++, "", reasonScroll);

        urgentCheckBox = new JCheckBox(
                "Mark this appointment as URGENT (doctor will receive an immediate email)");
        urgentCheckBox.setFont(LABEL_FONT);
        urgentCheckBox.setForeground(Color.RED.darker());
        urgentCheckBox.setBackground(PANEL_BACKGROUND);
        urgentCheckBox.setFocusPainted(false);
        urgentCheckBox.setToolTipText(
                "If checked, all assigned doctors will be notified by email that this appointment is URGENT.");
        addFormRow(formPanel, gbc, row++, "Urgent:", urgentCheckBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(PANEL_BACKGROUND);

        bookButton = new JButton("Book Appointment");
        bookButton.setFont(BUTTON_FONT);
        bookButton.setBackground(new Color(33, 150, 243));
        bookButton.setForeground(Color.WHITE);
        bookButton.setFocusPainted(false);
        bookButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookButton.setPreferredSize(new Dimension(220, 40));
        buttonPanel.add(bookButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        bookButton.addActionListener(e -> {
            if (bookAppointment(e)) {
                bookDoctorsAppt();

                if (urgentCheckBox.isSelected()) {
                    String patientInfo = txtPatient.getText().trim();
                    String safePatientName = extractPatientName(patientInfo);

                    String date = dateFormat.format((Date) dateSpinner.getValue());
                    String start = timeFormat.format((Date) startTimeSpinner.getValue());
                    String end = timeFormat.format((Date) endTimeSpinner.getValue());
                    String room = (String) roomBox.getSelectedItem();
                    String timeRange = start + " - " + end;
                    String reason = reasonField.getText().trim();

                    DAcontroller.sendUrgentEmailToDoctors(
                            apptId, safePatientName, date, timeRange, room, reason);
                }

                dispose();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Optional no-arg constructor
    public NewApptWindow() {
        this(new AppointmentModel());
    }

    // ======================== HELPERS & UI ========================

    private void addSectionLabel(String text, JPanel panel,
                                 GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel section = new JLabel(text);
        section.setFont(new Font("Segoe UI", Font.BOLD, 15));
        section.setForeground(PRIMARY_COLOR.darker());
        section.setBorder(BorderFactory.createEmptyBorder(10, 2, 0, 2));

        panel.add(section, gbc);
        gbc.gridwidth = 1;
    }

    private void bookDoctorsAppt() {
        int itemCount = getDoctorsComboBox().getItemCount();
        System.out.println("[NewApptWindow] Booking doctors for apptId=" + apptId +
                " (total doctors: " + itemCount + ")");

        for (int i = 0; i < itemCount; i++) {
            DoctorDTO doctor = getDoctorsComboBox().getItemAt(i);
            if (doctor != null && doctor.getId() != null) {
                System.out.println("[NewApptWindow] -> link doctorId=" + doctor.getId());
                DAcontroller.saveDoctorBooking(doctor.getId(), apptId);
            } else {
                System.out.println("[NewApptWindow] -> skipped null doctor or null id");
            }
        }
    }

    private void addPatientRow(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(getPatientLabel(), gbc);

        JPanel patientPanel = new JPanel(new BorderLayout(5, 0));
        patientPanel.setBackground(PANEL_BACKGROUND);
        patientPanel.add(getAddPatientButton(), BorderLayout.WEST);
        patientPanel.add(getTxtPatient(), BorderLayout.CENTER);
        patientPanel.add(getRemovePatientButton(), BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(patientPanel, gbc);
    }

    public JLabel getDoctorLabel() {
        if (labelDoctor == null) {
            labelDoctor = new JLabel("Doctor(s):");
            labelDoctor.setFont(HEADER_FONT);
            labelDoctor.setForeground(PRIMARY_COLOR);
        }
        return labelDoctor;
    }

    public JLabel getPatientLabel() {
        if (labelPatient == null) {
            labelPatient = new JLabel("Patient:");
            labelPatient.setFont(HEADER_FONT);
            labelPatient.setForeground(PRIMARY_COLOR);
        }
        return labelPatient;
    }

    public JButton getAddPatientButton() {
        if (addPatientButton == null) {
            addPatientButton = new JButton("Select patient");
            addPatientButton.setFont(BUTTON_FONT);
            addPatientButton.setBackground(PRIMARY_COLOR);
            addPatientButton.setForeground(Color.WHITE);
            addPatientButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR.darker()),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)));
            addPatientButton.setFocusPainted(false);
            addPatientButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            addPatientButton.addActionListener(e -> openAddPatientWindow());
        }
        return addPatientButton;
    }

    public JTextField getTxtPatient() {
        if (txtPatient == null) {
            txtPatient = new JTextField();
            txtPatient.setEditable(false);
            txtPatient.setBackground(Color.WHITE);
            txtPatient.setFont(LABEL_FONT);
            txtPatient.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 210, 210)),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        }
        return this.txtPatient;
    }

    public JButton getRemovePatientButton() {
        if (removePatientButton == null) {
            removePatientButton = new JButton("Clear");
            removePatientButton.setFont(LABEL_FONT);
            removePatientButton.setEnabled(false);
            removePatientButton.setBackground(new Color(189, 195, 199));
            removePatientButton.setForeground(Color.WHITE);
            removePatientButton.setBorder(
                    BorderFactory.createEmptyBorder(4, 10, 4, 10));
            removePatientButton.setFocusPainted(false);
            removePatientButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removePatientButton.addActionListener(e -> removePatient());
        }
        return this.removePatientButton;
    }

    private void addDoctorRow(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(getDoctorLabel(), gbc);

        JPanel doctorPanel = new JPanel(new BorderLayout(5, 0));
        doctorPanel.setBackground(PANEL_BACKGROUND);
        doctorPanel.add(getAddDoctorButton(), BorderLayout.WEST);
        doctorPanel.add(getDoctorsComboBox(), BorderLayout.CENTER);
        doctorPanel.add(getRemoveDoctorButton(), BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(doctorPanel, gbc);
    }

    public JButton getAddDoctorButton() {
        if (addDoctorButton == null) {
            addDoctorButton = new JButton("Add doctor(s)");
            addDoctorButton.setFont(BUTTON_FONT);
            addDoctorButton.setBackground(PRIMARY_COLOR);
            addDoctorButton.setForeground(Color.WHITE);
            addDoctorButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR.darker()),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)));
            addDoctorButton.setFocusPainted(false);
            addDoctorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            addDoctorButton.addActionListener(e -> openAddDoctorWindow());
        }
        return addDoctorButton;
    }

    public void setSelectedDoctors(List<DoctorDTO> selectedDoctors) {
        for (DoctorDTO doctorDTO : selectedDoctors) {
            getDoctorsComboBox().addItem(doctorDTO);
        }
        getRemoveDoctorButton().setEnabled(getDoctorsComboBox().getItemCount() > 0);
    }

    public JButton getRemoveDoctorButton() {
        if (removeDoctorButton == null) {
            removeDoctorButton = new JButton("Remove");
            removeDoctorButton.setEnabled(false);
            removeDoctorButton.setFont(LABEL_FONT);
            removeDoctorButton.setBackground(new Color(189, 195, 199));
            removeDoctorButton.setForeground(Color.WHITE);
            removeDoctorButton.setBorder(
                    BorderFactory.createEmptyBorder(4, 10, 4, 10));
            removeDoctorButton.setFocusPainted(false);
            removeDoctorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeDoctorButton.addActionListener(e -> removeSelectedDoctor());
        }
        return removeDoctorButton;
    }

    public JComboBox<DoctorDTO> getDoctorsComboBox() {
        if (doctorsComboBox == null) {
            doctorsComboBox = new JComboBox<>();
            doctorsComboBox.setBackground(Color.WHITE);
            doctorsComboBox.setFont(LABEL_FONT);
        }
        return doctorsComboBox;
    }

    private void openAddDoctorWindow() {
        new AddDoctorWindow(this).setVisible(true);
    }

    private void removeSelectedDoctor() {
        getDoctorsComboBox().removeItem(getDoctorsComboBox().getSelectedItem());
        getRemoveDoctorButton().setEnabled(getDoctorsComboBox().getItemCount() > 0);
    }

    private void openAddPatientWindow() {
        new AddPatientWindow(this).setVisible(true);
    }

    private void removePatient() {
        txtPatient.setText("");
        removePatientButton.setEnabled(false);
    }

    public void setSelectedPatient(String patientName, String patientId,
                                   PatientDTO dto) {
        this.patientDTO = dto;
        txtPatient.setText(patientName + " (ID: " + patientId + ")");
        removePatientButton.setEnabled(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String label, Component input) {
        // Label column
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        if (label != null && !label.isEmpty()) {
            JLabel jLabel = new JLabel(label);
            jLabel.setFont(LABEL_FONT);
            panel.add(jLabel, gbc);
        }

        // Input column
        gbc.gridx = 1;
        gbc.weightx = 0.8;

        if (input instanceof JScrollPane || input instanceof JPanel
                || input instanceof JTextArea) {
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
        } else {
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
        }

        panel.add(input, gbc);
    }

    private void loadRooms() {
        try {
            List<String> rooms = model.getAllRooms();
            for (String r : rooms) {
                roomBox.addItem(r);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading rooms: " + e.getMessage());
        }
    }

    private void autoAdjustEndTime() {
        Date startDate = (Date) startTimeSpinner.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.MINUTE, 30);
        endTimeSpinner.setValue(cal.getTime());
    }

    private Integer extractPatientId(String patientInfo) {
        try {
            int start = patientInfo.indexOf("(ID:");
            int end = patientInfo.indexOf(")", start);
            if (start != -1 && end != -1) {
                String idStr = patientInfo.substring(start + 4, end).trim();
                return Integer.parseInt(idStr);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String extractPatientName(String patientInfo) {
        int idx = patientInfo.indexOf(" (ID:");
        if (idx > 0) {
            return patientInfo.substring(0, idx);
        }
        return patientInfo;
    }

    // ======================== BOOKING LOGIC ========================
    private boolean bookAppointment(ActionEvent e) {
        try {

            String patientInfo = txtPatient.getText().trim();
            if (patientInfo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a patient.");
                return false;
            }

            Integer patientId = patientDTO.getId();
            if (patientId == null) {
                JOptionPane.showMessageDialog(this,
                        "Cannot detect patient ID. Please re-select the patient.");
                return false;
            }

            // ✅ NEW: require at least one doctor so it appears in doctor schedule
            int doctorCount = getDoctorsComboBox().getItemCount();
            if (doctorCount == 0) {
                JOptionPane.showMessageDialog(this,
                        "Please add at least one doctor for this appointment.\n" +
                        "Without a doctor, it will not appear in any doctor's schedule.");
                return false;
            }

            String date = dateFormat.format((Date) dateSpinner.getValue());
            String start = timeFormat.format((Date) startTimeSpinner.getValue());
            String end = timeFormat.format((Date) endTimeSpinner.getValue());
            String room = (String) roomBox.getSelectedItem();
            String reason = reasonField.getText().trim();
            boolean isUrgent = urgentCheckBox.isSelected();

            if (room == null || room.isEmpty() || reason.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ Please fill in all required fields.");
                return false;
            }

            String dbDate = LocalDate
                    .parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    .toString();

            StringBuilder warnings = new StringBuilder();

            // Room conflict
            if (model.isOfficeBooked(room, dbDate, start, end)) {
                warnings.append("- Room ").append(room)
                        .append(" is already booked during this time.\n");
            }

            // Doctor conflicts
            for (int i = 0; i < doctorCount; i++) {
                DoctorDTO doc = getDoctorsComboBox().getItemAt(i);
                if (doc == null) {
                    continue;
                }

                Integer docId = doc.getId() != 0 ? (int) doc.getId() : null;
                String docName = (doc.getName() != null ? doc.getName() : "")
                        + " "
                        + (doc.getSurname() != null ? doc.getSurname() : "");
                String docDni = null;
                try {
                    docDni = (String) DoctorDTO.class.getMethod("getDni")
                            .invoke(doc);
                } catch (Exception ignore) {
                }

                if (docId != null
                        && model.isDoctorBooked(docId, dbDate, start, end)) {
                    warnings.append("- Doctor ").append(docName.trim()).append(
                            " already has another appointment during this time.\n");
                }

                if (docDni != null && !docDni.isEmpty()
                        && !model.isDoctorWorking(docDni, dbDate, start, end)) {
                    warnings.append("- Doctor ").append(docName.trim()).append(
                            " is NOT scheduled to work at this time.\n");
                }
            }

            if (warnings.length() > 0) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "⚠️ The following scheduling issues were detected:\n\n"
                                + warnings + "\nDo you want to proceed anyway?",
                        "Scheduling Conflicts Detected",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (choice != JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this, "Booking cancelled.");
                    return false;
                }
            }

            String urgentHtml = isUrgent
                    ? "<span style='color:red; font-weight:bold;'>YES - URGENT</span>"
                    : "No";

            String summary = "<html><body style='font-family:Segoe UI; font-size:12px;'>"
                    + "<h3 style='text-align:center;'>📋 Confirm Appointment Details</h3>"
                    + "<table style='border-spacing:8px;'>"
                    + "<tr><td><b>Patient:</b></td><td>" + patientInfo
                    + "</td></tr>" + "<tr><td><b>Date:</b></td><td>" + date
                    + "</td></tr>" + "<tr><td><b>Time:</b></td><td>" + start
                    + " - " + end + "</td></tr>"
                    + "<tr><td><b>Room:</b></td><td>" + room + "</td></tr>"
                    + "<tr><td><b>Reason:</b></td><td>" + reason + "</td></tr>"
                    + "<tr><td><b>Urgent:</b></td><td>" + urgentHtml
                    + "</td></tr>"
                    + "</table><br><center>Do you confirm this booking?</center></body></html>";

            int confirm = JOptionPane.showConfirmDialog(this,
                    new JLabel(summary), "Confirm Appointment",
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Booking cancelled.");
                return false;
            }

            AppointmentDTO appt = new AppointmentDTO();
            appt.setPatient_id(patientId);
            appt.setPatient_name(patientDTO.getName());
            appt.setDate(dbDate);
            appt.setStart_time(start);
            appt.setEnd_time(end);
            appt.setOffice(room);
            appt.setNotes(reason);
            appt.setStatus("scheduled");
            appt.setUrgent(isUrgent);

            Integer savedId = model.saveAppointment(appt);
            if (savedId != null && savedId > 0) {
                apptId = savedId;
            } else {
                Integer fallbackId = model.getLastInsertedAppointmentId();
                if (fallbackId != null && fallbackId > 0) {
                    apptId = fallbackId;
                } else {
                    JOptionPane.showMessageDialog(this,
                            "❌ Error: appointment ID not generated.");
                    return false;
                }
            }

            model.saveContactInfo(patientDTO.getId(), savedId,
                    patientDTO.getPhone(), patientDTO.getEmail(),
                    patientDTO.getAddress());

            System.out.printf(
                    "patient " + patientDTO.getId() + " " + savedId + " "
                            + patientDTO.getPhone(),
                    patientDTO.getEmail() + " " + patientDTO.getAddress());

            JOptionPane.showMessageDialog(this,
                    "✅ Appointment booked successfully!");
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "❌ Error booking appointment: " + ex.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppointmentModel model = new AppointmentModel();
            new NewApptWindow(model);
        });
    }
}
