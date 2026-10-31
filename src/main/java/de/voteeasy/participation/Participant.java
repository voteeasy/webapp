package de.voteeasy.participation;

import lombok.*;

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
