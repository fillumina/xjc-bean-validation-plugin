package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class NumericValueFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numericValue";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("NumericValue", option("targetNamespace", "a")));
    }
}
