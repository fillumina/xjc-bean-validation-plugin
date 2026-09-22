package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class XsdRegexpTest {

    @Test
    void translatesUnicodeCharacterShorthandsInsteadOfUsingJavasDefaults() {
        assertMatches("\\d+", "42", "١٢");
        assertDoesNotMatch("\\d+", "A");
        assertMatches("\\D+", "A");
        assertDoesNotMatch("\\D+", "١٢");

        assertMatches("\\w+", "é", "€");
        assertDoesNotMatch("\\w+", "_", " ");
        assertMatches("\\W+", "_", " ");
        assertDoesNotMatch("\\W+", "é");

        assertMatches("\\s+", " \t\n\r");
        assertDoesNotMatch("\\s+", "\u000B");
        assertMatches("\\S+", "A", "\u000B");
        assertDoesNotMatch("\\S+", " ");
    }

    @Test
    void translatesTheFullXmlNameClassesAndTheirComplements() {
        assertMatches("\\i\\c*", "xmlName", "éclair", "名123");
        assertDoesNotMatch("\\i\\c*", "7name", "!name");
        assertMatches("\\c+", "éclair", "名123", "7-name");
        assertDoesNotMatch("\\c+", "name!");
        assertMatches("\\I+", "7", "!");
        assertDoesNotMatch("\\I+", "é");
        assertMatches("\\C+", "!");
        assertDoesNotMatch("\\C+", "é");
    }

    @Test
    void translatesXsdBlocksAndLeavesGeneralCategoriesInJavasCompatibleSyntax() {
        assertMatches("\\p{IsLatin-1Supplement}+", "é");
        assertDoesNotMatch("\\p{IsLatin-1Supplement}+", "A");
        assertMatches("\\P{IsLatin-1Supplement}+", "A");
        assertDoesNotMatch("\\P{IsLatin-1Supplement}+", "é");
        assertMatches("\\p{Lu}+", "ABC");
        assertDoesNotMatch("\\p{Lu}+", "Abc");
    }

    @Test
    void translatesSubtractionAndEscapesJavaOnlyMetacharacters() {
        assertMatches("[a-z-[aeiou]]+", "bcd");
        assertDoesNotMatch("[a-z-[aeiou]]+", "ace");
        assertMatches("^abc$", "^abc$");
        assertDoesNotMatch("^abc$", "abc");
        assertMatches("a.b", "a\u2028b");
        assertDoesNotMatch("a.b", "a\nb");
    }

    @Test
    void recognizesOnlyPatternsThatCompileAfterTranslation() {
        assertTrue(XsdRegexp.isSupported("[a-z-[aeiou]]+"));
        assertTrue(XsdRegexp.isSupported("\\I+"));
        assertFalse(XsdRegexp.isSupported(null));
    }

    private static void assertMatches(String xsdRegexp, String... values) {
        Pattern javaRegexp = Pattern.compile(XsdRegexp.translate(xsdRegexp));
        for (String value : values) {
            assertTrue(javaRegexp.matcher(value).matches(),
                    () -> xsdRegexp + " should match " + printable(value));
        }
    }

    private static void assertDoesNotMatch(String xsdRegexp, String... values) {
        Pattern javaRegexp = Pattern.compile(XsdRegexp.translate(xsdRegexp));
        for (String value : values) {
            assertFalse(javaRegexp.matcher(value).matches(),
                    () -> xsdRegexp + " should not match " + printable(value));
        }
    }

    private static String printable(String value) {
        return value.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
