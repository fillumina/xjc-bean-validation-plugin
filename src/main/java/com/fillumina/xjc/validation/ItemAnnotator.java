package com.fillumina.xjc.validation;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFieldVar;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The constraints of the items of a collection, written on the type argument of the field:
 * {@code List<@Size(max = 5) String>}.
 *
 * <p>The annotations are created on the field, because that is where codemodel renders and escapes
 * their parameters, and are taken off it again when {@link #apply()} puts them on the type argument.
 *
 * @author Francesco Illuminati
 */
class ItemAnnotator {

    private final JFieldVar field;
    private final AnnotationWriter annotator;

    ItemAnnotator(JFieldVar field, AnnotationLog logger) {
        this.field = field;
        this.annotator = new AnnotationWriter(field, logger);
    }

    void addSizeAnnotation(Integer minLength, Integer maxLength) {
        if (isSet(minLength) || isSet(maxLength)) {
            annotator.annotate(ValidationAnnotations.SIZE)
                    .paramIf(isSet(minLength), "min", minLength)
                    .paramIf(isSet(maxLength), "max", maxLength)
                    .log();
        }
    }

    void addDigitsAnnotation(Integer totalDigits, Integer fractionDigits) {
        if (isSet(totalDigits) || isSet(fractionDigits)) {
            annotator.annotate(ValidationAnnotations.DIGITS)
                    .param("integer", totalDigits, 0)
                    .param("fraction", fractionDigits, 0)
                    .log();
        }
    }

    void addDecimalMinAnnotation(BigDecimal minInclusive, BigDecimal minExclusive) {
        if (minInclusive != null || minExclusive != null) {
            annotator.annotate(ValidationAnnotations.DECIMAL_MIN)
                    .param("value", minInclusive)
                    .param("value", minExclusive)
                    .param("inclusive", minInclusive != null)
                    .log();
        }
    }

    void addDecimalMaxAnnotation(BigDecimal maxInclusive, BigDecimal maxExclusive) {
        if (maxInclusive != null || maxExclusive != null) {
            annotator.annotate(ValidationAnnotations.DECIMAL_MAX)
                    .param("value", maxInclusive)
                    .param("value", maxExclusive)
                    .param("inclusive", maxInclusive != null)
                    .log();
        }
    }

    void addPatterns(LinkedHashSet<LinkedHashSet<String>> patterns, boolean multiPattern) {
        PatternAnnotations.add(annotator, patterns, multiPattern);
    }

    void addValidAnnotation() {
        annotator.annotate(ValidationAnnotations.VALID).log();
    }

    /**
     * Puts the collected annotations on the type argument of the field. Does nothing when there is
     * none, and refuses a field whose type is not a one-argument generic.
     */
    void apply() {
        List<JAnnotationUse> annotations = annotator.detachAnnotations();
        if (annotations.isEmpty()) {
            return;
        }
        if (!(field.type() instanceof JClass)) {
            throw new IllegalStateException("the items of " + field.name()
                    + " cannot be annotated: its type is " + field.type().fullName());
        }
        JClass type = (JClass) field.type();
        List<JClass> arguments = type.getTypeParameters();
        if (arguments.size() != 1) {
            throw new IllegalStateException("the items of " + field.name()
                    + " cannot be annotated: its type is " + type.fullName());
        }
        field.type(type.erasure().narrow(new TypeUse(arguments.get(0), annotations)));
    }

    private static boolean isSet(Integer value) {
        return value != null && value != 0;
    }
}
