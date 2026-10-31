package de.voteeasy.serviceresult;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceResultTest {

    @Test
    void add() {
        ServiceResult result = ServiceResult.builder().build();
        assertThat(result.getPayload()).isEmpty();

        result.add("key", String.valueOf(4711));
        assertThat(result.getPayload()).hasSize(1);
        assertThat(result.get("key")).isEqualTo("4711");
    }

    @Test
    void get() {
        ServiceResult result = ServiceResult.builder().payload(Maps.newHashMap("get", "me")).build();
        assertThat(result.get("fantasy")).isEqualTo("");
        assertThat(result.get("get")).contains("me");
    }
}