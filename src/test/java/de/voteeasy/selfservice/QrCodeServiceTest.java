package de.voteeasy.selfservice;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        this.qrCodeService = new QrCodeService();
    }

    @Test
    void setupWorkedFine() {
        assertThat(this.qrCodeService).isNotNull();
    }

    @Test
    void localhostLinkGeneration() {
        when(httpServletRequest.getLocalName()).thenReturn("localhosti");
        when(httpServletRequest.getLocalPort()).thenReturn(4711);
        String url = qrCodeService.generateLink(httpServletRequest, new QrCodeService.QrCodeParameter("myTenant", "myUser", "myPassword", Optional.empty(), Optional.empty()));
        assertThat(url).contains("http://localhosti:4711/").contains("login?").contains("myTenant").contains("myUser").contains("myPassword");
    }

    @Test
    void nonLocalLinkGeneration() {
        when(httpServletRequest.getLocalName()).thenReturn("reallydeployed");
        String url = qrCodeService.generateLink(httpServletRequest, new QrCodeService.QrCodeParameter("myTenant", "myUser", "myPassword", Optional.empty(), Optional.empty()));
        assertThat(url).contains("http://localhost:8080/").contains("login?").contains("myTenant").contains("myUser").contains("myPassword");
    }

    @Test
    void renderQrCodeWithNoRealRequest() {
        assertThat(qrCodeService.renderQrCode(httpServletRequest, new QrCodeService.QrCodeParameter("myTenant", "myUser", "myPassword", Optional.empty(), Optional.empty()))).isNotNull();
    }

    @Test
    void renderQrCodeWithLongURLToProvokeInternalError() {
        when(httpServletRequest.getLocalName()).thenReturn("localhost" + StringUtils.repeat(UUID.randomUUID().toString(), 1000));
        assertThat(qrCodeService.renderQrCode(httpServletRequest, new QrCodeService.QrCodeParameter("myTenant", "myUser", "myPassword", Optional.empty(), Optional.empty()))).isNotNull();
    }


}