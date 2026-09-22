package com.fillumina.xjc.validation;

import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.impl.ListSimpleTypeImpl;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Starting from a base type it goes up the hierarchy and consolidates all inherited facets.
 *
 * @author Francesco Illuminati
 */
class FacetGatherer {

    static FacetSourceAccumulator gather(XSSimpleType type) {
        if (type == null) {
            return FacetSourceAccumulator.EMPTY;
        }
        FacetSourceAccumulator facet = new FacetSourceAccumulator();
        navigateUpTheHierarchy(facet, type);
        facet.translatePatterns();
        consolidatePatterns(facet);
        return facet;
    }

    private static void consolidatePatterns(FacetSourceAccumulator facet) {
        final LinkedHashSet<LinkedHashSet<String>> multiPatterns = facet.multiPatterns();
        final LinkedHashSet<String> multiEnumerations = facet.multiEnumerations();

        if (! (multiPatterns.isEmpty() && multiEnumerations.isEmpty()) ) {
            if (multiPatterns.size() > 1) {
                FacetSource.addIfNotNullOrEmpty(multiPatterns, multiEnumerations, Collection::isEmpty);
            } else if (!multiEnumerations.isEmpty()) {
                if (multiPatterns.isEmpty()) {
                    multiPatterns.add(new LinkedHashSet<>());
                }
                multiPatterns.iterator().next().addAll(multiEnumerations);
            }
        }

    }

    private static void navigateUpTheHierarchy(FacetSourceAccumulator facet, XSSimpleType type) {
        XSSimpleType baseType = null;
        if (type instanceof XSListSimpleType) {
            baseType = type.getBaseListType();
        } else if (type instanceof XSRestrictionSimpleType) {
            baseType = type.getSimpleBaseType();
        }

        if ((baseType == null || baseType == type) && type instanceof ListSimpleTypeImpl) {
            baseType = ((ListSimpleTypeImpl)type).getItemType();
            facet = facet.createItemFacet();
        }
        if (baseType != null && baseType != type) {
            navigateUpTheHierarchy(facet, baseType);
        }

        final FacetSourceView typeFacet = new FacetSourceView(type);
        facet.apply(typeFacet);
    }

}
