package ba.gmijo47.upravljanjebudetom.repos;

import ba.gmijo47.upravljanjebudetom.models.Household;
import ba.gmijo47.upravljanjebudetom.models.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyBudgetRepo extends JpaRepository<MonthlyBudget, Long> {
    List<MonthlyBudget> findByHouseholdOrderByYearDescMonthDesc(Household household);
    boolean existsByHouseholdAndYearAndMonth(Household household, Integer year, Integer month);
    List<MonthlyBudget> findByHouseholdIdOrderByYearDescMonthDesc(Long householdId);
}
