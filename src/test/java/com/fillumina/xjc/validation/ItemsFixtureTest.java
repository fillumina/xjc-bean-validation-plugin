package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The constraints of the items of a collection, with the option that writes them switched off and on. */
class ItemsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "items";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("ItemAnnotationsDisabled",
                        option("generateNotNullAnnotations", "true"),
                        option("generateItemAnnotations", "false")),
                Case.of("ItemAnnotationsEnabled",
                        option("generateNotNullAnnotations", "true"),
                        option("generateItemAnnotations", "true")));
    }
}
