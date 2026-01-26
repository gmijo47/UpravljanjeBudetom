package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login, Register and Refresh Token")
public class AuthController {

    @Autowired
    private AuthService authService;

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
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User savedUser = authService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (RuntimeException e) {
            // Ovdje hvatamo "User already exists" iz servisa
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
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

        try {
            String email = userData.get("email");
            String password = userData.get("password");
            Map<String, String> tokens = authService.login(email, password);
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Refresh Token")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @io.swagger.v3.oas.annotations.Parameter(description = "Refresh token UUID")
            @RequestParam String refreshToken) {

        try {
            Map<String, String> tokenMap = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(tokenMap);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }
}
