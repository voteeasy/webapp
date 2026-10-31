package de.voteeasy.invitation;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.settings.tenant.ReadSettingService;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class UserTokenServiceTest {
    private static final Tenant TENANT;
    private static final UUID TOKEN_ID;
    private static final User USER;

    static {
        TOKEN_ID = UUID.randomUUID();
        TENANT = new Tenant("UserTokenServiceTest");
        TENANT.setId(UUID.randomUUID());
        USER = User.builder().id(UUID.randomUUID()).createdAt(System.currentTimeMillis()).userName("UserTokenServiceTest").organisation(TENANT).build();
    }

    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private UserTokenRepository userTokenRepository;
    @Mock
    private ReadSettingService readSettingService;
    @Mock
    private TenantService tenantService;

    @InjectMocks
    private UserTokenService userTokenService;

    @Test
    void serviceIsWiredCorrectly() {
        assertThat(userTokenService).isNotNull();
    }

    @Test
    void getTokenByTenantIsNPESafe() {
        assertThat(userTokenService.getToken(null)).isEmpty();
    }

    @Test
    void createTokenWithoutUser() {
        final ServiceResult result = userTokenService.createToken();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void getUsableTokenWithoutUserIsEmpty() {
        assertThat(userTokenService.getUsableToken()).isEmpty();
    }

    @Test
    void getUsableTokenWithUserCallsCorrectRepositoryMethod() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.findAllByEnabledIsTrueAndRedeemedAtIsNullAndOrganisation(eq(TENANT))).thenReturn(createTokens(3));


        assertThat(userTokenService.getUsableToken()).hasSize(3);
        verify(applicationUserService).currentlyLoggedInUser();
        verify(userTokenRepository).findAllByEnabledIsTrueAndRedeemedAtIsNullAndOrganisation(eq(TENANT));
    }

    @Test
    void createTokenTakesLimitIntoAccount() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.findAllByOrganisation(eq(TENANT), any(Sort.class))).thenReturn(createTokens(50));

        assertThat(userTokenService.getToken(TENANT)).hasSize(50);
        final ServiceResult result = userTokenService.createToken();
        verify(applicationUserService, times(2)).currentlyLoggedInUser();
        verify(userTokenRepository, times(2)).findAllByOrganisation(eq(TENANT), any(Sort.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(FORBIDDEN);
        assertThat(result.getMessageKey()).isEqualTo("token.limit.reached");
    }

    @Test
    void createTokenGeneratesTokenSuccessfullyIfOneAlreadyExists() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.findAllByOrganisation(eq(TENANT), any(Sort.class))).thenReturn(createTokens(1));
        when(readSettingService.getIntegerOrDefault(anyString(), eq(50), any())).thenReturn(50);

        assertThat(userTokenService.getToken(TENANT)).hasSize(1);
        final ServiceResult result = userTokenService.createToken();
        verify(applicationUserService, times(2)).currentlyLoggedInUser();
        verify(userTokenRepository, times(2)).findAllByOrganisation(eq(TENANT), any(Sort.class));
        verify(userTokenRepository, times(49)).save(any());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK);
        assertThat(result.getMessageKey()).isEqualTo("token.generated");
    }

    @Test
    void createTokenGeneratesTokenSuccessfullyIfNoneAlreadyExists() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        assertThat(userTokenService.getToken(TENANT)).isEmpty();
        when(readSettingService.getIntegerOrDefault(anyString(), eq(50), any())).thenReturn(50);

        final ServiceResult result = userTokenService.createToken();
        verify(applicationUserService, times(2)).currentlyLoggedInUser();
        verify(readSettingService).getIntegerOrDefault(eq("usertoken.count.max"), eq(50), any());
        verify(userTokenRepository, times(2)).findAllByOrganisation(eq(TENANT), any(Sort.class));
        verify(userTokenRepository, times(50)).save(any());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK);
        assertThat(result.getMessageKey()).isEqualTo("token.generated");
    }

    @Test
    void createTokenInProtectedTenant() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.findAllByOrganisation(eq(TENANT), any(Sort.class))).thenReturn(createTokens(1));
        when(readSettingService.getIntegerOrDefault(anyString(), eq(50), any())).thenReturn(50);

        when(tenantService.isProtected(any(Tenant.class))).thenReturn(true);

        final ServiceResult result = userTokenService.createToken();
        assertThat(result).isNotNull();
        assertThat(result.getMessageKey()).isEqualTo("error.tenant.protected");
        assertThat(result.getStatus()).isEqualTo(METHOD_NOT_ALLOWED);
    }

    private Set<UserToken> createTokens(int max) {
        Set<UserToken> tokens = new TreeSet<>();
        IntStream.range(0, max).forEach(c -> tokens.add(UserToken.builder()
                .id(UUID.randomUUID()).token(String.valueOf(c)).organisation(TENANT)
                .build())
        );

        assertThat(tokens.size()).isEqualTo(max);
        return tokens;
    }

    @Test
    void toggleTokenWithoutTokenId() {
        final ServiceResult result = userTokenService.toggleToken(null);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void toggleTokenWithoutUser() {
        final ServiceResult result = userTokenService.toggleToken(TOKEN_ID);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void toggleTokenWithoutExistingToken() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));

        final ServiceResult result = userTokenService.toggleToken(TOKEN_ID);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void toggleTokenWithExistingTokenWorks() {
        UUID tokenId = UUID.randomUUID();
        UserToken token = UserToken.builder().enabled(false).createdAt(0L).token("TKN").id(tokenId).organisation(TENANT).build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.findByIdAndOrganisation(eq(TOKEN_ID), eq(TENANT))).thenReturn(Optional.of(token));

        final ServiceResult result = userTokenService.toggleToken(TOKEN_ID);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK);
        assertThat(result.getMessageKey()).isEqualTo("token.toggled");
        verify(userTokenRepository).save(eq(UserToken.builder().enabled(true).createdAt(0L).token("TKN").id(tokenId).organisation(TENANT).build()));
    }

    @Test
    void deleteTokenWithNullParam() {
        final ServiceResult result = userTokenService.deleteToken(null);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void deleteTokenWithIdButWrongTenant() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));

        final ServiceResult result = userTokenService.deleteToken(TOKEN_ID);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void deleteToken() {
        UserToken token = UserToken.builder().enabled(false).createdAt(0L).token("TKN").id(TOKEN_ID).organisation(TENANT).build();

        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.findByIdAndOrganisation(eq(TOKEN_ID), eq(TENANT))).thenReturn(Optional.of(token));

        final ServiceResult result = userTokenService.deleteToken(TOKEN_ID);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK);
        assertThat(result.getMessageKey()).isEqualTo("token.deleted");

        verify(userTokenRepository).deleteById(eq(TOKEN_ID));
    }

    @Test
    void redeemTokenWithNullParam() {
        final ServiceResult result = userTokenService.redeemToken(null, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");

        verify(userTokenRepository, never()).save(any());
    }

    @Test
    void redeemTokenWithInvalidTokenId() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));

        final ServiceResult result = userTokenService.redeemToken(TOKEN_ID, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");

        verify(userTokenRepository, never()).save(any());
    }

    @Test
    void redeemToken() {
        Long timestamp = System.currentTimeMillis();
        UserToken token = UserToken.builder().enabled(false).createdAt(0L).token("TKN").id(TOKEN_ID).organisation(TENANT).build();

        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.findByIdAndOrganisation(eq(TOKEN_ID), eq(TENANT))).thenReturn(Optional.of(token));

        final ServiceResult result = userTokenService.redeemToken(TOKEN_ID, timestamp);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK);
        assertThat(result.getMessageKey()).isEqualTo("token.redeemed");

        token.setRedeemedAt(timestamp);
        verify(userTokenRepository).save(eq(token));

    }

    @Test
    void redeemTokenTwice() {
        Long timestamp = System.currentTimeMillis();
        UserToken token = UserToken.builder().enabled(false).redeemedAt(timestamp).createdAt(0L).token("TKN").id(TOKEN_ID).organisation(TENANT).build();

        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.findByIdAndOrganisation(eq(TOKEN_ID), eq(TENANT))).thenReturn(Optional.of(token));

        final ServiceResult reredeemResult = userTokenService.redeemToken(TOKEN_ID, timestamp);
        assertThat(reredeemResult).isNotNull();
        assertThat(reredeemResult.getStatus()).isEqualTo(FORBIDDEN);
        assertThat(reredeemResult.getMessageKey()).isEqualTo("token.alreadyredeemed");
        verify(userTokenRepository, never()).save(any());
    }

    @Test
    void getRedeemedTokenCountWithoutUser() {
        assertThat(userTokenService.getRedeemedTokenCount()).isEqualTo(0L);
        verify(userTokenRepository, never()).countByRedeemedAtIsNotNullAndOrganisation(any());
    }

    @Test
    void getRedeemedTokenCount() {
        long expectedCount = 47L;
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.countByRedeemedAtIsNotNullAndOrganisation(eq(TENANT))).thenReturn(expectedCount);

        assertThat(userTokenService.getRedeemedTokenCount()).isEqualTo(47L);
        verify(userTokenRepository).countByRedeemedAtIsNotNullAndOrganisation(any());
    }

    @Test
    void getOpenTokenCountWithoutUser() {
        assertThat(userTokenService.getOpenTokenCount()).isEqualTo(0L);
        verify(userTokenRepository, never()).countByRedeemedAtIsNullAndOrganisation(any());
    }

    @Test
    void getOpenTokenCount() {
        long expectedCount = 47L;
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.countByRedeemedAtIsNullAndOrganisation(eq(TENANT))).thenReturn(expectedCount);

        assertThat(userTokenService.getOpenTokenCount()).isEqualTo(47L);
        verify(userTokenRepository).countByRedeemedAtIsNullAndOrganisation(any());
    }

    @Test
    void getDisabledTokenCountWithoutUser() {
        assertThat(userTokenService.getDisabledTokenCount()).isEqualTo(0L);
        verify(userTokenRepository, never()).countByEnabledIsFalseAndOrganisation(any());
    }

    @Test
    void getDisabledTokenCount() {
        long expectedCount = 47L;
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(USER));
        when(userTokenRepository.countByEnabledIsFalseAndOrganisation(eq(TENANT))).thenReturn(expectedCount);

        assertThat(userTokenService.getDisabledTokenCount()).isEqualTo(47L);
        verify(userTokenRepository).countByEnabledIsFalseAndOrganisation(any());
    }

    @Test
    void getTokenWithoutUser() {
        assertThat(userTokenService.getToken()).isEmpty();
    }
}