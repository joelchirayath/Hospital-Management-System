package giis.demo.service.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import giis.demo.service.appointment.icd10.ICDDTO;
import giis.demo.service.clinicalOrder.ClinicalOrderDTO;
import giis.demo.service.clinicalOrder.ClinicalOrderMessageDTO;
import giis.demo.service.doctor.DoctorDTO;
import giis.demo.service.icd10.Icd10CodeDTO;
import giis.demo.service.medicalRecord.MedicalRecordDTO;
import giis.demo.service.patient.PatientDTO;
import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class AppointmentModel {
	public Database db;

	// Add this static flag
	private static boolean initialized = false;

	public AppointmentModel() {
		this.db = new Database();
		if (!initialized) {
			db.createDatabase(true); // create schema once
			db.loadDatabase(); // load data once
			initialized = true;
		}
	}

	public AppointmentModel(Database db) {
		this.db = db;
	}

	/**
	 * Returns all appointments with patient and doctor names for the
	 * receptionist view
	 */
	public List<AppointmentDTO> getAllAppointmentsWithDetails() {
		String sql = "SELECT a.*, "

				+ "p.name AS patient_name, p.surname AS patient_surname, "
				+ "d.name AS doctor_name, d.surname AS doctor_surname " + // we
																			// asume
																			// that
																			// we
																			// only
																			// need
																			// one
																			// doctor
				"FROM appointments a "
				+ "JOIN patients p ON a.patient_id = p.id "
				+ "JOIN doctors_appointments da ON a.id = da.id_appointments "
				+ "JOIN doctors d ON da.id_doctors = d.id " + //
				"GROUP BY a.id " + //
				"ORDER BY a.date DESC, a.start_time ASC";

		return db.executeQueryPojo(AppointmentDTO.class, sql);
	}

	public List<AppointmentDTO> getAppointmentsForDoctorAndDate(int doctorId,
			LocalDate date) {
		String sql = "SELECT a.*, p.name as patient_name, p.surname as patient_surname, p.dni as patient_dni "
				+ "FROM appointments a JOIN patients p ON a.patient_id = p.id "
				+ "JOIN doctors_appointments da ON a.id = da.id_appointments "
				+ "WHERE da.id_doctors = ? AND a.date = ? ORDER BY a.start_time";
		return db.executeQueryPojo(AppointmentDTO.class, sql, doctorId,
				date.toString());
	}

	public AppointmentDTO getAppointmentById(int appointmentId) {
		String sql = "SELECT a.*, "

				+ "p.name as patient_name, p.surname as patient_surname, p.dni as patient_dni, "
				+ "d.name as doctor_name, d.surname as doctor_surname "
				+ "FROM appointments a "
				+ "JOIN patients p ON a.patient_id = p.id "
				+ "JOIN doctors_appointments da ON a.id = da.id_appointments "
				+ "JOIN doctors d ON da.id_doctors = d.id " + "WHERE a.id = ? "
				+ "GROUP BY a.id";
		List<AppointmentDTO> appointments = db
				.executeQueryPojo(AppointmentDTO.class, sql, appointmentId);

		return appointments.isEmpty() ? null : appointments.get(0);
	}

	public void updateAttendance(int appointmentId, Integer attended) {
		String sql = "UPDATE appointments SET attended = ? WHERE id = ?";
		db.executeUpdate(sql, attended, appointmentId);
	}

	public PatientDTO getPatientById(Integer patientId) {
		if (patientId == null) {
			return null;
		}

		System.out.println("=== DEBUG: TODOS LOS PACIENTES EN LA BD ===");
		try {
			String allPatientsSql = "SELECT id, name, surname, dni, phone, email, address, date_of_birth, gender FROM patients ORDER BY id";
			List<PatientDTO> allPatients = db.executeQueryPojo(PatientDTO.class,
					allPatientsSql);

			if (allPatients.isEmpty()) {
				System.out.println("❌ NO HAY PACIENTES EN LA BASE DE DATOS");
			} else {
				System.out.println("📋 LISTA COMPLETA DE PACIENTES ("
						+ allPatients.size() + " pacientes):");
				for (PatientDTO patient : allPatients) {
					System.out.println("   ID: " + patient.getId()
							+ " | Nombre: " + patient.getFullName() + " | DNI: "
							+ (patient.getDni() != null ? patient.getDni()
									: "N/A")
							+ " | Tel: "
							+ (patient.getPhone() != null ? patient.getPhone()
									: "N/A"));
				}
			}
		} catch (Exception e) {
			System.out.println(
					"❌ ERROR al obtener lista de pacientes: " + e.getMessage());
		}
		System.out.println("=== FIN DEBUG ===");

		String sql = "SELECT id, name, surname, dni, phone, email, address, date_of_birth, gender "
				+ "FROM patients WHERE id = ?";

		List<PatientDTO> patients = db.executeQueryPojo(PatientDTO.class, sql,
				patientId);

		if (patients != null && !patients.isEmpty()) {
			return patients.get(0); // Retorna el primer paciente encontrado
		}

		return null; // No se encontró el paciente
	}

	public void checkIn(int appointmentId) {
		String currentTime = LocalTime.now()
				.format(DateTimeFormatter.ofPattern("HH:mm"));
		String sql = "UPDATE appointments SET check_in_time = ? WHERE id = ?";
		db.executeUpdate(sql, currentTime, appointmentId);
	}

	public void checkIn(int appointmentId, String checkInTime) {
		String sql = "UPDATE appointments SET check_in_time = ? WHERE id = ?";
		db.executeUpdate(sql, checkInTime, appointmentId);
	}

	public void checkOut(int appointmentId) {
		String currentTime = LocalTime.now()
				.format(DateTimeFormatter.ofPattern("HH:mm"));
		String sql = "UPDATE appointments SET check_out_time = ? WHERE id = ?";
		db.executeUpdate(sql, currentTime, appointmentId);
	}

	public void checkOut(int appointmentId, String checkOutTime) {
		String sql = "UPDATE appointments SET check_out_time = ? WHERE id = ?";
		db.executeUpdate(sql, checkOutTime, appointmentId);
	}

	public void updateCheckTimes(int appointmentId, String checkInTime,
			String checkOutTime) {
		String sql = "UPDATE appointments SET check_in_time = ?, check_out_time = ? WHERE id = ?";
		db.executeUpdate(sql, checkInTime, checkOutTime, appointmentId);
	}

	public List<MedicalRecordDTO> getMedicalRecordsByPatient(int patientId) {
		String sql = "SELECT mr.*, d.name as doctor_name, d.surname as doctor_surname, a.date as appointment_date, icd.description as icd10_description, icd.chapter as icd10_chapter FROM medical_records mr JOIN doctors d ON mr.doctor_id = d.id JOIN appointments a ON mr.appointment_id = a.id JOIN icd10_codes icd ON mr.icd10_code = icd.code WHERE mr.patient_id = ? ORDER BY mr.diagnosis_date DESC, mr.created_at DESC";
		return db.executeQueryPojo(MedicalRecordDTO.class, sql, patientId);
	}

	public List<Icd10CodeDTO> getAllIcd10Codes() {
		String sql = "SELECT * FROM icd10_codes ORDER BY code";
		return db.executeQueryPojo(Icd10CodeDTO.class, sql);
	}

	public List<Icd10CodeDTO> searchIcd10Codes(String searchTerm) {
		String sql = "SELECT * FROM icd10_codes  WHERE code LIKE ? OR description LIKE ? ORDER BY code";
		String likeTerm = "%" + searchTerm + "%";
		return db.executeQueryPojo(Icd10CodeDTO.class, sql, likeTerm, likeTerm);
	}

	public Icd10CodeDTO getIcd10CodeByCode(String code) {
		String sql = "SELECT * FROM icd10_codes WHERE code = ?";
		List<Icd10CodeDTO> codes = db.executeQueryPojo(Icd10CodeDTO.class, sql,
				code);
		return codes.isEmpty() ? null : codes.get(0);
	}

	// ==========================================================
	// 🔽 RECEPTIONIST FEATURES
	// ==========================================================

	public boolean isOfficeBooked(String officeName, String date, String start,
			String end) {
		String sql = "SELECT COUNT(*) FROM appointments "
				+ "WHERE office = ? AND date = ? "
				+ "AND ( (start_time < ? AND end_time > ?) "
				+ "OR (start_time >= ? AND start_time < ?) "
				+ "OR (end_time > ? AND end_time <= ?) )";

		List<Object[]> result = db.executeQueryArray(sql, officeName, date, end,
				start, start, end, start, end);

		if (result.isEmpty() || result.get(0)[0] == null) {
			return false;
		}

		int count = ((Number) result.get(0)[0]).intValue();
		return count > 0;
	}

	// ✅ UPDATED: now saves urgent flag too (column `urgent` must exist)
	public Integer saveAppointment(AppointmentDTO appt) {
		boolean isUrgent = appt.isUrgent();

		String sql;
		Object[] params;

		if (isUrgent) {
			// Include urgent column only for urgent appointments
			sql = "INSERT INTO appointments "
					+ "(date, start_time, end_time, office, patient_id, notes, status, urgent) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			params = new Object[] {
					appt.getDate() != null ? appt.getDate()
							: LocalDate.now().toString(),
					appt.getStart_time(), appt.getEnd_time(), appt.getOffice(),
					appt.getPatient_id() != null ? appt.getPatient_id() : 1,
					appt.getNotes(), appt.getStatus(), 1 // urgent = true
			};
		} else {
			// Normal appointment → let urgent use DB default (0)
			sql = "INSERT INTO appointments "
					+ "(date, start_time, end_time, office, patient_id, notes, status) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
			params = new Object[] {
					appt.getDate() != null ? appt.getDate()
							: LocalDate.now().toString(),
					appt.getStart_time(), appt.getEnd_time(), appt.getOffice(),
					appt.getPatient_id() != null ? appt.getPatient_id() : 1,
					appt.getNotes(), appt.getStatus() };
		}

		db.executeUpdate(sql, params);

		// Return the newly inserted appointment ID
		String selectLastIdSql = "SELECT LAST_INSERT_ROWID()";
		try {
			List<Object[]> results = db.executeQueryArray(selectLastIdSql);
			if (!results.isEmpty() && results.get(0)[0] != null) {
				return ((Number) results.get(0)[0]).intValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<String> getAllDoctors() {
		String sql = "SELECT name || ' ' || surname AS full_name FROM doctors ORDER BY name";
		List<Object[]> result = db.executeQueryArray(sql);
		return result.stream().map(row -> (String) row[0])
				.collect(Collectors.toList());
	}

	public List<String> getAllRooms() {
		String sql = "SELECT name FROM rooms ORDER BY name";
		List<Object[]> result = db.executeQueryArray(sql);
		return result.stream().map(row -> (String) row[0])
				.collect(Collectors.toList());
	}

	public Integer getDoctorIdByName(String fullName) {
		String sql = "SELECT id FROM doctors WHERE name || ' ' || surname = ?";
		List<Object[]> result = db.executeQueryArray(sql, fullName);
		if (result.isEmpty() || result.get(0)[0] == null) {
			return null;
		}
		return ((Number) result.get(0)[0]).intValue();
	}

	// ==========================================================
	// 🔽 USER STORY: Doctor schedule + conflicts
	// ==========================================================

	public boolean isDoctorWorking(String doctorDni, String date, String start,
			String end) {
		LocalDate localDate = LocalDate.parse(date);
		String dayOfWeek = localDate.getDayOfWeek().toString();

		String sql = "SELECT COUNT(*) FROM working_day "
				+ "WHERE dni = ? AND worker_type = 'Doctor' "
				+ "AND (LOWER(dayOfweek) = LOWER(?)) "
				+ "AND start <= ? AND end >= ?";

		List<Object[]> result = db.executeQueryArray(sql, doctorDni, dayOfWeek,
				start, end);

		if (result.isEmpty() || result.get(0)[0] == null) {
			return false;
		}

		int count = ((Number) result.get(0)[0]).intValue();
		return count > 0;
	}

	public boolean isDoctorBooked(int doctorId, String date, String start,
			String end) {
		String sql = "SELECT COUNT(*) FROM appointments a "
				+ "JOIN doctors_appointments da ON a.id = da.id_appointments "
				+ "WHERE da.id_doctors = ? AND a.date = ? "
				+ "AND ((a.start_time < ? AND a.end_time > ?) OR (a.start_time >= ? AND a.start_time < ?))";

		List<Object[]> result = db.executeQueryArray(sql, doctorId, date, end,
				start, start, end);

		if (result.isEmpty() || result.get(0)[0] == null) {
			return false;
		}

		int count = ((Number) result.get(0)[0]).intValue();
		return count > 0;
	}

	// ==========================================================
	// 🔽 MANAGER FEATURE: Count appointments by date
	// ==========================================================
	public int getAppointmentCountByDate(String date) {
		String sql = "SELECT COUNT(*) FROM appointments WHERE date = ?";
		List<Object[]> result = db.executeQueryArray(sql, date);

		if (result.isEmpty() || result.get(0)[0] == null) {
			return 0;
		}

		return ((Number) result.get(0)[0]).intValue();
	}

	// ==========================================================
	// 🔽 MANAGER FEATURE: Appointments over time (for analytics)
	// ==========================================================
	/**
	 * Returns aggregated appointment counts for a given date range and
	 * interval.
	 *
	 * @param from         inclusive start date (yyyy-MM-dd)
	 * @param to           inclusive end date (yyyy-MM-dd)
	 * @param interval     grouping interval (HOUR/DAY/WEEK/MONTH/YEAR)
	 * @param doctorId     optional doctor id filter (nullable)
	 * @param departmentId optional department id filter (nullable)
	 */

	// ==========================================================
	// 🔽 MANAGER FEATURE: Overloaded version with hour range filter
	// (non-breaking)
	// ==========================================================
	/**
	 * Returns aggregated appointment counts for a given date range and
	 * interval, supporting optional time range filtering for the HOUR interval.
	 * This method does not modify existing teammate logic.
	 */
	public List<AppointmentTimeSeriesDTO> getAppointmentsOverTime(String from,
			String to, TimeInterval interval, Integer doctorId,
			Integer departmentId, String fromTime, String toTime) {

		if (from == null || to == null || interval == null) {
			throw new ApplicationException(
					"Date range and interval are required");
		}

		final String bucketExpr;
		switch (interval) {
		case HOUR:
			bucketExpr = "strftime('%Y-%m-%d %H:00', a.date || ' ' || COALESCE(a.start_time, '00:00'))";
			break;
		case DAY:
			bucketExpr = "a.date";
			break;
		case WEEK:
			bucketExpr = "strftime('%Y-W%W', a.date)";
			break;
		case MONTH:
			bucketExpr = "strftime('%Y-%m', a.date)";
			break;
		case YEAR:
			bucketExpr = "strftime('%Y', a.date)";
			break;
		default:
			throw new ApplicationException("Unsupported interval: " + interval);
		}

		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		sql.append("SELECT ").append(bucketExpr).append(" AS bucketLabel, ")
				.append("COUNT(*) AS count ").append("FROM appointments a ");

		boolean filterByDoctor = (doctorId != null);
		boolean filterByDepartment = (departmentId != null);

		if (filterByDoctor || filterByDepartment) {
			sql.append(
					"JOIN doctors_appointments da ON a.id = da.id_appointments ");
		}
		if (filterByDepartment) {
			sql.append("JOIN doctors d ON da.id_doctors = d.id ");
		}

		sql.append("WHERE a.date >= ? AND a.date <= ? ");
		params.add(from);
		params.add(to);

		if (filterByDoctor) {
			sql.append("AND da.id_doctors = ? ");
			params.add(doctorId);
		}

		// 🕐 Apply from/to time only when interval = HOUR
		if (interval == TimeInterval.HOUR && fromTime != null
				&& toTime != null) {
			sql.append("AND a.start_time >= ? AND a.start_time <= ? ");
			params.add(fromTime);
			params.add(toTime);
		}

		if (filterByDepartment) {
			sql.append("AND d.department_id = ? ");
			params.add(departmentId);
		}

		sql.append("GROUP BY bucketLabel ").append("ORDER BY bucketLabel ASC");

		return db.executeQueryPojo(AppointmentTimeSeriesDTO.class,
				sql.toString(), params.toArray());
	}

	// ==========================================================
	// 🔽 FAMILIAR ANTECEDENTS AND PERSONAL PROBLEMS
	// ==========================================================

	public void addFamiliarAntecedent(int patientId, String antecedent,
			int doctorId, String doctorName, String notes) {
		String sql = "INSERT INTO familiar_antecedents (patient_id, antecedent, doctor_id, doctor_name, notes) VALUES (?, ?, ?, ?, ?)";
		db.executeUpdate(sql, patientId, antecedent, doctorId, doctorName,
				notes);
	}

	public void addPersonalProblem(int patientId, String problem, int doctorId,
			String doctorName, String notes) {
		String sql = "INSERT INTO personal_problems (patient_id, problem, doctor_id, doctor_name, notes) VALUES (?, ?, ?, ?, ?)";
		db.executeUpdate(sql, patientId, problem, doctorId, doctorName, notes);
	}

	public String getFamiliarAntecedents(int patientId) {
		String sql = "SELECT antecedent, doctor_name, created_date FROM familiar_antecedents WHERE patient_id = ? ORDER BY created_date DESC";

		List<Object[]> results = db.executeQueryArray(sql, patientId);
		StringBuilder sb = new StringBuilder();

		for (Object[] row : results) {
			String antecedent = (String) row[0];
			String doctor = (String) row[1];
			String timestamp = (String) row[2];

			String formattedDate = timestamp.substring(0, 16);

			sb.append(String.format("[%s] %s - Added by: %s\n", formattedDate,
					antecedent, doctor));
		}

		return sb.toString().isEmpty() ? "No familiar antecedents recorded."
				: sb.toString();
	}

	public String getPersonalProblems(int patientId) {
		String sql = "SELECT problem, doctor_name, created_date FROM personal_problems WHERE patient_id = ? ORDER BY created_date DESC";

		List<Object[]> results = db.executeQueryArray(sql, patientId);
		StringBuilder sb = new StringBuilder();

		for (Object[] row : results) {
			String problem = (String) row[0];
			String doctor = (String) row[1];
			String timestamp = (String) row[2];

			String formattedDate = timestamp.substring(0, 16);
			sb.append(String.format("[%s] %s - Added by: %s\n", formattedDate,
					problem, doctor));
		}

		return sb.toString().isEmpty() ? "No personal problems recorded."
				: sb.toString();
	}

	public int getDoctorIdForAppointment(int appointmentId) {
		String sql = "SELECT id_doctors FROM doctors_appointments WHERE id_appointments = ?";
		List<Object[]> results = db.executeQueryArray(sql, appointmentId);

		if (results.isEmpty() || results.get(0)[0] == null) {
			throw new RuntimeException(
					"No doctor found for appointment ID: " + appointmentId);
		}

		return ((Number) results.get(0)[0]).intValue();
	}

	public String getDoctorNameById(int doctorId) {
		String sql = "SELECT name || ' ' || surname FROM doctors WHERE id = ?";
		List<Object[]> results = db.executeQueryArray(sql, doctorId);

		if (results.isEmpty() || results.get(0)[0] == null) {
			return "Unknown Doctor";
		}

		return (String) results.get(0)[0];
	}

	public List<String> getConsultationCauseOptions() {
		String sql = "SELECT cause FROM consultation_cause_options ORDER BY cause";
		List<Object[]> results = db.executeQueryArray(sql);
		return results.stream().map(row -> (String) row[0])
				.collect(java.util.stream.Collectors.toList());
	}

	public List<String> getFamiliarAntecedentOptions() {
		String sql = "SELECT antecedent FROM familiar_antecedent_options ORDER BY antecedent";
		List<Object[]> results = db.executeQueryArray(sql);
		return results.stream().map(row -> (String) row[0])
				.collect(java.util.stream.Collectors.toList());
	}

	public List<String> getPersonalProblemOptions() {
		String sql = "SELECT problem FROM personal_problem_options ORDER BY problem";
		List<Object[]> results = db.executeQueryArray(sql);
		return results.stream().map(row -> (String) row[0])
				.collect(java.util.stream.Collectors.toList());
	}

	public void addConsultationCause(int appointmentId, String cause,
			int doctorId, String doctorName, String notes) {
		String sql = "INSERT INTO consultation_causes (appointment_id, cause, doctor_id, doctor_name, notes) VALUES (?, ?, ?, ?, ?)";
		db.executeUpdate(sql, appointmentId, cause, doctorId, doctorName,
				notes);
	}

	public List<Object[]> getFamiliarAntecedentsAsList(int patientId) {
		String sql = "SELECT created_date, antecedent, doctor_name, notes FROM familiar_antecedents WHERE patient_id = ? ORDER BY created_date DESC";

		return db.executeQueryArray(sql, patientId);
	}

	public List<Object[]> getPersonalProblemsAsList(int patientId) {
		String sql = "SELECT created_date, problem, doctor_name, notes FROM personal_problems WHERE patient_id = ? ORDER BY created_date DESC";

		return db.executeQueryArray(sql, patientId);
	}

	public List<Object[]> getAppointmentsWithCauses(int patientId) {
		String sql = "SELECT a.date, a.start_time, a.end_time, a.office, "

				+ "d.name || ' ' || d.surname as doctor_name, "
				+ "COALESCE(cc.cause, 'No cause recorded') as consultation_cause, "
				+ "cc.created_date as cause_created_date, "
				+ "a.attended, a.check_in_time, a.check_out_time, "
				+ "cc.notes as cause_notes " + "FROM appointments a "
				+ "JOIN doctors_appointments da ON a.id = da.id_appointments "
				+ "JOIN doctors d ON da.id_doctors = d.id "
				+ "LEFT JOIN consultation_causes cc ON a.id = cc.appointment_id "
				+ "WHERE a.patient_id = ? "
				+ "ORDER BY a.date DESC, a.start_time DESC";

		return db.executeQueryArray(sql, patientId);
	}

	private List<Object[]> tempPersonalProblems = new ArrayList<>();
	private List<Object[]> tempFamiliarAntecedents = new ArrayList<>();
	private List<Object[]> tempConsultationCauses = new ArrayList<>();

	public void addTempPersonalProblem(String problem, String notes) {
		tempPersonalProblems.add(new Object[] { problem, notes });
	}

	public void addTempFamiliarAntecedent(String antecedent, String notes) {
		tempFamiliarAntecedents.add(new Object[] { antecedent, notes });
	}

	public void addTempConsultationCause(String cause, String notes) {
		tempConsultationCauses.add(new Object[] { cause, notes });
	}

	public List<Object[]> getTempPersonalProblems() {
		return new ArrayList<>(tempPersonalProblems);
	}

	public List<Object[]> getTempFamiliarAntecedents() {
		return new ArrayList<>(tempFamiliarAntecedents);
	}

	public List<Object[]> getTempConsultationCauses() {
		return new ArrayList<>(tempConsultationCauses);
	}

	public void removeTempPersonalProblem(int index) {
		if (index >= 0 && index < tempPersonalProblems.size()) {
			tempPersonalProblems.remove(index);
		}
	}

	public void removeTempFamiliarAntecedent(int index) {
		if (index >= 0 && index < tempFamiliarAntecedents.size()) {
			tempFamiliarAntecedents.remove(index);
		}
	}

	public void removeTempConsultationCause(int index) {
		if (index >= 0 && index < tempConsultationCauses.size()) {
			tempConsultationCauses.remove(index);
		}
	}

	public void saveToMedicalRecord(int patientId, int appointmentId,
			int doctorId, String doctorName) {

		for (Object[] problem : tempPersonalProblems) {
			String problemText = (String) problem[0];
			String notes = (String) problem[1];
			String sql = "INSERT INTO personal_problems (patient_id, problem, notes, doctor_id, doctor_name) VALUES (?, ?, ?, ?, ?)";
			db.executeUpdate(sql, patientId, problemText, notes, doctorId,
					doctorName);
		}

		for (Object[] antecedent : tempFamiliarAntecedents) {
			String antecedentText = (String) antecedent[0];
			String notes = (String) antecedent[1];
			String sql = "INSERT INTO familiar_antecedents (patient_id, antecedent, notes, doctor_id, doctor_name) VALUES (?, ?, ?, ?, ?)";
			db.executeUpdate(sql, patientId, antecedentText, notes, doctorId,
					doctorName);
		}

		for (Object[] cause : tempConsultationCauses) {
			String causeText = (String) cause[0];
			String notes = (String) cause[1];
			String sql = "INSERT INTO consultation_causes (appointment_id, cause, notes, doctor_id, doctor_name) VALUES (?, ?, ?, ?, ?)";
			db.executeUpdate(sql, appointmentId, causeText, notes, doctorId,
					doctorName);
		}

		tempPersonalProblems.clear();
		tempFamiliarAntecedents.clear();
		tempConsultationCauses.clear();
	}

	public boolean hasUnsavedChanges() {
		return !tempPersonalProblems.isEmpty()
				|| !tempFamiliarAntecedents.isEmpty()
				|| !tempConsultationCauses.isEmpty();
	}

	public List<Object[]> getVaccinesByPatient(int patientId) {
		String sql = "SELECT scheduled_date, vaccine_name, dose_number, status, "
				+ "administered_date, administered_by_doctor_name, needs_booster, notes "
				+ "FROM vaccines WHERE patient_id = ? ORDER BY scheduled_date DESC";

		return db.executeQueryArray(sql, patientId);
	}

	public void addVaccine(int patientId, String vaccineName, int doseNumber,
			String scheduledDate, int needsBooster, String notes) {
		String sql = "INSERT INTO vaccines (patient_id, vaccine_name, dose_number, scheduled_date, needs_booster, notes) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		db.executeUpdate(sql, patientId, vaccineName, doseNumber, scheduledDate,
				needsBooster, notes);
	}

	public void updateVaccineStatus(int vaccineId, String status,
			String administeredDate, int doctorId, String doctorName) {
		String sql = "UPDATE vaccines SET status = ?, administered_date = ?, "
				+ "administered_by_doctor_id = ?, administered_by_doctor_name = ? "
				+ "WHERE id = ?";
		db.executeUpdate(sql, status, administeredDate, doctorId, doctorName,
				vaccineId);
	}

	public void saveIllnessToMedicalRecord(int patientId, int appointmentId,
			int doctorId, String doctorName, ICDDTO illness) {
		try {
			String sql = "INSERT INTO medical_records (patient_id, appointment_id, doctor_id, "
					+ "icd10_code, diagnosis_date, notes) VALUES (?, ?, ?, ?, ?, ?)";

			db.executeUpdate(sql, patientId, appointmentId, doctorId,
					illness.getCode(), // Código ICD-10
					LocalDate.now().toString(), // Fecha de diagnóstico
					"Diagnosis: " + illness.getDescription() // Notas
																// adicionales
			);

		} catch (Exception e) {
			throw new ApplicationException(
					"Error saving illness to medical record: "
							+ e.getMessage());
		}
	}

	public List<DoctorDTO> getAllDoctorsLogin() {
		String sql = "SELECT id, dni, name, surname, email, specialization, created_at "
				+ "FROM doctors ORDER BY name, surname";
		return db.executeQueryPojo(DoctorDTO.class, sql);
	}

	public DoctorDTO getDoctorById(int doctorId) {
		String sql = "SELECT id, dni, name, surname, email, specialization, created_at "
				+ "FROM doctors WHERE id = ?";
		List<DoctorDTO> doctors = db.executeQueryPojo(DoctorDTO.class, sql,
				doctorId);
		return doctors.isEmpty() ? null : doctors.get(0);
	}

	public boolean validateDoctorExists(int doctorId) {
		String sql = "SELECT COUNT(*) FROM doctors WHERE id = ?";
		List<Object[]> result = db.executeQueryArray(sql, doctorId);
		return !result.isEmpty() && ((Number) result.get(0)[0]).intValue() > 0;
	}

	// ==========================================================
	// 🔽 Utility: Safely get last inserted appointment ID
	// ==========================================================
	public Integer getLastInsertedAppointmentId() {
		String sql = "SELECT MAX(id) FROM appointments";
		List<Object[]> results = db.executeQueryArray(sql);

		if (results.isEmpty() || results.get(0)[0] == null) {
			return null;
		}
		return ((Number) results.get(0)[0]).intValue();
	}

	public void saveContactInfo(int patientId, int appointmentId, String phone,
			String email, String address) {
		String sql = "INSERT INTO patient_contact_info (patient_id, appointment_id, phone, email, address) "
				+ "VALUES (?, ?, ?, ?, ?) "
				+ "ON CONFLICT(patient_id, appointment_id) DO UPDATE SET "
				+ "phone = excluded.phone, email = excluded.email, address = excluded.address, "
				+ "updated_at = CURRENT_TIMESTAMP";

		db.executeUpdate(sql, patientId, appointmentId, phone, email, address);
	}

	// ==========================================================
	// 🔽 MANAGER FEATURE: Appointments by office + date range
	// (supports "ALL" to include every office)
	// ==========================================================

	public List<AppointmentDTO> getAppointmentsByOfficeAndDateRange(
			String officeName, String fromDate, String toDate) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.*, ").append(
				"p.name AS patient_name, p.surname AS patient_surname, p.dni AS patient_dni ")
				.append("FROM appointments a ")
				.append("JOIN patients p ON a.patient_id = p.id ")
				.append("WHERE a.date >= ? AND a.date <= ? ");

		java.util.List<Object> params = new java.util.ArrayList<>();
		params.add(fromDate);
		params.add(toDate);

		if (officeName != null && !"ALL".equalsIgnoreCase(officeName.trim())) {
			sql.append("AND a.office = ? ");
			params.add(officeName);
		}

		sql.append("ORDER BY a.date ASC, a.start_time ASC");

		return db.executeQueryPojo(AppointmentDTO.class, sql.toString(),
				params.toArray());
	}

	// ==========================================================
	// 🔽 CLINICAL ORDERS METHODS
	// ==========================================================

	public void updateClinicalOrderResponse(Integer orderId, String response,
			String fileName, byte[] fileData) {
		String sql;
		if (fileName != null && !fileName.isEmpty()) {
			sql = "UPDATE clinical_orders SET response_text = ?, response_date = CURRENT_TIMESTAMP, status = 'responded', file_name = ?, file_data = ? WHERE id = ?";
			db.executeUpdate(sql, response, fileName, fileData, orderId);
		} else {
			sql = "UPDATE clinical_orders SET response_text = ?, response_date = CURRENT_TIMESTAMP, status = 'responded' WHERE id = ?";
			db.executeUpdate(sql, response, orderId);
		}
	}

	public List<DoctorDTO> getAllDoctorsForClinicalOrders(
			Integer excludeDoctorId) {
		String sql = "SELECT id, dni, name, surname, email, specialization, created_at "
				+ "FROM doctors WHERE id != ? ORDER BY name, surname";
		return db.executeQueryPojo(DoctorDTO.class, sql, excludeDoctorId);
	}

	public byte[] getClinicalOrderFileData(Integer orderId) {
		String sql = "SELECT file_data FROM clinical_orders WHERE id = ?";
		List<Object[]> results = db.executeQueryArray(sql, orderId);

		if (results.isEmpty() || results.get(0)[0] == null) {
			return null;
		}

		return (byte[]) results.get(0)[0];
	}

	public void saveClinicalOrder(Integer patientId, Integer appointmentId,
			Integer requestingDoctorId, Integer assignedDoctorId,
			String concept, String description, String fileName,
			byte[] fileData) {

		String orderSql = "INSERT INTO clinical_orders (patient_id, appointment_id, requesting_doctor_id, "
				+ "assigned_doctor_id, concept, initial_description, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, 'open')";

		db.executeUpdate(orderSql, patientId, appointmentId, requestingDoctorId,
				assignedDoctorId, concept, description);

		Integer orderId = getLastInsertedClinicalOrderId();

		if (orderId != null) {
			String messageSql = "INSERT INTO clinical_order_messages (clinical_order_id, doctor_id, message_text, file_name, file_data) "
					+ "VALUES (?, ?, ?, ?, ?)";
			db.executeUpdate(messageSql, orderId, requestingDoctorId,
					description, fileName, fileData);
		}
	}

	private Integer getLastInsertedClinicalOrderId() {
		String sql = "SELECT MAX(id) FROM clinical_orders";
		List<Object[]> results = db.executeQueryArray(sql);
		return results.isEmpty() || results.get(0)[0] == null ? null
				: ((Number) results.get(0)[0]).intValue();
	}

	public List<ClinicalOrderDTO> getClinicalOrdersForDoctor(Integer doctorId) {
		String sql = "SELECT co.*, "
				+ "p.name || ' ' || p.surname as patient_name, "
				+ "rd.name || ' ' || rd.surname as requesting_doctor_name, "
				+ "ad.name || ' ' || ad.surname as assigned_doctor_name "
				+ "FROM clinical_orders co "
				+ "JOIN patients p ON co.patient_id = p.id "
				+ "JOIN doctors rd ON co.requesting_doctor_id = rd.id "
				+ "JOIN doctors ad ON co.assigned_doctor_id = ad.id "
				+ "WHERE co.requesting_doctor_id = ? OR co.assigned_doctor_id = ? "
				+ "ORDER BY co.created_at DESC";

		List<ClinicalOrderDTO> orders = new ArrayList<>();

		try {
			List<Object[]> results = db.executeQueryArray(sql, doctorId,
					doctorId);

			for (Object[] row : results) {
				ClinicalOrderDTO order = mapClinicalOrderFromResult(row);

				order.setMessages(getClinicalOrderMessages(order.getId()));

				orders.add(order);
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"Error getting clinical orders for doctor: "
							+ e.getMessage(),
					e);
		}

		return orders;
	}

	public List<ClinicalOrderMessageDTO> getClinicalOrderMessages(
			Integer clinicalOrderId) {
		String sql = "SELECT com.*, d.name || ' ' || d.surname as doctor_name "
				+ "FROM clinical_order_messages com "
				+ "JOIN doctors d ON com.doctor_id = d.id "
				+ "WHERE com.clinical_order_id = ? "
				+ "ORDER BY com.created_at ASC";

		List<ClinicalOrderMessageDTO> messages = new ArrayList<>();

		try {
			List<Object[]> results = db.executeQueryArray(sql, clinicalOrderId);

			for (Object[] row : results) {
				ClinicalOrderMessageDTO message = new ClinicalOrderMessageDTO();
				message.setId(((Number) row[0]).intValue());
				message.setClinicalOrderId(((Number) row[1]).intValue());
				message.setDoctorId(((Number) row[2]).intValue());
				message.setMessageText((String) row[3]);
				message.setFileName((String) row[4]);

				if (row[6] != null) {
					message.setCreatedAt(java.sql.Timestamp
							.valueOf(row[6].toString()).toLocalDateTime());
				}

				message.setDoctorName((String) row[7]);

				messages.add(message);
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"Error getting clinical order messages: " + e.getMessage(),
					e);
		}

		return messages;
	}

	public void addClinicalOrderMessage(Integer clinicalOrderId,
			Integer doctorId, String messageText, String fileName,
			byte[] fileData) {
		String sql = "INSERT INTO clinical_order_messages (clinical_order_id, doctor_id, message_text, file_name, file_data) "
				+ "VALUES (?, ?, ?, ?, ?)";
		db.executeUpdate(sql, clinicalOrderId, doctorId, messageText, fileName,
				fileData);
	}

	public void closeClinicalOrder(Integer clinicalOrderId) {
		String sql = "UPDATE clinical_orders SET status = 'closed', closed_at = CURRENT_TIMESTAMP WHERE id = ?";
		db.executeUpdate(sql, clinicalOrderId);
	}

	public ClinicalOrderDTO getClinicalOrderById(Integer orderId) {
		String sql = "SELECT co.*, "
				+ "p.name || ' ' || p.surname as patient_name, "
				+ "rd.name || ' ' || rd.surname as requesting_doctor_name, "
				+ "ad.name || ' ' || ad.surname as assigned_doctor_name "
				+ "FROM clinical_orders co "
				+ "JOIN patients p ON co.patient_id = p.id "
				+ "JOIN doctors rd ON co.requesting_doctor_id = rd.id "
				+ "JOIN doctors ad ON co.assigned_doctor_id = ad.id "
				+ "WHERE co.id = ?";

		List<Object[]> results = db.executeQueryArray(sql, orderId);

		if (results.isEmpty()) {
			return null;
		}

		ClinicalOrderDTO order = mapClinicalOrderFromResult(results.get(0));
		order.setMessages(getClinicalOrderMessages(orderId));

		return order;
	}

	private ClinicalOrderDTO mapClinicalOrderFromResult(Object[] row) {
		ClinicalOrderDTO order = new ClinicalOrderDTO();
		order.setId(((Number) row[0]).intValue());
		order.setPatientId(((Number) row[1]).intValue());
		order.setAppointmentId(((Number) row[2]).intValue());
		order.setRequestingDoctorId(((Number) row[3]).intValue());
		order.setAssignedDoctorId(((Number) row[4]).intValue());
		order.setConcept((String) row[5]);
		order.setInitialDescription((String) row[6]);
		order.setStatus((String) row[7]);

		if (row[8] != null) {
			order.setCreatedAt(java.sql.Timestamp.valueOf(row[8].toString())
					.toLocalDateTime());
		}

		if (row[9] != null) {
			order.setClosedAt(java.sql.Timestamp.valueOf(row[9].toString())
					.toLocalDateTime());
		}

		order.setPatientName((String) row[10]);
		order.setRequestingDoctorName((String) row[11]);
		order.setAssignedDoctorName((String) row[12]);

		return order;
	}

	public Optional<List<PatientDTO>> getPossiblePatients(String string) {
		String searchTerm = "%" + string + "%";
		List<PatientDTO> patients = new ArrayList<>();

		try {
			String sql = "SELECT id, name, surname, dni, phone, email, address, date_of_birth, gender "
					+ "FROM patients "
					+ "WHERE name LIKE ? OR surname LIKE ? OR dni LIKE ? "
					+ "ORDER BY name, surname";

			List<Object[]> results = db.executeQueryArray(sql, searchTerm,
					searchTerm, searchTerm);

			for (Object[] row : results) {
				PatientDTO patient = new PatientDTO();
				patient.setId((Integer) row[0]);
				patient.setName((String) row[1]);
				patient.setSurname((String) row[2]);
				patient.setDni((String) row[3]);
				patient.setPhone((String) row[4]);
				patient.setEmail((String) row[5]);
				patient.setAddress((String) row[6]);
				patient.setDate_of_birth((String) row[7]);
				patient.setGender((String) row[8]);

				patients.add(patient);
			}

			return Optional.of(patients);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("No patients found ");
			return Optional.of(new ArrayList<>());
		}
	}

	public Optional<List<DoctorDTO>> getPossibleDoctors(String string) {
		String searchTerm = "%" + string + "%";
		List<DoctorDTO> doctors = new ArrayList<>();

		try {
			String sql = "SELECT id, dni, name, surname, email, specialization "
					+ "FROM doctors "
					+ "WHERE name LIKE ? OR surname LIKE ? OR dni LIKE ? "
					+ "ORDER BY name, surname";

			List<Object[]> results = db.executeQueryArray(sql, searchTerm,
					searchTerm, searchTerm);

			for (Object[] row : results) {
				DoctorDTO doctor = new DoctorDTO();
				doctor.setId((Integer) row[0]);
				doctor.setDni((String) row[1]);
				doctor.setName((String) row[2]);
				doctor.setSurname((String) row[3]);
				doctor.setEmail((String) row[4]);
				doctor.setSpecialization((String) row[5]);

				doctors.add(doctor);

			}

			return Optional.of(doctors);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("No doctors found ");
			return Optional.of(new ArrayList<>());
		}

	}

	public List<VaccineDTO> getAllVaccines() {
		String sql = "SELECT vaccine_name, recommended_doses FROM vaccine_model ORDER BY vaccine_name";

		try {
			List<Object[]> results = db.executeQueryArray(sql);
			List<VaccineDTO> vaccines = new ArrayList<>();

			for (Object[] row : results) {
				VaccineDTO vaccine = new VaccineDTO();
				vaccine.setName((String) row[0]);
				vaccine.setRecommendedDoses(((Number) row[1]).intValue());
				vaccines.add(vaccine);
			}

			return vaccines;

		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException(
					"Error loading vaccines from database: " + e.getMessage());
		}
	}

	/**
	 * Busca vacunas por nombre
	 */
	public List<VaccineDTO> searchVaccines(String searchTerm) {
		String sql = "SELECT vaccine_name, recommended_doses FROM vaccine_model "
				+ "WHERE LOWER(vaccine_name) LIKE LOWER(?) ORDER BY vaccine_name";
		String likeTerm = "%" + searchTerm + "%";

		try {
			List<Object[]> results = db.executeQueryArray(sql, likeTerm);
			List<VaccineDTO> vaccines = new ArrayList<>();

			for (Object[] row : results) {
				VaccineDTO vaccine = new VaccineDTO();
				vaccine.setName((String) row[0]);
				vaccine.setRecommendedDoses(((Number) row[1]).intValue());
				vaccines.add(vaccine);
			}

			return vaccines;

		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException(
					"Error searching vaccines: " + e.getMessage());
		}
	}

	// ==========================================================
	// 🔽 VACCINE APPOINTMENT METHODS
	// ==========================================================

	/**
	 * Saves a vaccine appointment to the database
	 */
	/**
	 * Saves a vaccine appointment to the database
	 */
	public Integer saveVaccineAppointment(int doctorId, int patientId,
			String date, String hour) {

		System.out.println("=== DEBUG saveVaccineAppointment ===");
		System.out.println("Doctor ID: " + doctorId);
		System.out.println("Patient ID: " + patientId);
		System.out.println("Date: " + date);
		System.out.println("Hour: " + hour);

		String sql = "INSERT INTO vaccine_appointment (doctor_id, patient_id, date, hour) "
				+ "VALUES (?, ?, ?, ?)";

		System.out.println("SQL: " + sql);

		try {
			// Primero verifiquemos el estado actual de la tabla
			System.out.println(
					"=== Checking current state of vaccine_appointment table ===");
			String countSql = "SELECT COUNT(*) FROM vaccine_appointment";
			List<Object[]> countResults = db.executeQueryArray(countSql);
			if (!countResults.isEmpty()) {
				int currentCount = ((Number) countResults.get(0)[0]).intValue();
				System.out.println(
						"Current number of vaccine appointments in table: "
								+ currentCount);
			}

			// Ejecutar la inserción
			db.executeUpdate(sql, doctorId, patientId, date, hour);

			System.out.println("✅ Vaccine appointment saved successfully");
		} catch (Exception e) {
			System.out.println(
					"❌ Error saving vaccine appointment: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

		// MÉTODO 1: Intentar obtener el último ID insertado
		System.out.println("=== Attempting to get last inserted ID ===");

		// Opción 1: LAST_INSERT_ROWID() (para SQLite)
		String selectLastIdSql = "SELECT LAST_INSERT_ROWID()";
		try {
			List<Object[]> results = db.executeQueryArray(selectLastIdSql);
			System.out.println("Query for last ID returned: " + results.size()
					+ " results");

			if (!results.isEmpty() && results.get(0)[0] != null) {
				Integer lastId = ((Number) results.get(0)[0]).intValue();
				System.out
						.println("✅ Last inserted ID from LAST_INSERT_ROWID(): "
								+ lastId);

				if (lastId > 0) {
					return lastId;
				} else {
					System.out.println(
							"⚠️ LAST_INSERT_ROWID() returned 0 or negative value");
				}
			} else {
				System.out
						.println("⚠️ No ID returned from LAST_INSERT_ROWID()");
			}
		} catch (Exception e) {
			System.out.println(
					"❌ Error with LAST_INSERT_ROWID(): " + e.getMessage());
		}

		// Opción 2: Obtener el máximo ID de la tabla (más confiable)
		System.out.println("=== Trying alternative method: MAX(id) ===");
		String maxIdSql = "SELECT MAX(id) FROM vaccine_appointment";
		try {
			List<Object[]> maxResults = db.executeQueryArray(maxIdSql);
			System.out.println("MAX(id) query returned: " + maxResults.size()
					+ " results");

			if (!maxResults.isEmpty() && maxResults.get(0)[0] != null) {
				Integer maxId = ((Number) maxResults.get(0)[0]).intValue();
				System.out
						.println("✅ Max ID from vaccine_appointment: " + maxId);

				if (maxId > 0) {
					return maxId;
				} else {
					System.out
							.println("⚠️ MAX(id) returned 0 or negative value");
				}
			} else {
				System.out.println("⚠️ No results from MAX(id) query");
			}
		} catch (Exception e) {
			System.out.println("❌ Error with MAX(id) query: " + e.getMessage());
			e.printStackTrace();
		}

		// Opción 3: Obtener el ID específico basado en los datos insertados
		System.out.println("=== Trying method 3: Get ID by inserted data ===");
		String findIdSql = "SELECT id FROM vaccine_appointment WHERE "
				+ "doctor_id = ? AND patient_id = ? AND date = ? AND hour = ? "
				+ "ORDER BY id DESC LIMIT 1";
		try {
			List<Object[]> findResults = db.executeQueryArray(findIdSql,
					doctorId, patientId, date, hour);
			System.out.println("Find by data query returned: "
					+ findResults.size() + " results");

			if (!findResults.isEmpty() && findResults.get(0)[0] != null) {
				Integer foundId = ((Number) findResults.get(0)[0]).intValue();
				System.out.println("✅ Found ID by data: " + foundId);
				return foundId;
			} else {
				System.out.println(
						"⚠️ Could not find appointment by inserted data");

				// Ver todos los registros para debugging
				String allSql = "SELECT id, doctor_id, patient_id, date, hour FROM vaccine_appointment ORDER BY id DESC";
				List<Object[]> allResults = db.executeQueryArray(allSql);
				System.out
						.println("=== ALL RECORDS IN vaccine_appointment ===");
				for (Object[] row : allResults) {
					System.out.println("ID: " + row[0] + ", Doctor: " + row[1]
							+ ", Patient: " + row[2] + ", Date: " + row[3]
							+ ", Hour: " + row[4]);
				}
			}
		} catch (Exception e) {
			System.out.println("❌ Error finding ID by data: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("❌ Could not retrieve vaccine appointment ID");
		return null;
	}

	/**
	 * Saves a vaccine to a vaccine appointment
	 */
	public void saveVaccineToAppointment(int vaccineAppointmentId,
			String vaccineName, String doseType) {

		System.out.println("=== DEBUG saveVaccineToAppointment ===");
		System.out.println("Appointment ID: " + vaccineAppointmentId);
		System.out.println("Vaccine Name: " + vaccineName);
		System.out.println("Dose Type: " + doseType);

		String sql = "INSERT INTO vaccine_appointment_vaccine "
				+ "(vaccine_appointment_id, vaccine_name, dose_type) "
				+ "VALUES (?, ?, ?)";

		System.out.println("SQL: " + sql);

		try {
			db.executeUpdate(sql, vaccineAppointmentId, vaccineName, doseType);
			System.out.println("✅ Vaccine saved to appointment successfully");
		} catch (Exception e) {
			System.out.println(
					"❌ Error saving vaccine to appointment: " + e.getMessage());
			e.printStackTrace();
			throw new ApplicationException(
					"Error saving vaccine to appointment: " + e.getMessage());
		}
	}

	/**
	 * Checks if a doctor has a vaccine appointment at a specific date and time
	 */
	public boolean isDoctorBookedForVaccine(int doctorId, String date,
			String hour) {
		System.out.println("=== DEBUG isDoctorBookedForVaccine ===");
		System.out.println("Checking if doctor " + doctorId + " is booked on "
				+ date + " at " + hour);

		String sql = "SELECT COUNT(*) FROM vaccine_appointment "
				+ "WHERE doctor_id = ? AND date = ? AND hour = ?";

		System.out.println("SQL: " + sql);

		List<Object[]> result;
		try {
			result = db.executeQueryArray(sql, doctorId, date, hour);
			System.out.println("Query returned " + result.size() + " results");
		} catch (Exception e) {
			System.out.println(
					"❌ Error checking doctor availability: " + e.getMessage());
			e.printStackTrace();
			return false; // Assume not booked if error
		}

		if (result.isEmpty() || result.get(0)[0] == null) {
			System.out.println("⚠️ No results or null result");
			return false;
		}

		int count = ((Number) result.get(0)[0]).intValue();
		System.out.println("Count of appointments found: " + count);

		boolean isBooked = count > 0;
		System.out.println("Doctor booked? " + isBooked);
		return isBooked;
	}

	/**
	 * Checks if a patient has a vaccine appointment at a specific date and time
	 */
	public boolean isPatientBookedForVaccine(int patientId, String date,
			String hour) {
		System.out.println("=== DEBUG isPatientBookedForVaccine ===");
		System.out.println("Checking if patient " + patientId + " is booked on "
				+ date + " at " + hour);

		String sql = "SELECT COUNT(*) FROM vaccine_appointment "
				+ "WHERE patient_id = ? AND date = ? AND hour = ?";

		System.out.println("SQL: " + sql);

		List<Object[]> result;
		try {
			result = db.executeQueryArray(sql, patientId, date, hour);
			System.out.println("Query returned " + result.size() + " results");
		} catch (Exception e) {
			System.out.println(
					"❌ Error checking patient availability: " + e.getMessage());
			e.printStackTrace();
			return false; // Assume not booked if error
		}

		if (result.isEmpty() || result.get(0)[0] == null) {
			System.out.println("⚠️ No results or null result");
			return false;
		}

		int count = ((Number) result.get(0)[0]).intValue();
		System.out.println("Count of appointments found: " + count);

		boolean isBooked = count > 0;
		System.out.println("Patient booked? " + isBooked);
		return isBooked;
	}

	// Additional utility methods with debug prints:

	/**
	 * Gets all vaccine appointments for a patient
	 */
	public List<Object[]> getVaccineAppointmentsByPatient(int patientId) {
		System.out.println("=== DEBUG getVaccineAppointmentsByPatient ===");
		System.out.println("Patient ID: " + patientId);

		String sql = "SELECT va.*, "
				+ "d.name || ' ' || d.surname as doctor_name, "
				+ "p.name || ' ' || p.surname as patient_name "
				+ "FROM vaccine_appointment va "
				+ "JOIN doctors d ON va.doctor_id = d.id "
				+ "JOIN patients p ON va.patient_id = p.id "
				+ "WHERE va.patient_id = ? "
				+ "ORDER BY va.date DESC, va.hour DESC";

		System.out.println("SQL: " + sql);

		try {
			List<Object[]> results = db.executeQueryArray(sql, patientId);
			System.out.println("Found " + results.size()
					+ " vaccine appointments for patient");
			return results;
		} catch (Exception e) {
			System.out.println(
					"❌ Error getting vaccine appointments: " + e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	/**
	 * Gets all vaccines for a specific vaccine appointment
	 */
	public List<Object[]> getVaccinesForAppointment(int vaccineAppointmentId) {
		System.out.println("=== DEBUG getVaccinesForAppointment ===");
		System.out.println("Appointment ID: " + vaccineAppointmentId);

		String sql = "SELECT vaccine_name, dose_type "
				+ "FROM vaccine_appointment_vaccine "
				+ "WHERE vaccine_appointment_id = ? " + "ORDER BY vaccine_name";

		System.out.println("SQL: " + sql);

		try {
			List<Object[]> results = db.executeQueryArray(sql,
					vaccineAppointmentId);
			System.out.println(
					"Found " + results.size() + " vaccines for appointment");

			for (int i = 0; i < results.size(); i++) {
				Object[] row = results.get(i);
				System.out.println("  Vaccine " + (i + 1) + ": " + row[0] + " ("
						+ row[1] + ")");
			}

			return results;
		} catch (Exception e) {
			System.out.println("❌ Error getting vaccines for appointment: "
					+ e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	/**
	 * Gets all vaccine appointments for a doctor
	 */
	public List<Object[]> getVaccineAppointmentsByDoctor(int doctorId) {
		System.out.println("=== DEBUG getVaccineAppointmentsByDoctor ===");
		System.out.println("Doctor ID: " + doctorId);

		String sql = "SELECT va.*, "
				+ "d.name || ' ' || d.surname as doctor_name, "
				+ "p.name || ' ' || p.surname as patient_name "
				+ "FROM vaccine_appointment va "
				+ "JOIN doctors d ON va.doctor_id = d.id "
				+ "JOIN patients p ON va.patient_id = p.id "
				+ "WHERE va.doctor_id = ? "
				+ "ORDER BY va.date DESC, va.hour DESC";

		System.out.println("SQL: " + sql);

		try {
			List<Object[]> results = db.executeQueryArray(sql, doctorId);
			System.out.println("Found " + results.size()
					+ " vaccine appointments for doctor");
			return results;
		} catch (Exception e) {
			System.out
					.println("❌ Error getting vaccine appointments for doctor: "
							+ e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	/**
	 * Gets vaccine appointment by ID
	 */
	public List<Object[]> getVaccineAppointmentById(int appointmentId) {
		System.out.println("=== DEBUG getVaccineAppointmentById ===");
		System.out.println("Appointment ID: " + appointmentId);

		String sql = "SELECT va.*, "
				+ "d.name || ' ' || d.surname as doctor_name, "
				+ "p.name || ' ' || p.surname as patient_name "
				+ "FROM vaccine_appointment va "
				+ "JOIN doctors d ON va.doctor_id = d.id "
				+ "JOIN patients p ON va.patient_id = p.id "
				+ "WHERE va.id = ?";

		System.out.println("SQL: " + sql);

		try {
			List<Object[]> results = db.executeQueryArray(sql, appointmentId);
			System.out.println("Found " + results.size()
					+ " appointment(s) with ID " + appointmentId);

			if (!results.isEmpty()) {
				Object[] row = results.get(0);
				System.out.println("  Doctor ID: " + row[1]);
				System.out.println("  Patient ID: " + row[2]);
				System.out.println("  Date: " + row[3]);
				System.out.println("  Hour: " + row[4]);
				System.out.println("  Doctor Name: " + row[6]);
				System.out.println("  Patient Name: " + row[7]);
			}

			return results;
		} catch (Exception e) {
			System.out.println("❌ Error getting vaccine appointment by ID: "
					+ e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	/**
	 * DEBUG: Check if vaccine_appointment table exists
	 */
	public boolean checkVaccineAppointmentTableExists() {
		System.out.println("=== DEBUG checkVaccineAppointmentTableExists ===");

		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='vaccine_appointment'";

		try {
			List<Object[]> results = db.executeQueryArray(sql);
			boolean exists = !results.isEmpty();
			System.out.println("vaccine_appointment table exists? " + exists);
			return exists;
		} catch (Exception e) {
			System.out.println(
					"❌ Error checking table existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * DEBUG: Check if vaccine_appointment_vaccine table exists
	 */
	public boolean checkVaccineAppointmentVaccineTableExists() {
		System.out.println(
				"=== DEBUG checkVaccineAppointmentVaccineTableExists ===");

		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='vaccine_appointment_vaccine'";

		try {
			List<Object[]> results = db.executeQueryArray(sql);
			boolean exists = !results.isEmpty();
			System.out.println(
					"vaccine_appointment_vaccine table exists? " + exists);
			return exists;
		} catch (Exception e) {
			System.out.println(
					"❌ Error checking table existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * DEBUG: Count rows in vaccine_appointment table
	 */
	public int countVaccineAppointments() {
		System.out.println("=== DEBUG countVaccineAppointments ===");

		String sql = "SELECT COUNT(*) FROM vaccine_appointment";

		try {
			List<Object[]> results = db.executeQueryArray(sql);
			if (!results.isEmpty() && results.get(0)[0] != null) {
				int count = ((Number) results.get(0)[0]).intValue();
				System.out.println(
						"Total vaccine appointments in database: " + count);
				return count;
			}
		} catch (Exception e) {
			System.out.println(
					"❌ Error counting vaccine appointments: " + e.getMessage());
		}

		return 0;
	}

	/**
	 * DEBUG: Show all vaccine appointments
	 */
	public void debugShowAllVaccineAppointments() {
		System.out.println("=== DEBUG debugShowAllVaccineAppointments ===");

		String sql = "SELECT va.id, va.doctor_id, va.patient_id, va.date, va.hour, "
				+ "d.name || ' ' || d.surname as doctor_name, "
				+ "p.name || ' ' || p.surname as patient_name "
				+ "FROM vaccine_appointment va "
				+ "LEFT JOIN doctors d ON va.doctor_id = d.id "
				+ "LEFT JOIN patients p ON va.patient_id = p.id "
				+ "ORDER BY va.date, va.hour";

		try {
			List<Object[]> results = db.executeQueryArray(sql);
			System.out.println("Total vaccine appointments: " + results.size());

			for (int i = 0; i < results.size(); i++) {
				Object[] row = results.get(i);
				System.out.println("  Appointment " + (i + 1) + ":");
				System.out.println("    ID: " + row[0]);
				System.out.println(
						"    Doctor ID: " + row[1] + " (" + row[5] + ")");
				System.out.println(
						"    Patient ID: " + row[2] + " (" + row[6] + ")");
				System.out.println("    Date: " + row[3]);
				System.out.println("    Hour: " + row[4]);
			}
		} catch (Exception e) {
			System.out.println(
					"❌ Error showing vaccine appointments: " + e.getMessage());
		}
	}

	// ==========================================================
	// 🔽 SCHEDULED VACCINE APPOINTMENTS METHODS
	// ==========================================================

	/**
	 * Obtiene todas las citas de vacunación con detalles del doctor y paciente
	 */
	/**
	 * Obtiene todas las citas de vacunación con detalles del doctor y paciente
	 * (método manual)
	 */
	public List<VaccineAppointmentDTO> getAllVaccineAppointmentsWithDetails() {
		System.out.println(
				"=== DEBUG getAllVaccineAppointmentsWithDetails (MANUAL) ===");

		List<VaccineAppointmentDTO> appointments = new ArrayList<>();

		String sql = "SELECT va.id, va.doctor_id, va.patient_id, va.date, va.hour, "
				+ "d.name, d.surname, d.specialization, "
				+ "p.name, p.surname, p.dni, "
				+ "(SELECT GROUP_CONCAT(vav.vaccine_name || ' (' || vav.dose_type || ')', ', ') "
				+ " FROM vaccine_appointment_vaccine vav "
				+ " WHERE vav.vaccine_appointment_id = va.id) as vaccine_list "
				+ "FROM vaccine_appointment va "
				+ "LEFT JOIN doctors d ON va.doctor_id = d.id "
				+ "LEFT JOIN patients p ON va.patient_id = p.id "
				+ "ORDER BY va.date DESC, va.hour DESC";

		try {
			List<Object[]> results = db.executeQueryArray(sql);
			System.out.println("Query returned: " + results.size() + " rows");

			for (Object[] row : results) {
				VaccineAppointmentDTO dto = new VaccineAppointmentDTO();

				// ID y campos básicos
				dto.setId(row[0] != null ? ((Number) row[0]).intValue() : null);
				dto.setDoctorId(
						row[1] != null ? ((Number) row[1]).intValue() : null);
				dto.setPatientId(
						row[2] != null ? ((Number) row[2]).intValue() : null);
				dto.setDate(row[3] != null ? row[3].toString() : null);
				dto.setHour(row[4] != null ? row[4].toString() : null);

				// Doctor
				String doctorFirstName = row[5] != null ? row[5].toString()
						: "";
				String doctorLastName = row[6] != null ? row[6].toString() : "";
				dto.setDoctorName(doctorFirstName + " " + doctorLastName);
				dto.setDoctorSpecialization(row[7] != null ? row[7].toString()
						: "Unknown Specialization");

				// Patient
				String patientFirstName = row[8] != null ? row[8].toString()
						: "";
				String patientLastName = row[9] != null ? row[9].toString()
						: "";
				dto.setPatientName(patientFirstName + " " + patientLastName);
				dto.setPatientDni(row[10] != null ? row[10].toString() : "N/A");

				// Vaccines
				dto.setVaccines(row[11] != null ? row[11].toString()
						: "No vaccines specified");

				appointments.add(dto);

				System.out.println("Manual DTO creado: ");
				System.out.println("  ID: " + dto.getId());
				System.out.println("  Doctor: " + dto.getDoctorName());
				System.out.println("  Patient: " + dto.getPatientName());
				System.out.println("  Vaccines: " + dto.getVaccines());
			}
		} catch (Exception e) {
			System.out.println("Error en consulta manual: " + e.getMessage());
			e.printStackTrace();
		}

		return appointments;
	}

	/**
	 * Obtiene citas de vacunación por fecha
	 */
	public List<VaccineAppointmentDTO> getVaccineAppointmentsByDate(
			LocalDate date) {
		String sql = "SELECT va.*, "
				+ "d.name || ' ' || d.surname as doctor_name, d.specialization as doctor_specialization, "
				+ "p.name || ' ' || p.surname as patient_name, p.dni as patient_dni, "
				+ "(SELECT GROUP_CONCAT(vav.vaccine_name || ' (' || vav.dose_type || ')', ', ') "
				+ " FROM vaccine_appointment_vaccine vav "
				+ " WHERE vav.vaccine_appointment_id = va.id) as vaccines "
				+ "FROM vaccine_appointment va "
				+ "JOIN doctors d ON va.doctor_id = d.id "
				+ "JOIN patients p ON va.patient_id = p.id "
				+ "WHERE va.date = ? " + "ORDER BY va.hour DESC";

		return db.executeQueryPojo(VaccineAppointmentDTO.class, sql,
				date.toString());
	}

	/**
	 * Obtiene citas de vacunación para un doctor específico en una fecha
	 */
	public List<VaccineAppointmentDTO> getVaccineAppointmentsForDoctorAndDate(
			int doctorId, LocalDate date) {
		String sql = "SELECT va.*, "
				+ "d.name || ' ' || d.surname as doctor_name, d.specialization as doctor_specialization, "
				+ "p.name || ' ' || p.surname as patient_name, p.dni as patient_dni, "
				+ "(SELECT GROUP_CONCAT(vav.vaccine_name || ' (' || vav.dose_type || ')', ', ') "
				+ " FROM vaccine_appointment_vaccine vav "
				+ " WHERE vav.vaccine_appointment_id = va.id) as vaccines "
				+ "FROM vaccine_appointment va "
				+ "JOIN doctors d ON va.doctor_id = d.id "
				+ "JOIN patients p ON va.patient_id = p.id "
				+ "WHERE va.doctor_id = ? AND va.date = ? "
				+ "ORDER BY va.hour DESC";

		return db.executeQueryPojo(VaccineAppointmentDTO.class, sql, doctorId,
				date.toString());
	}

	/**
	 * DEBUG: Verifica los datos en la tabla vaccine_appointment
	 */
	public void debugVaccineAppointmentData() {
		System.out.println(
				"=== DEBUG: VERIFICANDO DATOS EN vaccine_appointment ===");

		// Primero, mostrar datos simples sin JOIN
		String simpleSql = "SELECT id, doctor_id, patient_id, date, hour FROM vaccine_appointment";

		try {
			List<Object[]> simpleResults = db.executeQueryArray(simpleSql);
			System.out.println("Total registros en vaccine_appointment: "
					+ simpleResults.size());

			for (int i = 0; i < simpleResults.size(); i++) {
				Object[] row = simpleResults.get(i);
				System.out.println("Registro " + (i + 1) + ":");
				System.out.println("  ID: " + row[0]);
				System.out.println("  Doctor ID: " + row[1]);
				System.out.println("  Patient ID: " + row[2]);
				System.out.println("  Date: " + row[3]);
				System.out.println("  Hour: " + row[4]);
			}
		} catch (Exception e) {
			System.out.println(
					"Error al verificar datos simples: " + e.getMessage());
			e.printStackTrace();
		}

		// Ahora verificar con JOINs correctos
		System.out.println("\n=== VERIFICANDO DATOS CON JOINs ===");

		String joinSql = "SELECT va.id, va.doctor_id, va.patient_id, va.date, va.hour, "
				+ "d.name as doctor_name, d.surname as doctor_surname, "
				+ "p.name as patient_name, p.surname as patient_surname "
				+ "FROM vaccine_appointment va "
				+ "LEFT JOIN doctors d ON va.doctor_id = d.id "
				+ "LEFT JOIN patients p ON va.patient_id = p.id";

		try {
			List<Object[]> joinResults = db.executeQueryArray(joinSql);
			System.out.println("Registros con JOINs: " + joinResults.size());

			for (int i = 0; i < joinResults.size(); i++) {
				Object[] row = joinResults.get(i);
				System.out.println("Registro " + (i + 1) + ":");
				System.out.println("  ID: " + row[0]);
				System.out.println("  Doctor ID: " + row[1]);
				System.out.println("  Patient ID: " + row[2]);
				System.out.println("  Date: " + row[3]);
				System.out.println("  Hour: " + row[4]);
				System.out.println("  Doctor: " + row[5] + " " + row[6]);
				System.out.println("  Patient: " + row[7] + " " + row[8]);
			}
		} catch (Exception e) {
			System.out.println("Error al verificar JOINs: " + e.getMessage());
			e.printStackTrace();
		}

		// También verificar las tablas doctors y patients
		System.out.println("\n=== VERIFICANDO TABLAS RELACIONADAS ===");

		String checkDoctorSql = "SELECT id, name, surname FROM doctors WHERE id = ?";
		String checkPatientSql = "SELECT id, name, surname FROM patients WHERE id = ?";

		try {
			List<Object[]> appointments = db.executeQueryArray(
					"SELECT doctor_id, patient_id FROM vaccine_appointment");
			for (Object[] appt : appointments) {
				int doctorId = ((Number) appt[0]).intValue();
				int patientId = ((Number) appt[1]).intValue();

				System.out.println("Cita - Doctor ID: " + doctorId
						+ ", Patient ID: " + patientId);

				// Verificar doctor
				List<Object[]> doctorResult = db
						.executeQueryArray(checkDoctorSql, doctorId);
				if (doctorResult.isEmpty()) {
					System.out.println("  ⚠️ Doctor ID " + doctorId
							+ " NO EXISTE en tabla doctors");
				} else {
					Object[] doctorRow = doctorResult.get(0);
					System.out.println("  ✅ Doctor ID " + doctorId + " existe: "
							+ doctorRow[1] + " " + doctorRow[2]);
				}

				// Verificar paciente
				List<Object[]> patientResult = db
						.executeQueryArray(checkPatientSql, patientId);
				if (patientResult.isEmpty()) {
					System.out.println("  ⚠️ Patient ID " + patientId
							+ " NO EXISTE en tabla patients");
				} else {
					Object[] patientRow = patientResult.get(0);
					System.out
							.println("  ✅ Patient ID " + patientId + " existe: "
									+ patientRow[1] + " " + patientRow[2]);
				}
			}
		} catch (Exception e) {
			System.out
					.println("Error en verificación de IDs: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// ==========================================================
	// 🔽 VACCINE APPOINTMENT DETAIL METHODS
	// ==========================================================

	/**
	 * Gets planned vaccines for a specific vaccine appointment
	 */
	public List<VaccineDTO> getPlannedVaccinesForAppointment(
			int vaccineAppointmentId) {
		System.out.println("=== DEBUG getPlannedVaccinesForAppointment ===");
		System.out.println("Appointment ID: " + vaccineAppointmentId);

		String sql = "SELECT vav.vaccine_name, vav.dose_type, vm.recommended_doses "
				+ "FROM vaccine_appointment_vaccine vav "
				+ "LEFT JOIN vaccine_model vm ON vav.vaccine_name = vm.vaccine_name "
				+ "WHERE vav.vaccine_appointment_id = ? "
				+ "ORDER BY vav.vaccine_name";

		System.out.println("SQL: " + sql);

		try {
			List<Object[]> results = db.executeQueryArray(sql,
					vaccineAppointmentId);
			System.out.println("Found " + results.size() + " planned vaccines");

			List<VaccineDTO> vaccines = new ArrayList<>();
			for (Object[] row : results) {
				VaccineDTO vaccine = new VaccineDTO();
				vaccine.setName(row[0] != null ? row[0].toString() : "");
				vaccine.setDoseType(row[1] != null ? row[1].toString() : "");
				vaccine.setRecommendedDoses(
						row[2] != null ? ((Number) row[2]).intValue() : 0);
				vaccines.add(vaccine);

				System.out.println("  Vaccine: " + vaccine.getName() + " - "
						+ vaccine.getDoseType());
			}

			return vaccines;
		} catch (Exception e) {
			System.out.println(
					"❌ Error getting planned vaccines: " + e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	/**
	 * Gets administered vaccines for a specific vaccine appointment This would
	 * require an 'administered' column or a separate table For now, returning
	 * empty list - you need to implement based on your database structure
	 */
	public List<VaccineDTO> getAdministeredVaccinesForAppointment(
			int vaccineAppointmentId) {
		System.out
				.println("=== DEBUG getAdministeredVaccinesForAppointment ===");
		System.out.println("Appointment ID: " + vaccineAppointmentId);

		// This is a placeholder - you need to implement based on your database
		// structure
		// You might need a column like 'administered' in
		// vaccine_appointment_vaccine table
		// or a separate table for administered vaccines

		String sql = "SELECT vav.vaccine_name, vav.dose_type, vm.recommended_doses "
				+ "FROM vaccine_appointment_vaccine vav "
				+ "LEFT JOIN vaccine_model vm ON vav.vaccine_name = vm.vaccine_name "
				+ "WHERE vav.vaccine_appointment_id = ? "
				+ "AND vav.administered = 1 " + // Assuming you have this column
				"ORDER BY vav.vaccine_name";

		try {
			List<Object[]> results = db.executeQueryArray(sql,
					vaccineAppointmentId);
			List<VaccineDTO> vaccines = new ArrayList<>();

			for (Object[] row : results) {
				VaccineDTO vaccine = new VaccineDTO();
				vaccine.setName(row[0] != null ? row[0].toString() : "");
				vaccine.setDoseType(row[1] != null ? row[1].toString() : "");
				vaccine.setRecommendedDoses(
						row[2] != null ? ((Number) row[2]).intValue() : 0);
				vaccines.add(vaccine);
			}

			return vaccines;
		} catch (Exception e) {
			// If the query fails (maybe column doesn't exist), return empty
			// list
			System.out.println(
					"Note: Administered vaccines query failed - returning empty list");
			return new ArrayList<>();
		}
	}

	/**
	 * Adds a new vaccine to a vaccine appointment
	 */
	public void addVaccineToAppointment(int vaccineAppointmentId,
			String vaccineName, String doseType) {
		System.out.println("=== DEBUG addVaccineToAppointment ===");
		System.out.println("Appointment ID: " + vaccineAppointmentId);
		System.out.println("Vaccine Name: " + vaccineName);
		System.out.println("Dose Type: " + doseType);

		// Check if vaccine already exists in this appointment
		String checkSql = "SELECT COUNT(*) FROM vaccine_appointment_vaccine "
				+ "WHERE vaccine_appointment_id = ? AND vaccine_name = ? AND dose_type = ?";

		try {
			List<Object[]> checkResults = db.executeQueryArray(checkSql,
					vaccineAppointmentId, vaccineName, doseType);
			if (!checkResults.isEmpty()
					&& ((Number) checkResults.get(0)[0]).intValue() > 0) {
				System.out.println(
						"⚠️ Vaccine already exists in this appointment");
				throw new ApplicationException(
						"This vaccine with the same dose type already exists in this appointment");
			}

			// Insert the vaccine
			String insertSql = "INSERT INTO vaccine_appointment_vaccine "
					+ "(vaccine_appointment_id, vaccine_name, dose_type) "
					+ "VALUES (?, ?, ?)";

			db.executeUpdate(insertSql, vaccineAppointmentId, vaccineName,
					doseType);
			System.out.println("✅ Vaccine added to appointment successfully");

		} catch (ApplicationException e) {
			throw e; // Re-throw our custom exception
		} catch (Exception e) {
			System.out.println(
					"❌ Error adding vaccine to appointment: " + e.getMessage());
			e.printStackTrace();
			throw new ApplicationException(
					"Error adding vaccine to appointment: " + e.getMessage());
		}
	}

	/**
	 * Marks a vaccine as administered This requires modifying your database
	 * schema to add an 'administered' column or creating a separate table for
	 * administered vaccines
	 */
	public void markVaccineAsAdministered(int vaccineAppointmentId,
			String vaccineName, String doseType) {
		System.out.println("=== DEBUG markVaccineAsAdministered ===");
		System.out.println("Appointment ID: " + vaccineAppointmentId);
		System.out.println("Vaccine Name: " + vaccineName);
		System.out.println("Dose Type: " + doseType);

		// OPTION 1: If you add an 'administered' column to
		// vaccine_appointment_vaccine table
		String updateSql = "UPDATE vaccine_appointment_vaccine "
				+ "SET administered = 1, administered_date = CURRENT_TIMESTAMP "
				+ "WHERE vaccine_appointment_id = ? AND vaccine_name = ? AND dose_type = ?";

		try {
			db.executeUpdate(updateSql, vaccineAppointmentId, vaccineName,
					doseType);
			System.out.println("✅ Vaccine marked as administered");
		} catch (Exception e) {
			System.out.println("❌ Error marking vaccine as administered: "
					+ e.getMessage());
			e.printStackTrace();
			throw new ApplicationException(
					"Error marking vaccine as administered: " + e.getMessage());
		}
	}

	/**
	 * Alternative method if you have a separate table for administered vaccines
	 */
	public void moveVaccineToAdministered(int vaccineAppointmentId,
			String vaccineName, String doseType, int doctorId,
			String doctorName) {
		System.out.println("=== DEBUG moveVaccineToAdministered ===");

		try {
			// 1. Get the vaccine details
			String selectSql = "SELECT vaccine_name, dose_type FROM vaccine_appointment_vaccine "
					+ "WHERE vaccine_appointment_id = ? AND vaccine_name = ? AND dose_type = ?";

			List<Object[]> vaccineResults = db.executeQueryArray(selectSql,
					vaccineAppointmentId, vaccineName, doseType);

			if (vaccineResults.isEmpty()) {
				throw new ApplicationException(
						"Vaccine not found in appointment");
			}

			// 2. Insert into administered_vaccines table (you need to create
			// this)
			String insertSql = "INSERT INTO administered_vaccines "
					+ "(vaccine_appointment_id, vaccine_name, dose_type, administered_by_doctor_id, "
					+ "administered_by_doctor_name, administered_date) "
					+ "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

			db.executeUpdate(insertSql, vaccineAppointmentId, vaccineName,
					doseType, doctorId, doctorName);

			// 3. Remove from planned vaccines
			String deleteSql = "DELETE FROM vaccine_appointment_vaccine "
					+ "WHERE vaccine_appointment_id = ? AND vaccine_name = ? AND dose_type = ?";

			db.executeUpdate(deleteSql, vaccineAppointmentId, vaccineName,
					doseType);

			System.out.println("✅ Vaccine moved to administered successfully");

		} catch (Exception e) {
			System.out.println("❌ Error moving vaccine to administered: "
					+ e.getMessage());
			e.printStackTrace();
			throw new ApplicationException(
					"Error moving vaccine to administered: " + e.getMessage());
		}
	}

	/**
	 * Gets vaccine appointment by ID with all details
	 */
	public VaccineAppointmentDTO getVaccineAppointmentById1(int appointmentId) {
		System.out.println("=== DEBUG getVaccineAppointmentById ===");
		System.out.println("Appointment ID: " + appointmentId);

		String sql = "SELECT va.id, va.doctor_id, va.patient_id, va.date, va.hour, "
				+ "d.name || ' ' || d.surname as doctor_name, d.specialization as doctor_specialization, "
				+ "p.name || ' ' || p.surname as patient_name, p.dni as patient_dni, "
				+ "(SELECT GROUP_CONCAT(vav.vaccine_name || ' (' || vav.dose_type || ')', ', ') "
				+ " FROM vaccine_appointment_vaccine vav "
				+ " WHERE vav.vaccine_appointment_id = va.id) as vaccines "
				+ "FROM vaccine_appointment va "
				+ "JOIN doctors d ON va.doctor_id = d.id "
				+ "JOIN patients p ON va.patient_id = p.id "
				+ "WHERE va.id = ?";

		try {
			List<VaccineAppointmentDTO> results = db.executeQueryPojo(
					VaccineAppointmentDTO.class, sql, appointmentId);

			if (results.isEmpty()) {
				System.out.println("❌ No vaccine appointment found with ID: "
						+ appointmentId);
				return null;
			}

			VaccineAppointmentDTO appointment = results.get(0);
			System.out.println(
					"✅ Found appointment: " + appointment.getPatientName()
							+ " with Dr. " + appointment.getDoctorName());

			return appointment;

		} catch (Exception e) {
			System.out.println("❌ Error getting vaccine appointment by ID: "
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Removes a vaccine from a vaccine appointment
	 */
	public void removeVaccineFromAppointment(int vaccineAppointmentId,
			String vaccineName, String doseType) {
		System.out.println("=== DEBUG removeVaccineFromAppointment ===");

		String sql = "DELETE FROM vaccine_appointment_vaccine "
				+ "WHERE vaccine_appointment_id = ? AND vaccine_name = ? AND dose_type = ?";

		try {
			db.executeUpdate(sql, vaccineAppointmentId, vaccineName, doseType);

		} catch (Exception e) {
			System.out.println("❌ Error removing vaccine from appointment: "
					+ e.getMessage());
			throw new ApplicationException(
					"Error removing vaccine from appointment: "
							+ e.getMessage());
		}
	}

	public boolean saveVaccinesToMedicalRecord(int appointmentId, int patientId,
			List<VaccineDTO> vaccines, int doctorId, String doctorName) {
		try {
			// First, get vaccine appointment date for scheduled_date
			// Note: This is a VACCINE appointment, not regular appointment
			String appointmentDateSql = "SELECT date FROM vaccine_appointment WHERE id = ?";

			List<Object[]> dateResults = db
					.executeQueryArray(appointmentDateSql, appointmentId);
			if (dateResults.isEmpty() || dateResults.get(0)[0] == null) {
				throw new ApplicationException(
						"Vaccine appointment not found with ID: "
								+ appointmentId);
			}

			String appointmentDate = dateResults.get(0)[0].toString();

			// Get current date for administered_date
			String currentDate = LocalDate.now().toString();

			for (VaccineDTO vaccine : vaccines) {
				// Parse dose number from dose type (e.g., "Dose 1" -> 1)
				int doseNumber = 1;
				if (vaccine.getDoseType() != null
						&& vaccine.getDoseType().contains("Dose")) {
					try {
						String doseStr = vaccine.getDoseType()
								.replaceAll("[^0-9]", "");
						if (!doseStr.isEmpty()) {
							doseNumber = Integer.parseInt(doseStr);
						}
					} catch (NumberFormatException e) {
						doseNumber = 1;
					}
				}

				// Check if needs booster (based on recommended doses)
				int needsBooster = 0; // Default to 0 (no booster needed)
				if (vaccine.getRecommendedDoses() > doseNumber) {
					needsBooster = 1;
				}

				// Get administered date from vaccine or use current date
				String administeredDate = vaccine.getAdministeredDate();
				if (administeredDate == null || administeredDate.isEmpty()) {
					administeredDate = currentDate;
				}

				// Insert into vaccines table
				String sql = "INSERT INTO vaccines (patient_id, vaccine_name, dose_number, "
						+ "scheduled_date, administered_date, administered_by_doctor_id, "
						+ "administered_by_doctor_name, needs_booster, status) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

				db.executeUpdate(sql, patientId, vaccine.getName(), doseNumber,
						appointmentDate, // scheduled date (from
											// vaccine_appointment)
						administeredDate, // administered date
						doctorId, doctorName, needsBooster, "administered");

				System.out.println("Saved vaccine to medical record: "
						+ vaccine.getName() + " for patient " + patientId
						+ " by Dr. " + doctorName);
			}

			// OPTIONAL: You might want to update the vaccine appointment status
			// If you have a status column in vaccine_appointment table
			try {
				String updateAppointmentSql = "UPDATE vaccine_appointment SET status = 'completed' WHERE id = ?";
				db.executeUpdate(updateAppointmentSql, appointmentId);
				System.out.println("Updated vaccine appointment "
						+ appointmentId + " status.");
			} catch (Exception e) {
				System.out.println(
						"Note: Could not update vaccine appointment status. This is okay if the column doesn't exist.");
			}

			// Also remove administered vaccines from
			// vaccine_appointment_vaccine table
			// This cleans up the temporary data
			for (VaccineDTO vaccine : vaccines) {
				String deleteSql = "DELETE FROM vaccine_appointment_vaccine "
						+ "WHERE vaccine_appointment_id = ? AND vaccine_name = ? "
						+ "AND dose_type = ?";

				db.executeUpdate(deleteSql, appointmentId, vaccine.getName(),
						vaccine.getDoseType());
				System.out
						.println("Cleaned up vaccine from appointment_vaccine: "
								+ vaccine.getName());
			}

			return true;

		} catch (Exception e) {
			System.err.println("Error saving vaccines to medical record: "
					+ e.getMessage());
			e.printStackTrace();
			throw new ApplicationException(
					"Error saving vaccines to medical record: "
							+ e.getMessage());
		}
	}
}
