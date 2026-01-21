package ba.gmijo47.upravljanjebudetom.services;

import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepository;
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

   
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    
    @Transactional
    public User updateUser(Long id, User userDetails, Long currentUserId) {
        User currentUser = getUserById(currentUserId);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!currentUserId.equals(id) && !isAdmin) {
            throw new RuntimeException("You can only update your own profile");
        }

        User user = getUserById(id);

        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getLastname() != null) {
            user.setLastname(userDetails.getLastname());
        }
        if (userDetails.getEmail() != null) {
            if (!user.getEmail().equals(userDetails.getEmail()) &&
                    userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getDob() != null) {
            user.setDob(userDetails.getDob());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
