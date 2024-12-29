package com.majdi.book_network.auth;


import com.majdi.book_network.email.EmailService;
import com.majdi.book_network.email.EmailTemplateName;
import com.majdi.book_network.role.RoleRepository;
import com.majdi.book_network.user.Token;
import com.majdi.book_network.user.TokenRepository;
import com.majdi.book_network.user.User;
import com.majdi.book_network.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

//? Defining User authentication services
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    @Value("${application.mailing.frontend.activation_url}")
    private String activationUrl;

    //? Save the user in DB and send a verification code
    public void register(RegisterationRequest request) throws MessagingException {

        //! if user exist get the role or initialize it later
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("User roles are not initialized"));

        //! Building the User Object using Builder Design Pattern
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))    //! encrypt the password
                .accountLocked(false)
                .enabled(false)                 //! the account should not be enabled until the user activate his account
                .roles(List.of(userRole))
                .build();

        //! Persist the User in the db
        userRepository.save(user);

        //! Sending validation Email
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var code = generateAndSaveActivationCode(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                code,
                "Account Activation"
        );
    }

    //? Saving activation code
    private String generateAndSaveActivationCode(User user) {
        String generatedCode = generateActivationCode(6);
        var code = Token.builder()
                .token(generatedCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .user(user)
                .build();

        //! Persist the token in the db
        tokenRepository.save(code);
        return generatedCode;
    }

    //? Generating a 6 random numbers code
    private String generateActivationCode(int length) {
        String numbers = "0123456789";
        StringBuilder codBuilder = new StringBuilder();

        //! generates a secure integer using the numbers
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            codBuilder.append(numbers.charAt(secureRandom.nextInt(numbers.length())));
        }

        return codBuilder.toString();
    }
}
