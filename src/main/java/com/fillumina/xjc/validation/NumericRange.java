package com.fillumina.xjc.validation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The minimum and the maximum of the boxed numbers, to leave out a bound that the Java type
 * already states.
 *
 * @author Francesco Illuminati
 */
class NumericRange {
    private static final Map<String, NumericRange> MAP = new HashMap<>();

    static {
        MAP.put(Byte.class.getCanonicalName(), new NumericRange(Byte.MIN_VALUE, Byte.MAX_VALUE));
        MAP.put(Short.class.getCanonicalName(), new NumericRange(Short.MIN_VALUE, Short.MAX_VALUE));
        MAP.put(Integer.class.getCanonicalName(), new NumericRange(Integer.MIN_VALUE, Integer.MAX_VALUE));
        MAP.put(Long.class.getCanonicalName(), new NumericRange(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    final BigDecimal min;
    final BigDecimal max;

    NumericRange(Number min, Number max) {
        this.min = parse(min);
        this.max = parse(max);
    }

    private static BigDecimal parse(Number num) {
        if (num != null) {
            return new BigDecimal(Objects.toString(num));
        }
        return null;
    }

    /** @return true when the bound is one of the two limits of the Java type itself. */
    static boolean isBoundOfTheJavaType(String typeName, BigDecimal bound) {
        NumericRange range = MAP.get(typeName);
        return range != null && (bound.equals(range.min) || bound.equals(range.max));
    }
}
