package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class StringsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "strings";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Strings", option("targetNamespace", "a")));
    }
}
