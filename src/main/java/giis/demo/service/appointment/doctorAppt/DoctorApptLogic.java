package giis.demo.service.appointment.doctorAppt;

import giis.demo.jdbc.models.apointment.DoctorApptModel;
import giis.demo.jdbc.models.apointment.DoctorApptModelImpl;

import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class DoctorApptLogic {

    private DoctorApptModel model;

    public DoctorApptLogic() {
        model = new DoctorApptModelImpl();
    }

    public void saveDoctorBooking(int doctor, int apptId) {
        model.saveDoctorBooking(doctor, apptId);
    }

    /**
     * Sends an urgent email notification about an appointment
     * to ALL doctors in the database.
     *
     * The body includes:
     *  - Patient name
     *  - Consulted doctor(s) for this appointment
     *  - Date, time, room, reason
     *  - Urgent notice
     */
    public void sendUrgentEmailToDoctors(int apptId, String patientName, String date,
                                         String timeRange, String room, String reason) {
        try {
            // All doctors receive the email
            List<String> allDoctorEmails = model.getAllDoctorEmails();
            if (allDoctorEmails == null || allDoctorEmails.isEmpty()) {
                System.err.println("No doctor emails found. Urgent email not sent.");
                return;
            }

            // Doctors consulted for this specific appointment
            List<String> consulted = model.getDoctorNamesByAppointment(apptId);
            String consultedDoctors = (consulted == null || consulted.isEmpty())
                    ? "Not assigned"
                    : String.join(", ", consulted);

            // Hospital Gmail credentials - configure properly (use app password)
            final String hospitalEmail = "hospital.oviedo12@gmail.com"; // your hospital Gmail
            final String hospitalPassword = "vjrh xzcy hfxl gipe";   // use Gmail App Password

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(hospitalEmail, hospitalPassword);
                }
            });

            String subject = "🚨 URGENT: New Appointment (" + patientName + ")";

            String bodyTemplate =
                    "🚨 URGENT APPOINTMENT NOTICE\n"
                  + "Patient: %s\n"
                  + "Doctor: %s\n"
                  + "Date: %s\n"
                  + "Time: %s\n"
                  + "Room: %s\n"
                  + "Reason: %s\n\n"
                  + "⚠️ Please prioritize this appointment as URGENT.";

            String body = String.format(bodyTemplate,
                    patientName,
                    consultedDoctors,
                    date,
                    timeRange,
                    room,
                    reason != null ? reason : "-");

            for (String email : allDoctorEmails) {
                if (email == null || email.trim().isEmpty())
                    continue;

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(hospitalEmail, "Hospital Management System"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
            }

            System.out.println("Urgent email notifications sent to all doctors.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error sending urgent email notifications: " + e.getMessage());
        }
    }
}
