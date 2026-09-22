package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * The XSD numeric types and the annotations their facets produce.
 */
class PrimitiveFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "primitive";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("Primitive", option("targetNamespace", "a")));
    }
}
