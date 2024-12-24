package com.majdi.book_network.auth;


import com.majdi.book_network.role.RoleRepository;
import com.majdi.book_network.user.Token;
import com.majdi.book_network.user.TokenRepository;
import com.majdi.book_network.user.User;
import com.majdi.book_network.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

//? Defining User authentication methodes
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    public void register(RegisterationRequest request) {


        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("User roles are not initialized"));

        //! Building the User Object
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        //! Saving the User
        userRepository.save(user);

        //! Sending validation Email
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) {
        var code = generateAndSaveActivationCode(user);
    }

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

    private String generateActivationCode(int length) {
        String numbers = "0123456789";
        StringBuilder codBuilder = new StringBuilder();

        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            codBuilder.append(numbers.charAt(secureRandom.nextInt(numbers.length())));
        }

        return codBuilder.toString();
    }
}
