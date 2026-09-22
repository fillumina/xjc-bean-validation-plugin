package com.fillumina.xjc.validation;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes annotations with their parameters on a {@link JFieldVar}, keeping duplicates out.
 *
 * @author Francesco Illuminati
 */
class AnnotationWriter {

    private final JFieldVar field;
    private final AnnotationLog logger;
    /** When not null the annotations are collected here instead of being written to the field. */
    private final List<Annotate> collector;
    /** True when this writer annotates the items of the field, which go on its type argument. */
    private final boolean typeArgument;
    /** Every annotation written on the field, so it can be taken off again. */
    private final List<JAnnotationUse> written = new ArrayList<>();
    private final Set<Class<? extends Annotation>> annotationSet = new HashSet<>();

    AnnotationWriter(JFieldVar field, AnnotationLog logger) {
        this(field, logger, null, false);
    }

    AnnotationWriter(JFieldVar field, AnnotationLog logger, List<Annotate> collector) {
        this(field, logger, collector, false);
    }

    /**
     * @param typeArgument true when the annotations belong on the type argument of the field rather
     *     than on the field itself, so that a caller can tell the two apart while they are collected
     */
    AnnotationWriter(JFieldVar field, AnnotationLog logger, List<Annotate> collector,
            boolean typeArgument) {
        this.field = field;
        this.logger = logger;
        this.collector = collector;
        this.typeArgument = typeArgument;
    }

    Annotate annotate(Class<? extends Annotation> annotation) {
        return new Annotate(annotation);
    }

    /**
     * Takes the annotations off the field and returns them, so that the caller can write them
     * somewhere else, such as on the type argument of the field.
     */
    List<JAnnotationUse> detachAnnotations() {
        List<JAnnotationUse> detached = new ArrayList<>(written);
        for (JAnnotationUse annotation : detached) {
            field.removeAnnotation(annotation);
        }
        written.clear();
        return detached;
    }

    class Annotate {
        private final Class<? extends Annotation> annotationClass;
        private final JAnnotationUse annotationUse;
        /** False when the annotation is a duplicate and is written nowhere. */
        private final boolean active;
        private final boolean typeArgument;
        private final Map<String, String> parameterMap = new LinkedHashMap<>();

        Annotate(JAnnotationUse annotationUse) {
            this.annotationClass = null;
            this.annotationUse = annotationUse;
            this.active = annotationUse != null;
            this.typeArgument = false;
        }

        Annotate(Class<? extends Annotation> annotation) {
            this.annotationClass = annotation;
            this.typeArgument = AnnotationWriter.this.typeArgument;
            // @Pattern is allowed more than once on the same target
            boolean used = annotationSet.add(annotation) || annotation.equals(ValidationAnnotations.PATTERN);
            this.active = used;
            if (!used) {
                this.annotationUse = null;
            } else if (collector == null) {
                this.annotationUse = field.annotate(annotation);
                written.add(this.annotationUse);
            } else {
                this.annotationUse = null;
                collector.add(this);
            }
        }

        /** @return the annotation that would have been written, with the parameters it was given. */
        Class<? extends Annotation> getAnnotationClass() {
            return annotationClass;
        }

        /** @return true when the annotation belongs on the type argument of the field. */
        boolean isTypeArgument() {
            return typeArgument;
        }

        Map<String, String> getParameters() {
            return parameterMap;
        }

        /**
         * Records a parameter: it is always remembered, and written only when there is an
         * annotation to write it into — a collected annotation has none.
         *
         * @return true when the parameter also has to be written
         */
        private boolean record(String name, String value) {
            if (!active || value == null || parameterMap.containsKey(name)) {
                return false;
            }
            parameterMap.put(name, value);
            return annotationUse != null;
        }

        Annotate paramIf(boolean condition, String name, Integer value) {
            return condition ? param(name, value) : this;
        }

        Annotate param(String name, Integer value) {
            if (value != null && record(name, value.toString())) {
                annotationUse.param(name, value);
            }
            return this;
        }

        Annotate param(String name, Boolean value) {
            if (value != null && record(name, value.toString())) {
                annotationUse.param(name, value);
            }
            return this;
        }

        Annotate param(String name, BigDecimal value) {
            if (value != null && record(name, value.toString())) {
                annotationUse.param(name, value.toString());
            }
            return this;
        }

        Annotate param(String name, String value) {
            if (record(name, value)) {
                annotationUse.param(name, value);
            }
            return this;
        }

        Annotate param(String name, String value, String defaultValue) {
            String v = value == null ? defaultValue : value;
            if (record(name, v)) {
                annotationUse.param(name, v);
            }
            return this;
        }

        Annotate param(String name, Integer value, Integer defaultValue) {
            Integer v = value == null ? defaultValue : value;
            if (v != null && record(name, v.toString())) {
                annotationUse.param(name, v);
            }
            return this;
        }

        /** Only an annotation that was written is logged: a collected one was not. */
        void log() {
            if (annotationUse != null) {
                String annotationName = annotationUse.getAnnotationClass().name();
                logger.addAnnotation(annotationName, parameterMap);
            }
        }

        MultipleAnnotation multipleAnnotationContainer(String paramName) {
            JAnnotationArrayMember array = annotationUse.paramArray(paramName);
            return new MultipleAnnotation(array);
        }

        class MultipleAnnotation {
            private final JAnnotationArrayMember array;

            MultipleAnnotation(JAnnotationArrayMember array) {
                this.array = array;
            }

            Annotate annotate(Class<? extends Annotation> annotationClass) {
                JAnnotationUse annotationUse = array.annotate(annotationClass);
                return new Annotate(annotationUse);
            }
        }
    }
}
