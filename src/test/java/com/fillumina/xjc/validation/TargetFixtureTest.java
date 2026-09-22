package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * {@code targetNamespace}: only the elements of the named namespace carry {@code @Valid}.
 *
 * <p>The schema holds two namespaces, {@code a} and {@code b}, and each holds one element of a complex
 * type, {@code aNote} and {@code bNote}. {@code @Valid} cascades only into a complex type, so those
 * two elements are what show the option at work, and each case compares one file holding every class
 * the run generated.
 *
 * <p>The last case passes the literal text {@code null}, which the option reads like an unset one, so
 * both elements carry the annotation.
 */
class TargetFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "target";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("TargetANamespace", notNullOff(), namespace("a")),
                Case.of("TargetBNamespace", notNullOff(), namespace("b")),
                Case.of("TargetNullNamespace", notNullOff(), namespace("null")));
    }

    /** The {@code @NotNull} of the required elements is off, so only {@code @Valid} is under test. */
    private static String notNullOff() {
        return option("generateNotNullAnnotations", "false");
    }

    private static String namespace(String value) {
        return option("targetNamespace", value);
    }
}
