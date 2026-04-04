package backendlab.team4you.webauthn;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookingController {

    @GetMapping("/booking/application")
    public String bookingApplication() {
        return "fragments/booking-application :: content";
    }

    @GetMapping("/booking/search")
    public String bookingSearch() {
        return "fragments/booking-search :: content";
    }
}
