package de.voteeasy.votes.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class RelatedGathering {
    UUID id;
    String name;
}
