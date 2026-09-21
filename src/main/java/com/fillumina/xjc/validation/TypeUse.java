package com.fillumina.xjc.validation;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JTypeVar;
import java.util.Iterator;
import java.util.List;

/**
 * A type that prints its annotations before itself, so that a type argument can carry them:
 * {@code List<@Size(max = 5) String>}. Used as the type argument of a collection field.
 *
 * <p>This is the only way to write a constraint on the items of a collection: an annotation on the
 * field constrains the collection itself, and the {@code @Each*} annotations of the old line are
 * inert with a current provider. The annotations come from the field, where they were created, so
 * that codemodel renders and escapes their parameters; the caller takes them off the field first.
 *
 * <p>The methods that describe the wrapped type are answered by the element type, which is correct
 * while the element carries no type variables of its own.
 */
class TypeUse extends JClass {

    private final JClass element;
    private final List<JAnnotationUse> annotations;

    TypeUse(JClass element, List<JAnnotationUse> annotations) {
        super(element.owner());
        this.element = element;
        this.annotations = annotations;
    }

    @Override
    public void generate(JFormatter formatter) {
        for (JAnnotationUse annotation : annotations) {
            formatter.g(annotation).p(" ");
        }
        formatter.t(element);
    }

    @Override
    public String name() {
        return element.name();
    }

    @Override
    public String fullName() {
        return element.fullName();
    }

    @Override
    public JPackage _package() {
        return element._package();
    }

    @Override
    public JClass _extends() {
        return element._extends();
    }

    @Override
    public Iterator<JClass> _implements() {
        return element._implements();
    }

    @Override
    public boolean isInterface() {
        return element.isInterface();
    }

    @Override
    public boolean isAbstract() {
        return element.isAbstract();
    }

    @Override
    protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings) {
        return this;
    }
}
