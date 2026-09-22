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

Inside a Maven build the plugin goes on the classpath of whatever runs XJC. With the
`jaxb-maven-plugin` that means declaring it as an XJC plugin of that plugin and passing the options
among the arguments:

```xml
<plugin>
  <groupId>org.jvnet.jaxb</groupId>
  <artifactId>jaxb-maven-plugin</artifactId>
  <version>4.0.9</version>
  <executions>
    <execution>
      <goals>
        <goal>generate</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <extension>true</extension>
    <args>
      <!-- the plain option is what activates the plugin: without it, an option carrying a
           value is handed to every plugin and this one never runs -->
      <arg>-XBeanValidationAnnotations</arg>
      <!-- every constraint the schema states is written by default: these narrow it -->
      <arg>-XBeanValidationAnnotations:omitJavaTypeBounds=true</arg>
      <arg>-XBeanValidationAnnotations:patternList=true</arg>
      <!-- and these correct what was computed by the plugin -->
      <arg>-XBeanValidationAnnotations:override=*#code@Pattern:message = "not a code"</arg>
      <arg>-XBeanValidationAnnotations:override=*#children[@Valid]</arg>
      <arg>-XBeanValidationAnnotations:override=*#amount@Decimal*=@DecimalMax(value = "99.5")</arg>
    </args>
    <plugins>
      <plugin>
        <groupId>com.fillumina</groupId>
        <artifactId>xjc-bean-validation-plugin</artifactId>
        <version>1.0.0</version>
      </plugin>
    </plugins>
  </configuration>
</plugin>
```

Two details that matter:

- **the plain option has to be there.** XJC activates a plugin only for the argument equal to its name
  alone, so a build that passes only `-XBeanValidationAnnotations:override=…` generates classes with
  no annotations at all and no error to explain it;
- **the brackets of an item selector need no escaping**, in XML or in a shell, which is why the
  selector is written `[@Valid]` and not `<@Valid>`. Quotation marks inside a statement are fine as
  they are; in a shell, quote the whole statement because of its `*`.

## Options

Every option is written after the plugin name, as in
`-XBeanValidationAnnotations:generateNotNullAnnotations=false`.

**The default is to write every constraint the schema states.** A bound, a length, a pattern or a
requirement that the schema declares ends up on the generated class; the options are there to leave
one out, to choose the form of one, or to change a message — not to switch a kind of validation on.

On by default: `generateNotNullAnnotations`, `generateItemAnnotations` and
`generateValidOnCollections`. Off by default, because they only narrow what is written or change how
it reads: `omitJavaTypeBounds`, `patternList` and `verbose`.

- `targetNamespace` — adds `@Valid` to the elements of the given namespace only.
- `generateNotNullAnnotations` — on by default; writes `@NotNull` on the elements and attributes the
  schema requires.
- `notNullAnnotationsCustomMessages` — the message of `@NotNull`: `true`, `false`, `FieldName`,
  `ClassName` or a text, where `{ClassName}` and `{FieldName}` are replaced.
- `generateItemAnnotations` — on by default; writes the constraints of the items of a collection on
  its type argument.
- `generateValidOnCollections` — on by default; writes `@Valid` on the type argument of a
  collection, so that its elements are validated in turn.
- `omitJavaTypeBounds` — leaves out the `@DecimalMin` and `@DecimalMax` bounds that are the Java
  type's own limits, as in `@DecimalMin("-2147483648")` on an `int`: every bound the schema states is
  written by default.
- `patternList` — writes one `@Pattern.List` instead of one `@Pattern` per alternative. Both are
  legal and mean the same; one `@Pattern` per alternative is what the plugin writes by default.
- `override` — overrides what the plugin computed for a class or a property: leaves an annotation
  out, sets one of its parameters, or writes another in its place, on the field or on the type
  argument of a collection. Repeatable.
- `verbose` — prints the options in use and every annotation written.

### Override statements

`override` is repeatable and its statements apply in the order given. A statement is

```text
ClassGlob[#PropertyGlob][@AnnotationGlob][:parameter = value][= @Annotation(...)]
```

| part | what it matches |
| --- | --- |
| `ClassGlob` | the qualified name of the generated class: `*` and `?`, everything else literal |
| `#PropertyGlob` | the name of the property; without it the statement covers the whole class |
| `@AnnotationGlob` | the simple name of an annotation the plugin computed; without it, every annotation of the property. Written in brackets, as `[@Size]`, it covers the annotations of the items only, which go on the type argument |
| `:parameter = value` | sets one parameter of the computed annotation |
| `= @Annotation(...)` | writes that annotation in place of the computed one |

What a statement does follows from what it carries: with neither `:` nor `=` the matching annotations
are left out, with `:` they are written with the parameter replaced, and with `=` the annotation named
there is written instead.

In a value, `{...}` stands for something the plugin knows: `{className}`, `{fieldName}`, a parameter
it was about to write for that annotation (`{max}`, `{min}`, `{value}`, `{message}`, …), or the
default the annotation itself declares.

What a statement does, and when one would write it — every row is a case of the fixture, so every row
is also a test:

| statement, after `-XBeanValidationAnnotations:override=` | what it does, and when it earns its place |
| --- | --- |
| `*#code` | leaves every annotation of `code` out: the schema states something this class should not carry |
| `*#label@NotNull` | leaves out the `@NotNull` of `label` only; the other annotations of the property stay |
| `*#amount@Decimal*` | a glob over the annotation names: every `@DecimalMin` and `@DecimalMax` of `amount` goes |
| `*RootType#*` | everything computed for every property of `RootType` |
| `a.ChildType` | everything for one generated class, named in full, with no `#` and no `@` |
| `*#label@Size:max = 5` | keeps the computed `@Size` and gives it another maximum |
| `*#label@Size:message = at most {max} characters` | keeps it and changes the message, `{max}` resolving to the value the plugin was about to write |
| `*#labels[@Size]:max = 3` | the same on the **items** of a list, leaving the cardinality `@Size` of the field alone |
| `*#labels[@Size]` | removes the `@Size` of the items and keeps the one on the field |
| `*#child=@NotNull(message = "{message}")` | writes a replacement instead, `{message}` being the default the annotation itself declares |
| `*#label=@Size(max = {max})` | another replacement, reusing the maximum the plugin computed |

Two things to know before writing one:

- the annotation glob is matched against what the plugin **computed**, so it can only name the
  annotations this plugin writes — `Valid`, `NotNull`, `Size`, `Digits`, `DecimalMin`,
  `DecimalMax`, `Pattern` — and a replacement has to name one of them too;
- an annotation glob without brackets covers that annotation wherever it was computed, on the field
  **and** on the type argument of a collection: `*#labels@Size` takes the cardinality `@Size` of the
  list and the `@Size` of its items together. `*#labels[@Size]` takes the one on the items alone,
  which is how the `@NotNull` of a `List<@NotNull String>` is singled out; there is no marker for
  the field alone yet.

A statement that matched no class, no property or no annotation is reported as a warning, because a
statement that silently does nothing is worse than no statement.

## Building

The build needs JDK 21 and Maven, and nothing else. With both on the path:

```
mvn -B verify
```
