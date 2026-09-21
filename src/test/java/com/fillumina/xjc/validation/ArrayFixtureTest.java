package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * An array property, and the two cases that exercise an option written without a value.
 *
 * <p>The old line's cases also passed {@code generateServiceValidationAnnotations} that way, but that
 * option belongs to the frontend project now, and XJC refuses an argument no plugin consumes.
 *
 * <p>Three more expectation files sit in the old fixture, named after a test class that no longer
 * exists; they are not ported.
 */
class ArrayFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "array";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("Array", option("targetNamespace", "a")),
                Case.of("EmptyBooleanArgumentParser",
                        option("generateListAnnotations", ""),
                        option("generateNotNullAnnotations", ""),
                        option("verbose", "")),
                Case.of("MissingBooleanArgumentParser",
                        "-" + BeanValidationPlugin.PLUGIN_NAME + ":generateListAnnotations",
                        "-" + BeanValidationPlugin.PLUGIN_NAME + ":generateNotNullAnnotations",
                        "-" + BeanValidationPlugin.PLUGIN_NAME + ":verbose"));
    }
}
