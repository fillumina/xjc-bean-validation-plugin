package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * The XSD numeric types and the annotations their facets produce.
 *
 * <p>The old line's test of this fixture ran the primitives plugin as well, which boxed the fields
 * whose type XJC had made primitive. That is not needed here: the expectations hold annotations and
 * not types, and the boxing is the primitives project's own business, with its own test. This
 * fixture therefore runs this plugin alone, and records the same annotations.
 *
 * <p>The case is named after the test class that drove it in the old line, which is how every case
 * name of this harness is derived, so the expectation file has the old name.
 */
class PrimitiveFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "primitive";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("PrimitiveFixerPlugin", option("targetNamespace", "a")));
    }
}
