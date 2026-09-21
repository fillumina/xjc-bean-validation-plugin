package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code numericBounds} of the old line, ported unchanged. */
class NumericBoundsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numericBounds";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("NumericBounds", option("targetNamespace", "a")));
    }
}
