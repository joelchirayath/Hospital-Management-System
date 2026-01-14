package giis.demo.service.appointment;

import giis.demo.util.Database;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Aggregates appointments by disease and time bucket.
 * Uses full bucket range (even with 0 appointments) when there is data.
 * Disease label is always: "CODE - Description".
 */
public class AppointmentStatisticsModel {

    private final Database db = new Database();

    // ===== Backward-compatible entry point =====
    public List<AppointmentDiseaseStatsDTO> getDiseaseStats(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            List<String> diseaseCodes
    ) {
        // Default full-day time range, all doctors
        return getDiseaseStats(from, to, interval, diseaseCodes, "00:00", "23:59", null);
    }

    // ===== New overload with time-of-day and doctor filter =====
    public List<AppointmentDiseaseStatsDTO> getDiseaseStats(
            LocalDate from,
            LocalDate to,
            TimeInterval interval,
            List<String> diseaseCodes,
            String fromTime,
            String toTime,
            Integer doctorId
    ) {
        String fTime = (fromTime == null || fromTime.isEmpty()) ? "00:00" : fromTime;
        String tTime = (toTime == null || toTime.isEmpty()) ? "23:59" : toTime;

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT ");

        if (interval == TimeInterval.HOUR) {
            // Grouping by hour-of-day
            sql.append("strftime('%H', a.start_time) AS bucket, ");
        } else {
            // Keep raw date; bucketing is done in Java (day/week/month/year)
            sql.append("a.date AS bucket, ");
        }

        sql.append("mr.icd10_code AS code, i.description AS description ")
           .append("FROM medical_records mr ")
           .append("JOIN appointments a ON mr.appointment_id = a.id ");

        if (doctorId != null) {
            sql.append("JOIN doctors_appointments da ON a.id = da.id_appointments ");
        }

        sql.append("JOIN icd10_codes i ON mr.icd10_code = i.code ")
           .append("WHERE date(a.date) BETWEEN ? AND ? ");
        params.add(from.toString());
        params.add(to.toString());

        // Time-of-day filter
        sql.append("AND time(a.start_time) >= time(?) AND time(a.start_time) <= time(?) ");
        params.add(fTime);
        params.add(tTime);

        if (doctorId != null) {
            sql.append("AND da.id_doctors = ? ");
            params.add(doctorId);
        }

        if (diseaseCodes != null && !diseaseCodes.isEmpty()) {
            sql.append("AND mr.icd10_code IN (");
            for (int i = 0; i < diseaseCodes.size(); i++) {
                if (i > 0) sql.append(",");
                sql.append("?");
                params.add(diseaseCodes.get(i));
            }
            sql.append(") ");
        }

        if (interval == TimeInterval.HOUR) {
            sql.append("ORDER BY bucket ASC");
        } else {
            sql.append("ORDER BY a.date ASC, a.start_time ASC");
        }

        List<Map<String, Object>> rows = db.executeQueryMap(sql.toString(), params.toArray());

        Map<String, Integer> counter = new HashMap<>();
        LinkedHashSet<String> diseaseLabels = new LinkedHashSet<>();

        for (Map<String, Object> row : rows) {
            Object codeObj = row.get("code");
            Object descObj = row.get("description");
            Object bucketObj = row.get("bucket");
            if (codeObj == null || bucketObj == null) continue;

            String code = codeObj.toString();
            String desc = (descObj != null) ? descObj.toString() : "";
            String diseaseLabel = code + (desc.isEmpty() ? "" : " - " + desc);

            String bucket;
            if (interval == TimeInterval.HOUR) {
                // "HH" -> "HH:00"
                bucket = bucketObj.toString() + ":00";
            } else {
                bucket = toBucket(LocalDate.parse(bucketObj.toString()), interval);
            }

            String key = diseaseLabel + "||" + bucket;
            diseaseLabels.add(diseaseLabel);
            counter.put(key, counter.getOrDefault(key, 0) + 1);
        }

        // If absolutely no data, return empty list so UI can show "No appointments available"
        if (diseaseLabels.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> allBuckets = buildAllBuckets(from, to, interval);
        List<AppointmentDiseaseStatsDTO> result = new ArrayList<>();

        for (String disease : diseaseLabels) {
            for (String bucket : allBuckets) {
                int count = counter.getOrDefault(disease + "||" + bucket, 0);
                result.add(new AppointmentDiseaseStatsDTO(disease, bucket, count));
            }
        }

        return result;
    }

    /**
     * Convert a given date to a display bucket depending on the interval.
     */
    private String toBucket(LocalDate date, TimeInterval interval) {
        switch (interval) {
            case DAY:
                return date.toString();

            case MONTH: {
                String[] m = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                return m[date.getMonthValue() - 1] + " " + date.getYear();
            }

            case YEAR:
                return String.valueOf(date.getYear());

            case WEEK: {
                int day = date.getDayOfMonth();
                int weekIndex = (day - 1) / 7 + 1;
                YearMonth ym = YearMonth.of(date.getYear(), date.getMonth());
                int startDay = (weekIndex - 1) * 7 + 1;
                int endDay = Math.min(weekIndex * 7, ym.lengthOfMonth());

                String[] m2 = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                String month = m2[ym.getMonthValue() - 1];

                return month + " " + ym.getYear() +
                        " - Week " + weekIndex +
                        " (" + startDay + " - " + endDay + ")";
            }

            default:
                return date.toString();
        }
    }


    private List<String> buildAllBuckets(LocalDate from, LocalDate to, TimeInterval interval) {
        LinkedHashSet<String> set = new LinkedHashSet<>();

        switch (interval) {
            case YEAR:
                for (int y = from.getYear(); y <= to.getYear(); y++) {
                    set.add(String.valueOf(y));
                }
                break;

            case MONTH: {
                LocalDate cur = LocalDate.of(from.getYear(), from.getMonth(), 1);
                LocalDate end = LocalDate.of(to.getYear(), to.getMonth(), 1);
                while (!cur.isAfter(end)) {
                    set.add(toBucket(cur, TimeInterval.MONTH));
                    cur = cur.plusMonths(1);
                }
                break;
            }

            case WEEK:
            case DAY: {
                LocalDate cur = from;
                while (!cur.isAfter(to)) {
                    set.add(toBucket(cur, interval));
                    cur = cur.plusDays(1);
                }
                break;
            }

            case HOUR:
                for (int h = 0; h < 24; h++) {
                    set.add(String.format("%02d:00", h));
                }
                break;

            default:
                break;
        }

        return new ArrayList<>(set);
    }
}
