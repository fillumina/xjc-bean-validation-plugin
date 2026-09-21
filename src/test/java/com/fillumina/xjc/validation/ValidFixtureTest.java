package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** A schema with a list, a simple content type and the annotations that go with them. */
class ValidFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "valid";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Valid", option("targetNamespace", "a")));
    }
}
