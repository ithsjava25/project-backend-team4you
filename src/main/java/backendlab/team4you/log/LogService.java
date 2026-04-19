package backendlab.team4you.log;


import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void log(String action, String user, String details) {
        LogEntity log = new LogEntity();
        log.setAction(action);
        log.setPerformedBy(user);
        log.setDetails(details);
        log.setCreatedAt(ZonedDateTime.now());

        logRepository.save(log);
    }

    public void delete(String log) {

    }
}
