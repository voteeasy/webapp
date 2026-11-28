package de.voteeasy.gathering;

import de.voteeasy.AbstractIT;
import de.voteeasy.tenant.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.util.StringUtils;

import jakarta.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatheringValidationTest extends AbstractIT {

    @Autowired
    private GatheringService gatheringService;

    @Test
    void whenNameIsMissing_thenExceptionIsThrown() {
        assertThatThrownBy(() -> gatheringService.saveWithTenantAndUser(new Gathering()))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void whenNameIsTooLong_thenExceptionIsThrown() {
        assertThatThrownBy(() -> gatheringService.saveWithTenantAndUser(new Gathering(StringUtils.repeat("a", 300), new Tenant())))
                .isInstanceOf(ConstraintViolationException.class);
    }

}