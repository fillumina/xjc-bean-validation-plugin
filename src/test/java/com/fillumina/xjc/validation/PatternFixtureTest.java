package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code pattern} of the old line, ported with its cases. */
class PatternFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "pattern";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("Pattern", option("targetNamespace", "a")),
                Case.of("NoValidOnCollections", option("targetNamespace", "a"),
                        option("generateValidOnCollections", "false")));
    }
}
