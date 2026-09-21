package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code enumerationWithBase} of the old line, ported unchanged. */
class EnumerationWithBaseFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "enumerationWithBase";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("EnumerationWithBase", option("targetNamespace", "a")));
    }
}
