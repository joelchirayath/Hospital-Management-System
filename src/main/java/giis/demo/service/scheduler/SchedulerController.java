package giis.demo.service.scheduler;

import java.util.List;

public interface SchedulerController {

	

	List<WorkerDTO> getDoctors();

	List<WorkerDTO> getNurses();

	List<WorkingDayDTO> createWorkDays(WeekDTO weekInfo);

	void saveWorkingDays(List<WorkingDayDTO> diasAGuardar);

	
}
