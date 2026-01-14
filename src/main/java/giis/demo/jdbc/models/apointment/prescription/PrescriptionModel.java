package giis.demo.jdbc.models.apointment.prescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import giis.demo.service.appointment.prescription.PrescriptionDTO;
import giis.demo.util.Database;

public class PrescriptionModel {
	public Database db;

	public PrescriptionModel(Database db) {
		this.db = db;
//		db.createDatabase(true);
//		db.loadDatabase();
	}

	public List<PrescriptionDTO> getPrescriptionOptions() {
		List<PrescriptionDTO> prescriptions = new ArrayList<>();
		String sql = "SELECT id, name FROM prescriptions ORDER BY name";

		try (Connection conn = db.getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				prescriptions.add(new PrescriptionDTO(id, name, ""));
			}

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return prescriptions;
	}

	public void savePrescription(PrescriptionDTO prescription,
		int appointmentId) {
		String sql = "INSERT INTO assigned_prescriptions (appointment_id, prescription_id, notes) VALUES (?, ?, ?)";

		try (Connection conn = db.getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, appointmentId);
			pstmt.setInt(2, prescription.getId());
			pstmt.setString(3, prescription.getNotes());

			pstmt.executeUpdate();
			pstmt.close();
			System.out.println("Prescription saved : " + prescription.toString()
				+ "in appt " + appointmentId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(
				"Error saving prescription: " + e.getMessage(), e);
		}
	}
}