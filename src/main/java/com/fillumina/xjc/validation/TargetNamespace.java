package com.fillumina.xjc.validation;

/**
 * The {@code targetNamespace} option: which elements carry {@code @Valid}.
 */
final class TargetNamespace {

    /**
     * @return true when the option names no namespace, so every element qualifies, or when the
     *     namespace of the schema is one it names. The literal text {@code null} is read like an
     *     unset option: no restriction.
     */
    static boolean accepts(String option, String schemaNamespace) {
        if (option == null || option.isEmpty() || "null".equals(option)) {
            return true;
        }
        return schemaNamespace.startsWith(option);
    }

    private TargetNamespace() {
    }
}
