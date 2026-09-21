package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code each} of the old line, ported with its cases. */
class EachFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "each";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("EachGenerateListAnnotationDisabled",
                        option("generateNotNullAnnotations", "true"),
                        option("generateListAnnotations", "false")),
                Case.of("EachGenerateListAnnotationEnabled",
                        option("generateNotNullAnnotations", "true"),
                        option("generateListAnnotations", "true")));
    }
}
