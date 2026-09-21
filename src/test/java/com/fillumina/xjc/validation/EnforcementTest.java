package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.tools.xjc.Driver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The proof that a generated constraint is enforced: the class the plugin wrote is compiled here and
 * given to Hibernate Validator, which has to report the violation.
 */
class EnforcementTest {

    private static final Path SCHEMA = Path.of("src", "test", "resources", "validation.xsd");

    @TempDir
    Path workDirectory;

    /** Kept open while the test runs: the generated classes refer to one another. */
    private URLClassLoader loader;

    @AfterEach
    void closeTheLoader() throws Exception {
        if (loader != null) {
            loader.close();
        }
    }

    @Test
    void aListItemOutsideItsSizeIsReported() throws Exception {
        Class<?> order = generateAndLoad("Order");
        Object instance = order.getConstructor().newInstance();
        order.getMethod("setCode", String.class).invoke(instance, "x");

        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) order.getMethod("getName").invoke(instance);
        names.add("abc");
        names.add("abcdef");     // one character too many for ShortText

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        List<String> messages = messagesOf(validate(validator, instance));

        assertTrue(messages.stream().anyMatch(m -> m.startsWith("name[1]")
                && m.contains("size must be between 0 and 5")), messages.toString());
    }

    @Test
    void aRequiredFieldLeftEmptyIsReported() throws Exception {
        Class<?> order = generateAndLoad("Order");
        Object instance = order.getConstructor().newInstance();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        List<String> messages = messagesOf(validate(validator, instance));

        assertTrue(messages.stream().anyMatch(m -> m.startsWith("code ")), messages.toString());
    }

    private static List<String> messagesOf(Set<ConstraintViolation<Object>> violations) {
        List<String> messages = new ArrayList<>();
        for (ConstraintViolation<Object> violation : violations) {
            messages.add(violation.getPropertyPath() + " " + violation.getMessage());
        }
        return messages;
    }

    @SuppressWarnings("unchecked")
    private Set<ConstraintViolation<Object>> validate(Validator validator, Object instance) {
        return (Set<ConstraintViolation<Object>>) (Set<?>) validator.validate(instance);
    }

    /** Runs XJC over the schema, compiles what it wrote, and loads the named class. */
    private Class<?> generateAndLoad(String className) throws Exception {
        Path generated = Files.createDirectories(workDirectory.resolve("generated"));
        Path classes = Files.createDirectories(workDirectory.resolve("classes"));

        List<String> arguments = List.of("-quiet", "-extension",
                "-" + BeanValidationPlugin.PLUGIN_NAME, "-d", generated.toString(),
                SCHEMA.toAbsolutePath().toString());
        ByteArrayOutputStream messageOutput = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(messageOutput, true, StandardCharsets.UTF_8)) {
            int exitCode = Driver.run(arguments.toArray(String[]::new), stream, stream);
            assertEquals(0, exitCode, () -> "xjc failed: " + messageOutput);
        }

        List<String> sources = new ArrayList<>();
        try (var files = Files.walk(generated)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> sources.add(path.toString()));
        }
        assertFalse(sources.isEmpty(), "xjc wrote no source");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> options = new ArrayList<>(List.of(
                "-classpath", compilerClasspath(),
                "-d", classes.toString()));
        options.addAll(sources);
        assertEquals(0, compiler.run(null, null, null, options.toArray(String[]::new)),
                "the generated sources do not compile");

        loader = new URLClassLoader(new URL[] { classes.toUri().toURL() },
                getClass().getClassLoader());
        return loader.loadClass("com.example.validation." + className);
    }

    /**
     * The classpath the generated sources are compiled against. Inside a surefire fork the classpath
     * of the JVM is the booter of the runner, so the one the tests run with is asked for under its
     * own name.
     */
    private static String compilerClasspath() {
        String testClasspath = System.getProperty("surefire.test.class.path");
        return testClasspath != null ? testClasspath : System.getProperty("java.class.path");
    }
}
