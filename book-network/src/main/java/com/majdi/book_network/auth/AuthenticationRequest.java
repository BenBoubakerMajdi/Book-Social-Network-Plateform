package com.majdi.book_network.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {
    @Email(message = "Please check your Email format")
    private String email;
    @Size(min = 6, message = "Wrong password")
    private String password;
}
