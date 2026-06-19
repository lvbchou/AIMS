package com.aims.controller.auth;

import com.aims.dto.auth.LoginRequest;
import com.aims.dto.auth.LoginResponse;

import com.aims.service.auth.AuthService;

import com.aims.service.auth.IAuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}