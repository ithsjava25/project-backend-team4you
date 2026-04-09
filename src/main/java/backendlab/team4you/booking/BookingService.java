package backendlab.team4you.booking;


import org.springframework.stereotype.Service;

@Service
public class BookingService {

    BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
}
