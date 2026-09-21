package com.fillumina.xjc.validation;

import java.util.stream.Stream;

/** The fixture {@code modelGroup} of the old line, ported with its cases. */
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
