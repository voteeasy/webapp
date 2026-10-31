package de.voteeasy.export;

import de.voteeasy.invitation.UserToken;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@PreAuthorize("isAuthenticated()")
@Transactional
@Validated
public class UserTokenCSVService {
    private static final String NOBODY = "nobody";
    private static final String TEXT_CSV = "text/csv";
    private static final String FILENAME_HEADER = "attachment; filename=tokens_%s_%s.csv";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder().setTrim(true).setHeader("Number", "Token", "CreatedAt").get();

    private final ApplicationUserService applicationUserService;

    @Autowired
    public UserTokenCSVService(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    public void export(HttpServletResponse response, @NotNull Set<UserToken> tokens) throws IOException {
        if (response != null) {
            StopWatch sw = new StopWatch();
            sw.start();
            String currentDateTime = LocalDate.now().toString();
            Optional<User> usr = applicationUserService.currentlyLoggedInUser();

            response.setContentType(TEXT_CSV);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    String.format(FILENAME_HEADER,
                            usr.map(user -> user.getUserName() + '_' + user.getOrganisation().getName()).orElse(NOBODY),
                            currentDateTime));

            CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSV_FORMAT);
            if (tokens != null) {
                int count = 0;
                for (UserToken t : tokens) {
                    csvPrinter.printRecord(
                            ++count,
                            t.getToken(),
                            Instant.ofEpochMilli(t.getCreatedAt()).atZone(ZoneId.systemDefault()).toLocalDate()
                    );
                }
            }
            csvPrinter.flush();
            log.info("Generation of UserToken CSV took {} ms", sw.getDuration());
        } else {
            log.error("Given response for data export was null, this must not happen. Unable to generate CSV export.");
        }
    }
}
