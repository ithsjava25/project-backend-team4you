package backendlab.team4you.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, String> {
    Optional<BookingEntity> findById(String id);
}
