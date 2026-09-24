package com.fillumina.xjc.validation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The pattern constraints. A field and the items of a collection both carry them, so the code that
 * turns a set of schema patterns into annotations takes the annotator to write them into.
 *
 * @author Francesco Illuminati
 */
final class PatternAnnotations {

    private static final String REGEXP = "regexp";
    private static final String VALUE = "value";

    static void add(AnnotationWriter annotator, LinkedHashSet<LinkedHashSet<String>> multiPatterns,
            boolean patternList) {
        switch (multiPatterns.size()) {
            case 0:
                // do nothing at all
                break;
            case 1:
                addAll(annotator, multiPatterns.iterator().next());
                break;
            default:
                if (patternList) {
                    addList(annotator, multiPatterns);
                } else {
                    multiPatterns.forEach(patterns -> addAll(annotator, patterns));
                }
        }
    }

    /**
     * Writes each inherited pattern group as a nested {@code @Pattern} in {@code @Pattern.List}.
     * Groups from a derived type and its base are both mandatory (AND); alternatives within a group
     * are consolidated (OR). The writer also collects the nested values when an override applies.
     * <p>
     * see https://www.w3.org/TR/2011/CR-xmlschema11-2-20110721/datatypes.html#rf-pattern
     */
    private static void addList(AnnotationWriter annotator, LinkedHashSet<LinkedHashSet<String>> multiPatterns) {
        AnnotationWriter.Annotate.MultipleAnnotation multi = annotator
                .annotate(ValidationAnnotations.PATTERN_LIST)
                .multipleAnnotationContainer(VALUE);

        for (Set<String> patterns : multiPatterns) {
            switch (patterns.size()) {
                case 0:
                    // do nothing
                    break;
                case 1:
                    multi.annotate(ValidationAnnotations.PATTERN)
                            .param(REGEXP, patterns.iterator().next())
                            .log();
                    break;
                default:
                    multi.annotate(ValidationAnnotations.PATTERN)
                            .param(REGEXP, consolidate(patterns))
                            .log();
            }
        }
    }

    private static void addAll(AnnotationWriter annotator, Collection<String> patterns) {
        switch (patterns.size()) {
            case 0:
                // do nothing at all
                break;
            case 1:
                addOne(annotator, patterns.iterator().next());
                break;
            default:
                // all the patterns (A, B, C) as the options of a single one (A|B|C)
                addOne(annotator, consolidate(patterns));
        }
    }

    private static void addOne(AnnotationWriter annotator, String pattern) {
        annotator.annotate(ValidationAnnotations.PATTERN)
                .param(REGEXP, pattern)
                .log();
    }

    private static String consolidate(Collection<String> patterns) {
        StringBuilder regexp = new StringBuilder();
        for (String pattern : patterns) {
            regexp.append("(").append(pattern).append(")|");
        }
        return regexp.substring(0, regexp.length() - 1);
    }

    private PatternAnnotations() {
    }
}
