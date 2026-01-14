package giis.demo.service.manager;

import giis.demo.service.appointment.AppointmentDiseaseStatsDTO;
import giis.demo.service.appointment.AppointmentModel;
import giis.demo.service.appointment.AppointmentStatisticsModel;
import giis.demo.service.appointment.AppointmentTimeSeriesDTO;
import giis.demo.service.appointment.TimeInterval;

import java.time.LocalDate;
import java.util.List;

public class ReportsControllerImpl implements ReportsController {

    private final AppointmentStatisticsModel statisticsModel = new AppointmentStatisticsModel();
    private final AppointmentModel appointmentModel = new AppointmentModel();

    @Override
    public List<AppointmentDiseaseStatsDTO> getAppointmentsByDisease(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            List<String> diseaseCodes
    ) {
        return statisticsModel.getDiseaseStats(from, to, interval, diseaseCodes);
    }

    @Override
    public List<AppointmentDiseaseStatsDTO> getAppointmentsByDisease(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            List<String> diseaseCodes,
            String fromTime,
            String toTime,
            Integer doctorId
    ) {
        return statisticsModel.getDiseaseStats(
                from, to, interval, diseaseCodes,
                fromTime, toTime, doctorId
        );
    }

  
    @Override
    public List<AppointmentTimeSeriesDTO> getAppointmentVolumeOverTime(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            Integer doctorId,
            Integer departmentId
    ) {
        // default: full day range
        return getAppointmentVolumeOverTime(from, to, interval, doctorId, departmentId, "00:00", "23:59");
    }


    public List<AppointmentTimeSeriesDTO> getAppointmentVolumeOverTime(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            Integer doctorId,
            Integer departmentId,
            String fromTime,
            String toTime
    ) {
        return appointmentModel.getAppointmentsOverTime(
                from.toString(),
                to.toString(),
                interval,
                doctorId,
                departmentId,
                fromTime,
                toTime
        );
    }
}
