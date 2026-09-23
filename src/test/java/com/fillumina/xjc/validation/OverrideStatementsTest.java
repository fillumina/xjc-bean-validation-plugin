package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class OverrideStatementsTest {

    @Test
    void aCharacterClassMatchesOneOfItsCharacters() {
        assertTrue(matches("*#cod[e]", "code"));
        assertFalse(matches("*#cod[e]", "codf"));
        // the whole glob is anchored: a class is one character, not a prefix
        assertFalse(matches("*#cod[e]", "codee"));
    }

    @Test
    void aCharacterClassMatchesARange() {
        assertTrue(matches("*#item[0-9]", "item7"));
        assertFalse(matches("*#item[0-9]", "itemx"));
        assertTrue(matches("*#item[a-z]", "itemx"));
    }

    @Test
    void aCharacterClassIsNegatedWithAnExclamationMarkOrACaret() {
        assertFalse(matches("*#item[!0-9]", "item7"));
        assertTrue(matches("*#item[!0-9]", "itemx"));
        assertFalse(matches("*#item[^0-9]", "item7"));
        assertTrue(matches("*#item[^0-9]", "itemx"));
    }

    @Test
    void aStarAQuestionMarkAndABackslashInsideAClassAreLiteral() {
        assertTrue(matches("*#it[*]m", "it*m"));
        assertTrue(matches("*#it[?]m", "it?m"));
        assertFalse(matches("*#it[*]m", "item"));
        assertTrue(matches("*#it[\\]m", "it\\m"));
    }

    @Test
    void aClassIsMatchedOnTheClassGlobToo() {
        assertTrue(matchesClass("a.RootType", "a.RootTyp[e]#code", "code"));
        assertFalse(matchesClass("a.RootType", "a.RootTyp[f]#code", "code"));
        assertFalse(matchesClass("a.RootType", "a.RootTyp[e]#code", "label"));
    }

    @Test
    void aDotAndADollarOfAQualifiedNameAreLiteral() {
        assertTrue(matchesClass("a.RootType", "a.RootType#code", "code"));
        // a dot that were a metacharacter would match the "e" of RootType
        assertFalse(matchesClass("a.RootType", "a.RootTyp.#code", "code"));
        assertFalse(matchesClass("a.RootType", "aXRootType#code", "code"));
    }

    @Test
    void anUnclosedOrEmptyClassIsRejected() {
        assertNull(OverrideStatements.validate("*#item[0-9]"));
        assertNotNull(OverrideStatements.validate("*#item[0-9"));
        assertNotNull(OverrideStatements.validate("*#item[]"));
        assertNotNull(OverrideStatements.validate("*#item[!]"));
        assertNotNull(OverrideStatements.validate("*#item[^]"));
        // a set a pattern cannot read is an option error too, not a stack trace at generation
        assertNotNull(OverrideStatements.validate("*#item[[]"));
        assertNotNull(OverrideStatements.validate("*#item[9-0]"));
        assertNotNull(OverrideStatements.validate("*#item[a[b]"));
    }

    @Test
    void aStatementThatMatchesNoPropertyIsReportedAsUnmatched() {
        OverrideStatements statements = OverrideStatements.of(List.of("*#code[0]", "*#code"));
        statements.statementsFor("a.RootType", "code");
        assertEquals(List.of("*#code[0]"),
                statements.unmatched().stream().map(Object::toString).toList());
    }

    @Test
    void aHyphenIsLiteralAtTheStartOrTheEndOfASet() {
        assertTrue(matches("*#it[-a]m", "it-m"));
        assertTrue(matches("*#it[-a]m", "itam"));
        assertFalse(matches("*#it[-a]m", "itbm"));
        assertTrue(matches("*#it[a-]m", "it-m"));
        assertFalse(matches("*#it[a-]m", "itbm"));
    }

    @Test
    void aSetHoldsSeveralRanges() {
        assertTrue(matches("*#it[a-c0-9]m", "itbm"));
        assertTrue(matches("*#it[a-c0-9]m", "it5m"));
        assertFalse(matches("*#it[a-c0-9]m", "itdm"));
    }

    @Test
    void aCaretThatIsNotTheFirstCharacterOfASetIsLiteral() {
        assertTrue(matches("*#it[a^]m", "itam"));
        assertTrue(matches("*#it[a^]m", "it^m"));
        assertFalse(matches("*#it[a^]m", "itbm"));
    }

    @Test
    void aSetIsOneCharacterAmongTheOthersOfTheGlob() {
        assertTrue(matches("*#it[0-9]?m", "it7xm"));
        assertTrue(matches("*#it[0-9]?m", "it77m"));
        assertFalse(matches("*#it[0-9]?m", "it7m"));
    }

    @Test
    void severalSetsCanShareAGlob() {
        assertTrue(matches("*#li[n]e[1-3]", "line1"));
        assertTrue(matches("*#li[n]e[1-3]", "line3"));
        assertFalse(matches("*#li[n]e[1-3]", "line4"));
        assertFalse(matches("*#li[n]e[1-3]", "linex"));
    }

    @Test
    void theClosingBracketIsTheFirstOneAfterTheSet() {
        // the set ends at the first ], so a second one is an ordinary character
        assertTrue(matches("*#it[a]]m", "ita]m"));
        assertFalse(matches("*#it[a]]m", "itam"));
    }

    @Test
    void aSetIsMatchedOnTheAnnotationGlob() {
        assertTrue(covers("*#code@Siz[e]", "code", "Size", false));
        assertFalse(covers("*#code@Siz[e]", "code", "Sizf", false));
        assertFalse(covers("*#code@Siz[e]", "code", "Size", true));
    }

    @Test
    void aSetIsMatchedOnTheItemsGlob() {
        assertTrue(covers("*#labels@List.Siz[e]", "labels", "Size", true));
        assertFalse(covers("*#labels@List.Siz[e]", "labels", "Size", false));
        assertTrue(covers("*#labels@List.Siz[!f]", "labels", "Size", true));
    }

    @Test
    void aSetSelectsAClassThroughAPackageGlob() {
        assertTrue(matchesClass("a.RootType", "*Typ[e]#code", "code"));
        assertFalse(matchesClass("a.RootType", "*Typ[f]#code", "code"));
    }

    @Test
    void aSetWorksWithAParameterAndWithAReplacement() {
        String withParameter = "a.RootTyp[e]#labe[l]@Siz[e]:max = 10";
        assertNull(OverrideStatements.validate(withParameter));
        OverrideStatements.Statement parameter = statement(withParameter, "label");
        assertEquals("max", parameter.getParameter());
        assertEquals("10", parameter.getParameterValue());
        OverrideStatements.Statement replacement = statement("*#line[1-3]=@NotNull", "line1");
        assertTrue(replacement.hasReplacement());
        assertEquals("@NotNull", replacement.getReplacement());
    }

    private static boolean matches(String statement, String property) {
        return matchesClass("a.RootType", statement, property);
    }

    private static boolean matchesClass(String className, String statement, String property) {
        return !OverrideStatements.of(List.of(statement)).statementsFor(className, property).isEmpty();
    }

    private static OverrideStatements.Statement statement(String statement, String property) {
        return OverrideStatements.of(List.of(statement)).statementsFor("a.RootType", property).get(0);
    }

    private static boolean covers(String statement, String property, String annotation, boolean items) {
        return statement(statement, property).coversAnnotation(annotation, items);
    }
}
