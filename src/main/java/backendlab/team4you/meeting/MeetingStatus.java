package backendlab.team4you.meeting;

public enum MeetingStatus {

    PLANNED("Planerad"),
    PREPARING("Under förberedelse"),
    COMPLETED("Avslutad"),
    CANCELLED("Inställd");

    private final String label;

    MeetingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
