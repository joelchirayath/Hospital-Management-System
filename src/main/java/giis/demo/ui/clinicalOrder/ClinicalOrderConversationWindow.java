package giis.demo.ui.clinicalOrder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.clinicalOrder.ClinicalOrderDTO;
import giis.demo.service.clinicalOrder.ClinicalOrderMessageDTO;
import giis.demo.service.doctor.DoctorDTO;

public class ClinicalOrderConversationWindow extends JDialog {
	private static final long serialVersionUID = 1L;

	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
	private final Color BORDER_COLOR = new Color(220, 220, 220);
	private final Color PANEL_BACKGROUND = Color.WHITE;
	private final Color SUCCESS_COLOR = new Color(39, 174, 96);

	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

	private JPanel messagesPanel;
	private JTextArea responseArea;
	private JButton sendButton;
	private JButton uploadButton;
	private JButton closeOrderButton;
	private JLabel fileLabel;

	private ClinicalOrderDTO clinicalOrder;
	private AppointmentModel model;
	private DoctorDTO currentDoctor;

	private File selectedFile;
	private byte[] fileData;

	public ClinicalOrderConversationWindow(JFrame parent,
		AppointmentModel model, DoctorDTO currentDoctor,
		ClinicalOrderDTO clinicalOrder) {
		super(parent, "Clinical Order Conversation", true);
		this.model = model;
		this.currentDoctor = currentDoctor;
		this.clinicalOrder = clinicalOrder;
		initializeUI();
		loadMessages();
	}

	private void initializeUI() {
		setSize(800, 900);
		setLocationRelativeTo(getParent());
		setResizable(true);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		// Header
		JLabel headerLabel = new JLabel(
			"Clinical Order: " + clinicalOrder.getConcept(), JLabel.CENTER);
		headerLabel.setFont(TITLE_FONT);
		headerLabel.setForeground(PRIMARY_COLOR);
		headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
		contentPane.add(headerLabel, BorderLayout.NORTH);

		// Order info
		contentPane.add(createOrderInfoPanel(), BorderLayout.NORTH);

		// Messages panel
		contentPane.add(createMessagesPanel(), BorderLayout.CENTER);

		// Response panel
		contentPane.add(createResponsePanel(), BorderLayout.SOUTH);
	}

	private JPanel createOrderInfoPanel() {
		JPanel panel = createStyledPanel("Order Information", null);
		panel.setLayout(new GridLayout(0, 2, 10, 5));

		JLabel patientLabel = new JLabel(
			"Patient: " + clinicalOrder.getPatientName());
		JLabel statusLabel = new JLabel(
			"Status: " + (clinicalOrder.isOpen() ? "Open" : "Closed"));
		statusLabel.setForeground(
			clinicalOrder.isOpen() ? SUCCESS_COLOR : Color.GRAY);

		String doctorInfo = clinicalOrder	.getRequestingDoctorId()
											.equals(currentDoctor.getId())
												? "Assigned to: "
													+ clinicalOrder.getAssignedDoctorName()
												: "From: "
													+ clinicalOrder.getRequestingDoctorName();
		JLabel doctorLabel = new JLabel(doctorInfo);

		JLabel dateLabel = new JLabel(
			"Created: " + clinicalOrder.getFormattedDate());

		panel.add(patientLabel);
		panel.add(statusLabel);
		panel.add(doctorLabel);
		panel.add(dateLabel);

		return panel;
	}

	private JPanel createMessagesPanel() {
		JPanel panel = createStyledPanel("Conversation", null);
		panel.setLayout(new BorderLayout());

		messagesPanel = new JPanel();
		messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
		messagesPanel.setBackground(PANEL_BACKGROUND);

		JScrollPane scrollPane = new JScrollPane(messagesPanel);
		scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createResponsePanel() {
		JPanel panel = createStyledPanel("Send Message", null);
		panel.setLayout(new BorderLayout(10, 10));

		// Response area
		responseArea = new JTextArea(4, 30);
		responseArea.setLineWrap(true);
		responseArea.setWrapStyleWord(true);
		responseArea.setFont(LABEL_FONT);
		responseArea.setEnabled(clinicalOrder.isOpen());

		JScrollPane responseScroll = new JScrollPane(responseArea);

		// File upload section
		JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filePanel.setBackground(PANEL_BACKGROUND);

		uploadButton = createStyledButton("Upload File", PRIMARY_COLOR);
		uploadButton.setEnabled(clinicalOrder.isOpen());
		uploadButton.addActionListener(e -> handleUpload());

		fileLabel = new JLabel("No file selected");
		fileLabel.setFont(LABEL_FONT);

		filePanel.add(uploadButton);
		filePanel.add(fileLabel);

		// Buttons panel
		JPanel buttonsPanel = new JPanel(
			new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonsPanel.setBackground(PANEL_BACKGROUND);

		sendButton = createStyledButton("Send Message", SUCCESS_COLOR);
		sendButton.setEnabled(clinicalOrder.isOpen());
		sendButton.addActionListener(e -> sendMessage());

		closeOrderButton = createStyledButton("Close Order",
			new Color(192, 57, 43));
		closeOrderButton.setEnabled(clinicalOrder.isOpen()
			&& clinicalOrder.getRequestingDoctorId()
							.equals(currentDoctor.getId()));
		closeOrderButton.addActionListener(e -> closeOrder());

		JButton closeButton = createStyledButton("Close", Color.GRAY);
		closeButton.addActionListener(e -> dispose());

		buttonsPanel.add(closeOrderButton);
		buttonsPanel.add(sendButton);
		buttonsPanel.add(closeButton);

		panel.add(responseScroll, BorderLayout.CENTER);
		panel.add(filePanel, BorderLayout.NORTH);
		panel.add(buttonsPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createStyledPanel(String title, JPanel contentPanel) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1), title,
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, SECTION_FONT, PRIMARY_COLOR),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		if (contentPanel != null) {
			panel.add(contentPanel, BorderLayout.CENTER);
		}
		return panel;
	}

	private JButton createStyledButton(String text, Color color) {
		JButton button = new JButton(text);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setFont(BUTTON_FONT);
		button.setFocusPainted(false);
		return button;
	}

	private void loadMessages() {
		messagesPanel.removeAll();

		for (ClinicalOrderMessageDTO message : clinicalOrder.getMessages()) {
			messagesPanel.add(createMessagePanel(message));
			messagesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		}

		messagesPanel.revalidate();
		messagesPanel.repaint();

		// Scroll to bottom
		SwingUtilities.invokeLater(() -> {
			JScrollPane scrollPane = (JScrollPane) messagesPanel.getParent()
																.getParent();
			JScrollBar vertical = scrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMaximum());
		});
	}

