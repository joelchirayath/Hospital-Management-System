package giis.demo.service.scheduler;

public class WeekDTO {
	
	private WorkerDTO worker;
	
	private int startDay; private int endDay;
	private String startMonth; private String endMonth;
	private int startYear; private int endYear;
	
	private boolean Monday;
	private int hourStartMonday; private int minuteStartMonday;
	private int hourEndMonday; private int minuteEndMonday;
	
	private boolean Tuesday;
	private int hourStartTuesday; private int minuteStartTuesday;
	private int hourEndTuesday; private int minuteEndTuesday;
	
	private boolean Wednesday;
	private int hourStartWednesday; private int minuteStartWednesday;
	private int hourEndWednesday; private int minuteEndWednesday;
	
	private boolean Thursday;
	private int hourStartThursday; private int minuteStartThursday;
	private int hourEndThursday; private int minuteEndThursday;
	
	private boolean Friday;
	private int hourStartFriday; private int minuteStartFriday;
	private int hourEndFriday; private int minuteEndFriday;
	
	private boolean Saturday;
	private int hourStartSaturday; private int minuteStartSaturday;
	private int hourEndSaturday; private int minuteEndSaturday;
	
	private boolean Sunday;
	private int hourStartSunday; private int minuteStartSunday;
	private int hourEndSunday; private int minuteEndSunday;




	public WeekDTO(WorkerDTO worker, int startDay, int endDay, String startMonth, String endMonth, int startYear,
			int endYear, boolean monday, int hourStartMonday, int minnuteStartMonday, int hourEndMonday,
			int minuteEndMonday, boolean tuesday, int hourStartTuesday, int minuteStartTuesday, int hourEndTuesday,
			int minuteEndTuesday, boolean wednesday, int hourStartWednesday, int minuteStartWednesday,
			int hourEndWednesday, int minuteEndWednesday, boolean thursday, int hourStartThursday,
			int minuteStartThursday, int hourEndThursday, int minuteEndThursday, boolean friday, int hourStartFriday,
			int minuteStartFriday, int hourEndFriday, int minuteEndFriday, boolean saturday, int hourStartSaturday,
			int minuteStartSaturday, int hourEndSaturday, int minuteEndSaturday, boolean sunday, int hourStartSunday,
			int minuteStartSunday, int hourEndSunday, int minuteEndSunday) {
	
		this.worker = worker;
		this.startDay = startDay;
		this.endDay = endDay;
		this.startMonth = startMonth;
		this.endMonth = endMonth;
		this.startYear = startYear;
		this.endYear = endYear;
		Monday = monday;
		this.hourStartMonday = hourStartMonday;
		this.minuteStartMonday = minnuteStartMonday;
		this.hourEndMonday = hourEndMonday;
		this.minuteEndMonday = minuteEndMonday;
		Tuesday = tuesday;
		this.hourStartTuesday = hourStartTuesday;
		this.minuteStartTuesday = minuteStartTuesday;
		this.hourEndTuesday = hourEndTuesday;
		this.minuteEndTuesday = minuteEndTuesday;
		Wednesday = wednesday;
		this.hourStartWednesday = hourStartWednesday;
		this.minuteStartWednesday = minuteStartWednesday;
		this.hourEndWednesday = hourEndWednesday;
		this.minuteEndWednesday = minuteEndWednesday;
		Thursday = thursday;
		this.hourStartThursday = hourStartThursday;
		this.minuteStartThursday = minuteStartThursday;
		this.hourEndThursday = hourEndThursday;
		this.minuteEndThursday = minuteEndThursday;
		Friday = friday;
		this.hourStartFriday = hourStartFriday;
		this.minuteStartFriday = minuteStartFriday;
		this.hourEndFriday = hourEndFriday;
		this.minuteEndFriday = minuteEndFriday;
		Saturday = saturday;
		this.hourStartSaturday = hourStartSaturday;
		this.minuteStartSaturday = minuteStartSaturday;
		this.hourEndSaturday = hourEndSaturday;
		this.minuteEndSaturday = minuteEndSaturday;
		Sunday = sunday;
		this.hourStartSunday = hourStartSunday;
		this.minuteStartSunday = minuteStartSunday;
		this.hourEndSunday = hourEndSunday;
		this.minuteEndSunday = minuteEndSunday;
	}




