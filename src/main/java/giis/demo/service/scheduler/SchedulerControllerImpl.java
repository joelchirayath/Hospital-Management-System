package giis.demo.service.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SchedulerControllerImpl implements SchedulerController{
	
	private Scheduler scheduler;
	
	public SchedulerControllerImpl() {
		
		this.scheduler = new Scheduler();
	}

	@Override
	public List<WorkerDTO> getDoctors() {
		return scheduler.getDoctors();
	}

	@Override
	public List<WorkerDTO> getNurses() {
		return scheduler.getNurses();
	}

	

	private WorkingDayDTO makeDayBatch(
		    WorkerDTO worker, 
		    LocalDate date, 
		    int startHour, 
		    int startMinute, 
		    int endHour, 
		    int endMinute) {
		
		 	String type = worker.getType();

		    String day = String.valueOf(date.getDayOfMonth());
		    String month = String.valueOf(date.getMonthValue()); 
		    String year = String.valueOf(date.getYear());
		    String dayOfweek = date.getDayOfWeek().toString(); 
		    
		    String startTime = String.format("%02d:%02d:00", startHour, startMinute);
		    String endTime = String.format("%02d:%02d:00", endHour, endMinute);

		    
		    WorkingDayDTO dto = new WorkingDayDTO(
		        worker.getDni(), 
		        dayOfweek,
		        day, 
		        month, 
		        year, 
		        startTime, 
		        endTime,
		        type
		    );
		    
		    return dto;
		}
    
	@Override
	public List<WorkingDayDTO> createWorkDays(WeekDTO weekDTO) {
	    List<WorkingDayDTO> toSave = new ArrayList<>();
	    if (weekDTO == null || weekDTO.getWorker() == null) {
	        return toSave;
	    	}

	    
	 //DEBUGING
//	    System.out.println("=== DEBUG START ===");
//	    System.out.println("Worker: " + weekDTO.getWorker());
//	    System.out.println("Worker Type: " + (weekDTO.getWorker() != null ? weekDTO.getWorker().getType() : "null"));
//	    System.out.println("Start Month: '" + weekDTO.getStartMonth() + "' -> " + getMonthNumber(weekDTO.getStartMonth()));
//	    System.out.println("End Month: '" + weekDTO.getEndMonth() + "' -> " + getMonthNumber(weekDTO.getEndMonth()));
//	  
	    int startMonthNum = getMonthNumber(weekDTO.getStartMonth()); 
	    int endMonthNum = getMonthNumber(weekDTO.getEndMonth());

	    try {
	        LocalDate startDate = LocalDate.of(weekDTO.getStartYear(), startMonthNum, weekDTO.getStartDay());
	        LocalDate endDate = LocalDate.of(weekDTO.getEndYear(), endMonthNum, weekDTO.getEndDay());

	        LocalDate currentDay = startDate;
	        
	        while (!currentDay.isAfter(endDate)) {
	            DayOfWeek dayOfWeek = currentDay.getDayOfWeek();
	            boolean isScheduled = false;
	            int startHour = 0, startMinute = 0, endHour = 0, endMinute = 0;

	            switch (dayOfWeek) {
	                case MONDAY:
	                    isScheduled = weekDTO.isMonday();
	                    if (isScheduled) {
	                        startHour = weekDTO.getHourStartMonday();
	                        startMinute = weekDTO.getMinuteStartMonday(); 
	                        endHour = weekDTO.getHourEndMonday();
	                        endMinute = weekDTO.getMinuteEndMonday();
	                    }
	                    break;
	                    
	                case TUESDAY:
	                    isScheduled = weekDTO.isTuesday();
	                    if (isScheduled) {
	                        startHour = weekDTO.getHourStartTuesday();
	                        startMinute = weekDTO.getMinuteStartTuesday();
	                        endHour = weekDTO.getHourEndTuesday();
	                        endMinute = weekDTO.getMinuteEndTuesday();
	                    }
	                    break;
	                    
	                case WEDNESDAY:
	                    isScheduled = weekDTO.isWednesday();
	                    if (isScheduled) {
	                        startHour = weekDTO.getHourStartWednesday();
	                        startMinute = weekDTO.getMinuteStartWednesday();
	                        endHour = weekDTO.getHourEndWednesday();
	                        endMinute = weekDTO.getMinuteEndWednesday();
	                    }
	                    break;
	                    
	                case THURSDAY:
	                    isScheduled = weekDTO.isThursday();
	                    if (isScheduled) {
	                        startHour = weekDTO.getHourStartThursday();
	                        startMinute = weekDTO.getMinuteStartThursday();
	                        endHour = weekDTO.getHourEndThursday();
	                        endMinute = weekDTO.getMinuteEndThursday();
	                    }
	                    break;
	                    
	                case FRIDAY:
	                    isScheduled = weekDTO.isFriday();
	                    if (isScheduled) {
	                        startHour = weekDTO.getHourStartFriday();
	                        startMinute = weekDTO.getMinuteStartFriday();
	                        endHour = weekDTO.getHourEndFriday();
	                        endMinute = weekDTO.getMinuteEndFriday();
	                    }
	                    break;
	                    
	                case SATURDAY:
	                    isScheduled = weekDTO.isSaturday();
	                    if (isScheduled) {
	                        startHour = weekDTO.getHourStartSaturday();
	                        startMinute = weekDTO.getMinuteStartSaturday();
	                        endHour = weekDTO.getHourEndSaturday();
	                        endMinute = weekDTO.getMinuteEndSaturday();
	                    }
	                    break;
	                    
	                case SUNDAY:
	                    isScheduled = weekDTO.isSunday();
	                    if (isScheduled) {
	                        startHour = weekDTO.getHourStartSunday();
	                        startMinute = weekDTO.getMinuteStartSunday(); 
	                        endHour = weekDTO.getHourEndSunday();
	                        endMinute = weekDTO.getMinuteEndSunday();
	                    }
	                    break;
	                default:
	                    break;
	            }

	            
	            if (isScheduled) {
	                WorkingDayDTO wddto = makeDayBatch(
	                    weekDTO.getWorker(), 
	                    currentDay, 
	                    startHour, 
	                    startMinute, 
	                    endHour, 
	                    endMinute
	                );
	                toSave.add(wddto);
	            }

	            currentDay = currentDay.plusDays(1);
	        }
	    } catch (Exception e) {
	        System.err.println("Error fechas");
	    }

	    return toSave;
	}
   
    private int getMonthNumber(String monthName) {
    	if (monthName == null) {
            return -1;
        }
       
    	switch (monthName.toLowerCase()) {
        case "january":   return 1;
        case "february":  return 2;
        case "march":     return 3;
        case "april":     return 4;
        case "may":       return 5;
        case "june":      return 6;
        case "july":      return 7;
        case "august":    return 8;
        case "september": return 9;
        case "october":   return 10;
        case "november":  return 11;
        case "december":  return 12;
        default: return -1;
    }
    }

	@Override
	public void saveWorkingDays(List<WorkingDayDTO> toSave) {
		scheduler.saveInfo(toSave);
		
	}
}
