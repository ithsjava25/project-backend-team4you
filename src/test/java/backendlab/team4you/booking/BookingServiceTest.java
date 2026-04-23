package backendlab.team4you.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    BookingService bookingService;

    private BookingEntity booking;

    @BeforeEach
    void setUp() {
        booking = new BookingEntity();
        booking.setId(1L);
        booking.setName("Test User");
        booking.setEmail("test@test.com");
        booking.setStatus(BookingEnum.PENDING);
    }

    @Test
    void save_shouldReturnSavedBooking() {
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingEntity result = bookingService.save(booking);

        assertNotNull(result);
        assertEquals(booking.getName(), result.getName());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void findById_shouldReturnBooking_whenExists() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingEntity result = bookingService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldReturnNull_whenNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        BookingEntity result = bookingService.findById(99L);

        assertNull(result);
    }

    @Test
    void delete_shouldDeleteBooking_whenExists() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.delete(1L);

        verify(bookingRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookingService.delete(99L));
    }

    @Test
    void cancelBooking_shouldSetStatusToCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L);

        assertEquals(BookingEnum.CANCELLED, booking.getStatus());
    }

    @Test
    void cancelBooking_shouldThrowException_whenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookingService.cancelBooking(99L));
    }
}