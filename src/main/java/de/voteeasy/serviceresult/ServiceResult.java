package de.voteeasy.serviceresult;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Base result class that allows all operations to transfer
 * <li>a message key to be shown in the UI</li>
 * <li>a type of result similar to HTTP status codes</li>
 * <li>named parameters to transfer results into the UI/to the caller</li>
 */
@Data
@Builder(access = AccessLevel.PUBLIC)
public class ServiceResult {
    private String messageKey;
    private HttpStatus status;

    @Builder.Default
    private Map<String, String> payload = new HashMap<>();

    public void add(String key, String value) {
        payload.put(key, value);
    }

    public String get(String key) {
        return payload.getOrDefault(key, "");
    }

}
