package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class MultiplePatternsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "multiplePatterns";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("MultiplePattern", option("targetNamespace", "a")));
    }
}
