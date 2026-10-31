package de.voteeasy.selfservice;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.Role;
import de.voteeasy.user.RoleRepository;
import de.voteeasy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Optional;

import static de.voteeasy.selfservice.SelfServiceRegistrationService.CREATION_TIMESTAMP;
import static de.voteeasy.selfservice.SelfServiceRegistrationService.INITIAL_ADMIN_PASSWORD;
import static de.voteeasy.selfservice.SelfServiceRegistrationService.INITIAL_PASSWORD;
import static de.voteeasy.selfservice.SelfServiceRegistrationService.TENANT_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfServiceRegistrationServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private SelfServiceRegistrationService selfServiceRegistrationService;

    @Test
    void wiringWorks() {
        assertThat(selfServiceRegistrationService).isNotNull();
    }

    @Test
    void formatTimestampIsFixedAndWithCorrectTimezone() {
        assertThat(selfServiceRegistrationService.formatTimestamp(123123L)).isEqualTo("1970-01-01 00:02:03");
    }

    @Test
    void isCaptchaWrong() {
        assertThat(selfServiceRegistrationService.isCaptchaWrong(null)).isTrue();
        assertThat(selfServiceRegistrationService.isCaptchaWrong("anythingGoes")).isTrue();
        assertThat(selfServiceRegistrationService.isCaptchaWrong("star")).isFalse();
    }

    @Test
    void isSomeoneLoggedIn() {
        assertThat(selfServiceRegistrationService.isSomeoneLoggedIn()).isFalse();
    }

    @Test
    void createOrganisationThatAlreadyExists() {
        SelfServiceRegistrationForm form = SelfServiceRegistrationForm.builder().tenantname("testtenantalreadyexists").build();
        when(tenantRepository.findByNameIgnoreCase(form.getTenantname())).thenReturn(Collections.singleton(new Tenant("testtenantalreadyexists")));
        assertThat(selfServiceRegistrationService.createOrganisation(form)).isEqualTo(ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("error.tenants.doublenames").build());
    }

    @Test
    void createOrganisationWithHtmlFiltering() {
        SelfServiceRegistrationForm form = SelfServiceRegistrationForm.builder().tenantname("  yourtesttenantWithHTML<img src=\"pic.png\"><a href=\"Hello! I am an alert box!!\">removed     ").build();

        Tenant tenant = new Tenant("mytesttenantWithHTMLalert");
        tenant.setCreatedAt(4713L);
        when(tenantRepository.save(any())).thenReturn(tenant);
        when(roleRepository.findByName(any())).thenReturn(Optional.of(new Role("anythingGoesHere")));

        var result = selfServiceRegistrationService.createOrganisation(form);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getPayload()).containsKeys(TENANT_NAME, INITIAL_PASSWORD, INITIAL_ADMIN_PASSWORD, CREATION_TIMESTAMP);
        assertThat(result.getPayload().get(TENANT_NAME)).isEqualTo("yourtesttenantWithHTMLremoved");
    }

    @Test
    void createOrganisationWithErrorDuringTenantCreation() {
        SelfServiceRegistrationForm form = SelfServiceRegistrationForm.builder().tenantname("justatenant").build();

        Tenant tenant = new Tenant("justatenant");
        tenant.setCreatedAt(13L);
        when(tenantRepository.save(any())).thenThrow(new IllegalArgumentException("Tests throw an exception here"));

        var result = selfServiceRegistrationService.createOrganisation(form);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getPayload()).isEmpty();
    }

}