	public WorkerDTO getWorker() {
		return worker;
	}




	public void setWorker(WorkerDTO worker) {
		this.worker = worker;
	}




	public int getStartDay() {
		return startDay;
	}




	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}




	public int getEndDay() {
		return endDay;
	}




	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}




	public String getStartMonth() {
		return startMonth;
	}




	public void setStartMonth(String startMonth) {
		this.startMonth = startMonth;
	}




	public String getEndMonth() {
		return endMonth;
	}




	public void setEndMonth(String endMonth) {
		this.endMonth = endMonth;
	}




	public int getStartYear() {
		return startYear;
	}




	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}




	public int getEndYear() {
		return endYear;
	}




	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}




	public boolean isMonday() {
		return Monday;
	}




	public void setMonday(boolean monday) {
		Monday = monday;
	}




	public int getHourStartMonday() {
		return hourStartMonday;
	}




	public void setHourStartMonday(int hourStartMonday) {
		this.hourStartMonday = hourStartMonday;
	}




	public int getMinuteStartMonday() {
		return minuteStartMonday;
	}




	public void setMinuteStartMonday(int minnuteStartMonday) {
		this.minuteStartMonday = minnuteStartMonday;
	}




	public int getHourEndMonday() {
		return hourEndMonday;
	}




	public void setHourEndMonday(int hourEndMonday) {
		this.hourEndMonday = hourEndMonday;
	}




	public int getMinuteEndMonday() {
		return minuteEndMonday;
	}




	public void setMinuteEndMonday(int minuteEndMonday) {
		this.minuteEndMonday = minuteEndMonday;
	}




	public boolean isTuesday() {
		return Tuesday;
	}




	public void setTuesday(boolean tuesday) {
		Tuesday = tuesday;
	}




	public int getHourStartTuesday() {
		return hourStartTuesday;
	}




	public void setHourStartTuesday(int hourStartTuesday) {
		this.hourStartTuesday = hourStartTuesday;
	}




	public int getMinuteStartTuesday() {
		return minuteStartTuesday;
	}




	public void setMinuteStartTuesday(int minuteStartTuesday) {
		this.minuteStartTuesday = minuteStartTuesday;
	}




	public int getHourEndTuesday() {
		return hourEndTuesday;
	}




	public void setHourEndTuesday(int hourEndTuesday) {
		this.hourEndTuesday = hourEndTuesday;
	}




	public int getMinuteEndTuesday() {
		return minuteEndTuesday;
	}




	public void setMinuteEndTuesday(int minuteEndTuesday) {
		this.minuteEndTuesday = minuteEndTuesday;
	}




	public boolean isWednesday() {
		return Wednesday;
	}




	public void setWednesday(boolean wednesday) {
		Wednesday = wednesday;
	}




	public int getHourStartWednesday() {
		return hourStartWednesday;
	}




	public void setHourStartWednesday(int hourStartWednesday) {
		this.hourStartWednesday = hourStartWednesday;
	}




	public int getMinuteStartWednesday() {
		return minuteStartWednesday;
	}




	public void setMinuteStartWednesday(int minuteStartWednesday) {
		this.minuteStartWednesday = minuteStartWednesday;
	}




	public int getHourEndWednesday() {
		return hourEndWednesday;
	}




	public void setHourEndWednesday(int hourEndWednesday) {
		this.hourEndWednesday = hourEndWednesday;
	}




	public int getMinuteEndWednesday() {
		return minuteEndWednesday;
	}




	public void setMinuteEndWednesday(int minuteEndWednesday) {
		this.minuteEndWednesday = minuteEndWednesday;
	}




	public boolean isThursday() {
		return Thursday;
	}




	public void setThursday(boolean thursday) {
		Thursday = thursday;
	}




	public int getHourStartThursday() {
		return hourStartThursday;
	}




	public void setHourStartThursday(int hourStartThursday) {
		this.hourStartThursday = hourStartThursday;
	}




	public int getMinuteStartThursday() {
		return minuteStartThursday;
	}




	public void setMinuteStartThursday(int minuteStartThursday) {
		this.minuteStartThursday = minuteStartThursday;
	}




	public int getHourEndThursday() {
		return hourEndThursday;
	}




	public void setHourEndThursday(int hourEndThursday) {
		this.hourEndThursday = hourEndThursday;
	}




	public int getMinuteEndThursday() {
		return minuteEndThursday;
	}




	public void setMinuteEndThursday(int minuteEndThursday) {
		this.minuteEndThursday = minuteEndThursday;
	}




	public boolean isFriday() {
		return Friday;
	}




	public void setFriday(boolean friday) {
		Friday = friday;
	}




	public int getHourStartFriday() {
		return hourStartFriday;
	}




	public void setHourStartFriday(int hourStartFriday) {
		this.hourStartFriday = hourStartFriday;
	}




	public int getMinuteStartFriday() {
		return minuteStartFriday;
	}




	public void setMinuteStartFriday(int minuteStartFriday) {
		this.minuteStartFriday = minuteStartFriday;
	}




	public int getHourEndFriday() {
		return hourEndFriday;
	}




	public void setHourEndFriday(int hourEndFriday) {
		this.hourEndFriday = hourEndFriday;
	}




	public int getMinuteEndFriday() {
		return minuteEndFriday;
	}




	public void setMinuteEndFriday(int minuteEndFriday) {
		this.minuteEndFriday = minuteEndFriday;
	}




	public boolean isSaturday() {
		return Saturday;
	}




	public void setSaturday(boolean saturday) {
		Saturday = saturday;
	}




	public int getHourStartSaturday() {
		return hourStartSaturday;
	}




	public void setHourStartSaturday(int hourStartSaturday) {
		this.hourStartSaturday = hourStartSaturday;
	}




	public int getMinuteStartSaturday() {
		return minuteStartSaturday;
	}




	public void setMinuteStartSaturday(int minuteStartSaturday) {
		this.minuteStartSaturday = minuteStartSaturday;
	}




	public int getHourEndSaturday() {
		return hourEndSaturday;
	}




	public void setHourEndSaturday(int hourEndSaturday) {
		this.hourEndSaturday = hourEndSaturday;
	}




	public int getMinuteEndSaturday() {
		return minuteEndSaturday;
	}




	public void setMinuteEndSaturday(int minuteEndSaturday) {
		this.minuteEndSaturday = minuteEndSaturday;
	}




	public boolean isSunday() {
		return Sunday;
	}




	public void setSunday(boolean sunday) {
		Sunday = sunday;
	}




	public int getHourStartSunday() {
		return hourStartSunday;
	}




	public void setHourStartSunday(int hourStartSunday) {
		this.hourStartSunday = hourStartSunday;
	}




	public int getMinuteStartSunday() {
		return minuteStartSunday;
	}




	public void setMinuteStartSunday(int minuteStartSunday) {
		this.minuteStartSunday = minuteStartSunday;
	}




	public int getHourEndSunday() {
		return hourEndSunday;
	}




	public void setHourEndSunday(int hourEndSunday) {
		this.hourEndSunday = hourEndSunday;
	}




	public int getMinuteEndSunday() {
		return minuteEndSunday;
	}




	public void setMinuteEndSunday(int minuteEndSunday) {
		this.minuteEndSunday = minuteEndSunday;
	}
	
	
	
	
	

}
