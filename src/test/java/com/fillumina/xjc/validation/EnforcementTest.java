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
    private static final Path LISTS_SCHEMA = Path.of("src", "test", "resources", "lists", "lists.xsd");
    private static final Path XSD_REGEXP_SCHEMA =
            Path.of("src", "test", "resources", "xsdRegexp", "xsdRegexp.xsd");

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
    void exactListLengthsAreEnforcedWithAndWithoutItemAnnotations() throws Exception {
        for (boolean itemsEnabled : List.of(true, false)) {
            Class<?> container = generateAndLoad(LISTS_SCHEMA, "a.Container",
                    FixtureTest.option("generateItemAnnotations", itemsEnabled));
            Object instance = container.getConstructor().newInstance();
            @SuppressWarnings("unchecked")
            List<String> exactStrings = (List<String>) container.getMethod("getListOfExactString")
                    .invoke(instance);
            exactStrings.add("ab");
            exactStrings.add("abc");
            @SuppressWarnings("unchecked")
            List<String> derived = (List<String>) container.getMethod("getListOfDerivedExactString")
                    .invoke(instance);
            derived.add("ab");
            derived.add("abcd");
            @SuppressWarnings("unchecked")
            List<java.math.BigInteger> integers = (List<java.math.BigInteger>) container
                    .getMethod("getListOfExactInteger").invoke(instance);
            integers.add(java.math.BigInteger.ONE);

            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            List<String> messages = messagesOf(validate(validator, instance));
            assertTrue(messages.stream().anyMatch(m -> m.startsWith("listOfExactInteger ")
                    && m.contains("size must be between 2 and 2")), messages.toString());
            assertEquals(itemsEnabled, messages.stream().anyMatch(m -> m.startsWith("listOfExactString[0]")
                    && m.contains("size must be between 3 and 3")), messages.toString());
            assertEquals(itemsEnabled, messages.stream().anyMatch(m -> m.startsWith("listOfDerivedExactString[0]")
                    && m.contains("size must be between 3 and 3")), messages.toString());
            assertEquals(itemsEnabled, messages.stream().anyMatch(m -> m.startsWith("listOfDerivedExactString[1]")
                    && m.contains("size must be between 3 and 3")), messages.toString());
            integers.add(java.math.BigInteger.TWO);
            derived.clear();
            derived.add("abc");
            exactStrings.clear();
            exactStrings.add("abc");
            assertTrue(validator.validateProperty(instance, "listOfExactInteger").isEmpty());
            assertTrue(validator.validateProperty(instance, "listOfExactString").isEmpty());
            assertTrue(validator.validateProperty(instance, "listOfDerivedExactString").isEmpty());
            loader.close();
            loader = null;
        }
    }

    @Test
    void aRequiredFieldLeftEmptyIsReported() throws Exception {
        Class<?> order = generateAndLoad("Order");
        Object instance = order.getConstructor().newInstance();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        List<String> messages = messagesOf(validate(validator, instance));

        assertTrue(messages.stream().anyMatch(m -> m.startsWith("code ")), messages.toString());
    }

    @Test
    void translatedXsdPatternsAreEnforced() throws Exception {
        Class<?> patternProbe = generateAndLoad(XSD_REGEXP_SCHEMA, "PatternProbe");
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Object valid = patternProbe.getConstructor().newInstance();
        set(patternProbe, valid, "Digits", "١٢");
        set(patternProbe, valid, "Word", "€é");
        set(patternProbe, valid, "XmlName", "éclair");
        set(patternProbe, valid, "XmlCharacters", "éclair");
        set(patternProbe, valid, "NotXmlName", "7");
        set(patternProbe, valid, "WithoutVowels", "bcd");
        set(patternProbe, valid, "LatinOne", "é");
        set(patternProbe, valid, "LiteralAnchors", "^abc$");
        set(patternProbe, valid, "Whitespace", " \t");
        set(patternProbe, valid, "Wildcard", "a\u2028b");
        set(patternProbe, valid, "Uppercase", "ABC");
        assertTrue(validate(validator, valid).isEmpty());

        Object invalid = patternProbe.getConstructor().newInstance();
        set(patternProbe, invalid, "Digits", "A");
        set(patternProbe, invalid, "Word", "_");
        set(patternProbe, invalid, "XmlName", "7name");
        set(patternProbe, invalid, "XmlCharacters", "name!");
        set(patternProbe, invalid, "NotXmlName", "é");
        set(patternProbe, invalid, "WithoutVowels", "ace");
        set(patternProbe, invalid, "LatinOne", "A");
        set(patternProbe, invalid, "LiteralAnchors", "abc");
        set(patternProbe, invalid, "Whitespace", "A");
        set(patternProbe, invalid, "Wildcard", "a\nb");
        set(patternProbe, invalid, "Uppercase", "Abc");
        List<String> messages = messagesOf(validate(validator, invalid));

        for (String property : List.of("digits", "word", "xmlName", "xmlCharacters", "notXmlName",
                "withoutVowels", "latinOne", "literalAnchors", "whitespace", "wildcard", "uppercase")) {
            assertTrue(messages.stream().anyMatch(m -> m.startsWith(property + " ")), messages.toString());
        }
    }

    private static void set(Class<?> type, Object instance, String property, String value) throws Exception {
        type.getMethod("set" + property, String.class).invoke(instance, value);
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
        return generateAndLoad(SCHEMA, className);
    }

    private Class<?> generateAndLoad(Path schema, String className, String... pluginOptions) throws Exception {
        Path generated = Files.createDirectories(workDirectory.resolve("generated"));
        Path classes = Files.createDirectories(workDirectory.resolve("classes"));

        List<String> arguments = new ArrayList<>(List.of("-quiet", "-extension",
                "-" + BeanValidationPlugin.PLUGIN_NAME, "-d", generated.toString()));
        arguments.addAll(List.of(pluginOptions));
        arguments.add(schema.toAbsolutePath().toString());
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
        return loader.loadClass(className.contains(".") ? className : "com.example.validation." + className);
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
