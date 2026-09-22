# xjc-bean-validation-plugin

An XJC plugin that reads the restrictions of an XML Schema and writes the matching Jakarta Bean
Validation annotations into the classes XJC generates: `xs:maxLength` becomes `@Size`, `xs:pattern`
becomes `@Pattern`, a required element becomes `@NotNull`, and so on.

```java
// xs:element name="name" type="a:ShortText" maxOccurs="unbounded",
// with ShortText restricted to xs:maxLength value="5"
protected List<@Size(max = 5) String> name;
```

The constraint of the items of a collection is written on the type argument, which is the form a
current provider enforces. The plugin was split out of [`com.fillumina:krasa-jaxb-tools`](
https://github.com/fillumina/krasa-jaxb-tools), which continues the XJC addon that Vojtech Krasa
wrote; the code here is a port of that one. It lived there in the same jar and the same service file
as the primitives plugin, which is the entanglement this project breaks.

## Requirements

- JDK 21 or newer.
- An XJC 4 build, that is Jakarta XML Binding 4. The plugin is jakarta-only: there is no `javax`
  flavour, and no `validationAnnotations` option to choose one.
- Jakarta Bean Validation 3.1, which the generated classes need at runtime and the plugin brings
  with it.

## Using it

With the XJC command line:

```
xjc -extension -XBeanValidationAnnotations schema.xsd
```

Inside a Maven build the plugin goes on the classpath of whatever runs XJC, which with the
`jaxb-maven-plugin` means declaring it as a dependency of that plugin and passing
`-XBeanValidationAnnotations` among the arguments.

## Options

Every option is written after the plugin name, as in
`-XBeanValidationAnnotations:generateNotNullAnnotations=false`.

- `targetNamespace` — adds `@Valid` to the elements of the given namespace only.
- `generateNotNullAnnotations` — on by default; writes `@NotNull` on the elements and attributes the
  schema requires.
- `notNullAnnotationsCustomMessages` — the message of `@NotNull`: `true`, `false`, `FieldName`,
  `ClassName` or a text, where `{ClassName}` and `{FieldName}` are replaced.
- `generateItemAnnotations` — on by default; writes the constraints of the items of a collection on
  its type argument.
- `generateValidOnCollections` — on by default; writes `@Valid` on the type argument of a
  collection, so that its elements are validated in turn.
- `generateAllNumericConstraints` — writes `@DecimalMin` and `@DecimalMax` even when the bound is
  inside the range of the Java type.
- `multiPattern` — writes one `@Pattern` per alternative instead of a `@Pattern.List`.
- `exclude` — leaves a class or a property out of the annotations, sets one of their parameters, or
  writes another annotation in their place. Repeatable.
- `verbose` — prints the options in use and every annotation written.

## Building

The build needs JDK 21 and Maven, and nothing else. With both on the path:

```
mvn -B verify
```
