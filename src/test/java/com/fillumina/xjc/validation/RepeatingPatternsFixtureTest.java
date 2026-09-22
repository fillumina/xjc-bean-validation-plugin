package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class RepeatingPatternsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "repeatingPatterns";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("RepeatingPattern", option("targetNamespace", "a")));
    }
}
