package backendlab.team4you.controller;


import backendlab.team4you.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private final UserService userService;

    public SearchController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/search")
    public String search(
            @RequestParam String searchTerm,
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmx
    ) {

        model.addAttribute("results", userService.search(searchTerm));

        if (htmx != null) {
            return "fragments/search-results :: content";
        }

        return "search";
    }
}
