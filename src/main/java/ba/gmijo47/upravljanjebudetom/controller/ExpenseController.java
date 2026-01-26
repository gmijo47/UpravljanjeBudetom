package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.models.Expense;
import ba.gmijo47.upravljanjebudetom.services.ExpenseService;
import ba.gmijo47.upravljanjebudetom.services.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.Map;

@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
@Tag(name = "Expense Management", description = "Praćenje troškova")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    @Operation(summary = "Get household expenses", description = "Vraća sve troškove mog kućanstva.")
    @GetMapping
    public ResponseEntity<List<Expense>> getMyHouseholdExpenses(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(expenseService.getMyHouseholdExpenses(userId));
    }

    @Operation(summary = "Create expense", description = "Unos novog troška. Backend provjerava budžet za taj datum.")
    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = "{\"amount\": 50.00, \"date\": \"2026-01-21\", \"description\": \"Kava\", \"categoryId\": 1}"))
            )
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        throw new ResponseStatusException(HttpStatus.OK, "Expense has been successfully created.");
    }

    @Operation(summary = "Delete expense", description = "User briše svoje, Owner bilo čije, Admin sve.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        expenseService.deleteExpense(id, userId);
        throw new ResponseStatusException(HttpStatus.OK, "Expense has been deleted successfully.");
    }


    @Operation(summary = "ADMIN: Get by Household")
    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Expense>> getByHousehold(@PathVariable Long householdId) {
        return ResponseEntity.ok(expenseService.getAdminExpensesByHousehold(householdId));
    }

    @Operation(summary = "ADMIN: Get by User")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Expense>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getAdminExpensesByUser(userId));
    }

    private Long getUserId(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        return Long.valueOf(userService.getCurrentUser(claims.getSubject()).getId());
    }
}
