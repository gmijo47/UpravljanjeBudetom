package ba.gmijo47.upravljanjebudetom.repos;

import ba.gmijo47.upravljanjebudetom.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}