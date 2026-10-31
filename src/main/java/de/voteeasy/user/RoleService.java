package de.voteeasy.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
@Transactional
public class RoleService {
    public static final String ROLE_PREFIX = "ROLE_";
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Optional<Role> getByNameWithoutPrefix(String name) {
        if (StringUtils.isAnyEmpty(name)) {
            return Optional.empty();
        }
        return roleRepository.findByName(ROLE_PREFIX + name);
    }
}
