package com.fillumina.xjc.validation;


import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The facets one type of the hierarchy states, read from the schema model.
 *
 * @author Francesco Illuminati
 */
class FacetSourceView extends FacetSource {

    private final XSSimpleType simpleType;

    FacetSourceView(XSSimpleType simpleType) {
        this.simpleType = simpleType;
    }

    @Override
    Integer minLength() {
        return getIntegerFacet(XSFacet.FACET_MINLENGTH);
    }

    @Override
    Integer maxLength() {
        return getIntegerFacet(XSFacet.FACET_MAXLENGTH);
    }

    @Override
    Integer length() {
        return getIntegerFacet(XSFacet.FACET_LENGTH);
    }

    @Override
    Integer totalDigits() {
        return getIntegerFacet(XSFacet.FACET_TOTALDIGITS);
    }

    @Override
    Integer fractionDigits() {
        return getIntegerFacet(XSFacet.FACET_FRACTIONDIGITS);
    }

    @Override
    BigDecimal minInclusive() {
        return getDecimalFacet(XSFacet.FACET_MININCLUSIVE);
    }

    @Override
    BigDecimal minExclusive() {
        return getDecimalFacet(XSFacet.FACET_MINEXCLUSIVE);
    }

    @Override
    BigDecimal maxInclusive() {
        return getDecimalFacet(XSFacet.FACET_MAXINCLUSIVE);
    }

    @Override
    BigDecimal maxExclusive() {
        return getDecimalFacet(XSFacet.FACET_MAXEXCLUSIVE);
    }

    @Override
    String pattern() {
        return getStringFacet(XSFacet.FACET_PATTERN);
    }

    @Override
    LinkedHashSet<String> patternList() {
        return getMultipleStringFacets(XSFacet.FACET_PATTERN);
    }

    @Override
    String enumeration() {
        return getStringFacet(XSFacet.FACET_ENUMERATION);
    }

    @Override
    LinkedHashSet<String> enumerationList() {
        return getMultipleStringFacets(XSFacet.FACET_ENUMERATION);
    }

    private LinkedHashSet<String> getMultipleStringFacets(String param) {
        final List<XSFacet> facets = simpleType.getFacets(param);
        if (facets != null) {
            return facets.stream()
                    .map(facet -> facet.getValue().value)
                    .filter(v -> v != null && !v.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return new LinkedHashSet<>();
    }

    private String getStringFacet(String param) {
        final XSFacet facet = simpleType.getFacet(param);
        return facet == null ? null : facet.getValue().value;
    }

    private Integer getIntegerFacet(String param) {
        final XSFacet facet = simpleType.getFacet(param);
        if (facet != null) {
            try {
                return Integer.valueOf(facet.getValue().value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return null;
    }

    private BigDecimal getDecimalFacet(String param) {
        final XSFacet facet = simpleType.getFacet(param);
        if (facet != null) {
            final String str = facet.getValue().value;
            try {
                return new BigDecimal(str);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return null;
    }

}
