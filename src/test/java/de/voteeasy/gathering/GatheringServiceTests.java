package de.voteeasy.gathering;

import de.voteeasy.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GatheringServiceTests extends AbstractIT {

    @Autowired
    private GatheringService gatheringService;

    @Test
    void ensureGatheringTableIsEmptyAfterBootup() {
        assertThat(gatheringService.getAllWithTenantCheck()).isEmpty();
    }

    @Test
    void deleteWithUnknownIdDoesNotThrowException() {
        gatheringService.deleteCascadinglyWithoutTenantCheck(UUID.randomUUID());
    }

    @Test
    void getWithUnknownIdDoesNotThrowException() {
        assertThat(gatheringService.getWithTenantCheck(UUID.randomUUID())).isEmpty();
    }

}
