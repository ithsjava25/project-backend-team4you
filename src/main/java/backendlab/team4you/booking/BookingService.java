package backendlab.team4you.booking;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }


    @Transactional
    public void cancelBooking(String id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
    }

    @Transactional
    public void delete(Long id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        bookingRepository.deleteById(id);
    }
    @Transactional
    public BookingEntity save(BookingEntity bookingEntity){
        return bookingRepository.save(bookingEntity);
    }
    @Transactional
    public BookingEntity findById(Long id){
        return bookingRepository.findById(id).orElse(null);
    }

    public void deleteBooking(Long id, String username) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public void cancel(Long id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow();

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);
    }
}
