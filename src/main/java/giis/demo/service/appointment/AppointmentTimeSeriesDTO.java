package giis.demo.service.appointment;

/**
 * Represents aggregated appointment counts for a given time bucket.
 * Used for manager analytics: appointments over hours/days/weeks/months/years.
 */
public class AppointmentTimeSeriesDTO {
    private String bucketLabel; // e.g. "2025-02-01", "2025-02-01 10:00", "2025-02-W05", "2025-02", "2025"
    private int count;

    public AppointmentTimeSeriesDTO() {
    }

    public AppointmentTimeSeriesDTO(String bucketLabel, int count) {
        this.bucketLabel = bucketLabel;
        this.count = count;
    }

    public String getBucketLabel() {
        return bucketLabel;
    }

    public void setBucketLabel(String bucketLabel) {
        this.bucketLabel = bucketLabel;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
