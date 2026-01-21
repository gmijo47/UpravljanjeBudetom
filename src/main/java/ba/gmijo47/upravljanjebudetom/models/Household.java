package ba.gmijo47.upravljanjebudetom.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "household")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany
    @JoinColumn(name = "household_id")
    private List<User> users = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "household_id")
    @JsonIgnore
    private List<MonthlyBudget> monthlyBudgets = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "household_id")
    @JsonIgnore
    private List<Category> categories = new ArrayList<>();

    public Household() {}

    public Household(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }
}
