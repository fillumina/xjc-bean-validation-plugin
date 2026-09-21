package com.fillumina.xjc.validation;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A fixture test: every case is one run of the plugin over the fixture's schema, and one file of
 * expected annotations to compare the run with.
 *
 * <p>The cases are declared by the concrete class in a static {@code cases} method, so a fixture
 * says in one place which options it exercises and which expectation files it owns.
 */
abstract class FixtureTest {

    /** One run: the name of the expectation file and the options it is generated with. */
    record Case(String name, List<String> arguments) {

        static Case of(String name, String... arguments) {
            return new Case(name, List.of(arguments));
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** @return the option of this plugin with a value, as it is written on the command line. */
    static String option(String name, Object value) {
        return "-" + BeanValidationPlugin.PLUGIN_NAME + ":" + name + "=" + value;
    }

    /** @return the name of the directory under {@code src/test/resources} holding the schema. */
    abstract String fixture();

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void theAnnotationsAreTheExpectedOnes(Case testCase) throws Exception {
        new XjcFixture(fixture(), testCase.name(), testCase.arguments(), bindingDirectory(),
                schemaName()).check();
    }

    /** @return the directory of binding files this fixture needs, or null when it needs none. */
    String bindingDirectory() {
        return null;
    }

    /**
     * @return the name of the schema file inside the fixture directory, which is the name of the
     *     last segment of the directory unless the fixture says otherwise
     */
    String schemaName() {
        String folder = fixture();
        return folder.substring(folder.lastIndexOf('/') + 1) + ".xsd";
    }
}
