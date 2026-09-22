package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class PatternWithBaseFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "patternWithBase";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("PatternWithBase", option("targetNamespace", "a")));
    }
}
