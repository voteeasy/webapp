package de.voteeasy.tenant;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Service
@Slf4j
@PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
@Transactional(readOnly = true)
class TenantExportService {
    private static final String TEXT_CSV = "text/csv";
    private static final String FILENAME_HEADER = "attachment; filename=tenants_%s.csv";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder().setTrim(true).setHeader("Number", "Name", "CreatedAt", "Enabled", "Total Registered Users", "Total UserToken", "Available token including disabled tokens", "Disabled token", "Already redeemed token").get();

    private final TenantStatisticsService tenantStatisticsService;

    @Autowired
    TenantExportService(TenantStatisticsService tenantStatisticsService) {
        this.tenantStatisticsService = tenantStatisticsService;
    }

    void export(HttpServletResponse response, @NotNull Set<Tenant> tenants) throws IOException {
        if (response != null) {
            StopWatch sw = new StopWatch();
            sw.start();
            response.setContentType(TEXT_CSV);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    String.format(FILENAME_HEADER, LocalDateTime.now()));

            CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSV_FORMAT);
            if (tenants != null) {
                int count = 0;
                for (Tenant t : tenants) {
                    csvPrinter.printRecord(
                            ++count,
                            t.getName(),
                            Instant.ofEpochMilli(t.getCreatedAt()).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                            t.isEnabled(),
                            tenantStatisticsService.getActiveUserCount(t),
                            tenantStatisticsService.getUserTokenCount(t),
                            tenantStatisticsService.getAvailableUserTokenCount(t),
                            tenantStatisticsService.getDisabledUserTokenCount(t),
                            tenantStatisticsService.getAlreadyRedeemedUserTokenCount(t)
                    );
                }
            }
            csvPrinter.flush();
            log.info("Generation of Tenant CSV took {} ms", sw.getDuration());
        } else {
            log.error("Given response for data export was null, this must not happen. Unable to generate CSV export.");
        }
    }
}
