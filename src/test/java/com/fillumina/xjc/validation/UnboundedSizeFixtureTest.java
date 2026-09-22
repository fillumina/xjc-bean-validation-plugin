package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/**
 * A size restriction that comes from a binding file rather than from the schema, and an element whose
 * occurrences are unbounded.
 *
 * <p>The only fixture that passes a binding directory to XJC, and the only one whose schema sits in a
 * subdirectory: {@code size/schema}, with the bindings beside it in {@code size/bindings}.
 */
class UnboundedSizeFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "size/schema";
    }

    @Override
    String bindingDirectory() {
        return "src/test/resources/size/bindings";
    }

    @Override
    String schemaName() {
        return "size.xsd";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("UnboundedSize", option("targetNamespace", "a")));
    }
}
