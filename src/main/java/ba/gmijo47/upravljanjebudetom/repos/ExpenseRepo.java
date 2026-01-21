package ba.gmijo47.upravljanjebudetom.repos;

import ba.gmijo47.upravljanjebudetom.models.Expense;
import ba.gmijo47.upravljanjebudetom.models.Household;
import ba.gmijo47.upravljanjebudetom.models.MonthlyBudget;
import ba.gmijo47.upravljanjebudetom.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepo extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);

    List<Expense> findByMonthlyBudget(MonthlyBudget monthlyBudget);

    @Query("SELECT e FROM Expense e JOIN e.monthlyBudget mb WHERE mb.household = :household")
    List<Expense> findByHousehold(@Param("household") Household household);

    @Query("SELECT e FROM Expense e JOIN e.monthlyBudget mb WHERE mb.household.id = :householdId")
    List<Expense> findByHouseholdId(@Param("householdId") Long householdId);
}
