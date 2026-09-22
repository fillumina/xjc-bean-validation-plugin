package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class OptionsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "options";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("OptionDefault"),
                Case.of("OptionGenerateItemAnnotations",
                        option("generateNotNullAnnotations", "false"),
                        option("generateItemAnnotations", "true"),
                        option("targetNamespace", "null")));
    }
}
