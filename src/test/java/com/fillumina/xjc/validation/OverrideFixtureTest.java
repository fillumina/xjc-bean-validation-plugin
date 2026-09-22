package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * The {@code override} option: leaving an annotation out, setting one of its parameters, or writing
 * another annotation in its place, on a class, a property or a glob.
 */
class OverrideFixtureTest extends FixtureTest {

    private static final String NAMESPACE = "a";

    @Override
    String fixture() {
        return "override";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("OverrideBaseline", namespace()),
                Case.of("OverrideDrops", namespace(), override("*#code")),
                Case.of("OverrideDropsAnItemAnnotation", namespace(), override("*#children@List.Valid")),
                Case.of("OverrideDropsTheCollectionAnnotation", namespace(), override("*#labels@Size")),
                Case.of("OverrideDropsTheItemAnnotation", namespace(), override("*#labels@List.Size")),
                Case.of("OverrideDropsBothSizeAnnotations", namespace(),
                        override("*#labels@Size"), override("*#labels@List.Size")),
                Case.of("OverrideGlob", namespace(), override("*RootType#*")),
                Case.of("OverridePlaceholderDefaults", namespace(),
                        override("*#child=@NotNull(message = \"{message}\")")),
                Case.of("OverrideUsesDeclarationAndComputedPlaceholders", namespace(),
                        override("*#label@Size:message = {className}.{fieldName} has at most {max} characters")),
                Case.of("OverrideRepeatedStatements", namespace(),
                        override("*#code"), override("*#label=@Size(max = {max})")),
                Case.of("OverrideReplacesAnItemAnnotation", namespace(),
                        override("*#children@List.Valid=@NotNull")),
                Case.of("OverrideReplacesTheItemSize", namespace(),
                        override("*#labels@List.Size=@NotNull")),
                Case.of("OverrideRewritesAnotherAnnotation", namespace(),
                        override("*#amount=@Digits(integer = 3, fraction = 2)")),
                Case.of("OverrideRewritesSameAnnotation", namespace(),
                        override("*#label=@Size(max = {max})")),
                Case.of("OverrideSetsAMessage", namespace(),
                        override("*#label@Size:message = at most {max} characters")),
                Case.of("OverrideSetsANumericParameter", namespace(),
                        override("*#label@Size:max = 5")),
                Case.of("OverrideSetsACollectionParameter", namespace(),
                        override("*#labels@Size:max = 10")),
                Case.of("OverrideSetsAnItemParameter", namespace(),
                        override("*#labels@List.Size:max = 3")),
                Case.of("OverrideTargetsAGlob", namespace(), override("*#amount@Decimal*")),
                Case.of("OverrideTargetsOneAnnotation", namespace(), override("*#label@NotNull")),
                Case.of("OverrideTargetsWithAReplacement", namespace(),
                        override("*#label@NotNull=@NotNull(message = \"required\")")),
                Case.of("OverrideWholeClass", namespace(), override("a.ChildType")));
    }

    private static String namespace() {
        return option("targetNamespace", NAMESPACE);
    }

    private static String override(String statement) {
        return option("override", statement);
    }
}