package de.voteeasy.participation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Participant {
    private String gathering;
    private Long since;
    private String userName;
    private String fullName;
}
