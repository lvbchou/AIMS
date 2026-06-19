package com.aims.service.auth;

import com.aims.dto.auth.LoginRequest;
import com.aims.dto.auth.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
}
