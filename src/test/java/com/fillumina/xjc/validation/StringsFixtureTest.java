package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code strings} of the old line, ported unchanged. */
class StringsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "strings";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Strings", option("targetNamespace", "a")));
    }
}
