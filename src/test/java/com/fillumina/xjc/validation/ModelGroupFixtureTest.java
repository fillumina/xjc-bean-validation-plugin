package com.fillumina.xjc.validation;

import java.util.stream.Stream;

class ModelGroupFixtureTest extends FixtureTest {

    @Override
    String fixture() {
        return "modelGroup";
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("ModelGroup", option("targetNamespace", "a")),
                Case.of("NoValidOnCollectionsOnAGroup", option("targetNamespace", "a"),
                        option("generateValidOnCollections", "false")));
    }
}
