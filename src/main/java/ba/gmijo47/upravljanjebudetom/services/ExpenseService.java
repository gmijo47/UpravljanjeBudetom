package ba.gmijo47.upravljanjebudetom.services;

import ba.gmijo47.upravljanjebudetom.models.*;
import ba.gmijo47.upravljanjebudetom.repos.CategoryRepo;
import ba.gmijo47.upravljanjebudetom.repos.ExpenseRepo;
import ba.gmijo47.upravljanjebudetom.repos.HouseholdRepo;
import ba.gmijo47.upravljanjebudetom.repos.MonthlyBudgetRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepo expenseRepository;
    private final UserService userService;
    private final CategoryRepo categoryRepository;
    private final MonthlyBudgetRepo monthlyBudgetRepository;
    private final HouseholdRepo householdRepository;

    public List<Expense> getMyHouseholdExpenses(Long userId) {
        User user = userService.getUserById(userId);
        if (user.getHousehold() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Niste član kućanstva.");
        }
        return expenseRepository.findByHousehold(user.getHousehold());
    }

    public List<Expense> getAdminExpensesByHousehold(Long householdId) {
        if (!householdRepository.existsById(householdId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kućanstvo ne postoji");
        }
        return expenseRepository.findByHouseholdId(householdId);
    }

    public List<Expense> getAdminExpensesByUser(Long userId) {
        User user = userService.getUserById(userId);
        return expenseRepository.findByUser(user);
    }

    @Transactional
    public Expense createExpense(Map<String, Object> payload, Long userId) {
        User user = userService.getUserById(userId);
        Household household = user.getHousehold();

        if (household == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Morate biti u kućanstvu za unos troška.");
        }

        String description = (String) payload.get("description");
        LocalDate date = LocalDate.parse((String) payload.get("date"));
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Long categoryId = Long.valueOf(payload.get("categoryId").toString());

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategorija ne postoji"));

        if (!category.getHousehold().getId().equals(household.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kategorija ne pripada vašem kućanstvu.");
        }

        Integer year = date.getYear();
        Integer month = date.getMonthValue();

        MonthlyBudget monthlyBudget = (MonthlyBudget) monthlyBudgetRepository.findByHouseholdAndYearAndMonth(household, year, month)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ne postoji budžet za " + month + "/" + year + ". Kontaktirajte vlasnika."));


        BigDecimal currentTotal = expenseRepository.findByMonthlyBudget(monthlyBudget).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = currentTotal.add(amount);

        if (newTotal.compareTo(monthlyBudget.getTotalIncome()) > 0) {
            BigDecimal remaining = monthlyBudget.getTotalIncome().subtract(currentTotal);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Prekoračenje budžeta! Preostalo: " + remaining + ", Pokušaj: " + amount);
        }

        Expense expense = new Expense(user, category, monthlyBudget, amount, date, description);
        return expenseRepository.save(expense);
    }


    @Transactional
    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trošak ne postoji"));

        User currentUser = userService.getUserById(userId);
        boolean isAdmin = isAdmin(currentUser);
        boolean isOwner = isOwner(currentUser);

        boolean isSameHousehold = currentUser.getHousehold() != null &&
                currentUser.getHousehold().getId().equals(expense.getMonthlyBudget().getHousehold().getId());

        if (!isAdmin && !isSameHousehold) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pristup ovom trošku.");
        }

        boolean isCreator = expense.getUser().getId().equals(userId);

        if (isAdmin || (isSameHousehold && (isOwner || isCreator))) {
            expenseRepository.delete(expense);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pravo brisati tuđi trošak.");
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("ROLE_ADMIN"));
    }

    private boolean isOwner(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_OWNER"));
    }
}
