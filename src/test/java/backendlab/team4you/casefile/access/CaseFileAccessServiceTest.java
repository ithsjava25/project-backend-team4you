package backendlab.team4you.casefile.access;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.casefile.FileConfidentialityLevel;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseFileAccessServiceTest {

    @Mock
    private CaseFileAccessRepository caseFileAccessRepository;

    private CaseFileAccessService caseFileAccessService;

    private CaseRecord caseRecord;
    private CaseFile openFile;
    private CaseFile confidentialFile;

    @BeforeEach
    void setUp() {
        caseFileAccessService = new CaseFileAccessService(caseFileAccessRepository);

        caseRecord = new CaseRecord();
        caseRecord.setId(1L);

        openFile = new CaseFile();
        openFile.setCaseRecord(caseRecord);
        openFile.setConfidentialityLevel(FileConfidentialityLevel.OPEN);

        confidentialFile = new CaseFile();
        confidentialFile.setCaseRecord(caseRecord);
        confidentialFile.setConfidentialityLevel(FileConfidentialityLevel.CONFIDENTIAL);
    }

    @Test
    @DisplayName("canViewFile should return true for open file when user is null")
    void canViewFile_shouldReturnTrue_forOpenFileWhenUserIsNull() {
        boolean result = caseFileAccessService.canViewFile(null, openFile);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canViewFile should return false for confidential file when user is null")
    void canViewFile_shouldReturnFalse_forConfidentialFileWhenUserIsNull() {
        boolean result = caseFileAccessService.canViewFile(null, confidentialFile);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canViewFile should return true for confidential file when user is admin")
    void canViewFile_shouldReturnTrue_forConfidentialFileWhenUserIsAdmin() {
        UserEntity admin = new UserEntity();
        admin.setRole(UserRole.ADMIN);
        admin.setId(org.springframework.security.web.webauthn.api.Bytes.fromBase64("dGVzdA"));

        boolean result = caseFileAccessService.canViewFile(admin, confidentialFile);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canViewFile should return true for confidential file when user has case access")
    void canViewFile_shouldReturnTrue_forConfidentialFileWhenUserHasCaseAccess() {
        UserEntity user = new UserEntity();
        user.setRole(UserRole.USER);
        user.setId(org.springframework.security.web.webauthn.api.Bytes.fromBase64("dXNlcjE"));

        when(caseFileAccessRepository.existsByCaseRecordIdAndUserIdAndCanViewConfidentialFilesTrue(
                1L, user.getIdAsString()
        )).thenReturn(true);

        boolean result = caseFileAccessService.canViewFile(user, confidentialFile);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canViewFile should return false for confidential file when user lacks case access")
    void canViewFile_shouldReturnFalse_forConfidentialFileWhenUserLacksCaseAccess() {
        UserEntity user = new UserEntity();
        user.setRole(UserRole.USER);
        user.setId(org.springframework.security.web.webauthn.api.Bytes.fromBase64("dXNlcjI"));

        when(caseFileAccessRepository.existsByCaseRecordIdAndUserIdAndCanViewConfidentialFilesTrue(
                1L, user.getIdAsString()
        )).thenReturn(false);

        boolean result = caseFileAccessService.canViewFile(user, confidentialFile);

        assertThat(result).isFalse();
    }
}
