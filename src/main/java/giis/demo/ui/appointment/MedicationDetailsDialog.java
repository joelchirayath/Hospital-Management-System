package giis.demo.ui.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import giis.demo.service.appointment.prescription.MedicationDTO;

public class MedicationDetailsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private MedicationDTO medication;
	private boolean accepted = false;

	private JSpinner amountSpinner;
	private JSpinner intervalSpinner;
	private JSpinner durationSpinner;
	private JButton btnAccept;
	private JButton btnCancel;

	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color GRAY_COLOR = new Color(149, 165, 166);

	public MedicationDetailsDialog(MedicationDTO medication) {
		this.medication = medication;
		initialize();
		setDefaultValues();
	}

	private void initialize() {
		setTitle("Medication Details");
		setModal(true);
		setResizable(false);
		setBounds(100, 100, 400, 300);
		setLocationRelativeTo(null);

		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
		contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		setContentPane(contentPanel);

		// Title
		JLabel titleLabel = new JLabel("Configure " + medication.getMedication_name(), JLabel.CENTER);
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		titleLabel.setForeground(PRIMARY_COLOR);
		contentPanel.add(titleLabel, BorderLayout.NORTH);

		// Details panel
		contentPanel.add(createDetailsPanel(), BorderLayout.CENTER);

		// Buttons panel
		contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	private JPanel createDetailsPanel() {
		JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

		// Amount
		panel.add(createLabel("Amount per dose:"));
		amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
		panel.add(amountSpinner);

		// Interval
		panel.add(createLabel("Interval (hours):"));
		intervalSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 24, 1));
		panel.add(intervalSpinner);

		// Duration
		panel.add(createLabel("Duration (days):"));
		durationSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 90, 1));
		panel.add(durationSpinner);

		return panel;
	}

	private JLabel createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font("SansSerif", Font.PLAIN, 14));
		return label;
	}

	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

		btnCancel = new JButton("Cancel");
		btnCancel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btnCancel.setBackground(GRAY_COLOR);
		btnCancel.setForeground(Color.WHITE);
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				accepted = false;
				dispose();
			}
		});

		btnAccept = new JButton("Accept");
		btnAccept.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnAccept.setBackground(PRIMARY_COLOR);
		btnAccept.setForeground(Color.WHITE);
		btnAccept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveDetails();
				accepted = true;
				dispose();
			}
		});

		panel.add(btnCancel);
		panel.add(btnAccept);

		return panel;
	}

	private void setDefaultValues() {
		// Usar los valores del DTO si existen, sino valores por defecto
		if (medication.getAmount() > 0) {
			amountSpinner.setValue(medication.getAmount());
		}
		if (medication.getInterval_hours() > 0) {
			intervalSpinner.setValue(medication.getInterval_hours());
		}
		if (medication.getDuration() > 0) {
			durationSpinner.setValue(medication.getDuration());
		}
	}

	private void saveDetails() {
		medication.setAmount((Integer) amountSpinner.getValue());
		medication.setInterval_hours((Integer) intervalSpinner.getValue());
		medication.setDuration((Integer) durationSpinner.getValue());
	}

	public boolean isAccepted() {
		return accepted;
	}

	public MedicationDTO getMedication() {
		return medication;
	}
}
