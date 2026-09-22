package com.fillumina.xjc.validation;

import static com.fillumina.xjc.validation.BeanValidationPlugin.PLUGIN_NAME;

import com.sun.tools.xjc.BadCommandLineException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Parse the arguments passed to the plugin and initialize the {@link BeanValidationOptions}
 * accordingly.
 *
 * @author Francesco Illuminati
 */
enum BeanValidationOption {
    targetNamespace(
            // type:
            String.class,
            // help message:
            "adds @Valid annotation to all elements with given namespace.",
            // setter:
            (p, v) -> {
                if (v != null && !v.contains(" ")) {
                    p.targetNamespace(v);
                } else {
                    return "invalid namespace"; // error message
                }
                return null; // OK
            },
            // getter:
            (p) ->  p.getTargetNamespace()),
    multiPattern(
            Boolean.class,
            "uses a multiple Jakarta Bean Validation @Pattern instead of @Pattern.List",
            (p, v) -> setBoolean(v, r -> p.multiPattern(r)),
            p -> p.isMultiPattern()),
    generateAllNumericConstraints(
            Boolean.class,
            "adds @DecimalMin and @DecimalMax annotations even if within the range of the java type",
            (p,v) -> setBoolean(v, r -> p.allNumericConstraints(r)),
            (p) -> p.isAllNumericConstraints()),
    generateNotNullAnnotations(
            Boolean.class,
            "adds a @NotNull when an element has minOccurs not 0, is required or is not nillable",
            (p,v) -> setBoolean(v, r -> p.notNullAnnotations(r)),
            (p) -> p.isNotNullAnnotations()),
    notNullAnnotationsCustomMessages(
            String.class,
            "allowed values: true/false, 'FieldName', 'ClassName' or an actual message",
            (p,v) -> {
                Boolean b = toBoolean(v);

                if (b != null) {
                    p.notNullCustomMessage(b);
                } else if ("ClassName".equalsIgnoreCase(v)) {
                    p.notNullCustomMessage(true);
                    p.notNullPrefixFieldName(false);
                    p.notNullPrefixClassName(true);
                    p.notNullCustomMessageText(null);
                } else if ("FieldName".equalsIgnoreCase(v)) {
                    p.notNullCustomMessage(true);
                    p.notNullPrefixFieldName(true);
                    p.notNullPrefixClassName(false);
                    p.notNullCustomMessageText(null);
                } else {
                    p.notNullCustomMessage(false);
                    p.notNullPrefixFieldName(false);
                    p.notNullPrefixClassName(false);
                    p.notNullCustomMessageText(v);
                }
                return null;
            },
            (p) -> {
                if (p.isNotNullPrefixFieldName()) {
                    return "FieldName";
                } else if (p.isNotNullPrefixClassName()) {
                    return "ClassName";
                } else if (p.getNotNullCustomMessageText() != null) {
                    return p.getNotNullCustomMessageText();
                } else {
                    return p.isNotNullCustomMessage();
                }
            }),
    verbose(Boolean.class,
            "increases verbosity",
            (p,v) -> setBoolean(v, r -> p.verbose(r)),
            (p) -> p.isVerbose()),
    generateItemAnnotations(
            Boolean.class,
            "writes the constraints of the items of a collection on its type argument, as in "
                    + "List<@Size(max = 5) String>: that is the form a current provider enforces",
            (p,v) -> setBoolean(v, r -> p.itemAnnotations(r)),
            (p) -> p.isItemAnnotations()),
    generateValidOnCollections(
            Boolean.class,
            "adds a @Valid annotation to the type argument of collections, as in List<@Valid Other>: "
                    + "on the container itself Bean Validation deprecated it (HV000271). Turning it "
                    + "off means the elements of a collection are no longer validated through it",
            (p, v) -> setBoolean(v, r -> p.generateValidOnCollections(r)),
            (p) -> p.isGenerateValidOnCollections()),
    override(
            String.class,
            "leaves the given class or property out of the generated annotations: a glob for the class, "
                    + "optionally # and a glob for the property, optionally = and the annotation to write "
                    + "instead of the computed one",
            (p, v) -> {
                String error = OverrideStatements.validate(v);
                if (error != null) {
                    return error;
                }
                p.override(v);
                return null;
            },
            p -> p.getOverrides());

