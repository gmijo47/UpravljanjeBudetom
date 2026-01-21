package ba.gmijo47.upravljanjebudetom.repos;

import ba.gmijo47.upravljanjebudetom.models.Category;
import ba.gmijo47.upravljanjebudetom.models.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    List<Category> findByHousehold(Household household);

    List<Category> findByHouseholdId(Long householdId);
}
