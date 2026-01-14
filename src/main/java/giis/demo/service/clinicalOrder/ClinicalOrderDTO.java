package giis.demo.service.clinicalOrder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClinicalOrderDTO {
	private Integer id;
	private Integer patientId;
	private Integer appointmentId;
	private Integer requestingDoctorId;
	private Integer assignedDoctorId;
	private String concept;
	private String initialDescription;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime closedAt;

	// Joined fields
	private String patientName;
	private String requestingDoctorName;
	private String assignedDoctorName;

	// Messages
	private List<ClinicalOrderMessageDTO> messages = new ArrayList<>();

	// Getters and Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public Integer getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(Integer appointmentId) {
		this.appointmentId = appointmentId;
	}

	public Integer getRequestingDoctorId() {
		return requestingDoctorId;
	}

	public void setRequestingDoctorId(Integer requestingDoctorId) {
		this.requestingDoctorId = requestingDoctorId;
	}

	public Integer getAssignedDoctorId() {
		return assignedDoctorId;
	}

	public void setAssignedDoctorId(Integer assignedDoctorId) {
		this.assignedDoctorId = assignedDoctorId;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getInitialDescription() {
		return initialDescription;
	}

	public void setInitialDescription(String initialDescription) {
		this.initialDescription = initialDescription;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(LocalDateTime closedAt) {
		this.closedAt = closedAt;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getRequestingDoctorName() {
		return requestingDoctorName;
	}

	public void setRequestingDoctorName(String requestingDoctorName) {
		this.requestingDoctorName = requestingDoctorName;
	}

	public String getAssignedDoctorName() {
		return assignedDoctorName;
	}

	public void setAssignedDoctorName(String assignedDoctorName) {
		this.assignedDoctorName = assignedDoctorName;
	}

	public List<ClinicalOrderMessageDTO> getMessages() {
		return messages;
	}

	public void setMessages(List<ClinicalOrderMessageDTO> messages) {
		this.messages = messages;
	}

	public void addMessage(ClinicalOrderMessageDTO message) {
		this.messages.add(message);
	}

	public String getFormattedDate() {
		if (createdAt != null) {
			return createdAt.format(
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		}
		return "N/A";
	}

	public boolean isOpen() {
		return "open".equals(status);
	}

	public boolean isClosed() {
		return "closed".equals(status);
	}

	public int getMessageCount() {
		return messages.size();
	}

	public ClinicalOrderMessageDTO getLastMessage() {
		return messages.isEmpty() ? null : messages.get(messages.size() - 1);
	}

}