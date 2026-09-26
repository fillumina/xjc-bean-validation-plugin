package com.fillumina.xjc.validation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Predicate;

/**
 * One source of schema facets: the type XJC hands over, or the facets gathered from the types
 * below it. The accessors are the ones Processor reads, whatever produced the values.
 *
 * @author Francesco Illuminati
 */
abstract class FacetSource {

    abstract Integer minLength();

    abstract Integer maxLength();

    abstract Integer length();

    abstract Integer totalDigits();

    abstract Integer fractionDigits();

    abstract BigDecimal minInclusive();

    abstract BigDecimal minExclusive();

    abstract BigDecimal maxInclusive();

    abstract BigDecimal maxExclusive();

    abstract String pattern();

    abstract LinkedHashSet<String> patternList();

    abstract String enumeration();

    abstract LinkedHashSet<String> enumerationList();

    /** @return the raw pattern facets; translation happens later, where a skipped pattern can be logged. */
    LinkedHashSet<String> patterns() {
        final LinkedHashSet<String> patterns = patternList();
        addIfNotNullOrEmpty(patterns, pattern(), String::isEmpty);
        return patterns;
    }

    /** @return the enumeration facets, each value quoted as a literal regexp. */
    LinkedHashSet<String> enumerations() {
        final LinkedHashSet<String> enumerations = enumerationList();
        addIfNotNullOrEmpty(enumerations, enumeration(), String::isEmpty);
        if (enumerations != null && !enumerations.isEmpty()) {
            return enumerations.stream()
                    .filter(p -> p != null && !p.isEmpty())
                    .map(p -> XsdRegexp.quote(p))
                    .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        }
        return enumerations;
    }

    /** Adds a value unless it is null or empty, so a facet that says nothing is left out. */
    static <C extends Collection<T>, T> void addIfNotNullOrEmpty(C target, T value, Predicate<T> isEmpty) {
        if (value != null && !isEmpty.test(value)) {
            target.add(value);
        }
    }

    /** The same, for every value of a source collection. */
    static <T> void addAllIfNotNullOrEmpty(Collection<T> target, Collection<T> source, Predicate<T> isEmpty) {
        if (source != null && !source.isEmpty()) {
            for (T t : source) {
                if (t != null && !isEmpty.test(t)) {
                    target.add(t);
                }
            }
        }
    }
}
