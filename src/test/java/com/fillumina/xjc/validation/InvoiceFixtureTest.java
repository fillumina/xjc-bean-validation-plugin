package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class InvoiceFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "invoice";
    }

    static Stream<Case> cases() {
        return Stream.of(Case.of("Invoice", option("targetNamespace", "a")));
    }
}
