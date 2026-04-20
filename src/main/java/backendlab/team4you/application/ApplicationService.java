package backendlab.team4you.application;


import backendlab.team4you.booking.BookingEntity;
import backendlab.team4you.booking.BookingStatus;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApplicationStatus status = ApplicationStatus.REJECTED;


    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;

    }

    public void delete(Long id) {
        applicationRepository.deleteById(id);

    }

    public void save(ApplicationEntity applicationEntity) {
        applicationRepository.save(applicationEntity);

    }

    public ApplicationEntity findById(Long id) {
        return applicationRepository.findById(id).orElse(null);


    }

    public List<ApplicationEntity> getAll() {
        return applicationRepository.findAll();

    }

    public void extendApplication(Long id, String username) {

        ApplicationEntity app = applicationRepository.findById(id)

                .orElseThrow();

        if (!app.getOwner().getUsername().equals(username)) {
            throw new IllegalStateException("Du får inte förlänga denna ansökan");
        }

        app.setUpdatedAt(ZonedDateTime.now(ZoneId.of("Europe/Stockholm")));
        app.setStatus(ApplicationStatus.EXTENDED);



        applicationRepository.save(app);
    }

    public void createApplication(ApplicationForm form, String username){
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow();

        ApplicationEntity app = new ApplicationEntity();
        app.setTitle(form.getTitle());
        app.setDescription(form.getDescription());

        app.setOwner(user);
        app.setStatus(ApplicationStatus.PENDING);


        app.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Stockholm")));


        System.out.println("FORM TITLE: " + form.getTitle());
        applicationRepository.save(app);


    }
    public List<ApplicationEntity> getByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow();

        return applicationRepository.findByOwner(user);



    }

    public List<ApplicationEntity> getByUsernameAndStatus(String username, String cancelled) {
        return applicationRepository.findByOwnerUsernameAndStatus(username, status);
    }

    public void cancel(Long id) {
        ApplicationEntity app = applicationRepository.findById(id)
                .orElseThrow();

        app.setStatus(ApplicationStatus.CANCELLED);

        applicationRepository.save(app);
    }
}

