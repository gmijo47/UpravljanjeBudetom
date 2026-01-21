package ba.gmijo47.upravljanjebudetom.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;

    @ManyToOne
    @JoinColumn(name = "monthly_budget_id")
    @JsonIgnore
    private MonthlyBudget monthlyBudget;

    @Column
    private BigDecimal amount;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Column
    private String description;

    public Expense() {}

    public Expense(User user, Category category, MonthlyBudget monthlyBudget,
                   BigDecimal amount, LocalDate date) {
        this.user = user;
        this.category = category;
        this.monthlyBudget = monthlyBudget;
        this.amount = amount;
        this.date = date;
    }

    public Expense(User user, Category category, MonthlyBudget monthlyBudget,
                   BigDecimal amount, LocalDate date, String description) {
        this.user = user;
        this.category = category;
        this.monthlyBudget = monthlyBudget;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

}
