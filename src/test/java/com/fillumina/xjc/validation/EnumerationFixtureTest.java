package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * An enumeration becomes a Java enum and its restrictions become a {@code @Pattern} whose options
 * are the escaped values of the schema.
 */
class EnumerationFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "enumeration";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Enumeration", option("targetNamespace", "a")));
    }
}
