package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code options} of the old line, ported with its cases. */
class OptionsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "options";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("OptionDefault"),
                Case.of("OptionGenerateStringListAnnotation",
                        option("generateNotNullAnnotations", "false"),
                        option("generateListAnnotations", "true"),
                        option("targetNamespace", "null")));
    }
}