	private JPanel createMessagePanel(ClinicalOrderMessageDTO message) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1),
			BorderFactory.createEmptyBorder(10, 15, 10, 15)));

		// Header with doctor and date
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(panel.getBackground());

		JLabel doctorLabel = new JLabel(message.getDoctorName());
		doctorLabel.setFont(SECTION_FONT);
		doctorLabel.setForeground(PRIMARY_COLOR);

		JLabel dateLabel = new JLabel(message.getFormattedDate());
		dateLabel.setFont(LABEL_FONT);
		dateLabel.setForeground(Color.DARK_GRAY);

		headerPanel.add(doctorLabel, BorderLayout.WEST);
		headerPanel.add(dateLabel, BorderLayout.EAST);

		// Message text
		JTextArea messageArea = new JTextArea(message.getMessageText());
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setFont(LABEL_FONT);
		messageArea.setBackground(panel.getBackground());
		messageArea.setEditable(false);
		messageArea.setBorder(new EmptyBorder(5, 0, 5, 0));

		// File info
		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		footerPanel.setBackground(panel.getBackground());

		if (message.hasFile()) {
			JButton downloadButton = createStyledButton(
				"Download " + message.getFileName(), PRIMARY_COLOR);
			downloadButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			downloadButton.addActionListener(e -> downloadFile(message));
			footerPanel.add(downloadButton);
		}

		panel.add(headerPanel, BorderLayout.NORTH);
		panel.add(messageArea, BorderLayout.CENTER);
		if (message.hasFile()) {
			panel.add(footerPanel, BorderLayout.SOUTH);
		}

		return panel;
	}

	private void handleUpload() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select file to upload");

		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			fileLabel.setText(selectedFile.getName());

			try {
				fileData = Files.readAllBytes(selectedFile.toPath());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
					"Error reading file: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
				fileLabel.setText("No file selected");
				selectedFile = null;
				fileData = null;
			}
		}
	}

	private void downloadFile(ClinicalOrderMessageDTO message) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File(message.getFileName()));
		fileChooser.setDialogTitle("Save File As");

		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File outputFile = fileChooser.getSelectedFile();

			try {
				byte[] fileBytes = model.getClinicalOrderFileData(
					message.getId());
				if (fileBytes != null) {
					try (FileOutputStream fos = new FileOutputStream(
						outputFile)) {
						fos.write(fileBytes);
					}
					JOptionPane.showMessageDialog(this,
						"File downloaded successfully to: "
							+ outputFile.getAbsolutePath(),
						"Success", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
					"Error downloading file: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void sendMessage() {
		String messageText = responseArea.getText().trim();
		if (messageText.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter a message",
				"Validation Error", JOptionPane.WARNING_MESSAGE);
			responseArea.requestFocus();
			return;
		}

		try {
			String fileName = selectedFile != null ? selectedFile.getName()
				: null;
			model.addClinicalOrderMessage(clinicalOrder.getId(),
				currentDoctor.getId(), messageText, fileName, fileData);

			// Reload the conversation
			ClinicalOrderDTO updatedOrder = model.getClinicalOrderById(
				clinicalOrder.getId());
			if (updatedOrder != null) {
				this.clinicalOrder = updatedOrder;
				loadMessages();
			}

			// Clear response area
			responseArea.setText("");
			fileLabel.setText("No file selected");
			selectedFile = null;
			fileData = null;

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
				"Error sending message: " + e.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void closeOrder() {
		int result = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to close this clinical order?",
			"Close Clinical Order", JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			try {
				model.closeClinicalOrder(clinicalOrder.getId());
				clinicalOrder.setStatus("closed");

				// Update UI
				sendButton.setEnabled(false);
				uploadButton.setEnabled(false);
				closeOrderButton.setEnabled(false);
				responseArea.setEnabled(false);

				JOptionPane.showMessageDialog(this,
					"Clinical order closed successfully", "Success",
					JOptionPane.INFORMATION_MESSAGE);

			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
					"Error closing clinical order: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}