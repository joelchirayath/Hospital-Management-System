package giis.demo.jdbc.models.scheduler;

import java.util.List;

import giis.demo.service.scheduler.WorkerDTO;
import giis.demo.service.scheduler.WorkingDayDTO;
import giis.demo.util.Database;

public class SchedulerModelImpl implements SchedulerModel {

	private Database db;

	public SchedulerModelImpl() {
		this.db = new Database();
		db.createDatabase(true);
		db.loadDatabase();
	}

	@Override
	public List<WorkerDTO> getNurseList() {
		String sql = "SELECT dni, name, surname FROM nurses "
				+ "ORDER BY surname ASC;";
		List<WorkerDTO> nurses = db.executeQueryPojo(WorkerDTO.class, sql);
		for (WorkerDTO dto : nurses) {
			dto.setType("Nurse");
		}
		return nurses;
	}

	@Override
	public List<WorkerDTO> getDoctorsList() {
		String sql = "SELECT dni, name, surname FROM doctors ORDER BY surname ASC;";
		List<WorkerDTO> doctors = db.executeQueryPojo(WorkerDTO.class, sql);
		for (WorkerDTO dto : doctors) {
			dto.setType("Doctor");
		}
		return doctors;
	}

	@Override
	public void saveDays(List<WorkingDayDTO> days) {

		String sql = "INSERT INTO WORKING_DAY (dni, worker_type, dayOfweek, day, month, year, start, end, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Object[][] paramsArray = new Object[days.size()][];

		for (int i = 0; i < days.size(); i++) {
			WorkingDayDTO dto = days.get(i);

			paramsArray[i] = new Object[] { dto.getDni(), dto.getType(),
					dto.getDayOfweek(), dto.getDay(), dto.getMonth(),
					dto.getYear(), dto.getStart_time(), dto.getEnd_time(),
					dto.getType() };
		}

		db.executeBatchUpdate(sql, paramsArray);
	}

}
