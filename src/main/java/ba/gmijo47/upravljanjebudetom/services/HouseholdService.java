package ba.gmijo47.upravljanjebudetom.services;

import ba.gmijo47.upravljanjebudetom.models.Household;
import ba.gmijo47.upravljanjebudetom.models.Role;
import ba.gmijo47.upravljanjebudetom.models.User;
import ba.gmijo47.upravljanjebudetom.repos.HouseholdRepo;
import ba.gmijo47.upravljanjebudetom.repos.RoleRepo;
import ba.gmijo47.upravljanjebudetom.repos.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepo householdRepository;
    private final UserRepo userRepository;
    private final RoleRepo roleRepository;
    private final UserService userService;

    public Household getHouseholdById(Long id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Household not found"));
    }

    public List<Household> getAllHouseholds() {
        return householdRepository.findAll();
    }

    public Household getMyHousehold(Long userId) {
        User user = userService.getUserById(userId);
        if (user.getHousehold() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "You are not a member of any household");
        }
        return user.getHousehold();
    }

    @Transactional

    public Household createHousehold(Household householdData, Long creatorId) {

        User creator = userService.getUserById(creatorId);

        if (creator.getHousehold() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Već ste član kućanstva! Morate izaći iz trenutnog da bi kreirali novo."
            );
        }

        Household household = new Household();
        household.setName(householdData.getName());
        household.setCreatedAt(LocalDateTime.now());
        household = householdRepository.save(household);

        creator.setHousehold(household);

        boolean isAdmin = creator.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN") || role.getName().equals("ADMIN"));


        if (!isAdmin) {
            Role ownerRole = roleRepository.findByName("ROLE_OWNER");

            creator.getRoles().clear();

            creator.getRoles().add(ownerRole);
        }

        userRepository.save(creator);

        return household;
    }


    @Transactional
    public Household updateHousehold(Long id, Household details, Long currentUserId) {
        Household household = getHouseholdById(id);
        User currentUser = userService.getUserById(currentUserId);

        validateAccess(household, currentUser);

        if (details.getName() != null && !details.getName().isEmpty()) {
            household.setName(details.getName());
        }

        return householdRepository.save(household);
    }

    @Transactional
    public void addMember(Long householdId, Long newMemberId, Long currentUserId) {
        Household household = getHouseholdById(householdId);
        User currentUser = userService.getUserById(currentUserId);

        validateAccess(household, currentUser);

        User newMember = userService.getUserById(newMemberId);
        if (newMember.getHousehold() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a household");
        }

        newMember.setHousehold(household);
        userRepository.save(newMember);
    }

    @Transactional
    public void removeMember(Long householdId, Long memberIdToRemove, Long currentUserId) {
        Household household = getHouseholdById(householdId);
        User currentUser = userService.getUserById(currentUserId);

        validateAccess(household, currentUser);

        if (memberIdToRemove.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot kick themselves");
        }

        User member = userService.getUserById(memberIdToRemove);

        if (member.getHousehold() == null || !member.getHousehold().getId().equals(householdId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in this household");
        }

        member.setHousehold(null);
        userRepository.save(member);
    }

    @Transactional
    public void leaveHousehold(Long householdId, Long currentUserId) {
        if (isUserOwner(currentUserId, householdId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot leave. Delete household instead.");
        }

        User me = userService.getUserById(currentUserId);

        if (me.getHousehold() == null || !me.getHousehold().getId().equals(householdId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not in this household");
        }

        me.setHousehold(null);
        userRepository.save(me);
    }


    private boolean isUserOwner(Long userId, Long householdId) {
        return householdRepository.findByOwnerId(userId)
                .map(h -> h.getId().equals(householdId))
                .orElse(false);
    }

    private void validateAccess(Household targetHousehold, User currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        boolean isOwnerRole = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_OWNER"));

        if (!isOwnerRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to manage households.");
        }

        Household ownersHousehold = currentUser.getHousehold();
        if (ownersHousehold == null || !ownersHousehold.getId().equals(targetHousehold.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own household.");
        }
    }
    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
    }
}
