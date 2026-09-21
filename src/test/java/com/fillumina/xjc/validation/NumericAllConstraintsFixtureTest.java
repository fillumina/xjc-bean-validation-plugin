package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * {@code generateAllNumericConstraints}, off and on: off leaves out the bounds that are the natural
 * limits of the Java type, on writes them too.
 *
 * <p>The old line also carries an expectation file named after a test class that no longer exists;
 * it is not ported, and the two cases below are the ones its tests drove.
 */
class NumericAllConstraintsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "numericAllConstraints";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("NumericAllConstraintsDisabled",
                        option("generateAllNumericConstraints", "false")),
                Case.of("NumericAllConstraintsEnabled",
                        option("generateAllNumericConstraints", "true")));
    }
}
