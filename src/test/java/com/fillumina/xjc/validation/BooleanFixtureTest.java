package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * A boolean the schema pins with {@code fixed}: {@code @AssertTrue} for {@code true} and {@code 1},
 * {@code @AssertFalse} for {@code false} and {@code 0}, on an element and on an attribute — and only
 * {@code @NotNull} for a boolean the schema leaves free.
 */
class BooleanFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "boolean";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("BooleanFixedValues", option("targetNamespace", "a")));
    }
}
