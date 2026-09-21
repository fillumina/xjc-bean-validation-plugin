package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * {@code targetNamespace}: only the elements of the named namespace carry {@code @Valid}.
 *
 * <p>The schema holds two namespaces, {@code a} and {@code b}, and the old line kept one expectation
 * file per namespace, because its runner wrote one file per namespace. There is one file per case
 * here, holding every class the run generated, which is why the three cases below have one
 * expectation each rather than two.
 *
 * <p>The last case passes {@code null} as the namespace, which the old builder turned into the
 * literal text {@code null}: a namespace that matches nothing, so nothing carries the annotation.
 */
class TargetFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "target";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("TargetANamespace", notNullOff(), namespace("a")),
                Case.of("TargetBNamespaceJakarta", notNullOff(), namespace("b")),
                Case.of("TargetNullNamespaceJakarta", notNullOff(), namespace("null")));
    }

    /** The old cases also turned the {@code @NotNull} of the elements off. */
    private static String notNullOff() {
        return option("generateNotNullAnnotations", "false");
    }

    private static String namespace(String value) {
        return option("targetNamespace", value);
    }
}
