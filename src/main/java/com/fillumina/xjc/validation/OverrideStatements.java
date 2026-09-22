package com.fillumina.xjc.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The statements of the {@code override} option. A statement is
 * {@code ClassGlob[#FieldGlob][@AnnotationGlob][:parameter = value][=@Annotation]}:
 *
 * <ul>
 * <li>the class glob is matched against the qualified name of the generated class, and the property
 *     glob against the name of the property, both with {@code *} and {@code ?} and everything else
 *     literal;</li>
 * <li>a statement without {@code #} covers the whole class;</li>
 * <li>the annotation glob, when present, is matched against the simple name of the annotations this
 *     plugin computed, and only those are left out, or replaced, or given the parameter. Without a
 *     prefix it covers annotations on the field. Written as {@code @List.Size} it covers the
 *     annotations of the items of a collection, which go on the type argument;</li>
 * <li>a statement with no annotation part covers every annotation the plugin would write;</li>
 * <li>{@code :parameter = value} sets one parameter of the computed annotation, {@code {…}} being a
 *     value the plugin knows, and {@code = @Annotation(...)} writes that annotation instead.</li>
 * </ul>
 *
 * @author Francesco Illuminati
 */
class OverrideStatements {

    private static final OverrideStatements NONE = new OverrideStatements(Collections.<Statement>emptyList());

    /** @return {@code null} when the statement is well formed, the reason when it is not. */
    static String validate(String statement) {
        try {
            Statement.parse(statement);
            return null;
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    static OverrideStatements of(List<String> statements) {
        if (statements == null || statements.isEmpty()) {
            return NONE;
        }
        List<Statement> parsed = new ArrayList<>();
        for (String statement : statements) {
            parsed.add(Statement.parse(statement));
        }
        return new OverrideStatements(parsed);
    }

    private final List<Statement> statements;

    private OverrideStatements(List<Statement> statements) {
        this.statements = statements;
    }

    /**
     * A property can be covered by more than one statement once they name an annotation: one leaves
     * {@code @NotNull} out, another sets a message on {@code @Size}. They apply in the order given.
     */
    List<Statement> statementsFor(String className, String propertyName) {
        List<Statement> matching = new ArrayList<>();
        for (Statement statement : statements) {
            if (statement.matches(className, propertyName)) {
                matching.add(statement);
            }
        }
        return matching;
    }

    /**
     * @return the statements that matched no class, no property, or — when they name one — no
     *     annotation, which are likely typos: a statement that does nothing is worse than none.
     */
    List<Statement> unmatched() {
        List<Statement> unmatched = new ArrayList<>();
        for (Statement statement : statements) {
            if (statement.isUnmatched()) {
                unmatched.add(statement);
            }
        }
        return unmatched;
    }

    static class Statement {

        private static final String REGEX_SPECIAL = "\\.[]{}()<>*+-=!?^$|";

        /** The annotation glob of the items is the one behind this prefix, as in @List.Size. */
        private static final String ITEMS_PREFIX = "List.";

        private final String text;
        private final Pattern classPattern;
        private final Pattern propertyPattern;
        private final Pattern annotationPattern;
        private final String parameter;
        private final String parameterValue;
        private final String replacement;
        /** True when the statement names the annotations of the items, as {@code @List.Size} does. */
        private final boolean itemsOnly;
        private boolean matched;
        private boolean annotationMatched;

        static Statement parse(String text) {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("no class name");
            }
            final String value = text.trim();
            String head = value;
            String tail = null;
            final int equals = value.indexOf('=');
            if (equals != -1) {
                head = value.substring(0, equals).trim();
                tail = value.substring(equals + 1).trim();
            }
            String parameter = null;
            final int colon = head.indexOf(':');
            if (colon != -1) {
                parameter = head.substring(colon + 1).trim();
                head = head.substring(0, colon).trim();
                if (parameter.isEmpty()) {
                    throw new IllegalArgumentException("no parameter name after the :");
                }
                if (tail == null || tail.isEmpty()) {
                    throw new IllegalArgumentException(
                            "the parameter " + parameter + " needs a value after the =");
                }
            }
            String annotationGlob = null;
            boolean itemsOnly = false;
            final int at = head.indexOf('@');
            if (at != -1) {
                String before = head.substring(0, at).trim();
                annotationGlob = head.substring(at + 1).trim();
                if (annotationGlob.startsWith(ITEMS_PREFIX)) {
                    itemsOnly = true;
                    annotationGlob = annotationGlob.substring(ITEMS_PREFIX.length()).trim();
                    if (annotationGlob.isEmpty()) {
                        throw new IllegalArgumentException(
                                "no annotation name after " + ITEMS_PREFIX + ", as in @List.Size");
                    }
                }
                head = before;
                if (annotationGlob.isEmpty()) {
                    throw new IllegalArgumentException("no annotation name after the @");
                }
            }
            if (parameter != null && annotationGlob == null) {
                throw new IllegalArgumentException("the parameter " + parameter
                        + " needs the annotation it belongs to, as in @Size:max = 5");
            }
            String classGlob = head;
            String propertyGlob = null;
            final int hash = head.indexOf('#');
            if (hash != -1) {
                classGlob = head.substring(0, hash).trim();
                propertyGlob = head.substring(hash + 1).trim();
                if (classGlob.isEmpty()) {
                    throw new IllegalArgumentException("no class name before the #");
                }
                if (propertyGlob.isEmpty()) {
                    throw new IllegalArgumentException("no property name after the #");
                }
            }
            if (classGlob.isEmpty()) {
                throw new IllegalArgumentException("no class name");
            }
            // an empty value means the annotation is removed, as if it were not there
            String replacement = parameter == null && tail != null && !tail.isEmpty() ? tail : null;
            return new Statement(value, toPattern(classGlob),
                    propertyGlob == null ? null : toPattern(propertyGlob),
                    annotationGlob == null ? null : toPattern(annotationGlob),
                    parameter, parameter == null ? null : tail, replacement, itemsOnly);
        }

        /** @return the glob as a pattern: {@code *} and {@code ?} only, everything else literal. */
        private static Pattern toPattern(String glob) {
            StringBuilder regex = new StringBuilder("^");
            for (int i = 0; i < glob.length(); i++) {
                char c = glob.charAt(i);
                if (c == '*') {
                    regex.append(".*");
                } else if (c == '?') {
                    regex.append('.');
                } else {
                    if (REGEX_SPECIAL.indexOf(c) >= 0) {
                        regex.append('\\');
                    }
                    regex.append(c);
                }
            }
            return Pattern.compile(regex.append('$').toString());
        }

        private Statement(String text, Pattern classPattern, Pattern propertyPattern,
                Pattern annotationPattern, String parameter, String parameterValue,
                String replacement, boolean itemsOnly) {
            this.text = text;
            this.classPattern = classPattern;
            this.propertyPattern = propertyPattern;
            this.annotationPattern = annotationPattern;
            this.parameter = parameter;
            this.parameterValue = parameterValue;
            this.replacement = replacement;
            this.itemsOnly = itemsOnly;
        }

        boolean matches(String className, String propertyName) {
            if (!classPattern.matcher(className).matches()) {
                return false;
            }
            if (propertyPattern != null && !propertyPattern.matcher(propertyName).matches()) {
                return false;
            }
            matched = true;
            return true;
        }

        /** @return true when the statement covers this computed annotation. */
        boolean coversAnnotation(String simpleName, boolean isTypeArgument) {
            if (annotationPattern == null) {
                return true;
            }
            if (itemsOnly != isTypeArgument) {
                // A bare annotation selector names the field; the List. prefix names its items.
                return false;
            }
            if (annotationPattern.matcher(simpleName).matches()) {
                annotationMatched = true;
                return true;
            }
            return false;
        }

        /** @return true when nothing this statement names was ever found. */
        boolean isUnmatched() {
            return !matched || (annotationPattern != null && !annotationMatched);
        }

        boolean hasParameter() {
            return parameter != null;
        }

        String getParameter() {
            return parameter;
        }

        String getParameterValue() {
            return parameterValue;
        }

        boolean hasReplacement() {
            return replacement != null;
        }

        String getReplacement() {
            return replacement;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
