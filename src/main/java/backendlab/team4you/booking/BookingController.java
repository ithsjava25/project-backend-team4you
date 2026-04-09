package backendlab.team4you.booking;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BookingController {


    @GetMapping("/booking/application")
    public String bookingApplication() {
        return "fragments/booking-application :: content";
    }

    @GetMapping("/booking/search")
    public String search(
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmx
    ) {

        List<String> bookings = List.of(
                "Bokning #1 - 12 mars",
                "Bokning #2 - 15 april"
        );

        model.addAttribute("bookings", bookings);

        if (htmx != null) {
            return "fragments/booking-search :: content";
        }

        return "booking-search";
    }


    @GetMapping("/bookings")
    public String bookings(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmx
    ) {

        List<String> bookings = List.of(
                "Bokning " + (page * 3 + 1),
                "Bokning " + (page * 3 + 2),
                "Bokning " + (page * 3 + 3)
        );

        model.addAttribute("bookings", bookings);
        model.addAttribute("nextPage", page + 1);

        if (htmx != null) {
            return "fragments/booking-items :: list";
        }

        return "bookings";
    }

    @PostMapping("/booking/cancel")
    public String cancelBooking() {

        return "fragments/booking-cancelled :: content";
    }
}
