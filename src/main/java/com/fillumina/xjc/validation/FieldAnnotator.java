package com.fillumina.xjc.validation;

import com.sun.codemodel.JFieldVar;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The constraints of a property, on the field itself or on the items of a collection.
 *
 * @author Francesco Illuminati
 */
class FieldAnnotator {

    private static final String FRACTION = "fraction";
    private static final String INTEGER = "integer";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String INCLUSIVE = "inclusive";
    private static final String VALUE = "value";
    private static final String MESSAGE = "message";

    private final AnnotationWriter fields;
    private final ItemAnnotator items;

    /**
     * @param collector when not null the annotations are collected instead of written, see
     *     {@link OverrideStatements}
     */
    FieldAnnotator(JFieldVar field, AnnotationLog logger, List<AnnotationWriter.Annotate> collector) {
        this.fields = new AnnotationWriter(field, logger, collector);
        this.items = new ItemAnnotator(field, logger, collector);
    }

    void addNotNullAnnotation(String message) {
        fields.annotate(ValidationAnnotations.NOT_NULL)
                .param(MESSAGE, message)
                .log();
    }

    /** @see ItemAnnotator#addValidAnnotation() */
    void addValidAnnotation() {
        fields.annotate(ValidationAnnotations.VALID).log();
    }

    /** For a boolean the schema pins to true, with {@code fixed="true"} or {@code fixed="1"}. */
    void addAssertTrueAnnotation() {
        fields.annotate(ValidationAnnotations.ASSERT_TRUE).log();
    }

    /** The same for a boolean pinned to false, with {@code fixed="false"} or {@code fixed="0"}. */
    void addAssertFalseAnnotation() {
        fields.annotate(ValidationAnnotations.ASSERT_FALSE).log();
    }

    void addSizeAnnotation(Integer minLength, Integer maxLength, Integer length) {
        if (isValidLength(minLength) || isValidLength(maxLength)) {
            fields.annotate(ValidationAnnotations.SIZE)
                    .paramIf(isValidLength(minLength), MIN, minLength)
                    .paramIf(isValidLength(maxLength), MAX, maxLength)
                    .log();

        } else if (isValidLength(length)) {
            fields.annotate(ValidationAnnotations.SIZE)
                    .param(MIN, length)
                    .param(MAX, length)
                    .log();
        }
    }

    void addDecimalMinAnnotationExclusive(BigDecimal min) {
        addDecimalMinAnnotation(min, true);
    }

    void addDecimalMinAnnotationInclusive(BigDecimal min) {
        addDecimalMinAnnotation(min, false);
    }

    private void addDecimalMinAnnotation(BigDecimal min, boolean exclusive) {
        if (min != null) {
            fields.annotate(ValidationAnnotations.DECIMAL_MIN)
                    .param(VALUE, min.toString())
                    .param(INCLUSIVE, !exclusive)
                    .log();
        }
    }

    void addDecimalMaxAnnotationExclusive(BigDecimal max) {
        addDecimalMaxAnnotation(max, true);
    }

    void addDecimalMaxAnnotationInclusive(BigDecimal max) {
        addDecimalMaxAnnotation(max, false);
    }

    private void addDecimalMaxAnnotation(BigDecimal max, boolean exclusive) {
        if (max != null) {
            fields.annotate(ValidationAnnotations.DECIMAL_MAX)
                    .param(VALUE, max.toString())
                    .param(INCLUSIVE, !exclusive)
                    .log();
        }
    }

    void addDigitsAnnotation(Integer totalDigits, Integer fractionDigits) {
        if (totalDigits != null) {
            fields.annotate(ValidationAnnotations.DIGITS)
                    .param(INTEGER, valueOrZero(totalDigits))
                    .param(FRACTION, valueOrZero(fractionDigits))
                    .log();
        }
    }

    void addPatterns(LinkedHashSet<LinkedHashSet<String>> patterns, boolean patternList) {
        PatternAnnotations.add(fields, patterns, patternList);
    }

    /** @return the constraints of the items of a collection, which go on the type argument. */
    ItemAnnotator items() {
        return items;
    }

    /** Writes the item constraints on the type argument of the field, once and for all. */
    void applyItemAnnotations() {
        items.apply();
    }

    static boolean isValidLength(Integer length) {
        return length != null && length != -1;
    }

    static Integer valueOrZero(Integer value) {
        return value == null ? Integer.valueOf(0) : value;
    }
}
