package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code numbers} of the old line, ported unchanged. */
class NumbersFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numbers";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Numbers", option("targetNamespace", "a")));
    }
}
