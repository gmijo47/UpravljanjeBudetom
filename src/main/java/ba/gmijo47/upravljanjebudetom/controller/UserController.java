package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.services.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "Endpoints for user management and profile operations")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get current logged user",
            description = "Returns the profile information of the currently authenticated user based on JWT token",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user profile",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");

        if (claims == null) {
            return ResponseEntity.status(401).build();
        }

        String email = claims.getSubject();
        User user = userService.getCurrentUser(email);

        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Get all users (Admin only)",
            description = "Retrieves a list of all users in the system. Only accessible by users with ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of users",
                    content = @Content(
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(
                                    value = "[{\"id\": 1, \"name\": \"Marko\", \"lastname\": \"Petrović\", \"email\": \"marko@example.com\", \"dob\": \"1995-05-20\"}, {\"id\": 2, \"name\": \"Ana\", \"lastname\": \"Horvat\", \"email\": \"ana@example.com\", \"dob\": \"1998-03-15\"}]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have ADMIN role"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves detailed information about a specific user by their ID. Only accessible by ADMIN users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user",
                    content = @Content(
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"name\": \"Marko\", \"lastname\": \"Petrović\", \"email\": \"marko@example.com\", \"dob\": \"1995-05-20\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have ADMIN role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
            @PathVariable Long id
    ) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Update user profile",
            description = "Updates user profile information. Users can only update their own profile unless they have ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully updated",
                    content = @Content(
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"name\": \"Marko\", \"lastname\": \"Petrović\", \"email\": \"marko.novi@example.com\", \"dob\": \"1995-05-20\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User can only update their own profile"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID of the user to update", required = true, example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User object with updated fields (only include fields you want to update)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(
                                    value = "{\"name\": \"Marko\", \"lastname\": \"Petrović\", \"email\": \"marko.novi@example.com\", \"dob\": \"1995-05-20\"}"
                            )
                    )
            )
            @RequestBody User userDetails,

            HttpServletRequest request
    ) {
        Claims claims = (Claims) request.getAttribute("claims");
        String email = claims.getSubject();
        User currentUser = userService.getCurrentUser(email);

        User updatedUser = userService.updateUser(id, userDetails, Long.valueOf(currentUser.getId()));
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(
            summary = "Delete user (Admin only)",
            description = "Permanently deletes a user from the system. Only accessible by users with ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have ADMIN role"
            ),
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true, example = "1")
            @PathVariable Long id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
