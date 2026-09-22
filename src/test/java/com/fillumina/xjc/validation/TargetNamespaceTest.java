package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TargetNamespaceTest {

    @Test
    void configuredNamespaceMatchesSchemaNamespacesWithThatPrefix() {
        assertTrue(TargetNamespace.accepts("https://example.test/a", "https://example.test/a/v1"));
        assertFalse(TargetNamespace.accepts("https://example.test/a", "https://example.test/b"));
    }

    @Test
    void emptyAndLiteralNullNamespacesLeaveTheSelectionUnrestricted() {
        assertTrue(TargetNamespace.accepts("", "https://example.test/a"));
        assertTrue(TargetNamespace.accepts("null", "https://example.test/a"));
    }
}
