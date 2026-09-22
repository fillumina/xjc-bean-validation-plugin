package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FacetSourceAccumulatorTest {

    @Test
    void warnsAndOmitsAPatternWhoseJavaTranslationDoesNotCompile() {
        FacetSourceAccumulator accumulator = new FacetSourceAccumulator();
        accumulator.apply(new PatternFacet("\\q"));
        accumulator.translatePatterns();
        RecordingLog log = new RecordingLog();

        LinkedHashSet<LinkedHashSet<String>> patterns = accumulator.translatedMultiPatterns(log);

        assertTrue(patterns.iterator().next().isEmpty());
        assertEquals(List.of("skipping XML Schema pattern '\\q': its Java translation does not compile"),
                log.warnings);
    }

    private static final class PatternFacet extends FacetSource {
        private final String pattern;

        private PatternFacet(String pattern) {
            this.pattern = pattern;
        }

        @Override Integer minLength() { return null; }
        @Override Integer maxLength() { return null; }
        @Override Integer length() { return null; }
        @Override Integer totalDigits() { return null; }
        @Override Integer fractionDigits() { return null; }
        @Override BigDecimal minInclusive() { return null; }
        @Override BigDecimal minExclusive() { return null; }
        @Override BigDecimal maxInclusive() { return null; }
        @Override BigDecimal maxExclusive() { return null; }
        @Override String pattern() { return pattern; }
        @Override LinkedHashSet<String> patternList() { return new LinkedHashSet<>(); }
        @Override String enumeration() { return null; }
        @Override LinkedHashSet<String> enumerationList() { return null; }
    }

    private static final class RecordingLog implements AnnotationLog {
        private final List<String> warnings = new ArrayList<>();

        @Override public void addAnnotation(String annotationName, Map<String, String> parameterMap) {
        }
        @Override public void info(String message) {
        }
        @Override public void warning(String message) {
            warnings.add(message);
        }
    }
}
