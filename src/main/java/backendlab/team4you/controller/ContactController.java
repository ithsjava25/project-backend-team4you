package backendlab.team4you.controller;

import backendlab.team4you.contact.ContactFormDTO;
import backendlab.team4you.contact.ContactMessage;
import backendlab.team4you.contact.ContactRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @GetMapping("/contact")
    public String contact(Model model, @RequestParam(required = false) String submit) {

        model.addAttribute("contactForm", new ContactFormDTO("", "", "", "", ""));
        model.addAttribute("activePage", "contact");
        model.addAttribute("submit", submit);

        return "contact";
    }

    @PostMapping("/contact/send")
    public String receiveMessage(
            @Valid @ModelAttribute("contactForm") ContactFormDTO form,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("contactForm", form);
            return "fragments/contact-form :: form";
        }

        ContactMessage msg = new ContactMessage();
        msg.setFirstName(form.firstName());
        msg.setLastName(form.lastName());
        msg.setEmail(form.email());
        msg.setPhone(form.phone());
        msg.setMessage(form.message());

        contactRepository.save(msg);

        return "fragments/contact-success :: message";
    }




}
