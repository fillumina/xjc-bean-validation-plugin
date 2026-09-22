package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class MultiplePatternWithMultiPatternFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "multiplePatternWithMultiPattern";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("MultiplePatternWithMultiPattern", option("multiPattern", "true")));
    }
}
