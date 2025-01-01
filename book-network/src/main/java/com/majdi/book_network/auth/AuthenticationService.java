package com.majdi.book_network.auth;


import com.majdi.book_network.email.EmailService;
import com.majdi.book_network.email.EmailTemplateName;
import com.majdi.book_network.role.RoleRepository;
import com.majdi.book_network.security.JwtService;
import com.majdi.book_network.user.Token;
import com.majdi.book_network.user.TokenRepository;
import com.majdi.book_network.user.User;
import com.majdi.book_network.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

//? Defining User authentication services
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;
    @Value("${application.mailing.frontend.activation_url}")
    private String activationUrl;
    private final PasswordEncoder passwordEncoder;


    //? Save the user in DB and send a verification code to the user's email
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

    //? Generating and persisting the activation code in DB
    private String generateAndSaveActivationCode(User user) {
        String generatedCode = generateActivationCode(6);
        var code = Token.builder()
                .token(generatedCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .user(user)
                .build();

        tokenRepository.save(code);
        return generatedCode;
    }

    //? Generating a 6 random numbers code
    private String generateActivationCode(int length) {
        String numbers = "0123456789";
        StringBuilder codBuilder = new StringBuilder();

        //! generates a secure integer using the numbers string
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            codBuilder.append(numbers.charAt(secureRandom.nextInt(numbers.length())));
        }

        return codBuilder.toString();
    }

    //? Authenticate a user (email, password)
    public AuthenticationResponse authenticate(@Valid AuthenticationRequest request) {
        //! handle user authentication
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        //! Generating the jwt Token
        var claims = new HashMap<String, Object>();
        var user = (User) auth.getPrincipal();
        claims.put("fullName", user.getFullName());
        var jwtToken = jwtService.generateToken(claims, user);

        //! Sending authentication Response
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    //? Activate a user's account
    public void activateAccount(String token) throws MessagingException {
        //! check if the token exist
        var savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid found"));

        //! check the token expiration date otherwise send validation email for new token
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new activation token has been sent to the user's email");
        }

        //! check if user exist with using the credentials extracted from the token
        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //! activating user's account, updating the token and persisting them in the DB
        user.setEnabled(true);
        savedToken.setValidatedAt(LocalDateTime.now());
        userRepository.save(user);
        tokenRepository.save(savedToken);

    }
}
