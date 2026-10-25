# Changelog

## 1.0.1-SNAPSHOT

- Treat absent digit facets as unconstrained on fields and items. Fraction-only restrictions now
  write a constraint, including a zero fraction limit, without inventing an integer-digit limit.
- Resolve inclusive and exclusive numeric bounds to the strongest effective restriction before
  writing annotations, including integer items and attributes. A fixed value replaces both bounds.
- Keep item annotations after the package qualifier when class names collide across namespaces.
  Annotated top-level items use fully qualified names; generated sources compile without relying on
  an unambiguous import.
- Write the annotations of a collection item on the name of the item when its type is a nested class:
  `List<Battery.@Valid BatterySubgroup>` instead of `List<@Valid Battery.BatterySubgroup>`. javac
  refuses the old form for a static nested class, which is what XJC generates for an anonymous complex
  type, and for an inner class the same form marks the enclosing class instead of the item, so a
  provider cascades nowhere and the items are silently unvalidated.

## 1.0.0

- Fix `notNullAnnotationsCustomMessages` given more than once: the value asked for last is the one
  in force. Only the boolean values left the prefix of an earlier `FieldName` or `ClassName` in the
  message, so the option set to `ClassName` and then to `true` still produced the class-name message.
- Report a schema pattern that cannot be translated on a plain field as well, not only on the items
  of a collection: the field path took the patterns without the logger that reports the skip.
- A boolean an element or an attribute pins with `fixed` gets `@AssertTrue` or `@AssertFalse`, so a
  schema that admits one value says so in the generated model. A null stays valid, so an optional
  element needs nothing else.
- The `verbose` option works on its own: the plugin no longer overwrites it with the value of
  XJC's global `-verbose`, which only ever turns it on.
- XML Schema patterns are translated into the Java regular expressions `@Pattern` takes: the
  character-class shorthands, the XML name classes, the Unicode categories and blocks, the
  character-class subtraction and the metacharacters whose meanings differ. A pattern whose
  translation does not compile in Java is left out with a warning rather than written as an invalid
  expression. The matrix is in [docs/pattern-compatibility.md](docs/pattern-compatibility.md).
- Keep field-level `xs:list` length constraints when `generateItemAnnotations=false`; translate exact `xs:length` on repeating items to `@Size(min = n, max = n)` on the item type argument, taking precedence over inherited `minLength`/`maxLength`.
- Fix `patternList=true` with overrides: collected `@Pattern.List` annotations retain their nested patterns during replay instead of throwing an NPE. Overrides on other annotations of the same property still apply.
- Translate `xs:list` item patterns through the XML Schema-to-Java regex compatibility check, omitting unsupported expressions with a warning rather than emitting invalid regexes.

- First release of the standalone plugin. It was carried inside `com.fillumina:krasa-jaxb-tools`
  until 2.8.0, in the same jar and the same service file as the primitives plugin.
- Jakarta only: built for JDK 21 and XJC 4, the Jakarta XML Binding 4 line, and it writes Jakarta
  Bean Validation 3.1 annotations. The `validationAnnotations` option, which chose between `javax`
  and `jakarta`, is gone, and so is the `javax` half of the annotation surface.
- The constraints of the items of a collection are written on its type argument, as in
  `List<@Size(max = 5) String>`. They replace the `@Each*` annotations of the old line, which came
  from an unmaintained javax-only library and were inert with a current provider. They are on by
  default; `generateItemAnnotations` can leave them out. An `xs:list` is the exception: the
  length facets of its list type go on the field, which holds the items, and the number of times
  the element occurs is not written there, because that count is not the number of items.
- `@Valid` on a collection is written on the type argument, `List<@Valid Other>`, instead of on the
  container, where Bean Validation deprecated it (HV000271).
- Repeatable `override` statements remove, change, or replace the Jakarta annotations this plugin
  computes, on a field or a collection item type argument.
- The class, property and annotation globs of an `override` statement match with `*`, `?` and a
  character class: a set `[abc]`, a range `[a-z]`, or a negated set `[!abc]` and `[^abc]`. Every
  other character is literal, and an unclosed or unreadable set is an error on the option.
- A placeholder in an override value is read from the annotations the statement selects:
  `*#labels@Size` reads the field's `@Size`, `*#labels@List.Size` the item's. A statement that
  names no annotation covers both, and is refused when they disagree.
- The option is `-XBeanValidationAnnotations`; the old `-XJsr303Annotations` name is rejected.
