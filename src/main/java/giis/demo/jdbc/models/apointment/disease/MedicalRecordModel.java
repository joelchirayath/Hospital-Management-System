package giis.demo.jdbc.models.apointment.disease;

import java.time.LocalDate;

import giis.demo.service.appointment.icd10.ICDDTO;
import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class MedicalRecordModel {
	private Database db;

	public MedicalRecordModel(Database db) {
		this.db = db;
//		db.createDatabase(false);
//		db.loadDatabase();
	}

	/**
	 * Guarda una enfermedad (ICD-10) en el historial médico
	 */
	public void saveIllnessToMedicalRecord(int patientId, int appointmentId,
		int doctorId, String doctorName, ICDDTO illness) {
		try {
			String sql = "INSERT INTO medical_records (patient_id, appointment_id, doctor_id, "
				+ " icd10_code, diagnosis_date, notes) VALUES (?, ?, ?, ?, ?, ?)";

			db.executeUpdate(sql, patientId, appointmentId, doctorId,
				illness.getCode(), // Código ICD-10
				LocalDate.now().toString(), // Fecha de diagnóstico
				"Diagnosis: " + illness.getDescription() // Notas
															// adicionales
			);

			System.out.println("Mediacl record added to DB : patient "
				+ patientId + ", illnesses" + illness.getCode());

		} catch (Exception e) {
			throw new ApplicationException(
				"Error saving illness to medical record :(: " + e.getMessage());
		}
	}

}