    // parameter type
    private final Class<?> type;

    // help text
    private final String help;

    // set the value into the builder and return null if ok or a text with the error
    private final BiFunction<BeanValidationOptions.Builder, String, String> setter;

    // get the value
    private final Function<BeanValidationOptions, Object> getter;

    BeanValidationOption(
            Class<?> type,
            String help,
            BiFunction<BeanValidationOptions.Builder, String, String> setter,
            Function<BeanValidationOptions, Object> getter) {
        this.type = type;
        this.help = help;
        this.setter = setter;
        this.getter = getter;
    }

    String setValue(BeanValidationOptions.Builder optionBuilder, String value) {
        return setter.apply(optionBuilder, value);
    }

    Object getValue(BeanValidationOptions options) {
        return getter.apply(options);
    }

    String getTypeName() {
        return type.getSimpleName();
    }

    String optionPath() {
        return BeanValidationPlugin.PLUGIN_NAME + ":" + name();
    }

    static String usage() {
        return new StringBuilder()
                .append("  -")
                .append(PLUGIN_NAME)
                .append("      :  ")
                .append("inject Jakarta Bean Validation annotations")
                .append(System.lineSeparator())
                .append("   Options:")
                .append(helpWithPrefix("     "))
                .append(System.lineSeparator())
                .toString();
    }

    /** @return the options with the values they were given, one per line. */
    static String valuesInUse(BeanValidationOptions options) {
        StringBuilder buf = new StringBuilder("options in use:")
                .append(System.lineSeparator());
        for (BeanValidationOption option : values()) {
            buf.append("    ")
                    .append(option.name())
                    .append(": ")
                    .append(Objects.toString(option.getValue(options)))
                    .append(System.lineSeparator());
        }
        return buf.toString();
    }

    static BeanValidationOption parse(final String name) throws BadCommandLineException {
        try {
            return BeanValidationOption.valueOf(name);
        } catch (IllegalArgumentException ex) {
            throw new BadCommandLineException(BeanValidationPlugin.PLUGIN_NAME +
                    " unrecognized option " + name + ", usage:\n" +
                    BeanValidationOption.helpWithPrefix(""));
        }
    }

    /**
     * @param linePrefix a string prefixed to each output line
     * @return a multi line string containing an help for each option.
     */
    static String helpWithPrefix(String linePrefix) {
        StringBuilder buf = new StringBuilder();
        for (BeanValidationOption a : values()) {
            buf
                    .append(linePrefix)
                    .append(a.name())
                    .append(": ")
                    .append("(")
                    .append(a.type.getSimpleName())
                    .append(") ")
                    .append(a.help)
                    .append(System.lineSeparator());
        }
        return buf.toString();
    }

    String errorMessage(String wrongValue) {
        return optionPath() + " option expected a value of type " + type.getSimpleName() +
                " but got '" + Objects.toString(wrongValue) + "'";
    }

    static String setBoolean(String value, Consumer<Boolean> setter) {
        if (value == null || "".equals(value.trim())) {
            setter.accept(true);
            return null;
        }
        Boolean bool = toBoolean(value);
        if (bool == null) {
            return "argument not valid, must be 'true' or 'false'";
        }
        setter.accept(bool);
        return null;
    }

    /**
     * @return the boolean value of v (no case sensitive) or null otherwise.
     */
    static Boolean toBoolean(String v) {
        if (v != null) {
            String lc = v.toLowerCase().trim();
            if ("true".equals(lc)) {
                return Boolean.TRUE;
            } else if ("false".equals(lc)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

}
