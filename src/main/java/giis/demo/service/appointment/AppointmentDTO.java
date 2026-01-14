package giis.demo.service.appointment;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AppointmentDTO {
    private Integer id;
    private String date;
    private String start_time;  
    private String end_time;    
    private String office;
    private Integer patient_id;
    private String notes;
    private String status;
    private Integer attended; // NULL, 1 (attended), 0 (did not attend)
    private String check_in_time;
    private String check_out_time;
    
    private Integer doctor_id; // only used when fetching from DB
    
    // Additional join fields
    private String patient_name;
    private String patient_surname;
    private String patient_dni;

    // NEW FIELD ↓
    private boolean urgent; // indicates if appointment is urgent

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getStart_time() { return start_time; }  
    public void setStart_time(String start_time) { this.start_time = start_time; }
    
    public String getEnd_time() { return end_time; }      
    public void setEnd_time(String end_time) { this.end_time = end_time; }
    
    public String getOffice() { return office; }
    public void setOffice(String office) { this.office = office; }
    
    public Integer getPatient_id() { return patient_id; }
    public void setPatient_id(Integer patient_id) { this.patient_id = patient_id; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getAttended() { return attended; }
    public void setAttended(Integer attended) { this.attended = attended; }
    
    public String getCheck_in_time() { return check_in_time; }
    public void setCheck_in_time(String check_in_time) { this.check_in_time = check_in_time; }
    
    public String getCheck_out_time() { return check_out_time; }
    public void setCheck_out_time(String check_out_time) { this.check_out_time = check_out_time; }
    
    public String getPatient_name() { return patient_name; }
    public void setPatient_name(String patient_name) { this.patient_name = patient_name; }
    
    public String getPatient_surname() { return patient_surname; }
    public void setPatient_surname(String patient_surname) { this.patient_surname = patient_surname; }
    
    public String getPatient_dni() { return patient_dni; }
    public void setPatient_dni(String patient_dni) { this.patient_dni = patient_dni; }

    public Integer getDoctor_id() { return doctor_id; }
    public void setDoctor_id(Integer doctor_id) { this.doctor_id = doctor_id; }

    // NEW GETTER/SETTER ↓
    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public String getTimeSlot() {
        return start_time + " - " + end_time;
    }
    
    public String getActualTimeSlot() {
        if (check_in_time != null && check_out_time != null) {
            return check_in_time + " - " + check_out_time;
        } else if (check_in_time != null) {
            return check_in_time + " - (In consultation)";
        }
        return getTimeSlot() + " (Scheduled)";
    }
    
    public String getFullPatientName() {
        if (patient_name != null && patient_surname != null) {
            return patient_name + " " + patient_surname;
        }
        return "Patient ID: " + patient_id;
    }
    
    public String getAttendanceStatus() {
        if (attended == null) {
            return "Not Recorded";
        }
        return attended == 1 ? "Attended" : "Did Not Attend";
    }
    
    public Color getAttendanceColor() {
        if (attended == null) {
            return Color.GRAY;
        }
        return attended == 1 ? Color.GREEN : Color.RED;
    }
    
    public boolean hasCheckedIn() {
        return check_in_time != null && !check_in_time.isEmpty();
    }
    
    public boolean hasCheckedOut() {
        return check_out_time != null && !check_out_time.isEmpty();
    }
    
    public boolean isInProgress() {
        return hasCheckedIn() && !hasCheckedOut();
    }
    
    private String formatTime(String time) {
        if (time == null) return "Not set";
        try {
            LocalTime lt = LocalTime.parse(time);
            return lt.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return time;
        }
    }
    
    public String getFormattedCheckInTime() {
        return formatTime(check_in_time);
    }
    
    public String getFormattedCheckOutTime() {
        return formatTime(check_out_time);
    }
    
    @Override
    public String toString() {
        return "AppointmentDTO{id=" + id + ", date=" + date 
            + ", patient=" + getFullPatientName() 
            + ", urgent=" + urgent
            + ", check_in=" + check_in_time 
            + ", check_out=" + check_out_time + "}";
    }
}
