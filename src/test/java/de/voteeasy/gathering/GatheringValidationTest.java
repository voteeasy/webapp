package de.voteeasy.gathering;

import de.voteeasy.AbstractIT;
import de.voteeasy.tenant.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.util.StringUtils;

import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GatheringValidationTest extends AbstractIT {

    @Autowired
    private GatheringService gatheringService;

    @Test
    void whenNameIsMissing_thenExceptionIsThrown() {
        assertThrows(ConstraintViolationException.class, () -> gatheringService.saveWithTenantAndUser(new Gathering()));
    }

    @Test
    void whenNameIsTooLong_thenExceptionIsThrown() {
        assertThrows(ConstraintViolationException.class, () -> gatheringService.saveWithTenantAndUser(new Gathering(StringUtils.repeat("a", 300), new Tenant())));
    }

}