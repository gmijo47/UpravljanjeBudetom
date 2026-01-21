package ba.gmijo47.upravljanjebudetom.services;

import ba.gmijo47.upravljanjebudetom.models.Category;
import ba.gmijo47.upravljanjebudetom.models.Household;
import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.CategoryRepo;
import ba.gmijo47.upravljanjebudetom.repos.HouseholdRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepository;
    private final UserService userService;
    private final HouseholdRepo householdRepository;

    public List<Category> getMyCategories(Long userId) {
        User user = userService.getUserById(userId);
        if (user.getHousehold() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Niste član nijednog kućanstva.");
        }
        return categoryRepository.findByHousehold(user.getHousehold());
    }

    public List<Category> getCategoriesByHouseholdId(Long householdId) {

        if (!householdRepository.existsById(householdId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kućanstvo ne postoji.");
        }
        return categoryRepository.findByHouseholdId(householdId);
    }

    @Transactional
    public Category createMyCategory(Map<String, String> payload, Long userId) {
        User user = userService.getUserById(userId);
        if (user.getHousehold() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Morate biti u kućanstvu da bi kreirali kategoriju.");
        }

        String name = payload.get("name");
        String type = payload.get("type");

        if (name == null || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naziv kategorije je obavezan.");
        }

        Category category = new Category(user.getHousehold(), name, type);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category createCategoryForHousehold(Long householdId, Map<String, String> payload) {
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kućanstvo ne postoji"));

        String name = payload.get("name");
        String type = payload.get("type");

        Category category = new Category(household, name, type);
        return categoryRepository.save(category);
    }


    @Transactional
    public Category updateCategory(Long categoryId, Map<String, String> payload, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategorija ne postoji"));

        User user = userService.getUserById(userId);

        boolean isAdmin = isAdmin(user);
        boolean isSameHousehold = user.getHousehold() != null &&
                user.getHousehold().getId().equals(category.getHousehold().getId());

        if (!isAdmin && !isSameHousehold) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pravo uređivati ovu kategoriju.");
        }

        if (payload.containsKey("name")) category.setName(payload.get("name"));
        if (payload.containsKey("type")) category.setType(payload.get("type"));

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategorija ne postoji"));

        User user = userService.getUserById(userId);

        boolean isAdmin = isAdmin(user);
        boolean isSameHousehold = user.getHousehold() != null &&
                user.getHousehold().getId().equals(category.getHousehold().getId());

        if (!isAdmin && !isSameHousehold) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pravo brisati ovu kategoriju.");
        }

        categoryRepository.delete(category);
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("ROLE_ADMIN"));
    }
}
