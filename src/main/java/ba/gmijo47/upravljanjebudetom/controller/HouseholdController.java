package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.models.Household;
import ba.gmijo47.upravljanjebudetom.services.HouseholdService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/household")
@RequiredArgsConstructor
@Tag(name = "Household Management", description = "Upravljanje kućanstvima, članovima")
@SecurityRequirement(name = "bearerAuth")
public class HouseholdController {

    private final HouseholdService householdService;
    private final UserService userService;

    @Operation(summary = "Create new household", description = "Kreira novo kućanstvo i postavlja kreatora kao ROLE_OWNER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Household successfully created",
                    content = @Content(schema = @Schema(implementation = Household.class))),
            @ApiResponse(responseCode = "400", description = "User already belongs to a household or name exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/create")
    public ResponseEntity<Household> createHousehold(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Podaci o kućanstvu (samo name je obavezan)", required = true,
                    content = @Content(
                            schema = @Schema(implementation = Household.class),
                            examples = @ExampleObject(value = "{\"name\": \"Moje Kućanstvo\"}")
                    )
            )
            @RequestBody Household household,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(householdService.createHousehold(household, userId));
    }

    @Operation(summary = "Get my household", description = "Vraća kućanstvo kojem pripada trenutno ulogirani korisnik.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found household",
                    content = @Content(schema = @Schema(implementation = Household.class))),
            @ApiResponse(responseCode = "404", description = "User is not a member of any household")
    })
    @GetMapping("/my")
    public ResponseEntity<Household> getMyHousehold(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(householdService.getMyHousehold(userId));
    }

    @Operation(summary = "Get all households (ADMIN only)", description = "Vraća listu svih kućanstava u sustavu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not ADMIN")
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Household>> getAll() {
        return ResponseEntity.ok(householdService.getAllHouseholds());
    }

    @Operation(summary = "Update household", description = "Ažurira naziv kućanstva. Dozvoljeno samo VLASNIKU (Owner) ili ADMINU.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update successful"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not owner or admin"),
            @ApiResponse(responseCode = "404", description = "Household not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Household> update(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novo ime kućanstva",
                    content = @Content(
                            schema = @Schema(implementation = Household.class),
                            examples = @ExampleObject(value = "{\"name\": \"Novo Ime\"}")
                    )
            )
            @RequestBody Household details,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return ResponseEntity.ok(householdService.updateHousehold(id, details, userId));

    }

    @Operation(summary = "Add member", description = "Dodaje postojećeg korisnika u kućanstvo. Korisnik ne smije već biti u nekom kućanstvu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member added successfully"),
            @ApiResponse(responseCode = "400", description = "User already in a household"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only members/admin can add others")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping(value = {
            "/{householdId}/add/{userId}"
    })
    public ResponseEntity<Void> addMember(
            @PathVariable(required = false) Long householdId,
            @Parameter(description = "ID korisnika kojeg dodajemo") @PathVariable Long userId,
            HttpServletRequest request) {

        Long currentUserId = getUserId(request);
        householdService.addMember(householdId, userId, currentUserId);

        throw new ResponseStatusException(HttpStatus.OK, "User has been successfully added to household.");
    }

    @Operation(summary = "Kick member (Owner/Admin only)", description = "Izbacuje korisnika iz kućanstva. Ovu akciju mogu izvesti samo VLASNIK (Owner) ili ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully kicked"),
            @ApiResponse(responseCode = "400", description = "Cannot kick owner or user not in household"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You are not Owner or Admin")
    })
    @DeleteMapping("/{householdId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long householdId,
            @Parameter(description = "ID korisnika kojeg izbacujemo") @PathVariable Long userId,
            HttpServletRequest request) {
        Long currentUserId = getUserId(request);
        householdService.removeMember(householdId, userId, currentUserId);
        throw new ResponseStatusException(HttpStatus.OK, "User has been kicked successfully.");
    }

    @Operation(summary = "Leave household", description = "Korisnik samostalno napušta kućanstvo. Vlasnik ne može napustiti (mora obrisati ili prenijeti vlasništvo).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully left household"),
            @ApiResponse(responseCode = "400", description = "Owner cannot leave / Not a member")
    })
    @PostMapping("/{householdId}/leave")
    public ResponseEntity<Void> leave(@PathVariable Long householdId, HttpServletRequest request) {
        Long currentUserId = getUserId(request);
        householdService.leaveHousehold(householdId, currentUserId);
        throw new ResponseStatusException(HttpStatus.OK, "You have household successfully.");
    }

    private Long getUserId(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        String email = claims.getSubject();
        return Long.valueOf(userService.getCurrentUser(email).getId());
    }
}
