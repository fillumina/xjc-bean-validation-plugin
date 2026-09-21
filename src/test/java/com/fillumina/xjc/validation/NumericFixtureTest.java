package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code numeric} of the old line, ported unchanged. */
class NumericFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numeric";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Numeric", option("targetNamespace", "a")));
    }
}
