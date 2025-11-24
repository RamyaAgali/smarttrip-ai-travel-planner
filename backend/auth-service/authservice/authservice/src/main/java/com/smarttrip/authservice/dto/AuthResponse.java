package com.smarttrip.authservice.dto;

import com.smarttrip.authservice.model.AppUser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private AppUser user;
}
