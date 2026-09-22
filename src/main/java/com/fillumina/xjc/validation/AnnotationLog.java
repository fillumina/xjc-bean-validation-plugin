package com.fillumina.xjc.validation;

import java.util.Map;

/**
 * Writes what a run did, and what it could not do. There is one for each class and property the
 * verbose mode reports on, and {@link AnnotationLogWarning} for the runs that report nothing.
 *
 * @author Francesco Illuminati
 */
interface AnnotationLog {
    String PREFIX = "[" + BeanValidationPlugin.PLUGIN_NAME + "] ";

    void addAnnotation(String annotationName, Map<String, String> parameterMap);

    /** Reports the chatter of the verbose mode, which a quiet run is free to drop. */
    void info(String message);

    /** Reports something the caller should know about, whether or not the run is verbose. */
    void warning(String message);

}
