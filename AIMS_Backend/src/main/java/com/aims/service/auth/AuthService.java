package com.aims.service.auth;

import com.aims.dto.auth.LoginRequest;
import com.aims.dto.auth.LoginResponse;
import com.aims.security.jwt.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        System.out.println("USERNAME = " + request.getUsername());
        System.out.println("PASSWORD = " + request.getPassword());
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(request.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        return new LoginResponse(token, request.getUsername(), roles);
    }
}