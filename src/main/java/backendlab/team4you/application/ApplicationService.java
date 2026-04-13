package backendlab.team4you.application;


import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public void delete(Long id) {
        applicationRepository.deleteById(id);

    }

}

