# xjc-bean-validation-plugin

Generate standard Jakarta Bean Validation 3.1 annotations from an XML Schema while XJC generates its Java model. Jakarta Bean Validation is the Jakarta successor to Bean Validation 2.0 (JSR 380).
The plugin reads the restrictions already expressed by the schema and puts the matching constraints on the generated fields and, where needed, on collection type arguments. It keeps the XML Schema as the source of truth instead of asking an application to repeat those rules in hand-written Java classes.

```java
// xs:element name="name" type="a:ShortText" maxOccurs="unbounded",
// with ShortText restricted to xs:maxLength value="5"
protected List<@Size(max = 5) String> name;
```

This is the Jakarta-only successor to the Bean Validation part of
[`com.fillumina:krasa-jaxb-tools`](https://github.com/fillumina/krasa-jaxb-tools).

An example of it inside a real build, with the test of that wiring, is
[`xjc-bean-validation-plugin-example`](https://github.com/fillumina/xjc-bean-validation-plugin-example).
The three plugins of this line together in one build, which is where the split is shown to do what
the single plugin did, are in
[`xjc-plugins-example`](https://github.com/fillumina/xjc-plugins-example).

## Contents

- [Version and status](#version-and-status)
- [Compatibility](#compatibility)
- [Install and use](#install-and-use)
- [What the plugin writes](#what-the-plugin-writes)
- [Options](#options)
- [Override statements](#override-statements)
- [Limits and diagnostics](#limits-and-diagnostics)
- [Building](#building)
- [Changelog, license, and contributing](#changelog-license-and-contributing)

## Version and status

The current development version is **1.0.0-SNAPSHOT**.

## Compatibility

- **JDK:** 21 or newer.
- **XJC:** XJC 4, from Jakarta XML Binding 4.
- **Validation API:** Jakarta Bean Validation 3.1. Generated classes need that API at runtime and the
  plugin declares it as a dependency.
- **Namespace:** Jakarta only. There is no `javax` variant and no `validationAnnotations` switch.

## Install and use

From a checkout, install the snapshot into the local Maven repository:

```sh
mvn -B install
```

### XJC command line

Put the plugin and its dependencies on the class path of the XJC process, then activate it with the plain option:

```sh
xjc -extension -XBeanValidationAnnotations schema.xsd
```

The plain option is required. XJC activates a plugin only when it sees the option name on its own; an argument such as `-XBeanValidationAnnotations:override=...` configures a plugin but does not activate it.

### Maven

With `org.jvnet.jaxb:jaxb-maven-plugin`, declare this artifact as an XJC plugin and put the activation and configuration arguments under `args`:

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
      <arg>-XBeanValidationAnnotations</arg>
      <arg>-XBeanValidationAnnotations:omitJavaTypeBounds=true</arg>
      <arg>-XBeanValidationAnnotations:override=*#code@Pattern:message = "not a code"</arg>
    </args>
    <plugins>
      <plugin>
        <groupId>com.fillumina</groupId>
        <artifactId>xjc-bean-validation-plugin</artifactId>
        <version>1.0.0-SNAPSHOT</version>
      </plugin>
    </plugins>
  </configuration>
</plugin>
```

In XML, quotation marks inside an override are ordinary text. In a shell, quote the whole override argument so that `*` is not expanded by the shell.

## What the plugin writes

The defaults write every supported restriction the schema states. Options narrow that output or change how it is represented; they do not enable a separate family of constraints. Every annotation below is a standard Jakarta Bean Validation 3.1 constraint. That specification succeeds JSR 380, but generated source imports `jakarta.validation`, not JSR 380's `javax.validation` API.

| Schema meaning                                                       | Generated Jakarta Validation constraint     |
| -------------------------------------------------------------------- | ------------------------------------------- |
| required, non-nillable element or required attribute                 | `@NotNull`                                  |
| `minLength`, `maxLength`, `length`, or collection cardinality        | `@Size`                                     |
| `totalDigits` and `fractionDigits`                                   | `@Digits`                                   |
| inclusive or exclusive numeric bounds                                | `@DecimalMin` and `@DecimalMax`             |
| supported string pattern and enumeration facets                      | `@Pattern` or `@Pattern.List`               |
| a boolean element or attribute fixed to `true`, `false`, `1`, or `0` | `@AssertTrue` or `@AssertFalse`             |
| complex element or complex collection item                           | `@Valid` on the field or item type argument |

A collection has two distinct places for constraints:

```java
@Size(min = 1)                         // the collection cardinality
protected List<@Size(max = 5) String> name; // each item

protected List<@Valid Address> address; // cascade into each complex item
```

The item annotations are type-use annotations, which is the form a current Bean Validation provider enforces. `@Valid` is put on a collection's type argument only when its items are complex types; there is nothing to cascade into for strings or enumerations. An `xs:list` is different: its length facets describe the field itself, while its item facets describe the type argument, and the number of times the element occurs is not written there, because that count is not the number of items the field holds.

### Example: from schema to generated Java

Given this schema fragment:

```xml
<xs:complexType name="Order">
  <xs:sequence>
    <xs:element name="name" type="t:ShortText" minOccurs="0" maxOccurs="unbounded"/>
    <xs:element name="code" type="xs:string"/>
    <xs:element name="tag" type="t:Tag" minOccurs="0" maxOccurs="unbounded"/>
    <xs:element name="note" type="t:Note" minOccurs="0"/>
  </xs:sequence>
  <xs:attribute name="id" type="xs:string" use="required"/>
</xs:complexType>

<xs:simpleType name="ShortText">
  <xs:restriction base="xs:string">
    <xs:maxLength value="5"/>
  </xs:restriction>
</xs:simpleType>
```

XJC generates the fields and this plugin adds the validation annotations:

```java
protected List<@Size(max = 5) String> name;
@NotNull
protected String code;
protected List<@Valid Tag> tag;
@Valid
protected Note note;
@NotNull
protected String id;
```

The first `@Size` applies to each item, not to the `List`. `code` and `id` are required by the schema. `tag` and `note` are complex generated types, so `@Valid` makes a validator continue into them.

### Validate generated objects

The annotations describe constraints; a Bean Validation provider enforces them. Hibernate Validator is one such provider and is used by this project's end-to-end test. Add it to the application that contains the generated classes:

```xml
<dependency>
  <groupId>org.hibernate.validator</groupId>
  <artifactId>hibernate-validator</artifactId>
  <version>9.1.4.Final</version>
</dependency>
```

Follow Hibernate Validator's dependency guidance for an expression-language implementation if the application uses its default message interpolation. Then validate a generated object through the Jakarta Validation API:

```java
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
Set<ConstraintViolation<Order>> violations = validator.validate(order);

for (ConstraintViolation<Order> violation : violations) {
    System.out.println(violation.getPropertyPath() + ": " + violation.getMessage());
}
```

For the schema above, a list item longer than five characters is reported at a path such as `name[1]`; a missing `code` or `id` is reported as a `@NotNull` violation.

## Options

Every option follows the plugin name, for example:

```text
-XBeanValidationAnnotations:generateNotNullAnnotations=false
```

On by default: `generateNotNullAnnotations`, `generateItemAnnotations`, and
`generateValidOnCollections`. Off by default: `omitJavaTypeBounds`, `patternList`, and `verbose`.

| Option                             | Default      | Effect                                                                                                                                                             |
| ---------------------------------- | ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `targetNamespace`                  | unrestricted | Adds `@Valid` only to complex elements whose schema namespace begins with the supplied text. An empty value or the literal `null` leaves every namespace eligible. |
| `generateNotNullAnnotations`       | `true`       | Writes `@NotNull` for required elements and attributes.                                                                                                            |
| `notNullAnnotationsCustomMessages` | `false`      | Sets the `@NotNull` message. Use `true`, `false`, `FieldName`, `ClassName`, or literal text containing `{ClassName}` and `{FieldName}`.                            |
| `generateItemAnnotations`          | `true`       | Writes the constraints of a collection's items on its type argument.                                                                                               |
| `generateValidOnCollections`       | `true`       | Writes `@Valid` on a collection's complex-item type argument. Turning it off stops cascading through those collections.                                            |
| `omitJavaTypeBounds`               | `false`      | Leaves out a numeric bound that is already the natural limit of the Java type, such as `Integer.MIN_VALUE`.                                                        |
| `patternList`                      | `false`      | Writes one `@Pattern.List` instead of repeatable `@Pattern` annotations for inherited pattern groups. Both forms are valid.                                        |
| `override`                         | none         | Removes, changes, or replaces a computed annotation. Repeat the option to supply multiple statements. See [Override statements](#override-statements).             |
| `verbose`                          | `false`      | Prints the resolved options and every annotation the plugin writes. XJC's global `-verbose` also enables this output.                                              |

## Override statements

Use `override` when the schema is nearly, but not exactly, the validation contract wanted by the Java model. It changes only annotations this plugin would otherwise compute; it cannot add an arbitrary annotation or select an annotation that was never produced.

Pass one statement per option. Statements are processed in the order given:

```text
-XBeanValidationAnnotations:override=ClassGlob[#PropertyGlob][@AnnotationGlob][:parameter = value][= @Annotation(...)]
```

### Select what to change

| Part                   | Meaning                                                                                                                                                                          |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ClassGlob`            | Qualified generated class name. `*` matches any sequence, `?` one character and `[...]` one character of a set, a range or a negated set; all other characters are literal. |
| `#PropertyGlob`        | Optional generated property name. Without it, the statement applies to every property of the matching class.                                                                     |
| `@AnnotationGlob`      | Optional simple annotation name, with the same glob rules. It selects a matching annotation on the field; for example, `@Size` selects a collection-cardinality `@Size`. Without it, every computed annotation of the selected property is covered. |
| `@List.AnnotationGlob` | Selects only annotations of collection items, which are written on the type argument. `@List.Size` matches the `@Size` in `List<@Size(max = 5) String>`. |

The three globs share one syntax: `*` matches any sequence, `?` matches one character, and `[...]` matches one character out of a set, a range, or a negated set.

```text
*#cod[e]         matches code
*#childr[a-z]n   matches children
*#item[!0-9]     matches itemx but not item7
*#item[^0-9]     the same, with the caret a regular expression uses
```

Inside the brackets `*`, `?` and `\` are literal, and `-` is a range unless it comes first or last. The first `]` closes the set, so a `]` cannot be part of one. An unclosed `[`, an empty `[]` and a set a regular expression cannot read are reported as errors. Only the characters a generated name is made of are written as they are, so the `.` of a qualified name is not the "any character" of a regular expression.

The annotation name must be one the plugin manages: `Valid`, `NotNull`, `Size`, `Digits`, `DecimalMin`, `DecimalMax`, `Pattern`, `AssertTrue`, or `AssertFalse`.

An annotation selector without `List.` matches the field only. `*#labels@Size` selects the collection-cardinality `@Size` on `labels`; `*#labels@List.Size` selects the `@Size` of its items. To change or remove both, give both override statements.

### Choose the operation

| Statement                                       | Result                                                                                                  |
| ----------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `*#code`                                        | Removes every computed annotation from `code`.                                                          |
| `*#label@NotNull`                               | Removes only `@NotNull` from `label`.                                                                   |
| `*#amount@Decimal*`                             | Removes both `@DecimalMin` and `@DecimalMax` from `amount`.                                             |
| `*RootType#*`                                   | Removes every computed annotation from every property of a matching `RootType`.                         |
| `a.ChildType`                                   | Removes every computed annotation from every property of that qualified class.                          |
| `*#label@Size:max = 5`                          | Keeps the computed `@Size` and changes its `max` parameter.                                             |
| `*#labels@List.Size:max = 3`                    | Changes the item `@Size` only, leaving the field's cardinality `@Size` alone.                           |
| `*#labels@List.Size`                            | Removes the item `@Size` only.                                                                          |
| `*#child=@NotNull(message = "{message}")`       | Replaces every computed annotation on `child` with `@NotNull`, using that annotation's default message. |
| `*#amount@Decimal*=@DecimalMax(value = "99.5")` | Replaces matching decimal bounds with one `@DecimalMax`.                                                |

A statement with no `:` or `=` removes the annotations it selects. A statement with `:parameter = value` changes that parameter on the selected computed annotation. A statement with `=@Annotation(...)` replaces the selected computed annotation with the named managed annotation.

### Reuse computed values

An override value may use these placeholders:

- `{className}` and `{fieldName}` for the generated declaration;
- a parameter the plugin computed, such as `{min}`, `{max}`, `{value}`, or `{message}`; or
- the default value declared by the replacement annotation.

#### Preserve a computed value in a replacement

**Before**

```java
@Size(min = 2, max = 20)
protected String label;
```

**Option**

```text
-XBeanValidationAnnotations:override=*#label=@Size(max = {max})
```

This states: replace `@Size` on `label`, taking the computed maximum from the annotation it replaces.

**After**

```java
@Size(max = 20)
protected String label;
```

#### Build a message from the declaration and a computed value

**Before**

```java
@Size(min = 2, max = 20)
protected String label;
```

**Option**

```text
-XBeanValidationAnnotations:override=*#label@Size:message = {className}.{fieldName} has at most {max} characters
```

This states: keep `@Size`, and build its message from the generated class name, property name, and computed maximum.

**After**

```java
@Size(min = 2, max = 20, message = "a.RootType.label has at most 20 characters")
protected String label;
```

#### Reuse an annotation default

**Before**

```java
@NotNull
@Valid
protected ChildType child;
```

**Option**

```text
-XBeanValidationAnnotations:override=*#child=@NotNull(message = "{message}")
```

This states: replace the annotations on `child` with `@NotNull`, using the default message declared by `@NotNull`.

**After**

```java
@NotNull(message = "{jakarta.validation.constraints.NotNull.message}")
protected ChildType child;
```

If one placeholder name would resolve to different values from multiple selected annotations, the plugin rejects the statement and asks for a more specific annotation selector. A statement that matches no class, property, or named annotation is reported as a warning so a misspelled selector does not silently do nothing.

### Worked selector examples

Each example starts from the same generated field. The first `@Size` constrains the number of labels; the second constrains each label's length.

**Before**

```java
@Size(min = 1, max = 5)
protected List<@Size(min = 2, max = 20) String> labels;
```

#### Remove the collection-cardinality constraint

**Option**

```text
-XBeanValidationAnnotations:override=*#labels@Size
```

This states: for every generated class, select the `@Size` written on the `labels` field and remove it. It does not select the item annotation.

**After**

```java
protected List<@Size(min = 2, max = 20) String> labels;
```

#### Remove the item-length constraint

**Option**

```text
-XBeanValidationAnnotations:override=*#labels@List.Size
```

This states: for every generated class, select the `@Size` inside the `labels` list's type argument and remove it. It keeps the collection-cardinality annotation.

**After**

```java
@Size(min = 1, max = 5)
protected List<String> labels;
```

#### Change the collection maximum

**Option**

```text
-XBeanValidationAnnotations:override=*#labels@Size:max = 10
```

This states: keep the `@Size` on the field, but replace its `max` parameter with `10`. The item maximum remains `20`.

**After**

```java
@Size(min = 1, max = 10)
protected List<@Size(min = 2, max = 20) String> labels;
```

#### Change the item maximum

**Option**

```text
-XBeanValidationAnnotations:override=*#labels@List.Size:max = 3
```

This states: keep the item `@Size`, but replace its `max` parameter with `3`. The collection cardinality remains unchanged.

**After**

```java
@Size(min = 1, max = 5)
protected List<@Size(max = 3) String> labels;
```

#### Replace the item constraint

**Option**

```text
-XBeanValidationAnnotations:override=*#labels@List.Size=@NotNull
```

This states: replace the item `@Size` with `@NotNull`. The field `@Size` remains unchanged.

**After**

```java
@Size(min = 1, max = 5)
protected List<@NotNull String> labels;
```

#### Remove both constraints with multiple overrides

**Options**

```text
-XBeanValidationAnnotations:override=*#labels@Size
-XBeanValidationAnnotations:override=*#labels@List.Size
```

Both options target the same field. The first removes the collection-cardinality `@Size`; the second removes the item `@Size`.

**After**

```java
protected List<String> labels;
```

#### Change several properties with one statement

**Before**

```java
@Size(min = 2, max = 20)
protected String line1;
@Size(min = 2, max = 20)
protected String line2;
@Size(min = 2, max = 20)
protected String line3;
@Size(min = 2, max = 20)
protected String note;
```

**Option**

```text
-XBeanValidationAnnotations:override=*#line[1-3]@Size:max = 10
```

This states: for every generated class, select the `@Size` of the properties whose name is `line` followed by one character of the range from `1` to `3`, and change its `max` to `10`. `note` is not selected, so one statement does what three would otherwise do.

**After**

```java
@Size(min = 2, max = 10)
protected String line1;
@Size(min = 2, max = 10)
protected String line2;
@Size(min = 2, max = 10)
protected String line3;
@Size(min = 2, max = 20)
protected String note;
```

## Limits and diagnostics

- XML Schema restrictions are more expressive than standard Jakarta Bean Validation 3.1 annotations. The plugin projects only rules those annotations can express, with the contract that generated Bean Validation is never more restrictive than the XSD it represents. It does not replace schema validation or preserve every XML Schema rule.
- Pattern translation aims to preserve the [W3C XML Schema regular-expression semantics](https://www.w3.org/TR/xmlschema-2/#regexs), rather than Java's default shorthand meanings. The current conversion covers the common XML Schema shorthands, XML name classes, Unicode categories and blocks, character-class subtraction, and the metacharacters whose meanings differ. See the [pattern compatibility reference](docs/pattern-compatibility.md) for the translation matrix and known boundaries.
- This coverage is broad but not a formal proof of equivalence. Unusual legal expressions, Unicode-version changes, or differences between XML Schema validator implementations can still expose an edge case. Please report one with a minimal schema and values it should accept and reject; the plugin can then add it to its compatibility matrix.
- The plugin reports unmatched `override` statements and schema patterns it cannot translate to Java as warnings. Add `:verbose` or XJC's `-verbose` to see the resolved options and every annotation written.
- XJC generates properties as protected fields. The plugin annotates those generated properties rather than enumeration constants or JAXB implementation details.
- An element that repeats and whose type is an `xs:list` is generated as a list of `JAXBElement`, one list of items each, because JAXB cannot map a list of lists. Its cardinality is written on the outer list, where it counts the occurrences, and the items inside the `JAXBElement` carry no constraint: a provider refuses a constraint written there, because `JAXBElement` is not a container and has no value extractor.

## Building

The build needs JDK 21 and Maven:

```sh
mvn -B verify
```

The test suite runs XJC against schema fixtures, compares the generated annotations with expectations, and includes an end-to-end Hibernate Validator check. To add or change behavior, record the affected fixture with `-Dxjc.validation.record=true`, review the generated expectation diff, and then run the normal verification command.

## Changelog, license, and contributing

- [CHANGELOG.md](CHANGELOG.md) records the changes in the current development version.
- This project is licensed under the [Apache License 2.0](LICENSE.txt).
- Contributions should include a focused schema fixture or regression test for behavior changes, clear documentation for public options, and a passing `mvn -B verify` run.
