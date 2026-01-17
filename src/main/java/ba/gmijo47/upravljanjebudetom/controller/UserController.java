package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.UserRepo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepo userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/user/me")
    public User getCurrentUser(HttpServletRequest request){
        Claims claims  = (Claims) request.getAttribute("claims");
        if (claims == null) {
            throw new RuntimeException("Invalid token");
        }
        String email = claims.getSubject();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/all")
    public Iterable<User> getAllUsers () {
        return userRepository.findAll();
    }
}
