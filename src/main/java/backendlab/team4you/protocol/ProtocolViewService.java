package backendlab.team4you.protocol;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.ProtocolNotFoundException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProtocolViewService {
    private final ProtocolRepository protocolRepository;

    public ProtocolViewService(ProtocolRepository protocolRepository) {
        this.protocolRepository = protocolRepository;
    }
    public List<ProtocolParagraphViewDto> getParagraphsForViewer(Long protocolId, UserEntity viewer) {
        Protocol protocol = protocolRepository.findById(protocolId)
                .orElseThrow(() -> new ProtocolNotFoundException(protocolId));

        return protocol.getParagraphs().stream()
                .map(paragraph -> toViewDto(paragraph, viewer))
                .toList();
    }

    private ProtocolParagraphViewDto toViewDto(ProtocolParagraph paragraph, UserEntity viewer) {
        CaseRecord caseRecord = paragraph.getCaseRecord();

        boolean restricted = caseRecord.getConfidentialityLevel() == ConfidentialityLevel.CONFIDENTIAL;
        boolean canViewDecision = !restricted
                || isAdmin(viewer)
                || isAssignedCaseOfficer(viewer, caseRecord);

        return new ProtocolParagraphViewDto(
                paragraph.getId(),
                paragraph.getHeading(),
                caseRecord.getCaseNumber(),
                restricted && !canViewDecision,
                canViewDecision && paragraph.getDecisionType() != null
                        ? paragraph.getDecisionType().getLabel()
                        : null,
                canViewDecision
                        ? paragraph.getDecisionText()
                        : "Beslutet omfattas av sekretess."
        );
    }

    private boolean isAdmin(UserEntity viewer) {
        return viewer != null && viewer.getRole() == UserRole.ADMIN;
    }

    private boolean isAssignedCaseOfficer(UserEntity viewer, CaseRecord caseRecord) {
        return viewer != null
                && viewer.getRole() == UserRole.CASE_OFFICER
                && caseRecord.getAssignedUser() != null
                && caseRecord.getAssignedUser().getIdAsString().equals(viewer.getIdAsString());
    }
}
