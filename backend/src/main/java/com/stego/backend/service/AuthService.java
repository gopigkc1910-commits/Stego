package com.stego.backend.service;

import com.stego.backend.dto.request.LoginRequest;
import com.stego.backend.dto.request.RegisterRequest;
import com.stego.backend.dto.response.AuthResponse;
import com.stego.backend.entity.RefreshToken;
import com.stego.backend.entity.User;
import com.stego.backend.enums.Role;
import com.stego.backend.exception.BadRequestException;
import com.stego.backend.exception.RateLimitExceededException;
import com.stego.backend.repository.UserRepository;
import com.stego.backend.security.JwtUtils;
import com.stego.backend.security.UserDetailsImpl;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    // Rate limiting: per-email buckets (10 attempts per minute)
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String key) {
        return loginBuckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        Role role = Role.ROLE_USER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("RESTAURANT_OWNER")) {
            role = Role.ROLE_RESTAURANT_OWNER;
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        user = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(jwt, refreshToken.getToken(), user.getId(),
                user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Bucket bucket = resolveBucket(request.getEmail());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Too many login attempts. Please try again later.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return new AuthResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                userDetails.getName(), userDetails.getEmail(), role);
    }

    public AuthResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromEmail(user.getEmail());
                    return new AuthResponse(token, requestRefreshToken, user.getId(),
                            user.getName(), user.getEmail(), user.getRole().name());
                })
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));
    }
}
