package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class NumericFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numeric";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Numeric", option("targetNamespace", "a")));
    }
}
