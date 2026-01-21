package ba.gmijo47.upravljanjebudetom.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    @JsonIgnore
    private String password;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @Column
    @JsonIgnore
    private String refreshToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

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
