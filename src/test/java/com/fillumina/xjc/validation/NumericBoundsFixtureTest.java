package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class NumericBoundsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numericBounds";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("NumericBounds", option("targetNamespace", "a")));
    }
}
