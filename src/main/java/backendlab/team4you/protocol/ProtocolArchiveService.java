package backendlab.team4you.protocol;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordService;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.ProtocolNotFoundException;
import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProtocolArchiveService {

    private final ProtocolRepository protocolRepository;
    private final ProtocolPdfService protocolPdfService;
    private final CaseRecordService caseRecordService;
    private final CaseFileService caseFileService;

    public ProtocolArchiveService(
            ProtocolRepository protocolRepository,
            ProtocolPdfService protocolPdfService,
            CaseRecordService caseRecordService,
            CaseFileService caseFileService
    ) {
        this.protocolRepository = protocolRepository;
        this.protocolPdfService = protocolPdfService;
        this.caseRecordService = caseRecordService;
        this.caseFileService = caseFileService;
    }

    @Transactional
    public CaseFile archiveProtocolPdf(Long protocolId, UserEntity currentUser) {
        Protocol protocol = protocolRepository.findById(protocolId)
                .orElseThrow(() -> new ProtocolNotFoundException(protocolId));

        if (protocol.getArchivedPdfFile() != null) {
            return protocol.getArchivedPdfFile();
        }

        Meeting meeting = protocol.getMeeting();
        Registry registry = meeting.getRegistry();
        int year = meeting.getStartsAt().getYear();

        CaseRecord annualCase = caseRecordService.findOrCreateAnnualProtocolCase(
                registry,
                year,
                currentUser
        );

        byte[] pdfBytes = protocolPdfService.generatePdf(protocolId);

        String filename = "protokoll-"
                + registry.getCode().toLowerCase()
                + "-"
                + year
                + "-"
                + protocol.getId()
                + ".pdf";

        CaseFile archivedFile = caseFileService.uploadGeneratedFile(
                annualCase.getId(),
                filename,
                "application/pdf",
                pdfBytes,
                ConfidentialityLevel.OPEN,
                currentUser
        );

        protocol.setArchivedPdfFile(archivedFile);
        protocolRepository.save(protocol);

        return archivedFile;
    }
}
