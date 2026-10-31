package de.voteeasy.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    /*
    Remember to add "ROLE_" prefix to your name in order to properly find your roles!
     */
    Optional<Role> findByName(String name);
}
