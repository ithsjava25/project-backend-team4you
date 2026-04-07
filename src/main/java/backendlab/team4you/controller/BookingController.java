package backendlab.team4you.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Arrays;
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
    public String bookings() {
        return "fragments/bookings :: content";
    }
}
