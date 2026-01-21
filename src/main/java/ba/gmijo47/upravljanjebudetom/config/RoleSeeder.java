package ba.gmijo47.upravljanjebudetom.config;

import ba.gmijo47.upravljanjebudetom.models.Role;
import ba.gmijo47.upravljanjebudetom.repos.RoleRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepo roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ROLE_ADMIN"));
            roleRepository.save(new Role("ROLE_OWNER"));
            roleRepository.save(new Role("ROLE_USER"));

            System.out.println("Roles seeded.");
        } else {
            System.out.println("Skipping seeding roles, already in DB.");
        }
    }
}
