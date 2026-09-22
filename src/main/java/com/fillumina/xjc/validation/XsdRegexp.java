package com.fillumina.xjc.validation;

import java.util.regex.Pattern;

/**
 * The regular expressions of an XML Schema, in the dialect Java's {@link Pattern} understands.
 */
final class XsdRegexp {

    /** @return the text quoted, so that a value with regexp characters matches itself. */
    static String quote(String value) {
        return Pattern.quote(value);
    }

    /** @return the pattern with the XML Schema shorthands replaced by their Java equivalents. */
    static String translate(String pattern) {
        return pattern
                .replace("\\i", "[_:A-Za-z]")
                .replace("\\c", "[-._:A-Za-z0-9]")
                .replace("{IsBasicLatin}", "{InBasicLatin}");
    }

    /**
     * @return false for a pattern of nothing but {@code \c}, the name character class of XML Schema,
     *     which is dropped rather than translated: this check runs before {@link #translate(String)}.
     */
    static boolean isSupported(String pattern) {
        return pattern != null && !"\\c+".equals(pattern);
    }

    private XsdRegexp() {
    }
}
