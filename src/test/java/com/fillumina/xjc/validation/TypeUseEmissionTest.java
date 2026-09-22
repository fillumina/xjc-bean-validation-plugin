package com.fillumina.xjc.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pins the way a constraint is put on the items of a collection: the annotation is created on the
 * field, taken off it, and printed in front of the element type of the type argument.
 *
 * <p>The type of the field is replaced through {@code JVar.type(JType)}, which is public. The
 * emission spike reached the same result by setting a private field with reflection; this test is
 * what fails if that public setter is ever taken away.
 */
class TypeUseEmissionTest {

    @TempDir
    Path outputDirectory;

    @Test
    void theAnnotationIsPrintedInFrontOfTheElementTypeWithItsImport() throws Exception {
        String generated = generate(field -> {
            ItemAnnotator items = new ItemAnnotator(field, AnnotationLogWarning.INSTANCE, null);
            items.addSizeAnnotation(null, 5);
            items.apply();
        });

        assertTrue(generated.contains("import jakarta.validation.constraints.Size;"), generated);
        assertTrue(generated.contains("protected List<@Size(max = 5) String> names;"), generated);
    }

    @Test
    void theFieldKeepsItsOwnAnnotationAndItsOwnType() throws Exception {
        String generated = generate(field -> {
            new AnnotationWriter(field, AnnotationLogWarning.INSTANCE)
                    .annotate(ValidationAnnotations.NOT_NULL)
                    .log();
            ItemAnnotator items = new ItemAnnotator(field, AnnotationLogWarning.INSTANCE, null);
            items.addSizeAnnotation(1, 5);
            items.addValidAnnotation();
            items.apply();
        });

        assertTrue(generated.contains("@NotNull"), generated);
        assertTrue(generated.contains("protected List<@Size(min = 1, max = 5) @Valid String> names;"),
                generated);
    }

    @Test
    void nothingIsChangedWhenTheItemsCarryNoAnnotation() throws Exception {
        String generated = generate(field -> {
            new ItemAnnotator(field, AnnotationLogWarning.INSTANCE, null).apply();
        });

        assertTrue(generated.contains("protected List<String> names;"), generated);
        assertEquals(false, generated.contains("@Size"));
        assertEquals(false, generated.contains("import jakarta.validation"));
    }

    private interface FieldSetup {
        void apply(JFieldVar field);
    }

    /** Builds a one-field class through codemodel and returns the file it wrote. */
    private String generate(FieldSetup setup) throws Exception {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass bean = codeModel._class("example.Bean");
        JFieldVar field = bean.field(JMod.PROTECTED,
                codeModel.ref(List.class).narrow(String.class), "names");
        setup.apply(field);
        codeModel.build(outputDirectory.toFile());
        return Files.readString(outputDirectory.resolve("example/Bean.java"));
    }
}
