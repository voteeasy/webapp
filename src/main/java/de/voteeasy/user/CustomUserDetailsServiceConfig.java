package de.voteeasy.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@Slf4j
public class CustomUserDetailsServiceConfig {

    @Bean(name = "VoteEasyUserDetailsService")
    @Transactional(readOnly = true)
    public UserDetailsService voteEasyUserDetailsService(final UserRepository userRepository) {
        return username -> {
            String[] usernameTenant = StringUtils.split(username, String.valueOf(Character.LINE_SEPARATOR));
            if (usernameTenant == null || usernameTenant.length != 2) {
                log.info("LOGIN ATTEMPT: Wrong parameter count transferred");
                throw new UsernameNotFoundException("Wrong parameters sent during login, tenant, username and password need to be sent.");
            }

            return userRepository.getForLogin(usernameTenant[0], usernameTenant[1]).map(
                    entity -> {
                        if (entity.getOrganisation() == null || entity.getOrganisation().getId() == null || entity.getOrganisation().getName() == null) {
                            log.error("LOGIN ATTEMPT: Illegal tenant configuration for username {}", username);
                            throw new UsernameNotFoundException("Illegal tenant configuration for " + username);
                        }

                        if (entity.getRoles() == null || entity.getRoles().isEmpty()) {
                            log.error("LOGIN ATTEMPT: Illegal role configuration for username {}", username);
                            throw new UsernameNotFoundException("Corrupt user setup, no roles defined for " + username);
                        }

                        if (!entity.getOrganisation().isEnabled()) {
                            log.error("LOGIN ATTEMPT: Tenant is disabled - login not accepted for tenant {} and user {}", entity.getOrganisation().getId(), entity.getId());
                            throw new UsernameNotFoundException("Tenant is disabled");
                        }

                        return new ApplicationUser(
                                UserTenant.builder().tenantId(entity.getOrganisation().getId()).tenantName(entity.getOrganisation().getName()).build(),
                                entity.getUserName(),
                                entity.getPasswordHash(),
                                getAuthorities(entity.getRoles()));
                    }).orElseThrow(() ->
                    new UsernameNotFoundException("No such user " + username));
        };
    }

    // inspired by https://github.com/Baeldung/spring-security-registration/blob/master/src/main/java/com/baeldung/security/MyUserDetailsService.java
    private Collection<? extends GrantedAuthority> getAuthorities(final Collection<Role> roles) {
        return getGrantedAuthorities(roles);
    }

    private List<GrantedAuthority> getGrantedAuthorities(final Collection<Role> privileges) {
        final List<GrantedAuthority> authorities = new ArrayList<>();
        for (final String privilege : privileges.stream().map(Role::getName).toList()) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

}
