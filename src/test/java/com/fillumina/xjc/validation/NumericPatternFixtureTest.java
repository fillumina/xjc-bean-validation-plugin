package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code numericPattern} of the old line, ported unchanged. */
class NumericPatternFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numericPattern";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("NumericPattern", option("targetNamespace", "a")));
    }
}
