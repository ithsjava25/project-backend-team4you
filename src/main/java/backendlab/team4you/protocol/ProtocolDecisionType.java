package backendlab.team4you.protocol;

public enum ProtocolDecisionType {
    APPROVED("Bifall"),
    REJECTED("Avslag");

    private final String label;

    ProtocolDecisionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
