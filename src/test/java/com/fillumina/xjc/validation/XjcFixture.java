package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.tools.xjc.Driver;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * One fixture: a schema under {@code src/test/resources}, one run of the plugin over it, and the
 * file holding the annotations that run has to produce.
 *
 * <p>Generation runs in this JVM through {@code com.sun.tools.xjc.Driver}, the entry point of the
 * {@code xjc} command line, so a fixture needs neither a Maven build nor a lifecycle. The generated
 * sources are written under {@code target/generated-sources}, which no test reads afterwards.
 *
 * <p>With {@code -Dxjc.validation.record=true} the expectation file is written from the run instead
 * of being compared with it. That is how a fixture is re-baselined: record, then read the diff, so
 * that every change in an expectation is a decision rather than an accident.
 */
class XjcFixture {

    private static final String RECORD = "xjc.validation.record";

    private final String folder;
    private final String caseName;
    private final List<String> arguments;
    /** The directory of binding files to pass with {@code -b}, or null when the fixture needs none. */
    private final String bindingDirectory;
    /** The name of the schema file inside the fixture directory. */
    private final String schemaName;

    /**
     * @param folder the directory under {@code src/test/resources}, which holds the schema and the
     *     expectation file
     * @param caseName the name of the expectation file, without its suffix
     * @param arguments the options of the plugin for this run, without the plain option, which is
     *     always given
     */
    XjcFixture(String folder, String caseName, List<String> arguments) {
        this(folder, caseName, arguments, null, caseName + ".xsd");
    }

    XjcFixture(String folder, String caseName, List<String> arguments, String bindingDirectory,
            String schemaName) {
        this.folder = folder;
        this.caseName = caseName;
        this.arguments = arguments;
        this.bindingDirectory = bindingDirectory;
        this.schemaName = schemaName;
    }

    void check() throws Exception {
        String actual = SourceAnnotations.of(generate());

        Path expectedFile = expectedFile();
        if (Boolean.getBoolean(RECORD)) {
            Files.writeString(expectedFile, actual);
        }
        assertEquals(Files.readString(expectedFile), actual,
                () -> "the annotations of " + caseName + " are not the expected ones, see "
                        + expectedFile);
    }

    /** @return the generated sources, sorted by file name. */
    private List<Path> generate() throws Exception {
        Path output = Path.of("target", "generated-sources", folder);
        deleteRecursively(output);
        Files.createDirectories(output);

        List<String> arguments = new ArrayList<>(List.of(
                "-quiet",
                "-extension",
                "-d", output.toString(),
                "-" + BeanValidationPlugin.PLUGIN_NAME));
        arguments.addAll(this.arguments);
        if (bindingDirectory != null) {
            arguments.add("-b");
            arguments.add(bindingDirectory);
        }
        arguments.add(schema().toString());

        ByteArrayOutputStream messages = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(messages, true, StandardCharsets.UTF_8)) {
            int exitCode = Driver.run(arguments.toArray(String[]::new), stream, stream);
            assertEquals(0, exitCode, () -> "xjc failed on " + schema() + ": " + messages);
        }

        try (Stream<Path> files = Files.walk(output)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().startsWith("package-info"))
                    .filter(path -> !path.getFileName().toString().startsWith("ObjectFactory"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    private Path schema() {
        Path schema = Path.of("src", "test", "resources", folder, schemaName);
        if (!Files.exists(schema)) {
            throw new AssertionError("no schema at " + schema.toAbsolutePath());
        }
        return schema;
    }

    private Path expectedFile() {
        return Path.of("src", "test", "resources", folder, caseName + "-annotation.txt");
    }

    private static void deleteRecursively(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> files = Files.walk(directory)) {
            for (Path path : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
