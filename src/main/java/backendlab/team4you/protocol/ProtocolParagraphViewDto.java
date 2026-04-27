package backendlab.team4you.protocol;

public record ProtocolParagraphViewDto(
        Long id,
        String heading,
        String caseNumber,
        boolean decisionRestricted,
        ProtocolDecisionType decisionType,
        String decisionLabel,
        String decisionText
) {
}
