package giis.demo.service.clinicalOrder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClinicalOrderMessageDTO {
	private Integer id;
	private Integer clinicalOrderId;
	private Integer doctorId;
	private String messageText;
	private String fileName;
	private byte[] fileData;
	private LocalDateTime createdAt;

	// Joined fields
	private String doctorName;

	// Getters and Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getClinicalOrderId() {
		return clinicalOrderId;
	}

	public void setClinicalOrderId(Integer clinicalOrderId) {
		this.clinicalOrderId = clinicalOrderId;
	}

	public Integer getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Integer doctorId) {
		this.doctorId = doctorId;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getFormattedDate() {
		if (createdAt != null) {
			return createdAt.format(
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		}
		return "N/A";
	}

	public boolean hasFile() {
		return fileName != null && !fileName.isEmpty();
	}
}