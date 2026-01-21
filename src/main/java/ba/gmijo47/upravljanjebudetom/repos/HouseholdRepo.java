package ba.gmijo47.upravljanjebudetom.repos;

import ba.gmijo47.upravljanjebudetom.models.Household;
import ba.gmijo47.upravljanjebudetom.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepo extends JpaRepository<Household, Long> {
    List<Household> findByUsersContaining(User user);

    boolean existsByName(String name);

    @Query("SELECT h FROM Household h JOIN h.users u JOIN u.roles r WHERE u.id = :userId AND r.name = 'ROLE_OWNER'")
    Optional<Household> findByOwnerId(@Param("userId") Long userId);
}
