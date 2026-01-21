package ba.gmijo47.upravljanjebudetom.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
@Entity
public class User {

    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String name;

    @Column
    private String lastname;

    @Column
    private String email;

    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String refreshToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Role> roles = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "household_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Household household;

    @OneToMany
    @JoinColumn(name = "user_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<BudgetAllocation> budgetAllocations = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "user_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Expense> expenses = new ArrayList<>();

    public User(Integer id, String name, String lastname, String email, String password, Date dob) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.dob = dob;
    }

    public User() {}

}
