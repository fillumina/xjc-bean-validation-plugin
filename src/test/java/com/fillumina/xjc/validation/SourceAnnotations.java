package com.fillumina.xjc.validation;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * The annotations of the generated classes, read from the syntax tree instead of from the text, so
 * that a change in the way the generator wraps or orders its lines cannot move the expectations.
 *
 * <p>The text is one line per class, one per field, and then one per annotation, with the
 * annotations of the items of a collection under an {@code items} line:
 *
 * <pre>
 * Container
 *     listOfString
 *         @NotNull
 *         items
 *             @Size(max = 5)
 * </pre>
 */
final class SourceAnnotations {

    private SourceAnnotations() {
    }

    /** @param sources the generated sources, in the order the fixture found them. */
    static String of(List<Path> sources) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager files =
                compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units = files.getJavaFileObjectsFromPaths(sources);
            JavacTask task = (JavacTask) compiler.getTask(null, files, null,
                    List.of("-proc:none"), null, units);
            StringBuilder text = new StringBuilder();
            for (CompilationUnitTree unit : task.parse()) {
                for (Tree declaration : unit.getTypeDecls()) {
                    if (declaration instanceof ClassTree type) {
                        appendClass(text, type);
                    }
                }
            }
            return text.toString();
        }
    }

    private static void appendClass(StringBuilder text, ClassTree type) {
        List<VariableTree> fields = new ArrayList<>();
        for (Tree member : type.getMembers()) {
            if (member instanceof VariableTree field && isAProperty(field)) {
                fields.add(field);
            }
        }
        // a class with no fields of its own is listed too, as the old line's extraction listed it:
        // that it exists and carries nothing is information, and it keeps the expectations
        // comparable with the ones they were ported from
        fields.sort(Comparator.comparing(field -> field.getName().toString()));

        text.append(type.getSimpleName()).append('\n');
        for (VariableTree field : fields) {
            text.append("    ").append(field.getName()).append('\n');
            for (String annotation : annotationsOf(field.getModifiers().getAnnotations())) {
                text.append("        ").append(annotation).append('\n');
            }
            List<String> items = itemAnnotationsOf(field.getType());
            if (!items.isEmpty()) {
                text.append("        items").append('\n');
                for (String annotation : items) {
                    text.append("            ").append(annotation).append('\n');
                }
            }
        }
    }

    /**
     * Whether this member is one of the properties XJC generates, which are {@code protected}. The
     * old line's extraction looked for lines starting with {@code protected}, so an enumeration's
     * constants and the {@code value} field JAXB puts in a generated enum were not part of the
     * expectations, and they are not part of them here either.
     */
    private static boolean isAProperty(VariableTree field) {
        return field.getModifiers().getFlags().contains(Modifier.PROTECTED);
    }

    /**
     * The annotations of a declaration, sorted, with the ones of the binding left out: they are not
     * the subject here, and they carry no meaning for the validation.
     */
    private static List<String> annotationsOf(List<? extends AnnotationTree> annotations) {
        List<String> rendered = new ArrayList<>();
        for (AnnotationTree annotation : annotations) {
            String name = nameOf(annotation.getAnnotationType());
            if (!name.startsWith("Xml")) {
                rendered.add(render(name, annotation.getArguments()));
            }
        }
        Collections.sort(rendered);
        return rendered;
    }

    /** @return the annotations the type arguments of a field carry, which is where a container's
     *      constraints are written. */
    private static List<String> itemAnnotationsOf(Tree type) {
        if (!(type instanceof ParameterizedTypeTree parameterized)) {
            return List.of();
        }
        List<String> rendered = new ArrayList<>();
        for (Tree argument : parameterized.getTypeArguments()) {
            if (argument instanceof AnnotatedTypeTree annotated) {
                rendered.addAll(annotationsOf(annotated.getAnnotations()));
            }
        }
        return rendered;
    }

    private static String render(String name, List<? extends ExpressionTree> arguments) {
        if (arguments.isEmpty()) {
            return "@" + name;
        }
        return "@" + name + "(" + arguments.stream()
                .map(SourceAnnotations::render)
                .collect(Collectors.joining(", ")) + ")";
    }

    private static String render(ExpressionTree expression) {
        if (expression instanceof AssignmentTree assignment) {
            return assignment.getVariable() + " = " + render(assignment.getExpression());
        }
        if (expression instanceof LiteralTree literal) {
            Object value = literal.getValue();
            return value instanceof String ? quote((String) value) : String.valueOf(value);
        }
        if (expression instanceof NewArrayTree array) {
            List<? extends ExpressionTree> initializers = array.getInitializers();
            return "{" + (initializers == null ? "" : initializers.stream()
                    .map(SourceAnnotations::render)
                    .collect(Collectors.joining(", "))) + "}";
        }
        if (expression instanceof AnnotationTree annotation) {
            return render(nameOf(annotation.getAnnotationType()), annotation.getArguments());
        }
        return expression.toString();
    }

    /**
     * @return the name of the annotation as it was written: {@code Pattern.List} keeps its
     *     qualifier, since a nested annotation is not the same as a top level one of the same name
     */
    private static String nameOf(Tree type) {
        if (type instanceof MemberSelectTree select) {
            return select.getExpression() + "." + select.getIdentifier();
        }
        return type.toString();
    }

    /** The literal as a Java string, so that a pattern reads as it was written in the schema. */
    private static String quote(String value) {
        StringBuilder quoted = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> quoted.append("\\\\");
                case '"' -> quoted.append("\\\"");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> quoted.append(c);
            }
        }
        return quoted.append('"').toString();
    }
}
