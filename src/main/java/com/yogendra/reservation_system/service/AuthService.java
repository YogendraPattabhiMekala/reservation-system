package com.yogendra.reservation_system.service;

import com.yogendra.reservation_system.dto.RegisterRequest;
import com.yogendra.reservation_system.dto.LoginRequest;
import com.yogendra.reservation_system.dto.LoginResponse;

public interface AuthService {

    String register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}