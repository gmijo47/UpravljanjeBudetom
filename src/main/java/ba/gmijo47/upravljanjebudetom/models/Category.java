package ba.gmijo47.upravljanjebudetom.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "household_id")
    @JsonIgnore
    private Household household;

    @Column
    private String name;

    @Column
    private String type;

    @OneToMany
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private List<Expense> expenses = new ArrayList<>();

    public Category() {
    }

    public Category(Household household, String name, String type) {
        this.household = household;
        this.name = name;
        this.type = type;
    }

}
