package com.example.orderflow.auth;

import com.example.orderflow.auth.dto.FirebaseLoginRequest;
import com.example.orderflow.auth.dto.LoginRequest;
import com.example.orderflow.auth.dto.LoginResponse;
import com.example.orderflow.firebase.FirebaseTokenVerifier;
import com.example.orderflow.firebase.FirebaseUser;
import com.example.orderflow.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, jwtService.getExpirationSeconds());
    }

    @PostMapping("/firebase")
    public LoginResponse firebaseLogin(@Valid @RequestBody FirebaseLoginRequest request) {
        FirebaseUser firebaseUser = firebaseTokenVerifier.verify(request.idToken());
        String subject = firebaseUser.email() != null ? firebaseUser.email() : firebaseUser.uid();
        String token = jwtService.generateToken(subject, List.of("ROLE_USER"));
        return new LoginResponse(token, jwtService.getExpirationSeconds());
    }
}
