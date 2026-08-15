package com.expense_splitter.expense_splitter.controller;

import com.expense_splitter.expense_splitter.dto.LoginRequest;
import com.expense_splitter.expense_splitter.dto.SignupRequest;
import com.expense_splitter.expense_splitter.model.User;
import com.expense_splitter.expense_splitter.repository.UserRepository;
import com.expense_splitter.expense_splitter.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.expense_splitter.expense_splitter.dto.GoogleLoginRequest;
import com.expense_splitter.expense_splitter.service.EmailService;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "Welcome to SplitEasy!",
                "Hi " + user.getName() + ",\n\nYour account has been created successfully. Start splitting expenses with your friends!\n\n- SplitEasy Team");

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        User user = userOptional.get();

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!passwordMatches) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        emailService.sendEmail(user.getEmail(), "Login Notification - SplitEasy",
                "Hi " + user.getName() + ",\n\nYou just logged into your SplitEasy account.\n\n- SplitEasy Team");

        return ResponseEntity.ok(token);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) throws Exception {

        com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier =
                new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                        new com.google.api.client.http.javanet.NetHttpTransport(),
                        new com.google.api.client.json.gson.GsonFactory())
                        .setAudience(java.util.Collections.singletonList(
                                "866211342910-fhp7668n17655mcgtd1ghaqsegcvi300.apps.googleusercontent.com"))
                        .build();

        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(request.getIdToken());

        if(idToken==null){
            return ResponseEntity.status(401).body("Invalid Google token");
        }

        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = new User();
            user.setName(name != null ? name : email);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getEmail());

        if (userOptional.isPresent()) {
            emailService.sendEmail(user.getEmail(), "Login Notification - SplitEasy",
                    "Hi " + user.getName() + ",\n\nYou just logged into your SplitEasy account via Google.\n\n- SplitEasy Team");
        } else {
            emailService.sendEmail(user.getEmail(), "Welcome to SplitEasy!",
                    "Hi " + user.getName() + ",\n\nYour account has been created via Google Sign-In. Start splitting expenses with your friends!\n\n- SplitEasy Team");
        }

        return ResponseEntity.ok(token);
    }
}
