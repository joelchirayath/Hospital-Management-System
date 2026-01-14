package giis.demo.jdbc.models.apointment;

import giis.demo.util.Database;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorApptModelImpl implements DoctorApptModel {

    private Database db;
    private static boolean initialized = false;

    public DoctorApptModelImpl() {
        this.db = new Database();
        // ❌ DO NOT call createDatabase or loadDatabase here
        // The DB is initialized once in AppointmentModel
    }
	
    @Override
    public void saveDoctorBooking(int doctor, int apptId) {
        String sql = "INSERT INTO doctors_appointments (id_doctors, id_appointments) VALUES (?, ?)";
        Object[][] paramsArray = new Object[][] { { doctor, apptId } };
        db.executeBatchUpdate(sql, paramsArray);
        System.out.println("MODEL: doctor-appt added -> doctor: " + doctor + " appt id: " + apptId);
    }

    @Override
    public List<String> getDoctorEmailsByAppointment(int apptId) {
        String sql = "SELECT d.email FROM doctors d "
                   + "JOIN doctors_appointments da ON d.id = da.id_doctors "
                   + "WHERE da.id_appointments = ?";
        List<Object[]> result = db.executeQueryArray(sql, apptId);
        return result.stream().map(r -> (String) r[0]).collect(Collectors.toList());
    }

    // NEW: all doctors' emails
    @Override
    public List<String> getAllDoctorEmails() {
        String sql = "SELECT email FROM doctors";
        List<Object[]> result = db.executeQueryArray(sql);
        return result.stream().map(r -> (String) r[0]).collect(Collectors.toList());
    }

    // NEW: names of doctors linked to the appointment
    @Override
    public List<String> getDoctorNamesByAppointment(int apptId) {
        String sql = "SELECT d.name || ' ' || d.surname FROM doctors d "
                   + "JOIN doctors_appointments da ON d.id = da.id_doctors "
                   + "WHERE da.id_appointments = ?";
        List<Object[]> result = db.executeQueryArray(sql, apptId);
        return result.stream().map(r -> (String) r[0]).collect(Collectors.toList());
    }
}
