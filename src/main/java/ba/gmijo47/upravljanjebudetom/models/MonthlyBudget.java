package ba.gmijo47.upravljanjebudetom.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "monthly_budget", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"household_id", "year", "month"})
})
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "household_id")
    @JsonIgnore
    private Household household;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(name = "total_income")
    private BigDecimal totalIncome;

    @OneToMany
    @JoinColumn(name = "monthly_budget_id")
    @JsonIgnore
    private List<Expense> expenses = new ArrayList<>();

    public MonthlyBudget() {}

    public MonthlyBudget(Household household, Integer year, Integer month, BigDecimal totalIncome) {
        this.household = household;
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
    }

    public Long getHouseholdId() {
        return household != null ? household.getId() : null;
    }
}
