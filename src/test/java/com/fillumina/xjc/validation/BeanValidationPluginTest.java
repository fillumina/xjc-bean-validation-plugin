package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Driver;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Generates a schema through XJC and checks the annotations the plugin wrote into the result.
 *
 * <p>Generation runs in this JVM through {@link Driver}, the entry point of the {@code xjc} command
 * line, so the test needs neither a Maven build nor a lifecycle. The expected text is collapsed to
 * single spaces first, which makes the assertions independent of how the generator wraps its lines.
 */
class BeanValidationPluginTest {

    private static final Path SCHEMA = Path.of("src", "test", "resources", "validation.xsd");

    @TempDir
    Path outputDirectory;

    @Test
    void theConstraintsOfTheSchemaAreWrittenOnTheGeneratedFields() throws Exception {
        String generated = generate(new ArrayList<>());

        // xs:element name="name" type="t:ShortText" maxOccurs="unbounded",
        // with ShortText restricted to xs:maxLength value="5"
        assertTrue(generated.contains("protected List<@Size(max = 5) String> name;"), generated);

        // a required element and a required attribute
        assertTrue(generated.contains("@NotNull protected String code;"), generated);
        assertTrue(generated.contains("@NotNull protected String id;"), generated);

        // a collection of a complex type cascades into its elements, a single one does not need to
        assertTrue(generated.contains("protected List<@Valid Tag> tag;"), generated);
        assertTrue(generated.contains("@Valid protected Note note;"), generated);

        assertTrue(generated.contains("import jakarta.validation.constraints.Size;"), generated);
        assertTrue(generated.contains("import jakarta.validation.constraints.NotNull;"), generated);
        assertTrue(generated.contains("import jakarta.validation.Valid;"), generated);
    }

    @Test
    void pluginVerboseOptionPrintsThePluginLogWithoutXjcVerbose() throws Exception {
        Path output = Files.createDirectories(outputDirectory.resolve("plugin-verbose"));
        List<String> arguments = List.of("-quiet", "-extension",
                "-" + BeanValidationPlugin.PLUGIN_NAME,
                "-" + BeanValidationPlugin.PLUGIN_NAME + ":verbose=true",
                "-d", output.toString(), SCHEMA.toAbsolutePath().toString());
        ByteArrayOutputStream messages = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream stream = new PrintStream(messages, true, StandardCharsets.UTF_8)) {
            System.setOut(stream);
            int exitCode = Driver.run(arguments.toArray(String[]::new), stream, stream);
            assertEquals(0, exitCode, () -> "xjc failed: " + messages);
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(messages.toString(StandardCharsets.UTF_8).contains("options in use:"),
                () -> "plugin verbose output missing: " + messages);
    }

    @Test
    void theNotNullAnnotationCanBeTurnedOff() throws Exception {
        List<String> extra = new ArrayList<>();
        extra.add("-XBeanValidationAnnotations:generateNotNullAnnotations=false");

        String generated = generate(extra);

        assertTrue(generated.contains("protected String code;"), generated);
        assertFalse(generated.contains("@NotNull"), generated);
    }

    @Test
    void oldNameOfTheItemAnnotationOptionIsRefused() {
        BadCommandLineException refused = assertThrows(BadCommandLineException.class,
                () -> BeanValidationOptions.builder().parseArgument(
                        "-XBeanValidationAnnotations:generateListAnnotations=true"));

        assertTrue(refused.getMessage().contains("unrecognized option generateListAnnotations"),
                refused::getMessage);
    }

    /** The plugin does not answer to the name it had in the line this project was split from. */
    @Test
    void theOldNameOfTheOptionIsRefused() throws Exception {
        Path output = Files.createDirectories(outputDirectory.resolve("old-name"));
        List<String> arguments = List.of("-quiet", "-extension", "-XJsr303Annotations",
                "-d", output.toString(), SCHEMA.toAbsolutePath().toString());

        ByteArrayOutputStream messages = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(messages, true, StandardCharsets.UTF_8)) {
            BadCommandLineException refused = assertThrows(BadCommandLineException.class,
                    () -> Driver.run(arguments.toArray(String[]::new), stream, stream));
            assertEquals("unrecognized parameter -XJsr303Annotations", refused.getMessage());
        }

        try (Stream<Path> files = Files.walk(output)) {
            assertEquals(0, files.filter(Files::isRegularFile).count(),
                    "the plugin ran under the old name of the option");
        }
    }

    /**
     * Runs XJC once over the schema and returns the generated class with its whitespace collapsed.
     *
     * @param extraArguments further arguments, such as an option of the plugin
     */
    private String generate(List<String> extraArguments) throws Exception {
        Path schema = SCHEMA.toAbsolutePath();
        assertTrue(Files.exists(schema), "schema not found: " + schema);

        List<String> arguments = new ArrayList<>();
        arguments.add("-quiet");
        arguments.add("-extension");
        arguments.add("-d");
        arguments.add(outputDirectory.toString());
        // the plain option is what activates the plugin: XJC hands the options that carry a value to
        // every plugin but activates only the one whose name was given on its own
        arguments.add("-" + BeanValidationPlugin.PLUGIN_NAME);
        arguments.addAll(extraArguments);
        arguments.add(schema.toString());

        ByteArrayOutputStream messages = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(messages, true, StandardCharsets.UTF_8)) {
            int exitCode = Driver.run(arguments.toArray(String[]::new), stream, stream);
            assertEquals(0, exitCode, () -> "xjc failed: " + messages);
        }

        return Files.readString(generatedSource("Order.java")).replaceAll("\\s+", " ");
    }

    private Path generatedSource(String name) throws Exception {
        return generatedSource(outputDirectory, name);
    }

    private Path generatedSource(Path directory, String name) throws Exception {
        try (Stream<Path> files = Files.walk(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("no " + name + " generated in "
                            + directory));
        }
    }
}
