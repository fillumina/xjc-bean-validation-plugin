package com.fillumina.xjc.validation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 *
 * @author Francesco Illuminati
 */
class FacetSourceAccumulator extends FacetSource {

    static final FacetSourceAccumulator EMPTY = new FacetSourceAccumulator() {
        @Override
        void apply(FacetSource facet) {}
    };

    private FacetSourceAccumulator itemFacet;

    private Integer minLength;
    private Integer maxLength;
    private Integer length;
    private Integer totalDigits;
    private Integer fractionDigits;
    private BigDecimal minInclusive;
    private BigDecimal minExclusive;
    private BigDecimal maxInclusive;
    private BigDecimal maxExclusive;
    private String pattern;
    private LinkedHashSet<String> patternList;
    private String enumeration;
    private LinkedHashSet<String> enumerationList;

    private final LinkedHashSet<LinkedHashSet<String>> multiPatterns = new LinkedHashSet<>();
    private final LinkedHashSet<String> multiEnumerations = new LinkedHashSet<>();

    /**
     * Pins the value of the element to one: {@code fixed} means it can only have that value, so it
     * is both the minimum and the maximum, and whatever bounds the type declares is beside the
     * point.
     */
    void setFixedValue(BigDecimal value) {
        this.minInclusive = value;
        this.maxInclusive = value;
    }

    void apply(FacetSource facet) {
        final Integer minLength = facet.minLength();
        if (minLength != null) {
            this.minLength = minLength;
        }

        final Integer maxLength = facet.maxLength();
        if (maxLength != null) {
            this.maxLength = maxLength;
        }

        final Integer length = facet.length();
        if (length != null) {
            this.length = length;
        }

        final Integer totalDigits = facet.totalDigits();
        if (totalDigits != null) {
            this.totalDigits = totalDigits;
        }

        final Integer fractionDigits = facet.fractionDigits();
        if (fractionDigits != null) {
            this.fractionDigits = fractionDigits;
        }

        final BigDecimal minInclusive = facet.minInclusive();
        if (minInclusive != null) {
            this.minInclusive = minInclusive;
        }

        final BigDecimal minExclusive = facet.minExclusive();
        if (minExclusive != null) {
            this.minExclusive = minExclusive;
        }

        final BigDecimal maxInclusive = facet.maxInclusive();
        if (maxInclusive != null) {
            this.maxInclusive = maxInclusive;
        }

        final BigDecimal maxExclusive = facet.maxExclusive();
        if (maxExclusive != null) {
            this.maxExclusive  = maxExclusive;
        }

        final String pattern = facet.pattern();
        if (pattern != null) {
            this.pattern = pattern;
        }

        final LinkedHashSet<String> patternList = facet.patternList();
        if (patternList != null) {
            this.patternList = patternList;
        }

        final String enumeration = facet.enumeration();
        if (enumeration != null) {
            this.enumeration = enumeration;
        }

        final LinkedHashSet<String> enumerationList = facet.enumerationList();
        if (enumerationList != null) {
            this.enumerationList = enumerationList;
        }

        addIfNotNullOrEmpty(multiPatterns, facet.patterns(), Collection::isEmpty);
        addAllIfNotNullOrEmpty(multiEnumerations, facet.enumerations(), String::isEmpty);
    }

    FacetSourceAccumulator createItemFacet() {
        if (itemFacet == null) {
            itemFacet = new FacetSourceAccumulator();
        }
        return itemFacet;
    }

    FacetSourceAccumulator itemFacet() {
        return itemFacet;
    }

    LinkedHashSet<LinkedHashSet<String>> multiPatterns() {
        return multiPatterns;
    }

    LinkedHashSet<String> multiEnumerations() {
        return multiEnumerations;
    }

    @Override
    Integer minLength() {
        return this.minLength;
    }

    @Override
    Integer maxLength() {
        return this.maxLength;
    }

    @Override
    Integer length() {
        return this.length;
    }

    @Override
    Integer totalDigits() {
        return totalDigits;
    }

    @Override
    Integer fractionDigits() {
        return fractionDigits;
    }

    @Override
    BigDecimal minInclusive() {
        return minInclusive;
    }

    @Override
    BigDecimal minExclusive() {
        return minExclusive;
    }

    @Override
    BigDecimal maxInclusive() {
        return maxInclusive;
    }

    @Override
    BigDecimal maxExclusive() {
        return maxExclusive;
    }

    @Override
    String pattern() {
        return pattern;
    }

    @Override
    LinkedHashSet<String> patternList() {
        return patternList;
    }

    @Override
    String enumeration() {
        return enumeration;
    }

    @Override
    LinkedHashSet<String> enumerationList() {
        return enumerationList;
    }

}
