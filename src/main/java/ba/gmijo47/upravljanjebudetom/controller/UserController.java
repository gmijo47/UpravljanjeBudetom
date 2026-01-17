package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.UserRepo;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "Endpoints for user retrieval")
public class UserController {

    @Autowired
    UserRepo userRepository;

    @Operation(summary = "Get current logged user", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public User getCurrentUser(HttpServletRequest request){
        Claims claims  = (Claims) request.getAttribute("claims");
        if (claims == null) {
            throw new RuntimeException("Invalid token or claims missing");
        }
        String email = claims.getSubject();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Operation(summary = "Get all users (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public Iterable<User> getAllUsers () {
        return userRepository.findAll();
    }
}
