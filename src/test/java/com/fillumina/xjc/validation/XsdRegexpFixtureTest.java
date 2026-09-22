package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class XsdRegexpFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "xsdRegexp";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("XsdRegexp"));
    }
}
