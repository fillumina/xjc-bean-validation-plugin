package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code multiplePatternWithMultiPattern} of the old line, ported with its cases. */
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
