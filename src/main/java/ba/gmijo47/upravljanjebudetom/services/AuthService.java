package ba.gmijo47.upravljanjebudetom.services;

import ba.gmijo47.upravljanjebudetom.auth.JWTUtil;
import ba.gmijo47.upravljanjebudetom.models.Role;
import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.RoleRepo;
import ba.gmijo47.upravljanjebudetom.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_USER");
        if (role != null) user.getRoles().add(role);

        return userRepository.save(user);
    }

    public Map<String, String> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            String accessToken = jwtUtil.generateToken(user.getEmail(), roles);
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            // Spremanje refresh tokena u bazu
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            );
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), roles);

        return Map.of("accessToken", newAccessToken);
    }
}
