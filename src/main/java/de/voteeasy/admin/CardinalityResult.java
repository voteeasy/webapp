package de.voteeasy.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class CardinalityResult {
    private final Map<String, Long> tableCardinalities = new TreeMap<>(Comparator.comparing(String::toString));

    public void put(String tableName, Long cardinality) {
        tableCardinalities.put(tableName, cardinality);
    }

    public Long calculateAndGetTotal() {
        return tableCardinalities.values().stream().mapToLong(cardinality -> cardinality).sum();
    }

    public List<Object> getAsChart() {
        if (tableCardinalities.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object> innerElements = new ArrayList<>(tableCardinalities.size());
        for (Map.Entry<String, Long> element : tableCardinalities.entrySet()) {
            innerElements.add(List.of(element.getKey(), element.getValue()));
        }

        // sort by name/key
        innerElements.sort(Comparator.comparing(Object::toString));

        return innerElements;

    }

}
