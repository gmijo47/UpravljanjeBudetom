package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.models.MonthlyBudget;
import ba.gmijo47.upravljanjebudetom.services.MonthlyBudgetService;
import ba.gmijo47.upravljanjebudetom.services.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.Map;

@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
@Tag(name = "Monthly Budget Management", description = "Upravljanje mjesečnim budžetima")
@SecurityRequirement(name = "bearerAuth")
public class MonthlyBudgetController {

    private final MonthlyBudgetService budgetService;
    private final UserService userService;

    @Operation(summary = "Get my budgets", description = "Vraća listu budžeta za moje kućanstvo. (User vidi, Owner vidi)")
    @GetMapping
    public ResponseEntity<List<MonthlyBudget>> getMyBudgets(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(budgetService.getMyBudgets(userId));
    }

    @Operation(summary = "Create budget (Owner only)", description = "Kreira budžet za tekuće kućanstvo. Samo Owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Budget for this month already exists"),
            @ApiResponse(responseCode = "403", description = "Not Owner")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<MonthlyBudget> createBudget(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = "{\"year\": 2026, \"month\": 1, \"totalIncome\": 1500.00}"))
            )
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(budgetService.createBudget(payload, userId));
    }

    @Operation(summary = "Update budget (Owner only)", description = "Ažurira iznos prihoda. Samo Owner.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<MonthlyBudget> updateBudget(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = "{\"totalIncome\": 2000.00}"))
            )
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(budgetService.updateBudget(id, payload, userId));
    }


    @Operation(summary = "ADMIN: Get budgets by Household ID")
    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MonthlyBudget>> getByHouseholdId(@PathVariable Long householdId) {
        return ResponseEntity.ok(budgetService.getBudgetsByHouseholdId(householdId));
    }

    @Operation(summary = "ADMIN: Create budget for Household")
    @PostMapping("/household/{householdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MonthlyBudget> createForHousehold(
            @PathVariable Long householdId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = "{\"year\": 2026, \"month\": 5, \"totalIncome\": 3000.00}"))
            )
            @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(budgetService.createBudgetForHousehold(householdId, payload));
    }

    @Operation(summary = "ADMIN ONLY: Delete budget", description = "Owner ne može brisati budžet, samo Admin.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok().build();
    }

    private Long getUserId(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        return Long.valueOf(userService.getCurrentUser(claims.getSubject()).getId());
    }
}