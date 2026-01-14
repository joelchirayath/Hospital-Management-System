package giis.demo.jdbc.models.scheduler;

import java.util.List;

import giis.demo.service.scheduler.WorkerDTO;
import giis.demo.service.scheduler.WorkingDayDTO;

public interface SchedulerModel {


	List<WorkerDTO> getNurseList();

	List<WorkerDTO> getDoctorsList();

	void saveDays(List<WorkingDayDTO> days);



}
