package giis.demo.jdbc.models.apointment.prescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import giis.demo.service.appointment.prescription.MedicationDTO;
import giis.demo.util.Database;

public class MedicationModel {
	public Database db;

	public MedicationModel(Database db) {
		this.db = db;
//		db.createDatabase(true);
//		db.loadDatabase();
	}

	public List<MedicationDTO> getMedicationOptions() {
		List<MedicationDTO> medications = new ArrayList<>();
		String sql = "SELECT id, medication_name, amount, duration, interval_hours FROM medications ORDER BY medication_name";

		try (Connection conn = db.getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("medication_name");
				int amount = rs.getInt("amount");
				int duration = rs.getInt("duration");
				int interval_hours = rs.getInt("interval_hours");
				medications.add(new MedicationDTO(id, name, amount, duration,
					interval_hours, ""));
			}

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return medications;
	}

	public void saveMedication(MedicationDTO medication, int appointmentId) {
		String sql = "INSERT INTO prescribed_medications (medication_id, appointment_id, amount, duration, interval_hours, notes) "
			+ "VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = db.getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, medication.getId());
			pstmt.setInt(2, appointmentId);
			pstmt.setInt(3, medication.getAmount());
			pstmt.setInt(4, medication.getDuration());
			pstmt.setInt(5, medication.getInterval_hours());
			pstmt.setString(6, medication.getNotes());

			pstmt.executeUpdate();
			pstmt.close();
			System.out.println("Medication saved : " + medication.toString()
				+ "in appt " + appointmentId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(
				"Error saving medication: " + e.getMessage(), e);
		}
	}
}