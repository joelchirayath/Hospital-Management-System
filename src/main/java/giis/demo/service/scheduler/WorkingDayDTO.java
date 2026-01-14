package giis.demo.service.scheduler;

public class WorkingDayDTO {

	
	private String dni;
	private String dayOfweek;
	private String day;
	private String month;
	private String year;
	private String start_time;
	private String end_time;
	private String type;


	public WorkingDayDTO(String dni, String dayOfweek, String day, String month, String year, String start_time,
			String end_time, String type) {

		this.dni = dni;
		this.dayOfweek = dayOfweek;
		this.day = day;
		this.month = month;
		this.year = year;
		this.start_time = start_time;
		this.end_time = end_time;
		this.type = type;
	}



	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}



	public String getMonth() {
		return month;
	}



	public void setMonth(String month) {
		this.month = month;
	}



	public String getYear() {
		return year;
	}



	public void setYear(String year) {
		this.year = year;
	}



	public String getDni() {
		return dni;
	}



	public void setDni(String dni) {
		this.dni = dni;
	}



	public String getDay() {
		return day;
	}



	public void setDay(String day) {
		this.day = day;
	}



	public String getStart_time() {
		return start_time;
	}



	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}



	public String getEnd_time() {
		return end_time;
	}



	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	
	


	public String getDayOfweek() {
		return dayOfweek;
	}



	public void setDayOfweek(String dayOfweek) {
		this.dayOfweek = dayOfweek;
	}



	@Override
	public String toString() {
		return "WorkingDayDTO [dni=" + dni + ", day=" + day + ", start_time=" + start_time + ", end_time=" + end_time
				+ "]";
	}
	
	
	
}
