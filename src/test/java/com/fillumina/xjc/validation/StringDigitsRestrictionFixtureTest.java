package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * A string field whose type is restricted by numeric facets, which a binding file would have to
 * turn into a number: the annotations follow the facets, not the Java type.
 *
 * <p>The fixture also holds a binding file, which its test never passed to XJC in the old line
 * either, so it is not passed here.
 */
class StringDigitsRestrictionFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "stringDigitsRestriction";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("StringDigitsRestriction", option("targetNamespace", "a")));
    }
}
