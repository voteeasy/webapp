package de.voteeasy.export;

import de.voteeasy.invitation.UserToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class UserTokenExcelServiceTest {

    @InjectMocks
    private UserTokenExcelService userTokenExcelService;

    @Test
    void wiringWorks() {
        assertThat(userTokenExcelService).isNotNull();
    }

    @Test
    void downloadAsExcelWithEmptyList() throws IOException {
        assertThat(userTokenExcelService.downloadAsExcel(Collections.emptySet())).isNotNull();
    }

    @Test
    void downloadAsExcelWithNullList() throws IOException {
        assertThat(userTokenExcelService.downloadAsExcel(null)).isNotNull();
    }

    @Test
    void downloadAsExcelWithElementsInList() throws IOException {
        assertThat(userTokenExcelService.downloadAsExcel(Sets.newSet(UserToken.builder().token("token1").build(), UserToken.builder().token("token2").build()))).isNotNull();
    }

    @Test
    void exportWithEmptyList() throws IOException {
        assertThat(userTokenExcelService.export(null)).isNotNull();
    }

    @Test
    void exportWithElementsInList() throws IOException {
        final ResponseEntity<InputStreamResource> responseEntity = userTokenExcelService.export(Sets.newSet(UserToken.builder().token("token1").build(), UserToken.builder().token("token2").build()));
        assertThat(responseEntity).isNotNull();
        HttpHeaders headers = responseEntity.getHeaders();
        assertThat(headers).isNotNull();
        assertThat(headers.getContentDisposition()).isNotNull();
        assertThat(responseEntity.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).contains("attachment; filename=UserToken.xlsx");
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    }
}