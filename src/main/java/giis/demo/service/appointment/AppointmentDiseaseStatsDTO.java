package giis.demo.service.appointment;

public class AppointmentDiseaseStatsDTO {

    private String diseaseName;   // e.g. "Influenza"
    private String bucketLabel;   // e.g. "2025-09-01", "2025-W36", "2025-09", "2025"
    private int count;            // number of appointments

    public AppointmentDiseaseStatsDTO(String diseaseName, String bucketLabel, int count) {
        this.diseaseName = diseaseName;
        this.bucketLabel = bucketLabel;
        this.count = count;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public String getBucketLabel() {
        return bucketLabel;
    }

    public int getCount() {
        return count;
    }
}
