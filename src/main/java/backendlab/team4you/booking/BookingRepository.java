package backendlab.team4you.booking;

import backendlab.team4you.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    Optional<BookingEntity> findById(String id);


    Page<BookingEntity> findAll(Pageable pageable);





    Page<BookingEntity> findByStatusContainingIgnoreCaseOrReferenceContainingIgnoreCase(
            String status,
            String reference,
            Pageable pageable
    );


}
