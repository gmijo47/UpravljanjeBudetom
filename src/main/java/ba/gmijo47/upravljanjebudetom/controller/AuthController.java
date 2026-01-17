package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.auth.JWTUtil;
import ba.gmijo47.upravljanjebudetom.models.Role;
import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.RoleRepo;
import ba.gmijo47.upravljanjebudetom.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody User user){

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT).body(Map.of("message", "User already exists"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName("USER");
        user.getRoles().add(role);
        User savedUsr =  userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsr);
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String email, @RequestParam String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {

            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            String accessToken = jwtUtil.generateToken(user.getEmail(), roles);
            String refreshToken = jwtUtil.generateRefreshToken();
            user.setRefreshToken(refreshToken);
            userRepository.save(user);


            return ResponseEntity.ok(
                    Map.of("accessToken", accessToken,
                            "refreshToken", refreshToken)
            );
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken (@RequestParam String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));


        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), roles);
        return ResponseEntity.ok(
                Map.of("accessToken", newAccessToken)
        );
    }

}
