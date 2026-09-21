package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** A nillable element is not required, so it carries no {@code @NotNull}. */
class NillableFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "nillable";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Nillable", option("targetNamespace", "a")));
    }
}
