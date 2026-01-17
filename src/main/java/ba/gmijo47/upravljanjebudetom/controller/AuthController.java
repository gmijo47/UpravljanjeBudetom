package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.auth.JWTUtil;
import ba.gmijo47.upravljanjebudetom.models.Role;
import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.RoleRepo;
import ba.gmijo47.upravljanjebudetom.repos.UserRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login, Register and Refresh Token")
public class AuthController {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Register new user", description = "Creates a new user account")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = "{\"email\": \"admin@test.com\", \"password\": \"sifra123\", \"name\": \"Ime\", \"lastname\": \"Prezime\", \"dob\": \"1995-05-20\"}"
                    )
            )
    )
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody User user){

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT).body(Map.of("message", "User already exists"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName("ROLE_USER");
        if(role != null) user.getRoles().add(role);

        User savedUsr =  userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsr);
    }
    @Operation(
            summary = "Prijava korisnika",
            description = "Provjerava email i lozinku te vraća JWT access i refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uspješna prijava"),
            @ApiResponse(responseCode = "401", description = "Neispravni podaci")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    examples = @ExampleObject(
                            value = "{\"email\": \"admin@test.com\", \"password\": \"sifra123\"}"
                    )
            ))
            @RequestBody Map<String, String> userData) {

        String email = userData.get("email");
        String password = userData.get("password");

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

    @Operation(summary = "Refresh Token")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken (
            @io.swagger.v3.oas.annotations.Parameter(description = "Refresh token UUID")
            @RequestParam String refreshToken) {

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
