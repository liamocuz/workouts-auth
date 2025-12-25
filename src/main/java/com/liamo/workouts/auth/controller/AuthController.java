package com.liamo.workouts.auth.controller;

import com.liamo.workouts.auth.exception.UserAlreadyExistsException;
import com.liamo.workouts.auth.model.dto.CreateUserRequestDTO;
import com.liamo.workouts.auth.model.entity.UserVerification;
import com.liamo.workouts.auth.service.EmailService;
import com.liamo.workouts.auth.service.UserInfoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserInfoService userInfoService;
    private final EmailService emailService;

    public AuthController(UserInfoService userInfoService, EmailService emailService) {
        this.userInfoService = userInfoService;
        this.emailService = emailService;
    }

    @GetMapping("/signin")
    public String signin() {
        return "signin";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute(
            "createUserRequestDTO",
            new CreateUserRequestDTO("", "", "", "", "")
        );
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignupForm(
        @Valid @ModelAttribute CreateUserRequestDTO createUserRequestDTO,
        BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            UserVerification userVerification = userInfoService.registerNewLocalUser(createUserRequestDTO);
            emailService.sendRegistrationEmail(userVerification.getUserInfo().getEmail(), userVerification.getToken());
        } catch (UserAlreadyExistsException ex) {
            logger.error("UserAlreadyExistsException: {}", ex.getMessage(), ex);
        }

        return "signup-result";
    }

    @GetMapping("/resend-verification")
    public String resendVerification() {
        return "resend-verification";
    }

    @PostMapping("/resend-verification")
    public String processResendVerification(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        Optional<UserVerification> optionalUserVerification = userInfoService.createNewUserVerification(email);
        if (optionalUserVerification.isPresent()) {
            UserVerification userVerification = optionalUserVerification.get();
            emailService.sendRegistrationEmail(userVerification.getUserInfo().getEmail(), userVerification.getToken());
            redirectAttributes.addFlashAttribute("message", "Verification email has been resent. Please check your inbox.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Account is already verified or does not exist.");
        }

        return "redirect:/resend-verification";
    }

    @GetMapping("/verify")
    public String verifyUserEmail(
        @RequestParam(value = "token", required = false) String token,
        RedirectAttributes redirectAttributes
    ) {
        if (token == null || token.isBlank()) {
            redirectAttributes.addFlashAttribute("message", "Verification link is invalid. Please resend verification link.");
            return "redirect:/resend-verification";
        }
        boolean isValidToken = userInfoService.verifyUserEmail(token);
        if (isValidToken) {
            redirectAttributes.addFlashAttribute("message", "Email has been verified!");
            return "redirect:/signin";
        } else {
            redirectAttributes.addFlashAttribute(
                "message",
                "Account is already verified or link is invalid or expired. Please Sign In or resend verification link."
            );
            return "redirect:/resend-verification";
        }
    }
}