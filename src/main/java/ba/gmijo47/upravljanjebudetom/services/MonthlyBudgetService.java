package ba.gmijo47.upravljanjebudetom.services;

import ba.gmijo47.upravljanjebudetom.models.Household;
import ba.gmijo47.upravljanjebudetom.models.MonthlyBudget;
import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.HouseholdRepo;
import ba.gmijo47.upravljanjebudetom.repos.MonthlyBudgetRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthlyBudgetService {

    private final MonthlyBudgetRepo budgetRepository;
    private final UserService userService;
    private final HouseholdRepo householdRepository;

    public List<MonthlyBudget> getMyBudgets(Long userId) {
        User user = userService.getUserById(userId);
        if (user.getHousehold() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Niste član nijednog kućanstva.");
        }
        return budgetRepository.findByHouseholdOrderByYearDescMonthDesc(user.getHousehold());
    }

    public List<MonthlyBudget> getBudgetsByHouseholdId(Long householdId) {
        if (!householdRepository.existsById(householdId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kućanstvo ne postoji");
        }
        return budgetRepository.findByHouseholdIdOrderByYearDescMonthDesc(householdId);
    }

    @Transactional
    public MonthlyBudget createBudget(Map<String, Object> payload, Long userId) {
        User user = userService.getUserById(userId);

        // Logika: Samo Owner ili Admin
        boolean isOwner = isOwner(user);
        boolean isAdmin = isAdmin(user);

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo vlasnik kućanstva može kreirati budžet.");
        }

        if (user.getHousehold() == null && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nemate kućanstvo.");
        }

        Household household = user.getHousehold();

        return createBudgetInternal(household, payload);
    }

    @Transactional
    public MonthlyBudget createBudgetForHousehold(Long householdId, Map<String, Object> payload) {
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kućanstvo ne postoji"));

        return createBudgetInternal(household, payload);
    }

    private MonthlyBudget createBudgetInternal(Household household, Map<String, Object> payload) {
        Integer year = (Integer) payload.get("year");
        Integer month = (Integer) payload.get("month");
        BigDecimal totalIncome = new BigDecimal(payload.get("totalIncome").toString());

        if (budgetRepository.existsByHouseholdAndYearAndMonth(household, year, month)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budžet za ovaj mjesec i godinu već postoji.");
        }

        MonthlyBudget budget = new MonthlyBudget(household, year, month, totalIncome);
        return budgetRepository.save(budget);
    }


    @Transactional
    public MonthlyBudget updateBudget(Long budgetId, Map<String, Object> payload, Long userId) {
        MonthlyBudget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budžet ne postoji"));

        User user = userService.getUserById(userId);
        boolean isAdmin = isAdmin(user);

        boolean isSameHousehold = user.getHousehold() != null &&
                user.getHousehold().getId().equals(budget.getHousehold().getId());

        if (!isAdmin) {
            if (!isSameHousehold) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ovo nije vaš budžet.");
            if (!isOwner(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo Owner može uređivati budžet.");
        }

        if (payload.containsKey("totalIncome")) {
            budget.setTotalIncome(new BigDecimal(payload.get("totalIncome").toString()));
        }

        return budgetRepository.save(budget);
    }


    @Transactional
    public void deleteBudget(Long budgetId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Budžet ne postoji");
        }
        budgetRepository.deleteById(budgetId);
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("ROLE_ADMIN"));
    }

    private boolean isOwner(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_OWNER"));
    }
}
