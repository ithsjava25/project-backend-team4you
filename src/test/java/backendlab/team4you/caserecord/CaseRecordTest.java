package backendlab.team4you.caserecord;

import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.webauthn.api.Bytes;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class CaseRecordTest {

    @Test
    @DisplayName("should trim title and default status and confidentiality level")
    void shouldTrimTitleAndApplyDefaults() {
        Registry registry = new Registry("Kommunstyrelsen", "KS");

        UserEntity owner = new UserEntity();
        owner.setId(1L);
        owner.setUsername("owner@example.com");
        owner.setDisplayName("Owner");

        UserEntity assignedUser = new UserEntity();
        assignedUser.setId(2L);
        assignedUser.setUsername("assigned@example.com");
        assignedUser.setDisplayName("Assigned");

        CaseRecord caseRecord = new CaseRecord(
                registry,
                "  test title  ",
                "description",
                "   ",
                owner,
                assignedUser,
                "   ",
                (ZonedDateTime) null
        );

        assertThat(caseRecord.getTitle()).isEqualTo("test title");
        assertThat(caseRecord.getStatus()).isEqualTo("OPEN");
        assertThat(caseRecord.getConfidentialityLevel()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("should throw when title is blank")
    void shouldThrowWhenTitleIsBlank() {
        Registry registry = new Registry("Kommunstyrelsen", "KS");
        UserEntity owner = new UserEntity();
        owner.setId(1L);
        owner.setUsername("owner@example.com");
        owner.setDisplayName("Owner");

        UserEntity assignedUser = new UserEntity();
        assignedUser.setId(2L);
        assignedUser.setUsername("assigned@example.com");
        assignedUser.setDisplayName("Assigned");


        assertThatThrownBy(() -> new CaseRecord(
                registry,
                "   ",
                "description",
                "OPEN",
                owner,
                assignedUser,
                "OPEN",
                (ZonedDateTime) null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title is required");
    }

    @Test
    @DisplayName("should not allow case number to be set more than once")
    void shouldNotAllowCaseNumberToBeSetMoreThanOnce() {
        Registry registry = new Registry("Kommunstyrelsen", "KS");
        UserEntity owner = new UserEntity();
        owner.setId(1L);
        owner.setUsername("owner@example.com");
        owner.setDisplayName("Owner");

        UserEntity assignedUser = new UserEntity();
        assignedUser.setId(2L);
        assignedUser.setUsername("assigned@example.com");
        assignedUser.setDisplayName("Assigned");


        CaseRecord caseRecord = new CaseRecord(
                registry,
                "test title",
                "description",
                "OPEN",
                owner,
                assignedUser,
                "OPEN",
                (ZonedDateTime)  null
        );

        caseRecord.setCaseNumber("KS26-1");

        assertThatThrownBy(() -> caseRecord.setCaseNumber("KS26-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("caseNumber is immutable once set");
    }
}
