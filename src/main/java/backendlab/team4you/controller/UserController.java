package backendlab.team4you.controller;


import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import backendlab.team4you.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;

    }



    @GetMapping("/")
    public String homepage(){
        return "home";
    }




    @GetMapping("/user/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {
        UserEntity user = userService.findById(id);

        if (user == null) {
            throw new UserNotFoundException("Not found");
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/user/update/{id}")
    public String redirectToEdit(@PathVariable String id) {
        return "redirect:/user/edit/" + id;
    }




}
