package com.fillumina.xjc.validation;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JTypeVar;
import java.util.Iterator;
import java.util.List;

/**
 * A type that prints annotations immediately before its simple name, so that a
 * type argument can carry them: {@code List<java.lang.@Size(max = 5) String>}.
 *
 * <p>This is the only way to write a constraint on the items of a collection: an annotation on the
 * field constrains the collection itself, so the constraints of the items have to travel with the
 * type argument. They come from the field, where they were created, so that codemodel renders and
 * escapes their parameters; the caller takes them off the field first.
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
        JClass enclosing = element.outer();
        if (enclosing != null) {
            formatter.t(enclosing).p(".");
        } else if (element._package() != null && !element._package().isUnnamed()) {
            // The formatter may qualify a type after resolving import collisions. Write the
            // package explicitly so annotations always precede the type name, not its package.
            formatter.p(element._package().name()).p(".");
        }
        writeAnnotations(formatter);
        formatter.p(element.name());
    }

    private void writeAnnotations(JFormatter formatter) {
        for (JAnnotationUse annotation : annotations) {
            formatter.g(annotation).p(" ");
        }
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
