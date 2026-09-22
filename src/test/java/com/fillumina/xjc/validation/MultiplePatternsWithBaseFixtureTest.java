package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class MultiplePatternsWithBaseFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "multiplePatternsWithBase";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("MultiplePatternsWithBase", option("targetNamespace", "a")));
    }
}
