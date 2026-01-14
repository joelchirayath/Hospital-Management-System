package giis.demo.service.appointment.doctorAppt;

public interface DoctorApptController {

    void saveDoctorBooking(int doctorId, int apptId);

    // Send urgent emails to ALL doctors, including consulted doctor(s) in body
    void sendUrgentEmailToDoctors(int apptId, String patientName, String date,
                                  String timeRange, String room, String reason);
}
