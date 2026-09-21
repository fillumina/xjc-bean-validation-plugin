package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** A complex type that extends another one. */
class ExtendFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "extend";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Extend", option("targetNamespace", "a")));
    }
}
