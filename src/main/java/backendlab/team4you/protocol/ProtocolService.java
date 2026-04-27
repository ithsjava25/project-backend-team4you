package backendlab.team4you.protocol;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.meeting.MeetingAgendaItem;
import backendlab.team4you.meeting.MeetingRepository;
import backendlab.team4you.meeting.MeetingStatus;
import backendlab.team4you.registry.Registry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;

@Service
public class ProtocolService {

    private final ProtocolRepository protocolRepository;
    private final ProtocolParagraphSequenceRepository sequenceRepository;
    private final MeetingRepository meetingRepository;
    private final ProtocolParagraphRepository paragraphRepository;

    public ProtocolService(
            ProtocolRepository protocolRepository,
            ProtocolParagraphSequenceRepository sequenceRepository,
            MeetingRepository meetingRepository,
            ProtocolParagraphRepository paragraphRepository
    ) {
        this.protocolRepository = protocolRepository;
        this.sequenceRepository = sequenceRepository;
        this.meetingRepository = meetingRepository;
        this.paragraphRepository = paragraphRepository;
    }

    @Transactional
    public Protocol createProtocolForCompletedMeeting(Long meetingId) {
        Objects.requireNonNull(meetingId, "meetingId is required");

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(meetingId));

        if (meeting.getStatus() != MeetingStatus.COMPLETED) {
            throw new InvalidMeetingStateException(
                    "Only completed meetings can have protocols. meetingId=" + meetingId
            );
        }

        if (protocolRepository.existsByMeetingId(meetingId)) {
            throw new ProtocolAlreadyExistsException(meetingId);
        }

        Registry registry = meeting.getRegistry();
        Integer year = meeting.getStartsAt().getYear();

        Protocol protocol = new Protocol(
                meeting,
                registry,
                buildProtocolTitle(registry, year),
                year
        );

        for (MeetingAgendaItem agendaItem : meeting.getAgendaItems()) {
            CaseRecord caseRecord = agendaItem.getCaseRecord();

            Long paragraphNumber = allocateNextParagraphNumber(registry, year);

            ProtocolParagraph paragraph = new ProtocolParagraph(
                    caseRecord,
                    paragraphNumber,
                    buildParagraphHeading(paragraphNumber, caseRecord)
            );

            protocol.addParagraph(paragraph);
        }

        try {
            return protocolRepository.save(protocol);
        } catch (DataIntegrityViolationException e) {
            throw new ProtocolAlreadyExistsException(meetingId);
        }
    }

    @Transactional(readOnly = true)
    public Protocol getProtocol(Long protocolId) {
        Objects.requireNonNull(protocolId, "protocolId is required");

        return protocolRepository.findById(protocolId)
                .orElseThrow(() -> new ProtocolNotFoundException(protocolId));
    }

    @Transactional(readOnly = true)
    public boolean protocolExistsForMeeting(Long meetingId) {
        Objects.requireNonNull(meetingId, "meetingId is required");
        return protocolRepository.existsByMeetingId(meetingId);
    }

    @Transactional
    public Protocol updateParagraphDecision(
            Long paragraphId,
            ProtocolDecisionType decisionType,
            String decisionText
    ) {
        Objects.requireNonNull(paragraphId, "paragraphId is required");
        Objects.requireNonNull(decisionType, "decisionType is required");

        ProtocolParagraph paragraph = paragraphRepository.findById(paragraphId)
                .orElseThrow(() -> new ProtocolParagraphNotFoundException(paragraphId));

        Protocol protocol = paragraph.getProtocol();

        if (protocol.getArchivedPdfFile() != null) {
            throw new ProtocolAlreadyArchivedException(protocol.getId());
        }

        paragraph.updateDecision(decisionType, decisionText);

        return protocol;
    }

    @Transactional(readOnly = true)
    public String buildDefaultDecisionText(Long paragraphId, ProtocolDecisionType decisionType) {
        Objects.requireNonNull(paragraphId, "paragraphId is required");
        Objects.requireNonNull(decisionType, "decisionType is required");

        ProtocolParagraph paragraph = paragraphRepository.findById(paragraphId)
                .orElseThrow(() -> new ProtocolParagraphNotFoundException(paragraphId));

        String registryName = paragraph.getProtocol().getRegistry().getName();

        return switch (decisionType) {
            case APPROVED -> registryName + " beslutar att bifalla ärendet.";
            case REJECTED -> registryName + " beslutar att avslå ärendet.";
        };
    }

    private Long allocateNextParagraphNumber(Registry registry, Integer year) {
        sequenceRepository.insertIfMissing(registry.getId(), year);

        ProtocolParagraphSequence sequence = sequenceRepository
                .findByRegistryIdAndYear(registry.getId(), year)
                .orElseThrow(() -> new IllegalStateException("Protocol paragraph sequence could not be initialized."));

        sequence.increment();
        sequenceRepository.saveAndFlush(sequence);

        return sequence.getLastValue();
    }

    private String buildProtocolTitle(Registry registry, Integer year) {
        return "Protokoll - " + registry.getName() + " - " + year;
    }

    private String buildParagraphHeading(Long paragraphNumber, CaseRecord caseRecord) {
        return "§ " + paragraphNumber + " " + caseRecord.getTitle();
    }
}
