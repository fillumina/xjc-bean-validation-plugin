package com.fillumina.xjc.validation;

import java.util.Map;

/**
 * The logger of a run that is not verbose: it drops what the plugin did and reports only what it
 * could not do.
 *
 * @author Francesco Illuminati
 */
class AnnotationLogWarning implements AnnotationLog {

    static final AnnotationLogWarning INSTANCE = new AnnotationLogWarning();

    private AnnotationLogWarning() {}

    @Override
    public void addAnnotation(String annotationName, Map<String, String> parameterMap) {
        // do nothing
    }

    @Override
    public void info(String message) {
        // do nothing
    }

    /**
     * A warning is not the chatter this logger exists to silence: it says that something asked for
     * had no effect, so it goes to the error stream.
     */
    @Override
    public void warning(String message) {
        System.err.println(PREFIX + "warning: " + message);
    }

}
