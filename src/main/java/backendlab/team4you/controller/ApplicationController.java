package backendlab.team4you.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Arrays;
import java.util.List;

@Controller
public class ApplicationController {

    @GetMapping("/application")
    public String application(
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest
    ) {

        List<String> application = Arrays.asList(
                "Ärende #1 - Pågående",
                "Ärende #15 - Väntar på beslut"
        );

        model.addAttribute("application", application);
        model.addAttribute("userName", "Test user");

        if (htmxRequest != null) {
            return "fragments/application :: content";
        }

        return "application";
    }


}
