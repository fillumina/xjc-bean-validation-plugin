# Changelog

## 1.0.0-SNAPSHOT

- First release of the standalone plugin. It was carried inside `com.fillumina:krasa-jaxb-tools`
  until 2.8.0, in the same jar and the same service file as the primitives plugin.
- Jakarta only: built for JDK 21 and XJC 4, the Jakarta XML Binding 4 line, and it writes Jakarta
  Bean Validation 3.1 annotations. The `validationAnnotations` option, which chose between `javax`
  and `jakarta`, is gone, and so is the `javax` half of the annotation surface.
- The constraints of the items of a collection are written on its type argument, as in
  `List<@Size(max = 5) String>`. They replace the `@Each*` annotations of the old line, which came
  from an unmaintained javax-only library and were inert with a current provider, and they are on by
  default. The option that used to switch them on keeps its name, `generateListAnnotations`.
- `@Valid` on a collection is written on the type argument, `List<@Valid Other>`, instead of on the
  container, where Bean Validation deprecated it (HV000271).
- `exclude` no longer names the `@Each*` annotations, which no longer exist: it names the Jakarta
  ones.
- The option is `-XBeanValidationAnnotations`, and `-XJsr303Annotations` is still accepted as its
  old name.
