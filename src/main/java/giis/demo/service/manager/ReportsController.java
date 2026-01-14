package giis.demo.service.manager;

import giis.demo.service.appointment.AppointmentDiseaseStatsDTO;
import giis.demo.service.appointment.AppointmentTimeSeriesDTO;
import giis.demo.service.appointment.TimeInterval;

import java.time.LocalDate;
import java.util.List;

public interface ReportsController {

    // Existing (kept for backward compatibility)
    List<AppointmentDiseaseStatsDTO> getAppointmentsByDisease(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            List<String> diseaseCodes
    );

    // NEW: with time-of-day and optional doctor
    List<AppointmentDiseaseStatsDTO> getAppointmentsByDisease(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            List<String> diseaseCodes,
            String fromTime,     // "HH:mm"
            String toTime,       // "HH:mm"
            Integer doctorId     // nullable; null = all doctors
    );

    // Unchanged
    List<AppointmentTimeSeriesDTO> getAppointmentVolumeOverTime(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            Integer doctorId,
            Integer departmentId
    );
}
