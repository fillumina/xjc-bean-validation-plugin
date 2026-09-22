package com.fillumina.xjc.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Jakarta Bean Validation annotations this plugin writes, and the names it knows them by.
 */
final class ValidationAnnotations {

    static final Class<? extends Annotation> VALID = Valid.class;
    static final Class<? extends Annotation> NOT_NULL = NotNull.class;
    static final Class<? extends Annotation> SIZE = Size.class;
    static final Class<? extends Annotation> DIGITS = Digits.class;
    static final Class<? extends Annotation> DECIMAL_MIN = DecimalMin.class;
    static final Class<? extends Annotation> DECIMAL_MAX = DecimalMax.class;
    static final Class<? extends Annotation> PATTERN = Pattern.class;
    static final Class<? extends Annotation> PATTERN_LIST = Pattern.List.class;
    static final Class<? extends Annotation> ASSERT_TRUE = AssertTrue.class;
    static final Class<? extends Annotation> ASSERT_FALSE = AssertFalse.class;

    /** The annotations an {@code override} statement may name, by simple name. */
    static Map<String, Class<? extends Annotation>> bySimpleName() {
        Map<String, Class<? extends Annotation>> byName = new LinkedHashMap<>();
        byName.put("Valid", VALID);
        byName.put("NotNull", NOT_NULL);
        byName.put("Size", SIZE);
        byName.put("Digits", DIGITS);
        byName.put("DecimalMin", DECIMAL_MIN);
        byName.put("DecimalMax", DECIMAL_MAX);
        byName.put("Pattern", PATTERN);
        byName.put("AssertTrue", ASSERT_TRUE);
        byName.put("AssertFalse", ASSERT_FALSE);
        return byName;
    }

    private ValidationAnnotations() {
    }
}
