package de.voteeasy.user;

import de.voteeasy.invitation.RegisterByUserTokenForm;
import de.voteeasy.invitation.UserToken;
import de.voteeasy.invitation.UserTokenRepository;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.voteeasy.user.UserRegistrationService.KEY_TENANT;
import static de.voteeasy.user.UserRegistrationService.KEY_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserTokenRepository userTokenRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private RegisterByUserTokenForm form;

    @BeforeEach
    void getNewForm() {
        form = RegisterByUserTokenForm.builder().token("something   ").password("p").passwordAgain("p").build();
    }

    @Test
    void wiringWorksProperly() {
        assertThat(userRegistrationService).isNotNull();
    }

    @Test
    void redeemWithoutTokenInDatabase() {
        when(userTokenRepository.findByTokenIgnoreCase(eq("something"))).thenReturn(Optional.empty());

        final ServiceResult redeemWithoutTokenInDatabase = userRegistrationService.createUserFromToken(form, new Tenant("redeemWithoutUser"));
        assertThat(redeemWithoutTokenInDatabase).isNotNull();
        assertThat(redeemWithoutTokenInDatabase.getStatus()).isEqualTo(FORBIDDEN);
        assertThat(redeemWithoutTokenInDatabase.getMessageKey()).isEqualTo("token.redeemedAlready");
    }

    @Test
    void createUserFromTokenEmptyUserName() {
        final RegisterByUserTokenForm form = RegisterByUserTokenForm.builder().build();
        assertThat(userRegistrationService.createUserFromToken(form, null).getStatus()).isEqualTo(FORBIDDEN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserFromTokenWithoutTenantGiven() {
        assertThat(userRegistrationService.createUserFromToken(form, null).getStatus()).isEqualTo(FORBIDDEN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserFromTokenUserNameAlreadyUsed() {
        when(userRepository.findByUserNameIgnoreCase(eq("something"))).thenReturn(Optional.of(new User()));
        assertThat(userRegistrationService.createUserFromToken(form, new Tenant("something")).getStatus()).isEqualTo(FORBIDDEN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserFromToken() {
        when(roleRepository.findByName(eq("ROLE_USER"))).thenReturn(Optional.of(new Role("ROLE_USER")));

        User userFromDb = new User();
        userFromDb.setUserName("savedInDb");
        userFromDb.setOrganisation(new Tenant("savedInDB"));
        when(userRepository.save(any())).thenReturn(userFromDb);

        UserToken userToken = mock(UserToken.class);
        when(userTokenRepository.findByTokenIgnoreCase(anyString())).thenReturn(Optional.of(userToken));
        when(userTokenRepository.save(any())).thenReturn(userToken);

        ServiceResult result = userRegistrationService.createUserFromToken(form, new Tenant("something"));
        // created user is persisted
        verify(userRepository).save(any());
        // and token is redeemed properly
        verify(userTokenRepository).findByTokenIgnoreCase(eq("something"));
        verify(userToken).setRedeemedAt(any());
        verify(userTokenRepository).save(any());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK);
        assertThat(result.get(KEY_USERNAME)).isEqualTo("savedInDb");
        assertThat(result.get(KEY_TENANT)).isEqualTo("savedInDB");
    }

}