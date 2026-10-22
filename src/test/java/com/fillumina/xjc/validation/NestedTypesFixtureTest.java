package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * The item type of a collection that is a nested class, in the three shapes the generator produces:
 * a top-level class, one level of nesting and two. The generated sources have to compile, which is
 * where a static nested class changes the emission: an annotation written in front of the whole
 * qualified name is refused by javac, and it has to be written between the enclosing class and the
 * name of the item type.
 */
class NestedTypesFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "nested";
    }

    @Override
    boolean compilesGeneratedSources() {
        return true;
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Nested", option("targetNamespace", "a")));
    }
}
