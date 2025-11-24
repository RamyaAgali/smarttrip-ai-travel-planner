package com.smarttrip.authservice.service;

import com.smarttrip.authservice.model.AppUser;
import com.smarttrip.authservice.model.PasswordResetToken;
import com.smarttrip.authservice.repository.PasswordResetTokenRepository;
import com.smarttrip.authservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.smarttrip.authservice.util.PasswordValidator;
import org.springframework.transaction.annotation.Transactional;


import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    public AppUser registerUser(AppUser user) {
        if (!PasswordValidator.isValid(user.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters, include uppercase, lowercase, number, and special character.");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    public Optional<AppUser> loginUser(String email, String password) {
        Optional<AppUser> user = userRepository.findByEmail(email);
        if (user.isPresent() && encoder.matches(password, user.get().getPassword())) {
            return user;
        }
        return Optional.empty();
    }

    // ✅ Add this method
    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
 // 🔹 Forgot password: generate token
    public String createPasswordResetToken(String email) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() ->
                new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1); // valid for 1 hour

        PasswordResetToken resetToken = new PasswordResetToken(token, user, calendar.getTime());
        tokenRepository.save(resetToken);

        // Later: send this link by email
        return "http://localhost:5173/reset-password?token=" + token;
    }

 // 🔹 Reset password
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Token expired");
        }

        // ✅ Check password strength
        if (!PasswordValidator.isValid(newPassword)) {
            throw new RuntimeException("Password must be at least 8 characters, include uppercase, lowercase, number, and special character.");
        }

        AppUser user = resetToken.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }


}
