package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code abase} of the old line, ported with its cases. */
class AbaseFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "abase";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("Complex", option("targetNamespace", "")),
                Case.of("DefaultOption"));
    }
}
