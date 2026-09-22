package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * The {@code exclude} option: leaving an annotation out, setting one of its parameters, or writing
 * another annotation in its place, on a class, a property or a glob.
 */
class ExcludeFixtureTest extends FixtureTest {

    private static final String NAMESPACE = "a";

    @Override
    String fixture() {
        return "exclude";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("ExcludeBaseline", namespace()),
                Case.of("ExcludeDrops", namespace(), exclude("*#code")),
                Case.of("ExcludeDropsAnItemAnnotation", namespace(), exclude("*#children@Valid")),
                Case.of("ExcludeGlob", namespace(), exclude("*RootType#*")),
                Case.of("ExcludePlaceholderDefaults", namespace(),
                        exclude("*#child=@NotNull(message = \"{message}\")")),
                Case.of("ExcludeRepeatedStatements", namespace(),
                        exclude("*#code"), exclude("*#label=@Size(max = {max})")),
                Case.of("ExcludeReplacesAnItemAnnotation", namespace(),
                        exclude("*#children@Valid=@NotNull")),
                Case.of("ExcludeRewritesAnotherAnnotation", namespace(),
                        exclude("*#amount=@Digits(integer = 3, fraction = 2)")),
                Case.of("ExcludeRewritesSameAnnotation", namespace(),
                        exclude("*#label=@Size(max = {max})")),
                Case.of("ExcludeSetsAMessage", namespace(),
                        exclude("*#label@Size:message = at most {max} characters")),
                Case.of("ExcludeSetsANumericParameter", namespace(),
                        exclude("*#label@Size:max = 5")),
                Case.of("ExcludeTargetsAGlob", namespace(), exclude("*#amount@Decimal*")),
                Case.of("ExcludeTargetsOneAnnotation", namespace(), exclude("*#label@NotNull")),
                Case.of("ExcludeTargetsWithAReplacement", namespace(),
                        exclude("*#label@NotNull=@NotNull(message = \"required\")")),
                Case.of("ExcludeWholeClass", namespace(), exclude("a.ChildType")));
    }

    private static String namespace() {
        return option("targetNamespace", NAMESPACE);
    }

    private static String exclude(String statement) {
        return option("exclude", statement);
    }
}
