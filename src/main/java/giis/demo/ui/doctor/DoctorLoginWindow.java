package giis.demo.ui.doctor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.doctor.DoctorDTO;

public class DoctorLoginWindow extends JDialog {
	private static final long serialVersionUID = 1L;

	private JTextField idTextField;
	private JButton loginButton;
	private JButton cancelButton;
	private AppointmentModel model;
	private DoctorDTO selectedDoctor;

	public DoctorLoginWindow(JFrame parent, AppointmentModel model) {
		super(parent, "Doctor Login", true);
		this.model = model;
		initializeUI();
	}

	private void initializeUI() {
		setSize(400, 250);
		setLocationRelativeTo(getParent());
		setResizable(false);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		setContentPane(contentPane);

		// Header
		JLabel headerLabel = new JLabel("Doctor Portal Login",
				SwingConstants.CENTER);
		headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
		headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
		contentPane.add(headerLabel, BorderLayout.NORTH);

		// Main form panel
		JPanel formPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Doctor ID input
		JLabel idLabel = new JLabel("Enter your Doctor ID:");
		idLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.4;
		formPanel.add(idLabel, gbc);

		idTextField = new JTextField();
		idTextField.setFont(new Font("SansSerif", Font.PLAIN, 14));
		idTextField.setToolTipText("Enter your numeric Doctor ID");
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0.6;
		formPanel.add(idTextField, gbc);

		JLabel infoLabel = new JLabel(
				"Please enter your unique Doctor ID to access your appointments");
		infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
		infoLabel.setForeground(Color.GRAY);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		formPanel.add(infoLabel, gbc);

		contentPane.add(formPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		loginButton = new JButton("Login");
		cancelButton = new JButton("Cancel");

		loginButton.setBackground(new Color(41, 128, 185));
		loginButton.setForeground(Color.WHITE);
		loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
		loginButton.setFocusPainted(false);

		cancelButton.setBackground(Color.GRAY);
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("SansSerif", Font.BOLD, 14));
		cancelButton.setFocusPainted(false);

		buttonPanel.add(loginButton);
		buttonPanel.add(cancelButton);

		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		setupEventListeners();

		idTextField.requestFocusInWindow();
	}

	private void setupEventListeners() {
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performLogin();
			}
		});

		idTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					performLogin();
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedDoctor = null;
				dispose();
			}
		});
	}

	private void performLogin() {
		String idText = idTextField.getText().trim();

		if (idText.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter your Doctor ID",
					"Input Required", JOptionPane.WARNING_MESSAGE);
			idTextField.requestFocus();
			return;
		}

		int doctorId;
		try {
			doctorId = Integer.parseInt(idText);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this,
					"Doctor ID must be a numeric value", "Invalid Input",
					JOptionPane.ERROR_MESSAGE);
			idTextField.selectAll();
			idTextField.requestFocus();
			return;
		}

		DoctorDTO doctor = model.getDoctorById(doctorId);
		if (doctor == null) {
			JOptionPane.showMessageDialog(this, "Doctor with ID " + doctorId
					+ " not found.\nPlease check your ID and try again.",
					"Doctor Not Found", JOptionPane.ERROR_MESSAGE);
			idTextField.selectAll();
			idTextField.requestFocus();
			return;
		}

		selectedDoctor = doctor;
		System.out.println("Doctor logged in: " + selectedDoctor.getFullName()
				+ " (ID: " + selectedDoctor.getId() + ")");

		dispose();
	}

	public DoctorDTO getSelectedDoctor() {
		return selectedDoctor;
	}

	public Integer getSelectedDoctorId() {
		return selectedDoctor != null ? selectedDoctor.getId() : null;
	}
}