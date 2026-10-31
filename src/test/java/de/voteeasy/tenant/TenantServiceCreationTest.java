package de.voteeasy.tenant;

import de.voteeasy.gathering.GatheringService;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.Role;
import de.voteeasy.user.RoleService;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import de.voteeasy.votes.OptionRepository;
import de.voteeasy.votes.VoteProposalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static de.voteeasy.tenant.TenantServiceTest.getTenant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class TenantServiceCreationTest {
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String TENANT_NAME = "TenantServiceCreationTest";

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private GatheringService gatheringService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private VoteProposalRepository voteProposalRepository;

    @InjectMocks
    private TenantService tenantService;

    private static CreateTenantForm populateForm() {
        return CreateTenantForm.builder().name(TENANT_NAME).adminPassword("n/a").build();
    }

    @Test
    void injectionWorkedProperly() {
        assertThat(tenantService).isNotNull();
    }

    private void rolesAreAvailable() {
        when(roleService.getByNameWithoutPrefix(eq("ADMIN"))).thenReturn(Optional.of(new Role(UUID.randomUUID(), UUID.randomUUID().toString())));
        when(roleService.getByNameWithoutPrefix(eq("USER"))).thenReturn(Optional.of(new Role(UUID.randomUUID(), UUID.randomUUID().toString())));
    }

    @Test
    void createTenantInteractsWithAllRepositories() {
        when(tenantRepository.findByNameIgnoreCase(eq(TENANT_NAME))).thenReturn(Collections.emptySet());

        rolesAreAvailable();

        User user = new User();
        user.setId(UUID.randomUUID());
        when(userRepository.save(any())).thenReturn(user);

        Tenant tenant = new Tenant(TENANT_NAME);
        tenant.setId(TENANT_ID);
        when(tenantRepository.save(any())).thenReturn(tenant);

        final ServiceResult serviceResult = tenantService.createTenant(populateForm());
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(OK);
        assertThat(serviceResult.getMessageKey()).isEqualTo("success.tenant.new");

        // ensure interaction with all relating repositories/service implementations
        verify(tenantRepository).save(any());
        verify(userRepository).save(any());
        verify(tenantRepository).save(any());
        verify(gatheringService).saveAsGiven(any());
        verify(voteProposalRepository, times(2)).save(any());
        verify(optionRepository, times(6)).save(any());
    }

    @Test
    void createTenantNameAlreadyExisting() {
        when(tenantRepository.findByNameIgnoreCase(eq(TENANT_NAME))).thenReturn(Sets.newSet(getTenant(false)));

        final ServiceResult serviceResult = tenantService.createTenant(populateForm());
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(FORBIDDEN);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.tenants.doublenames");
    }

}