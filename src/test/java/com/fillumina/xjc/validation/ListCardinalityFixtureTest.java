package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * An element whose type is an {@code xsd:list}, against the number of times the element occurs.
 *
 * <p>The Java field holds the items of the list, not the occurrences of the element, so the
 * occurrence count has no meaning on it; a list type that states how many items its value holds
 * states it there. The repeating element that is not a list, and the single optional one, are the
 * controls: on those the field holds the occurrences, and the cardinality belongs on the field.
 */
class ListCardinalityFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "listcardinality";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("ListCardinality", option("targetNamespace", "a")));
    }
}
