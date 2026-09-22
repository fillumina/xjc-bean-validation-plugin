package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * The bounds that are the natural limits of the Java type: written by default, left out when
 * {@code omitJavaTypeBounds} asks for it.
 */
class JavaTypeBoundsFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "javaTypeBounds";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("JavaTypeBoundsWritten"),
                Case.of("JavaTypeBoundsOmitted",
                        option("omitJavaTypeBounds", "true")));
    }
}
