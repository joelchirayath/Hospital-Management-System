package giis.demo.service.appointment.doctorAppt;

public class DoctorApptControllerImpl implements DoctorApptController {

    private DoctorApptLogic logic;
	
    public DoctorApptControllerImpl() {
        logic = new DoctorApptLogic();
    }

    @Override
    public void saveDoctorBooking(int doctorId, int apptId) {
        logic.saveDoctorBooking(doctorId, apptId);
    }

    @Override
    public void sendUrgentEmailToDoctors(int apptId, String patientName, String date,
                                         String timeRange, String room, String reason) {
        logic.sendUrgentEmailToDoctors(apptId, patientName, date, timeRange, room, reason);
    }
}
