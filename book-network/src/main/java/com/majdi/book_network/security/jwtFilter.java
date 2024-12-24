package com.majdi.book_network.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


//? Defining JWT Filter that will be integrated in the Spring Security Filter Chain
@Service
@RequiredArgsConstructor
//! OncePerRequestFilter : provides a mechanism for ensuring that a filter is applied only once per request.
public class jwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        //! no need for applying the JWT Filter because the user is visiting an authentication route (Login/ Signup)
        if (request.getServletPath().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwt;
        final String userEmail;

        //! Testing : Authorization Header && Starts with "Bearer ....."
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //! Extracting the JWT From the String "Bearer ....."
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        //! Testing :  UserEmail && access details about the currently authenticated user
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //! Retrieve User Details from the database
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            //! Compare the jwt with the user details
            if (jwtService.isTokenValid(jwt, userDetails)) {
                //! Create an authToken that contain the user details and authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        userDetails.getAuthorities()
                );

                //! setting the details of the Token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                //! Storing the Token in the Security Context so that the user is consdered authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
