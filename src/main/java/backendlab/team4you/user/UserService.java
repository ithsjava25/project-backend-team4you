package backendlab.team4you.user;

import backendlab.team4you.repository.UserRepository;
import groovy.util.logging.Slf4j;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.util.List;

@Slf4j
@Service
public class UserService {


    UserRepository userRepository;

    public UserService(UserRepository userRepository){


        this.userRepository = userRepository;
    }


    @Transactional
    public void save(UserEntity userEntity){
        userRepository.save(userEntity);
    }

    @Transactional
    public List<UserEntity> findAll(){
        return userRepository.findAll();
    }

    @Transactional
    public UserEntity findById(String id){
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteById(String id){
        if(userRepository.findById(id).isEmpty()){
            throw new RuntimeException("User not found"); // tills vi har globalexception
        }
        userRepository.deleteById(id);
    }
    public UserEntity update(UserEntity userEntity){
        return userRepository.save(userEntity);
    }



}
