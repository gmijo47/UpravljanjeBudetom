package ba.gmijo47.upravljanjebudetom.controller;

import ba.gmijo47.upravljanjebudetom.models.Category;
import ba.gmijo47.upravljanjebudetom.services.CategoryService;
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
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Upravljanje kategorijama troškova")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    @Operation(summary = "Get my categories", description = "Vraća kategorije za kućanstvo trenutnog korisnika.")
    @GetMapping
    public ResponseEntity<List<Category>> getMyCategories(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(categoryService.getMyCategories(userId));
    }

    @Operation(summary = "Create category", description = "Kreira novu kategoriju u kućanstvu trenutnog korisnika.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "User not in household or missing name")
    })
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Podaci kategorije",
                    content = @Content(examples = @ExampleObject(value = "{\"name\": \"Hrana\", \"type\": \"EXPENSE\"}"))
            )
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(categoryService.createMyCategory(payload, userId));
    }

    @Operation(summary = "Update category", description = "Ažurira kategoriju. Članovi/Owner mogu samo svoje, Admin može bilo koju.")
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "ID kategorije") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = "{\"name\": \"Nova Hrana\", \"type\": \"EXPENSE\"}"))
            )
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(categoryService.updateCategory(id, payload, userId));
    }

    @Operation(summary = "Delete category", description = "Briše kategoriju. Članovi/Owner mogu samo svoje, Admin može bilo koju.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        categoryService.deleteCategory(id, userId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "ADMIN: Get categories by Household ID", description = "Dohvaća kategorije za bilo koje kućanstvo po ID-u.")
    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Category>> getByHouseholdId(@PathVariable Long householdId) {
        return ResponseEntity.ok(categoryService.getCategoriesByHouseholdId(householdId));
    }

    @Operation(summary = "ADMIN: Create category for Household", description = "Kreira kategoriju u specifičnom kućanstvu.")
    @PostMapping("/household/{householdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createForHousehold(
            @PathVariable Long householdId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = "{\"name\": \"Admin Kat\", \"type\": \"INCOME\"}"))
            )
            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(categoryService.createCategoryForHousehold(householdId, payload));
    }

    private Long getUserId(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        return Long.valueOf(userService.getCurrentUser(claims.getSubject()).getId());
    }
}
