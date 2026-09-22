package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.Test;

/** Compares the XSD validator with the Java expression the plugin will emit. */
class XsdRegexpCompatibilityTest {

    @Test
    void translatedPatternsAgreeWithTheXsdValidatorForMeasuredCommonCases() throws Exception {
        for (Case test : List.of(
                new Case("\\d+", "42", "١٢", "A"),
                new Case("\\w+", "word", "é", "_"),
                new Case("\\i\\c*", "xmlName", "éclair", "7name"),
                new Case("\\c+", "name-7", "éclair", "name!"),
                new Case("\\I+", "7", "!", "é"),
                new Case("\\C+", "!", "é"),
                new Case("[\\i-[:]][\\c-[:]]*", "xmlName", "éclair", ":name"),
                new Case("[a-z-[aeiou]]+", "bcd", "ace"),
                new Case("[\\p{L}-[\\p{Lu}]]+", "é", "A"),
                new Case("(ab|cd){1,2}", "ab", "abcd", "ac"),
                new Case("[A-Z]{2}[0-9]{3}", "AB123", "A123"),
                new Case("\\.\\?\\*\\+", ".?*+", "?*+"),
                new Case("\\p{IsLatin-1Supplement}+", "é", "A"),
                new Case("\\p{IsGreek}+", "Ω", "A"),
                new Case("\\p{IsCyrillic}+", "Ж", "A"),
                new Case("\\p{IsArabic}+", "ب", "A"),
                new Case("\\p{IsCJKUnifiedIdeographs}+", "漢", "A"),
                new Case("\\p{IsPrivateUse}+", "\uE000", "A"),
                new Case("\\P{IsLatin-1Supplement}+", "A", "é"),
                new Case("\\p{Lu}+", "ABC", "Abc"),
                new Case("^abc$", "^abc$", "abc"))) {
            assertSameResult(test);
        }
    }

    @Test
    void allXsdGeneralCategoriesCompileInBothRegexEngines() throws Exception {
        for (String category : List.of(
                "L", "Lu", "Ll", "Lt", "Lm", "Lo", "M", "Mn", "Mc", "Me",
                "N", "Nd", "Nl", "No", "P", "Pc", "Pd", "Ps", "Pe", "Pi", "Pf", "Po",
                "Z", "Zs", "Zl", "Zp", "S", "Sm", "Sc", "Sk", "So",
                "C", "Cc", "Cf", "Co", "Cn")) {
            assertCompilesInBothRegexEngines("\\p{" + category + "}+");
            assertCompilesInBothRegexEngines("\\P{" + category + "}+");
        }
    }

    private static void assertCompilesInBothRegexEngines(String xsdPattern) throws Exception {
        schema(xsdPattern);
        Pattern.compile(XsdRegexp.translate(xsdPattern));
    }

    @Test
    void allXsdUnicodeBlocksCompileInBothRegexEngines() throws Exception {
        for (String block : List.of(
                "BasicLatin", "Latin-1Supplement", "LatinExtended-A", "LatinExtended-B",
                "IPAExtensions", "SpacingModifierLetters", "CombiningDiacriticalMarks", "Greek",
                "Cyrillic", "Armenian", "Hebrew", "Arabic", "Syriac", "Thaana", "Devanagari",
                "Bengali", "Gurmukhi", "Gujarati", "Oriya", "Tamil", "Telugu", "Kannada",
                "Malayalam", "Sinhala", "Thai", "Lao", "Tibetan", "Myanmar", "Georgian",
                "HangulJamo", "Ethiopic", "Cherokee", "UnifiedCanadianAboriginalSyllabics",
                "Ogham", "Runic", "Khmer", "Mongolian", "LatinExtendedAdditional", "GreekExtended",
                "GeneralPunctuation", "SuperscriptsandSubscripts", "CurrencySymbols",
                "CombiningMarksforSymbols", "LetterlikeSymbols", "NumberForms", "Arrows",
                "MathematicalOperators", "MiscellaneousTechnical", "ControlPictures",
                "OpticalCharacterRecognition", "EnclosedAlphanumerics", "BoxDrawing", "BlockElements",
                "GeometricShapes", "MiscellaneousSymbols", "Dingbats", "BraillePatterns",
                "CJKRadicalsSupplement", "KangxiRadicals", "IdeographicDescriptionCharacters",
                "CJKSymbolsandPunctuation", "Hiragana", "Katakana", "Bopomofo",
                "HangulCompatibilityJamo", "Kanbun", "BopomofoExtended", "CJKCompatibility",
                "CJKUnifiedIdeographsExtensionA", "CJKUnifiedIdeographs", "YiSyllables", "YiRadicals",
                "HangulSyllables", "PrivateUse", "CJKCompatibilityIdeographs",
                "AlphabeticPresentationForms", "ArabicPresentationForms-A", "CombiningHalfMarks",
                "CJKCompatibilityForms", "SmallFormVariants", "ArabicPresentationForms-B",
                "HalfwidthandFullwidthForms", "Specials")) {
            assertCompilesInBothRegexEngines("\\p{Is" + block + "}+");
            assertCompilesInBothRegexEngines("\\P{Is" + block + "}+");
        }
    }

    /**
     * XML Schema defines {@code .} as every character except line feed and carriage return. The JDK
     * SchemaFactory currently rejects U+2028 here, while the standard-preserving Java translation
     * accepts it. Keep the processor difference visible while its policy is decided.
     */
    @Test
    void jdkSchemaFactoryDiffersFromTheStandardWildcardTranslation() throws Exception {
        String value = "a\u2028b";

        assertFalse(isValidInXsd(schema("a.b"), value));
        assertTrue(Pattern.compile(XsdRegexp.translate("a.b")).matcher(value).matches());
    }

    private static void assertSameResult(Case test) throws Exception {
        Schema schema = schema(test.pattern());
        Pattern javaPattern = Pattern.compile(XsdRegexp.translate(test.pattern()));
        for (String value : test.values()) {
            assertEquals(isValidInXsd(schema, value), javaPattern.matcher(value).matches(),
                    () -> test.pattern() + " disagreed for " + value);
        }
    }

    private static Schema schema(String pattern) throws Exception {
        String source = """
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                  <xs:element name="value"><xs:simpleType><xs:restriction base="xs:string">
                    <xs:pattern value="%s"/>
                  </xs:restriction></xs:simpleType></xs:element>
                </xs:schema>
                """.formatted(pattern);
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new StreamSource(new StringReader(source)));
    }

    private static boolean isValidInXsd(Schema schema, String value) {
        try {
            schema.newValidator().validate(new StreamSource(new StringReader("<value>" + value + "</value>")));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private record Case(String pattern, String... values) {
    }
}
