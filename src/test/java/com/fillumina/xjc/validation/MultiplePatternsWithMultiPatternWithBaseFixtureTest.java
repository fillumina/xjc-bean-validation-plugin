package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code multiplePatternsWithMultiPatternWithBase} of the old line, ported with its cases. */
class MultiplePatternsWithMultiPatternWithBaseFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "multiplePatternsWithMultiPatternWithBase";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("MultiplePatternsWithMultiPatternWithBase", option("multiPattern", "true")));
    }
}
