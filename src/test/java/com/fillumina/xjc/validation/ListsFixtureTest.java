package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * The constraints of the items of a collection, from the fixture the old line used: a list of a
 * restricted string, of a complex type, and of a decimal.
 */
class ListsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "lists";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("Lists", option("targetNamespace", "a")),
                Case.of("ListsDisabled", option("targetNamespace", "a"),
                        option("generateListAnnotations", "false")));
    }
}
