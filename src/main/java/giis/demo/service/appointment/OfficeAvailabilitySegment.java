package giis.demo.service.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single continuous segment in the availability timeline.
 * - busy=false -> free (green)
 * - busy=true  -> occupied (red), may represent one or several merged appointments.
 */
public class OfficeAvailabilitySegment {

    private LocalDate date;
    private LocalTime from;
    private LocalTime to;
    private boolean busy;
    private int appointmentCount;                  // how many appts are inside this segment
    private List<AppointmentDTO> appointments;     // optional details for tooltips

    public OfficeAvailabilitySegment(LocalDate date, LocalTime from, LocalTime to, boolean busy) {
        this.date = date;
        this.from = from;
        this.to = to;
        this.busy = busy;
        this.appointmentCount = 0;
        this.appointments = new ArrayList<>();
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getFrom() { return from; }
    public void setFrom(LocalTime from) { this.from = from; }

    public LocalTime getTo() { return to; }
    public void setTo(LocalTime to) { this.to = to; }

    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }

    public int getAppointmentCount() { return appointmentCount; }
    public void setAppointmentCount(int appointmentCount) { this.appointmentCount = appointmentCount; }

    public List<AppointmentDTO> getAppointments() { return appointments; }
    public void setAppointments(List<AppointmentDTO> appointments) { this.appointments = appointments; }

    public void addAppointment(AppointmentDTO dto) {
        if (appointments == null)
            appointments = new ArrayList<>();
        appointments.add(dto);
        appointmentCount = appointments.size();
    }

    @Override
    public String toString() {
        return "OfficeAvailabilitySegment{" +
                "date=" + date +
                ", from=" + from +
                ", to=" + to +
                ", busy=" + busy +
                ", appointmentCount=" + appointmentCount +
                '}';
    }
}
