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

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;

@Service
public class ProtocolService {

    private final ProtocolRepository protocolRepository;
    private final ProtocolParagraphSequenceRepository sequenceRepository;
    private final MeetingRepository meetingRepository;
    private final ProtocolParagraphRepository paragraphRepository;
    private static final int MAX_SEQUENCE_ALLOCATION_ATTEMPTS = 3;

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
                buildProtocolTitle(meeting, registry, year),
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

        return protocolRepository.save(protocol);
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

        ProtocolParagraph paragraph = paragraphRepository.findById(paragraphId)
                .orElseThrow(() -> new ProtocolParagraphNotFoundException(paragraphId));

        paragraph.updateDecision(decisionType, decisionText);

        return paragraph.getProtocol();
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
        for (int attempt = 1; attempt <= MAX_SEQUENCE_ALLOCATION_ATTEMPTS; attempt++) {
            try {
                ProtocolParagraphSequence sequence = sequenceRepository
                        .findByRegistryIdAndYear(registry.getId(), year)
                        .orElseGet(() -> new ProtocolParagraphSequence(registry, year, 0L));

                sequence.increment();
                sequenceRepository.saveAndFlush(sequence);

                return sequence.getLastValue();

            } catch (DataIntegrityViolationException exception) {
                if (attempt == MAX_SEQUENCE_ALLOCATION_ATTEMPTS) {
                    throw exception;
                }
            }
        }

        throw new IllegalStateException("Could not allocate protocol paragraph number.");
    }

    private String buildProtocolTitle(Meeting meeting, Registry registry, Integer year) {
        return "Protokoll - " + registry.getName() + " - " + year;
    }

    private String buildParagraphHeading(Long paragraphNumber, CaseRecord caseRecord) {
        return "§ " + paragraphNumber + " " + caseRecord.getTitle();
    }
}
