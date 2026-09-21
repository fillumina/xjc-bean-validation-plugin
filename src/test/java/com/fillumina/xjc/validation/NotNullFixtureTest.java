package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * The message of {@code @NotNull}, one case per value of the option that decides it.
 *
 * <p>The old fixture kept two expectation files per case, one per annotation library; there is one
 * library here, so the jakarta ones are the source. A bare {@code annotation.txt} and the
 * {@code -javax-} files are not ported.
 */
class NotNullFixtureTest extends FixtureTest {

    private static final String MESSAGES = "notNullAnnotationsCustomMessages";

    @Override
    String fixture() {
        return "notNull";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("ClassName", option("targetNamespace", "a"),
                        option(MESSAGES, "ClassName")),
                Case.of("NotNullDefault", option("targetNamespace", "a"),
                        option(MESSAGES, "false")),
                Case.of("NotNullMessageClassName", option("targetNamespace", "a"),
                        option(MESSAGES, "ClassName")),
                Case.of("NotNullMessageFalse", option("targetNamespace", "a"),
                        option(MESSAGES, "false")),
                Case.of("NotNullMessageFieldName", option("targetNamespace", "a"),
                        option(MESSAGES, "FieldName")),
                Case.of("NotNullMessageText", option("targetNamespace", "a"),
                        option(MESSAGES, "{FieldName} in {ClassName} should be not null")),
                Case.of("NotNullMessageTrue", option("targetNamespace", "a"),
                        option(MESSAGES, "true")));
    }
}
