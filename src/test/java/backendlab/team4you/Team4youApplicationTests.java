package backendlab.team4you;

import backendlab.team4you.audit.AuditLogRepository;
import backendlab.team4you.audit.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class Team4youApplicationTests {

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @Test
    void contextLoads() {
    }

}