package giis.demo.service.scheduler;

import java.util.List;

import giis.demo.jdbc.models.scheduler.SchedulerModel;
import giis.demo.jdbc.models.scheduler.SchedulerModelImpl;

public class Scheduler {
	
	private SchedulerModel model;

	public Scheduler() {
		this.model = new SchedulerModelImpl();
		
	}


	public List<WorkerDTO> getNurses() {
		return model.getNurseList();
	}

	public List<WorkerDTO> getDoctors() {
		return model.getDoctorsList();
	}


	public void saveInfo(List<WorkingDayDTO> days) {
		
		model.saveDays(days);
	}

}
