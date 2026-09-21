package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** A schema whose content is a choice. */
class ChoicesFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "choices";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Choice", option("targetNamespace", "a")));
    }
}
