package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class EnumerationWithBaseFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "enumerationWithBase";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("EnumerationWithBase", option("targetNamespace", "a")));
    }
}
