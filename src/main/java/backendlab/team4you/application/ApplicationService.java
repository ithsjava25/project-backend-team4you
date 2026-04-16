package backendlab.team4you.application;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
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


}

