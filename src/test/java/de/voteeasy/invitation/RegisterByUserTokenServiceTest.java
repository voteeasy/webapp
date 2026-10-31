package de.voteeasy.invitation;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class RegisterByUserTokenServiceTest {
    @Mock
    private UserTokenRepository userTokenRepository;
    @Mock
    private UserRegistrationService userRegistrationService;

    @InjectMocks
    private RegisterByUserTokenService registerByUserTokenService;

    @Test
    void wiringWorkedCorrectly() {
        assertThat(registerByUserTokenService).isNotNull();
    }

    @Test
    void redeemTokenEmptyToken() {
        RegisterByUserTokenForm form = RegisterByUserTokenForm.builder().build();
        assertThat(registerByUserTokenService.redeemToken(form).getStatus()).isEqualTo(FORBIDDEN);
    }

    @Test
    void redeemTokenUnknownUserToken() {
        RegisterByUserTokenForm form = RegisterByUserTokenForm.builder().token("notFoundInDatabase").build();
        assertThat(registerByUserTokenService.redeemToken(form).getStatus()).isEqualTo(FORBIDDEN);
    }

    @Test
    void redeemTokenWithDisabledTenant() {
        String token = "DEADBEAF";
        Tenant tenant = new Tenant("TestTenant");
        RegisterByUserTokenForm form = RegisterByUserTokenForm.builder().token(token).build();
        when(userTokenRepository.findByTokenIgnoreCaseAndOrganisationEnabledIsTrue(eq(token))).thenReturn(Optional.of(UserToken.builder().token(token).organisation(tenant).build()));
        ServiceResult expectedResult = ServiceResult.builder().status(FOUND).build();
        when(userRegistrationService.createUserFromToken(eq(form), eq(tenant))).thenReturn(expectedResult);

        ServiceResult result = registerByUserTokenService.redeemToken(form);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(FOUND);
    }

    @Test
    void redeemToken() {
        ServiceResult expectedResult = ServiceResult.builder().messageKey("foo").status(FAILED_DEPENDENCY).build();
        when(userRegistrationService.createUserFromToken(any(), any())).thenReturn(expectedResult);
        when(userTokenRepository.findByTokenIgnoreCaseAndOrganisationEnabledIsTrue(eq("LETMEIN"))).thenReturn(Optional.of(UserToken.builder().organisation(new Tenant("UnderTest")).build()));

        RegisterByUserTokenForm form = RegisterByUserTokenForm.builder().token("LETMEIN").build();
        final ServiceResult serviceResult = registerByUserTokenService.redeemToken(form);
        assertThat(serviceResult).isNotNull().isEqualTo(expectedResult);
    }
}