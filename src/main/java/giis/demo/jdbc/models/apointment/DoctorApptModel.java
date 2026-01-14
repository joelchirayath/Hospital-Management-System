package giis.demo.jdbc.models.apointment;

import java.util.List;

public interface DoctorApptModel {

    void saveDoctorBooking(int doctor, int apptId);

    List<String> getDoctorEmailsByAppointment(int apptId);

    // NEW: all doctor emails in the system
    List<String> getAllDoctorEmails();
    // NEW: names of doctors linked to a specific appointment
    List<String> getDoctorNamesByAppointment(int apptId);
}
