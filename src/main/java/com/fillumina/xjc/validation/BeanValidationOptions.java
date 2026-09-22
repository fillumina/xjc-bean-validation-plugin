package com.fillumina.xjc.validation;

import com.sun.tools.xjc.BadCommandLineException;
import java.util.ArrayList;
import java.util.List;

/**
 * Set the options using the included builder and then create an immutable option bean to
 * be passed around.
 *
 * @author Francesco Illuminati
 */
class BeanValidationOptions {
    // set default values in Builder not here
    private final String targetNamespace;
    private final boolean multiPattern;
    private final boolean verbose;
    private final boolean allNumericConstraints;
    private final boolean notNullAnnotations;
    private final boolean notNullCustomMessage;
    private final boolean notNullPrefixFieldName;
    private final boolean notNullPrefixClassName;
    private final String notNullCustomMessageText;
    private final boolean itemAnnotations;
    private final boolean generateValidOnCollections;
    private final List<String> exclusions;

    String getTargetNamespace() {
        return targetNamespace;
    }

    boolean isMultiPattern() {
        return multiPattern;
    }

    boolean isVerbose() {
        return verbose;
    }

    boolean isAllNumericConstraints() {
        return allNumericConstraints;
    }

    boolean isNotNullAnnotations() {
        return notNullAnnotations;
    }

    boolean isNotNullCustomMessage() {
        return notNullCustomMessage;
    }

    boolean isNotNullPrefixFieldName() {
        return notNullPrefixFieldName;
    }

    boolean isNotNullPrefixClassName() {
        return notNullPrefixClassName;
    }

    String getNotNullCustomMessageText() {
        return notNullCustomMessageText;
    }

    /** @return whether the constraints of the items of a collection are written on its type argument. */
    boolean isItemAnnotations() {
        return itemAnnotations;
    }

    boolean isGenerateValidOnCollections() {
        return generateValidOnCollections;
    }

    /** @return the statements of the {@code exclude} option, in the order they were given. */
    List<String> getExclusions() {
        return exclusions;
    }

    static class Builder {
        private String targetNamespace = null;
        private boolean multiPattern = false;
        private boolean verbose = false;
        private boolean allNumericConstraints = false;
        private boolean notNullAnnotations = true;
        private boolean notNullCustomMessage = false;
        private boolean notNullPrefixFieldName = false;
        private boolean notNullPrefixClassName = false;
        private String notNullCustomMessageText = null;
        private boolean itemAnnotations = true;
        private boolean generateValidOnCollections = true;
        private final List<String> exclusions = new ArrayList<>();

        private Builder() {
        }

        /** @return 1 if the argument is an option of this plugin, 0 otherwise. */
        int parseArgument(String option) throws BadCommandLineException {
            if (option.equals("-" + BeanValidationPlugin.PLUGIN_NAME)) {
                // the plain option switches the plugin on, which XJC did before calling this
                return 1;
            }
            if (!option.startsWith(BeanValidationPlugin.OPTION_PREFIX)) {
                return 0;
            }
            final String rest = option.substring(BeanValidationPlugin.OPTION_PREFIX.length());
            final int equals = rest.indexOf('=');
            final String name = equals == -1 ? rest : rest.substring(0, equals);
            final String value = equals == -1 ? "true" : rest.substring(equals + 1);
            setValue(BeanValidationOption.parse(name), value);
            return 1;
        }

        private void setValue(BeanValidationOption argument, final String value)
                throws BadCommandLineException {
            try {
                String error = argument.setValue(this, value);
                if (error != null) {
                    throw new BadCommandLineException(
                            "option " + argument.name() + ": " +
                            (error.length() > 0 ? error + ", " : "") +
                            "cannot accept '" + value + "' as a " + argument.getTypeName());
                }
            } catch (NullPointerException ex) {
                throw new BadCommandLineException(argument.errorMessage(value));
            }
        }

        Builder targetNamespace(final String value) {
            this.targetNamespace = value;
            return this;
        }

        Builder multiPattern(final boolean value) {
            this.multiPattern = value;
            return this;
        }

        Builder verbose(final boolean value) {
            this.verbose = value;
            return this;
        }

        Builder notNullAnnotations(final boolean value) {
            this.notNullAnnotations = value;
            return this;
        }

        Builder allNumericConstraints(final boolean value) {
            this.allNumericConstraints = value;
            return this;
        }

        Builder notNullCustomMessage(final boolean value) {
            this.notNullCustomMessage = value;
            return this;
        }

        Builder notNullPrefixFieldName(final boolean value) {
            this.notNullPrefixFieldName = value;
            return this;
        }

        Builder notNullPrefixClassName(final boolean value) {
            this.notNullPrefixClassName = value;
            return this;
        }

        Builder notNullCustomMessageText(final String value) {
            this.notNullCustomMessageText = value;
            return this;
        }

        Builder itemAnnotations(final boolean value) {
            this.itemAnnotations = value;
            return this;
        }

        Builder generateValidOnCollections(final boolean value) {
            this.generateValidOnCollections = value;
            return this;
        }

        /** Adds one statement of the {@code exclude} option, which can be repeated. */
        Builder exclusion(final String value) {
            this.exclusions.add(value);
            return this;
        }

        BeanValidationOptions build() {
            return new BeanValidationOptions(targetNamespace, multiPattern,
                    verbose, allNumericConstraints, notNullAnnotations, notNullCustomMessage,
                    notNullPrefixFieldName, notNullPrefixClassName, notNullCustomMessageText,
                    itemAnnotations, generateValidOnCollections,
                    exclusions);
        }
    }

    static BeanValidationOptions.Builder builder() {
        return new BeanValidationOptions.Builder();
    }

    private BeanValidationOptions(final String targetNamespace, final boolean multiPattern, final boolean verbose,
            final boolean allNumericConstraints, final boolean notNullAnnotations,
            final boolean notNullCustomMessage, final boolean notNullPrefixFieldName,
            final boolean notNullPrefixClassName, final String notNullCustomMessageText,
            final boolean itemAnnotations,
            final boolean generateValidOnCollections,
            final List<String> exclusions) {
        this.targetNamespace = targetNamespace;
        this.multiPattern = multiPattern;
        this.verbose = verbose;
        this.allNumericConstraints = allNumericConstraints;
        this.notNullAnnotations = notNullAnnotations;
        this.notNullCustomMessage = notNullCustomMessage;
        this.notNullPrefixFieldName = notNullPrefixFieldName;
        this.notNullPrefixClassName = notNullPrefixClassName;
        this.notNullCustomMessageText = notNullCustomMessageText;
        this.itemAnnotations = itemAnnotations;
        this.generateValidOnCollections = generateValidOnCollections;
        this.exclusions = exclusions;
    }
}
