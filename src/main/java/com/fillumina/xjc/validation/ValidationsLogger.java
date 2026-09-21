package com.fillumina.xjc.validation;

import java.util.Map;

/**
 *
 * @author Francesco Illuminati 
 */
interface ValidationsLogger {
    static final String PREFIX = "[" + BeanValidationPlugin.PLUGIN_NAME + "] ";

    void addAnnotation(String annotationName, Map<String, String> parameterMap);

    /** Reports something the caller should know about, whether or not the run is verbose. */
    void warning(String message);

}
