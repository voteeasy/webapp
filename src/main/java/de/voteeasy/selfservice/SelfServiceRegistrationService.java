package de.voteeasy.selfservice;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.Role;
import de.voteeasy.user.RoleRepository;
import de.voteeasy.user.RoleService;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@Transactional
@Validated
public class SelfServiceRegistrationService {
    /**
     * Placeholder for creation date of the generated tenant.
     */
    public static final String CREATION_TIMESTAMP = "sr_created_at";
    /**
     * Initial generated password for non-admin account.
     */
    public static final String INITIAL_PASSWORD = "sr_init_pw";
    /**
     * Initial generated password admin account.
     */
    public static final String INITIAL_ADMIN_PASSWORD = "sr_init_apw";
    /**
     * Name of the freshly generated tenant.
     */
    public static final String TENANT_NAME = "sr_t_name";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Autowired
    SelfServiceRegistrationService(RoleRepository roleRepository, UserRepository userRepository, TenantRepository tenantRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Verify if the wrong captcha value was chosen.
     *
     * @param captcha selected captcha value.
     * @return {@code true} if the chosen captcha value is wrong, {@code false} otherwise.
     */
    boolean isCaptchaWrong(String captcha) {
        return !"star".equals(captcha);
    }

    public ServiceResult createOrganisation(@Valid SelfServiceRegistrationForm form) {
        Set<Tenant> tenants = tenantRepository.findByNameIgnoreCase(form.getTenantname());
        if (tenants.isEmpty()) {
            log.info("Creating new organisation {} via self service registration.", form.getTenantname());
            return createTenantWithAccounts(form);
        }

        log.error("Unable to create already existing tenant {}", form.getTenantname());
        return ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("error.tenants.doublenames").build();
    }

    private ServiceResult createTenantWithAccounts(@Valid SelfServiceRegistrationForm form) {
        try {
            String initialPassword = CredentialsGenerator.generateSecureRandomString();
            String initialAdminPassword = CredentialsGenerator.generateSecureRandomString();
            Tenant newTenant = createTenant(form.getTenantname());
            bootstrapUserRelatedData(newTenant, initialPassword, initialAdminPassword);
            Map<String, String> payload = new HashMap<>();
            // as we consume the URL parameter values directly in the UI, we cannot apply
            // payload.put(TENANT_NAME, HtmlUtils.htmlEscape(form.getTenantname()));
            // here
            payload.put(TENANT_NAME, escapeHtml(form.getTenantname()));
            payload.put(INITIAL_PASSWORD, initialPassword);
            payload.put(INITIAL_ADMIN_PASSWORD, initialAdminPassword);
            payload.put(CREATION_TIMESTAMP, formatTimestamp(newTenant.getCreatedAt()));
            return ServiceResult.builder().status(HttpStatus.OK).payload(payload).build();
        } catch (IllegalArgumentException iae) {
            log.info("Unable to create new tenant {}", form.getTenantname(), iae);
            return ServiceResult.builder().status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper to determine if someone is logged in or not.
     *
     * @return {@code true} if someone is already logged in.
     */
    boolean isSomeoneLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && !Objects.equals(auth.getName(), "anonymousUser") && auth.isAuthenticated();
    }

    /**
     * Clean up and trim the given tenant name.
     *
     * @param input given user input.
     * @return trimmed and cleaned up user input.
     */
    private String escapeHtml(String input) {
        if (input != null) {
            return StringUtils.trim(Jsoup.clean(input, Safelist.none()));  // Clean and escape HTML tags
        }
        return null;
    }

    /**
     * Format the given timestamp to be shown in the UI.
     *
     * @param timestamp timestamp.
     * @return formatted timestamp.
     */
    String formatTimestamp(Long timestamp) {
        DateTimeFormatter europeanDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return europeanDateFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC")));
    }

    private Optional<Role> get(String roleName) {
        return roleRepository.findByName(RoleService.ROLE_PREFIX + roleName);
    }

    private Tenant createTenant(String tenantName) {
        Tenant t = tenantRepository.save(new Tenant(tenantName));
        log.debug("Tenant {} has been created. CreationDate = {}", tenantName, t.getCreatedAt());
        return t;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void bootstrapUserRelatedData(Tenant tenant, String initialPassword, String initialAdminPassword) {
        String tenantName = tenant.getName();
        // in case any of the roles do not exist the installation is faulty and we want this exception to happen
        Role user = get("USER").get();
        Role admin = get("ADMIN").get();

        userRepository.save(User.builder().organisation(tenant).fullName("Initial user of " + tenantName).userName(tenantName).passwordHash(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(initialPassword)).roles(Collections.singletonList(user)).build());
        log.debug("Created non-admin user for tenant {} ....", tenant.getName());
        userRepository.save(User.builder().organisation(tenant).fullName("Admin of " + tenantName).userName(tenantName + "-admin").passwordHash(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(initialAdminPassword)).roles(Arrays.asList(user, admin)).build());
        log.debug("Created admin user for tenant {} ....", tenant.getName());
    }
}
