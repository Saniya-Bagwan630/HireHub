package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.entity.UsersType;
import com.HireHub.hirehub.services.UsersService;
import com.HireHub.hirehub.services.UsersTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class UsersController {

    private final UsersTypeService usersTypeService;
    private final UsersService usersService;

    @Autowired
    public UsersController(UsersTypeService usersTypeService, UsersService usersService) {
        this.usersTypeService = usersTypeService;
        this.usersService = usersService;
    }

    @GetMapping("/register")
    public String register(Model model) {
        List<UsersType> usersTypes = usersTypeService.getAll();
        model.addAttribute("getAllTypes", usersTypes);
        model.addAttribute("user", new Users());
        return "register";
    }

    @PostMapping("/register/new")
    public String userRegistration(@Valid Users users,
                                   @RequestParam(value = "firstName", required = false) String firstName,
                                   @RequestParam(value = "lastName", required = false) String lastName,
                                   @RequestParam(value = "city", required = false) String city,
                                   @RequestParam(value = "state", required = false) String state,
                                   @RequestParam(value = "country", required = false) String country,
                                   @RequestParam(value = "company", required = false) String company,
                                   @RequestParam(value = "workAuthorization", required = false) String workAuthorization,
                                   @RequestParam(value = "employmentType", required = false) String employmentType,
                                   Model model) {

        if (users.getEmail() == null || users.getEmail().isBlank() || !users.getEmail().contains("@")) {
            model.addAttribute("error", "Please provide a valid email address.");
            populateRegisterModel(model, users, firstName, lastName, city, state, country, company, workAuthorization, employmentType);
            return "register";
        }

        if (users.getPassword() == null || users.getPassword().trim().length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long.");
            populateRegisterModel(model, users, firstName, lastName, city, state, country, company, workAuthorization, employmentType);
            return "register";
        }

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            model.addAttribute("error", "First name and Last name are required.");
            populateRegisterModel(model, users, firstName, lastName, city, state, country, company, workAuthorization, employmentType);
            return "register";
        }

        Optional<Users> optionalUsers = usersService.getUserByEmail(users.getEmail());
        if (optionalUsers.isPresent()) {
            model.addAttribute("error", "Email already registered,try to login or register with other email.");
            populateRegisterModel(model, users, firstName, lastName, city, state, country, company, workAuthorization, employmentType);
            return "register";
        }

        usersService.addNew(users, firstName, lastName, city, state, country, company, workAuthorization, employmentType);
        return "redirect:/dashboard/";
    }

    private void populateRegisterModel(Model model, Users users, String firstName, String lastName, String city,
                                       String state, String country, String company, String workAuthorization,
                                       String employmentType) {
        List<UsersType> usersTypes = usersTypeService.getAll();
        model.addAttribute("getAllTypes", usersTypes);
        model.addAttribute("user", users);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("city", city);
        model.addAttribute("state", state);
        model.addAttribute("country", country);
        model.addAttribute("company", company);
        model.addAttribute("workAuthorization", workAuthorization);
        model.addAttribute("employmentType", employmentType);
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:/";
    }
}