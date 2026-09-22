package com.fillumina.xjc.validation;

import java.util.stream.Stream;

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
