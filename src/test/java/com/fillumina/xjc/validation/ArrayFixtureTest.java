package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * An array property, and the two cases that exercise an option written without a value.
 *
 * <p>An option written with no value is passed as the bare option name; an argument no plugin
 * consumes makes XJC fail.
 */
class ArrayFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "array";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("Array", option("targetNamespace", "a")),
                Case.of("OptionWithEmptyValue",
                        option("generateItemAnnotations", ""),
                        option("generateNotNullAnnotations", ""),
                        option("verbose", "")),
                Case.of("OptionWithNoValue",
                        "-" + BeanValidationPlugin.PLUGIN_NAME + ":generateItemAnnotations",
                        "-" + BeanValidationPlugin.PLUGIN_NAME + ":generateNotNullAnnotations",
                        "-" + BeanValidationPlugin.PLUGIN_NAME + ":verbose"));
    }
}
