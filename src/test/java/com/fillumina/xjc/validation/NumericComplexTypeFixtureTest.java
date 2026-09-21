package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** A complex type whose base type is a restricted simple type. */
class NumericComplexTypeFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numericComplexType";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("NumericComplexType", option("targetNamespace", "a")));
    }
}
