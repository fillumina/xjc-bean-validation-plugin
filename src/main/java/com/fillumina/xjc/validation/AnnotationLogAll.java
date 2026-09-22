package com.fillumina.xjc.validation;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * The logger of a verbose run: it prints every annotation it writes, and what it writes it on.
 *
 * @author Francesco Illuminati
 */
class AnnotationLogAll implements AnnotationLog {
    private final String className;
    private final String propertyName;

    AnnotationLogAll(String className, String propertyName) {
        this.className = className;
        this.propertyName = propertyName;
    }

    @Override
    public void addAnnotation(String annotationName, Map<String, String> parameterMap) {
        String params = "";
        if (!parameterMap.isEmpty()) {
            params = parameterMap.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));
            if (!params.isEmpty()) {
                params = "(" + params + ")";
            }
        }
        log("adding @" + annotationName + params + " to " + className + "." + propertyName);
    }

    @Override
    public void info(String message) {
        log(message);
    }

    @Override
    public void warning(String message) {
        log("warning: " + message);
    }

    private static void log(String message) {
        System.out.println(PREFIX + message);
    }

}
