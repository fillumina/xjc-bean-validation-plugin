package com.fillumina.xjc.validation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Translates the XML Schema regular-expression constructs this plugin supports into Java's dialect.
 *
 * <p>The XML name classes use the XML 1.0 Fifth Edition ranges. Other translations follow XML
 * Schema Part 2, Appendix F.
 */
final class XsdRegexp {

    private static final String XML_NAME_START =
            "[:A-Z_a-z\\x{C0}-\\x{D6}\\x{D8}-\\x{F6}\\x{F8}-\\x{2FF}"
            + "\\x{370}-\\x{37D}\\x{37F}-\\x{1FFF}\\x{200C}-\\x{200D}"
            + "\\x{2070}-\\x{218F}\\x{2C00}-\\x{2FEF}\\x{3001}-\\x{D7FF}"
            + "\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFFD}\\x{10000}-\\x{EFFFF}]";
    private static final String XML_NAME_CHARACTER =
            "[-.0-:A-Z_a-z\\x{B7}\\x{C0}-\\x{D6}\\x{D8}-\\x{F6}\\x{F8}-\\x{37D}"
            + "\\x{37F}-\\x{1FFF}\\x{200C}-\\x{200D}\\x{203F}-\\x{2040}"
            + "\\x{2070}-\\x{218F}\\x{2C00}-\\x{2FEF}\\x{3001}-\\x{D7FF}"
            + "\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFFD}\\x{10000}-\\x{EFFFF}]";

    private XsdRegexp() {
    }

    /** @return the text quoted, so that a value with regexp characters matches itself. */
    static String quote(String value) {
        return Pattern.quote(value);
    }

    /** @return the XML Schema pattern rewritten into Java regular-expression syntax. */
    static String translate(String pattern) {
        StringBuilder java = new StringBuilder();
        int characterClassDepth = 0;
        for (int index = 0; index < pattern.length(); index++) {
            char character = pattern.charAt(index);
            if (character == '\\' && index + 1 < pattern.length()) {
                index = translateEscape(pattern, index, java);
                continue;
            }
            if (character == '[') {
                characterClassDepth++;
                java.append(character);
            } else if (character == ']') {
                characterClassDepth--;
                java.append(character);
            } else if (character == '-' && characterClassDepth > 0
                    && index + 1 < pattern.length() && pattern.charAt(index + 1) == '[') {
                java.append("&&[^");
                characterClassDepth++;
                index++;
            } else if (characterClassDepth == 0 && (character == '.' || character == '^' || character == '$')) {
                translateMetaCharacter(character, java);
            } else {
                java.append(character);
            }
        }
        return java.toString();
    }

    private static int translateEscape(String pattern, int index, StringBuilder java) {
        char escaped = pattern.charAt(index + 1);
        switch (escaped) {
            case 'i' -> java.append(XML_NAME_START);
            case 'I' -> appendComplement(java, XML_NAME_START);
            case 'c' -> java.append(XML_NAME_CHARACTER);
            case 'C' -> appendComplement(java, XML_NAME_CHARACTER);
            case 'd' -> java.append("\\p{Nd}");
            case 'D' -> java.append("\\P{Nd}");
            case 'w' -> java.append("[^\\p{P}\\p{Z}\\p{C}]");
            case 'W' -> java.append("[\\p{P}\\p{Z}\\p{C}]");
            case 's' -> java.append("[ \\t\\n\\r]");
            case 'S' -> java.append("[^ \\t\\n\\r]");
            case 'p', 'P' -> {
                int end = pattern.indexOf('}', index + 3);
                if (index + 4 < pattern.length() && pattern.charAt(index + 2) == '{'
                        && pattern.startsWith("Is", index + 3) && end >= 0) {
                    java.append('\\').append(escaped).append("{In")
                            .append(javaBlockName(pattern.substring(index + 5, end))).append('}');
                    return end;
                }
                java.append('\\').append(escaped);
            }
            default -> java.append('\\').append(escaped);
        }
        return index + 1;
    }

    /** Maps the XML Schema block spelling to Java's {@link Character.UnicodeBlock} spelling. */
    private static String javaBlockName(String xsdBlockName) {
        return switch (xsdBlockName) {
            case "PrivateUse" -> "PrivateUseArea";
            default -> xsdBlockName;
        };
    }

    private static void appendComplement(StringBuilder java, String characterClass) {
        java.append("[^").append(characterClass, 1, characterClass.length());
    }

    private static void translateMetaCharacter(char character, StringBuilder java) {
        switch (character) {
            case '.' -> java.append("[^\\n\\r]");
            case '^', '$' -> java.append('\\').append(character);
            default -> throw new IllegalArgumentException("unexpected metacharacter: " + character);
        }
    }

    /** @return whether the translated expression compiles in Java. */
    static boolean isSupported(String pattern) {
        if (pattern == null) {
            return false;
        }
        try {
            Pattern.compile(translate(pattern));
            return true;
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }
}
