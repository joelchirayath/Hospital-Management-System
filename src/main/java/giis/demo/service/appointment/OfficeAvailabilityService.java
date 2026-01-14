package giis.demo.service.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds timeline segments (green/red) for an office over a date range,
 * for a given time window (e.g. 00:00–24:00 or 12:00–14:00).
 *
 * This class is read-only: it only uses AppointmentModel to fetch data.
 */
public class OfficeAvailabilityService {

    private final AppointmentModel appointmentModel;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public OfficeAvailabilityService() {
        this(new AppointmentModel());
    }

    public OfficeAvailabilityService(AppointmentModel model) {
        this.appointmentModel = model;
    }

    /**
     * Returns a map: date -> list of ordered segments (free/busy) for that day.
     *
     * @param officeName  office/room name (matches appointments.office)
     * @param fromDate    start date inclusive
     * @param toDate      end date inclusive
     * @param windowStart start time of the time window (e.g. 00:00)
     * @param windowEnd   end time of the time window (e.g. 24:00 → use 23:59)
     * @param aggregateBusySegments if true, merge close/overlapping busy slots
     */
    public Map<LocalDate, List<OfficeAvailabilitySegment>> getOfficeAvailability(
            String officeName,
            LocalDate fromDate,
            LocalDate toDate,
            LocalTime windowStart,
            LocalTime windowEnd,
            boolean aggregateBusySegments) {

        Map<LocalDate, List<OfficeAvailabilitySegment>> result = new HashMap<>();

        // Fetch all raw appointments in that office + date range from DB
        List<AppointmentDTO> all = appointmentModel
                .getAppointmentsByOfficeAndDateRange(officeName,
                        fromDate.toString(),
                        toDate.toString());

        // Group by date as LocalDate
        Map<LocalDate, List<AppointmentDTO>> byDate = all.stream()
                .collect(Collectors.groupingBy(a -> LocalDate.parse(a.getDate())));

        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            List<AppointmentDTO> apptsForDay = byDate.getOrDefault(current, new ArrayList<>());
            List<OfficeAvailabilitySegment> segmentsForDay =
                    buildSegmentsForDay(current, apptsForDay, windowStart, windowEnd, aggregateBusySegments);

            result.put(current, segmentsForDay);
            current = current.plusDays(1);
        }

        return result;
    }

    // ---------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------

    private static class BusyBlock {
        LocalTime start;
        LocalTime end;
        List<AppointmentDTO> appointments = new ArrayList<>();
    }

    private List<OfficeAvailabilitySegment> buildSegmentsForDay(
            LocalDate date,
            List<AppointmentDTO> appointments,
            LocalTime windowStart,
            LocalTime windowEnd,
            boolean aggregateBusySegments) {

        List<OfficeAvailabilitySegment> segments = new ArrayList<>();

        // If there are no appointments at all for this day in the window -> one big free segment
        if (appointments == null || appointments.isEmpty()) {
            segments.add(new OfficeAvailabilitySegment(date, windowStart, windowEnd, false));
            return segments;
        }

        // 1) Clamp appointments to the time window (ignore those fully outside)
        List<BusyBlock> rawBlocks = new ArrayList<>();

        for (AppointmentDTO dto : appointments) {
            if (dto.getStart_time() == null || dto.getEnd_time() == null)
                continue; // skip malformed

            LocalTime apptStart = LocalTime.parse(dto.getStart_time(), TIME_FMT);
            LocalTime apptEnd = LocalTime.parse(dto.getEnd_time(), TIME_FMT);

            // Fully outside window -> ignore
            if (!apptEnd.isAfter(windowStart) || !apptStart.isBefore(windowEnd)) {
                continue;
            }

            // Clamp to window
            LocalTime clampedStart = apptStart.isBefore(windowStart) ? windowStart : apptStart;
            LocalTime clampedEnd = apptEnd.isAfter(windowEnd) ? windowEnd : apptEnd;

            if (!clampedEnd.isAfter(clampedStart)) {
                continue;
            }

            BusyBlock b = new BusyBlock();
            b.start = clampedStart;
            b.end = clampedEnd;
            b.appointments.add(dto);
            rawBlocks.add(b);
        }

        if (rawBlocks.isEmpty()) {
            // There were appointments, but all outside current window
            segments.add(new OfficeAvailabilitySegment(date, windowStart, windowEnd, false));
            return segments;
        }

        // 2) Sort by start time
        rawBlocks.sort(Comparator.comparing(bb -> bb.start));

        // 3) Optionally merge overlapping / very close busy blocks
        List<BusyBlock> merged = new ArrayList<>();
        final int MERGE_GAP_MINUTES = aggregateBusySegments ? 5 : 0;

        for (BusyBlock current : rawBlocks) {
            if (merged.isEmpty()) {
                merged.add(current);
            } else {
                BusyBlock last = merged.get(merged.size() - 1);
                // If overlapping OR gap <= MERGE_GAP_MINUTES -> merge
                boolean overlapping = !current.start.isAfter(last.end);
                boolean smallGap = last.end.plusMinutes(MERGE_GAP_MINUTES)
                        .isAfter(current.start);

                if (overlapping || smallGap) {
                    if (current.end.isAfter(last.end)) {
                        last.end = current.end;
                    }
                    last.appointments.addAll(current.appointments);
                } else {
                    merged.add(current);
                }
            }
        }

        // 4) Build final segments with free gaps in between merged busy blocks
        LocalTime cursor = windowStart;

        for (BusyBlock block : merged) {
            if (block.start.isAfter(cursor)) {
                // Free gap
                segments.add(new OfficeAvailabilitySegment(date, cursor, block.start, false));
            }

            OfficeAvailabilitySegment busySeg =
                    new OfficeAvailabilitySegment(date, block.start, block.end, true);
            for (AppointmentDTO dto : block.appointments) {
                busySeg.addAppointment(dto);
            }

            segments.add(busySeg);
            cursor = block.end;
        }

        // Tail free time
        if (cursor.isBefore(windowEnd)) {
            segments.add(new OfficeAvailabilitySegment(date, cursor, windowEnd, false));
        }

        return segments;
    }
}
