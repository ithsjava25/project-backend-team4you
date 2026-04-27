package backendlab.team4you.protocol;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordService;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.ProtocolNotFoundException;
import backendlab.team4you.exceptions.ProtocolNotReadyForPdfException;
import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.meeting.MeetingStatus;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProtocolArchiveServiceTest {

    @Mock
    private ProtocolRepository protocolRepository;

    @Mock
    private ProtocolPdfService protocolPdfService;

    @Mock
    private CaseRecordService caseRecordService;

    @Mock
    private CaseFileService caseFileService;

    @InjectMocks
    private ProtocolArchiveService protocolArchiveService;

    private Protocol protocol;
    private Meeting meeting;
    private Registry registry;
    private CaseRecord annualCase;
    private UserEntity currentUser;

    @BeforeEach
    void setUp() {
        registry = new Registry("Kommunstyrelsen", "KS");
        setField(registry, "id", 1L);

        meeting = new Meeting(
                registry,
                "KS april",
                LocalDateTime.of(2026, 4, 27, 13, 0),
                LocalDateTime.of(2026, 4, 27, 15, 0),
                "Sessionssalen",
                MeetingStatus.COMPLETED,
                null
        );

        protocol = new Protocol(meeting, registry, "Protokoll - Kommunstyrelsen - 2026", 2026);
        setField(protocol, "id", 1L);

        annualCase = mock(CaseRecord.class);
        currentUser = mock(UserEntity.class);
    }

    @Test
    @DisplayName("archiveProtocolPdf should throw when protocol does not exist")
    void archiveProtocolPdf_shouldThrow_whenProtocolNotFound() {
        when(protocolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> protocolArchiveService.archiveProtocolPdf(1L, currentUser))
                .isInstanceOf(ProtocolNotFoundException.class);
    }

    @Test
    @DisplayName("archiveProtocolPdf should return existing file when already archived")
    void archiveProtocolPdf_shouldReturnExistingFile_whenAlreadyArchived() {
        CaseFile existingFile = mock(CaseFile.class);
        protocol.setArchivedPdfFile(existingFile);

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        CaseFile result = protocolArchiveService.archiveProtocolPdf(1L, currentUser);

        assertThat(result).isSameAs(existingFile);

        verifyNoInteractions(protocolPdfService);
        verifyNoInteractions(caseRecordService);
        verifyNoInteractions(caseFileService);
    }

    @Test
    @DisplayName("archiveProtocolPdf should throw when protocol is not ready")
    void archiveProtocolPdf_shouldThrow_whenProtocolNotReady() {
        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        assertThatThrownBy(() -> protocolArchiveService.archiveProtocolPdf(1L, currentUser))
                .isInstanceOf(ProtocolNotReadyForPdfException.class);
    }

    @Test
    @DisplayName("archiveProtocolPdf should archive pdf when protocol is ready")
    void archiveProtocolPdf_shouldArchivePdf_whenReady() {
        CaseRecord caseRecord = mock(CaseRecord.class);

        ProtocolParagraph paragraph = new ProtocolParagraph(
                caseRecord,
                1L,
                "§ 1 Ärende"
        );
        paragraph.updateDecision(
                ProtocolDecisionType.APPROVED,
                "Kommunstyrelsen beslutar att bifalla ärendet."
        );
        protocol.addParagraph(paragraph);

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        when(caseRecordService.findOrCreateAnnualProtocolCase(eq(registry), eq(2026), eq(currentUser)))
                .thenReturn(annualCase);

        when(annualCase.getId()).thenReturn(20L);

        byte[] pdfBytes = "pdf".getBytes();
        when(protocolPdfService.generatePdf(eq(1L), any(UserEntity.class)))
                .thenReturn(pdfBytes);

        CaseFile savedFile = mock(CaseFile.class);
        when(caseFileService.uploadGeneratedFile(
                eq(20L),
                eq("protokoll-ks-2026-1.pdf"),
                eq("application/pdf"),
                eq(pdfBytes),
                eq(ConfidentialityLevel.OPEN),
                eq(currentUser)
        )).thenReturn(savedFile);

        CaseFile result = protocolArchiveService.archiveProtocolPdf(1L, currentUser);

        assertThat(result).isSameAs(savedFile);
        assertThat(protocol.getArchivedPdfFile()).isSameAs(savedFile);

        verify(protocolRepository).save(protocol);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
