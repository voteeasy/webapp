package de.voteeasy.export;

import de.voteeasy.invitation.UserToken;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTokenCSVServiceTest {
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private HttpServletResponse response;
    @Mock
    private PrintWriter writer;

    @InjectMocks
    private UserTokenCSVService userTokenCSVService;

    private static UserToken getToken() {
        return UserToken.builder().token("EATBEET").createdAt(1L).build();
    }

    @Test
    void wiringWorks() {
        assertThat(userTokenCSVService).isNotNull();
    }

    @Test
    void getUsableTokenCheckCsvContents() throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        User user = new User("UUSER", "FFNAME", "pw", new Tenant("TTENANT"));
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        userTokenCSVService.export(response, Sets.newLinkedHashSet(getToken()));

        verify(applicationUserService).currentlyLoggedInUser();
        String csvResult = stringWriter.toString();
        // do not check for equality to avoid line break fiddling
        assertThat(csvResult).contains("Number,Token,CreatedAt").contains("1,EATBEET,1970-01-01");
    }

    @Test
    void getUsableTokenWithMockResponse() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        User user = new User("UUSER", "FFNAME", "pw", new Tenant("TTENANT"));
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        userTokenCSVService.export(response, Sets.newLinkedHashSet(getToken()));

        String expectedFilename = "attachment; filename=tokens_UUSER_TTENANT_" + LocalDate.now() + ".csv";
        verify(applicationUserService).currentlyLoggedInUser();
        verify(response).setContentType(eq("text/csv"));
        verify(response).setHeader(eq("Content-Disposition"), eq(expectedFilename));
    }

    @Test
    void getUsableTokenWithNullParameterResponse() throws IOException {
        userTokenCSVService.export(null, null);
    }

    @Test
    void getUsableTokenWithNullParameterTokens() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        userTokenCSVService.export(response, null);
    }

    @Test
    void getUsableTokenWithoutUser() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.empty());

        userTokenCSVService.export(response, Sets.newLinkedHashSet(getToken()));
        verify(applicationUserService).currentlyLoggedInUser();
    }

    @Test
    void getUsableTokenWithoutDataGiven() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        userTokenCSVService.export(response, new HashSet<>());

        verify(applicationUserService).currentlyLoggedInUser();
    }

}