package giis.demo.ui.clinicalOrder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.clinicalOrder.ClinicalOrderDTO;
import giis.demo.service.clinicalOrder.ClinicalOrderMessageDTO;
import giis.demo.service.doctor.DoctorDTO;

public class ClinicalOrdersListWindow extends JDialog {
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

	private JPanel ordersPanel;
	private AppointmentModel model;
	private DoctorDTO currentDoctor;

	public ClinicalOrdersListWindow(JFrame parent, AppointmentModel model,
		DoctorDTO currentDoctor) {
		super(parent, "Clinical Orders", true);
		this.model = model;
		this.currentDoctor = currentDoctor;
		initializeUI();
		loadClinicalOrders();
	}

	private void initializeUI() {
		setSize(900, 700);
		setLocationRelativeTo(getParent());
		setResizable(true);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
		contentPane.setBackground(BACKGROUND_COLOR);
		setContentPane(contentPane);

		// Header
		JLabel headerLabel = new JLabel("Clinical Orders", JLabel.CENTER);
		headerLabel.setFont(TITLE_FONT);
		headerLabel.setForeground(PRIMARY_COLOR);
		headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
		contentPane.add(headerLabel, BorderLayout.NORTH);

		// Orders list
		contentPane.add(createOrdersPanel(), BorderLayout.CENTER);

		// Close button
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBackground(BACKGROUND_COLOR);
		JButton closeButton = createStyledButton("Close",
			new Color(192, 57, 43));
		closeButton.addActionListener(e -> dispose());
		buttonPanel.add(closeButton);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel createOrdersPanel() {
		JPanel panel = createStyledPanel("Clinical Orders List", null);
		panel.setLayout(new BorderLayout());

		ordersPanel = new JPanel();
		ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
		ordersPanel.setBackground(PANEL_BACKGROUND);

		JScrollPane scrollPane = new JScrollPane(ordersPanel);
		scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

		panel.add(scrollPane, BorderLayout.CENTER);
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
			BorderFactory.createEmptyBorder(5, 5, 5, 5)));

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

	private void loadClinicalOrders() {
		List<ClinicalOrderDTO> orders = model.getClinicalOrdersForDoctor(
			currentDoctor.getId());
		displayClinicalOrders(orders);
	}

	private void displayClinicalOrders(List<ClinicalOrderDTO> orders) {
		ordersPanel.removeAll();

		if (orders.isEmpty()) {
			JLabel noOrdersLabel = new JLabel("No clinical orders found",
				JLabel.CENTER);
			noOrdersLabel.setFont(LABEL_FONT);
			noOrdersLabel.setForeground(Color.GRAY);
			ordersPanel.add(noOrdersLabel);
		} else {
			for (ClinicalOrderDTO order : orders) {
				ordersPanel.add(createOrderListItem(order));
				ordersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			}
		}

		ordersPanel.revalidate();
		ordersPanel.repaint();
	}

	private JPanel createOrderListItem(ClinicalOrderDTO order) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PANEL_BACKGROUND);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1),
			BorderFactory.createEmptyBorder(15, 15, 15, 15)));

		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBackground(PANEL_BACKGROUND);

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(PANEL_BACKGROUND);

		JLabel conceptLabel = new JLabel(order.getConcept());
		conceptLabel.setFont(SECTION_FONT);
		conceptLabel.setForeground(PRIMARY_COLOR);

		JLabel statusLabel = new JLabel();
		statusLabel.setFont(LABEL_FONT);
		if (order.isOpen()) {
			statusLabel.setText("Open");
			statusLabel.setForeground(SUCCESS_COLOR);
		} else {
			statusLabel.setText("Closed");
			statusLabel.setForeground(Color.GRAY);
		}

		headerPanel.add(conceptLabel, BorderLayout.WEST);
		headerPanel.add(statusLabel, BorderLayout.EAST);

		// Details
		JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
		detailsPanel.setBackground(PANEL_BACKGROUND);

		String doctorText = order	.getRequestingDoctorId()
									.equals(currentDoctor.getId())
										? "To: " + order.getAssignedDoctorName()
										: "From: "
											+ order.getRequestingDoctorName();

		JLabel doctorLabel = new JLabel(doctorText);
		doctorLabel.setFont(LABEL_FONT);

		JLabel patientLabel = new JLabel("Patient: " + order.getPatientName());
		patientLabel.setFont(LABEL_FONT);

		JLabel dateLabel = new JLabel("Created: " + order.getFormattedDate());
		dateLabel.setFont(LABEL_FONT);

		JLabel messagesLabel = new JLabel(
			"Messages: " + order.getMessageCount());
		messagesLabel.setFont(LABEL_FONT);

		ClinicalOrderMessageDTO lastMessage = order.getLastMessage();
		if (lastMessage != null) {
			JLabel lastMessageLabel = new JLabel(
				"Last: " + (lastMessage.getMessageText().length() > 50
					? lastMessage.getMessageText().substring(0, 50) + "..."
					: lastMessage.getMessageText()));
			lastMessageLabel.setFont(LABEL_FONT);
			lastMessageLabel.setForeground(Color.DARK_GRAY);
			detailsPanel.add(lastMessageLabel);
		}

		detailsPanel.add(doctorLabel);
		detailsPanel.add(patientLabel);
		detailsPanel.add(dateLabel);
		detailsPanel.add(messagesLabel);

		infoPanel.add(headerPanel, BorderLayout.NORTH);
		infoPanel.add(detailsPanel, BorderLayout.CENTER);

		JButton openButton = createStyledButton("Open", PRIMARY_COLOR);
		openButton.addActionListener(e -> openClinicalOrder(order));

		panel.add(infoPanel, BorderLayout.CENTER);
		panel.add(openButton, BorderLayout.EAST);

		return panel;
	}

	private void openClinicalOrder(ClinicalOrderDTO order) {
		ClinicalOrderConversationWindow conversationWindow = new ClinicalOrderConversationWindow(
			(JFrame) SwingUtilities.getWindowAncestor(this), model,
			currentDoctor, order);
		conversationWindow.setVisible(true);

		loadClinicalOrders();
	}
}