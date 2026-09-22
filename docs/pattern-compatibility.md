# XSD and Java pattern compatibility

This reference describes how the plugin turns XML Schema regular expressions into the Java regular expressions used by Jakarta Validation's `@Pattern`.

The target is the [W3C XML Schema regular-expression language](https://www.w3.org/TR/xmlschema-2/#regexs), not Java's default shorthand behavior. Java behavior is described by the [Java `Pattern` reference](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html). A generated constraint must never reject a value that the XSD pattern accepts. If a translated expression does not compile in Java, the plugin omits that pattern constraint rather than emit an invalid or stricter one.

## Scope

XML Schema patterns and Java regular expressions have related syntax but different character classes and a few different metacharacters. The plugin parses and rewrites the differences below before it writes `@Pattern`.

The XSD pattern facet and the generated Java constraint both apply to the whole value. No extra `^` or `$` anchoring is added.

## Translation matrix

| XSD construct | XSD meaning | Java default or syntax | Generated Java expression |
| --- | --- | --- | --- |
| literals, grouping, alternation, `?`, `*`, `+`, `{m,n}`, ranges, and negated character classes | Same regular-language operations | Compatible | Unchanged |
| `\d` / `\D` | Unicode decimal digits, or their complement | Java defaults to ASCII digits | `\p{Nd}` / `\P{Nd}` |
| `\w` / `\W` | Every character except Unicode punctuation, separators, and other characters, or its complement | Java's `\w` is ASCII letters, digits, and `_` unless a Unicode flag is enabled | `[^\p{P}\p{Z}\p{C}]` / `[\p{P}\p{Z}\p{C}]` |
| `\s` / `\S` | Space, tab, line feed, and carriage return, or its complement | Java also treats other control characters as whitespace | `[ \t\n\r]` / `[^ \t\n\r]` |
| `.` | Every character except line feed and carriage return | Java's dot also excludes its line terminators | `[^\n\r]` |
| `\i` / `\I` | XML name-start characters, or their complement | Not valid Java escapes | XML 1.0 Fifth Edition name-start class, or its complement |
| `\c` / `\C` | XML name characters, or their complement | Not valid Java escapes | XML 1.0 Fifth Edition name-character class, or its complement |
| `\p{category}` / `\P{category}` | Unicode general category or its complement | The XML Schema 1.0 category names are accepted by Java | Unchanged |
| `\p{IsBlock}` / `\P{IsBlock}` | Unicode block or its complement | Java spells block properties `InBlock` | `\p{InBlock}` / `\P{InBlock}`; `PrivateUse` becomes `PrivateUseArea` |
| `[base-[excluded]]` | XSD character-class subtraction | Java interprets this spelling differently | `[base&&[^excluded]]` |
| `^` and `$` outside a character class | Literal caret and dollar | Java treats them as anchors | `\^` and `\$` |
| escaped metacharacters, such as `\.` or `\+` | Literal regular-expression punctuation | Compatible | Unchanged |

## Examples

| XSD pattern | Java pattern written by the plugin | Consequence |
| --- | --- | --- |
| `\d+` | `\p{Nd}+` | `١٢` is accepted, as it is by XSD; Java's default `\d+` would reject it. |
| `\w+` | `[^\p{P}\p{Z}\p{C}]+` | `é` and `€` are accepted; `_` is rejected. |
| `\i\c*` | XML name-start class followed by XML name-character class | `éclair` and `名123` are accepted; `7name` is rejected. |
| `[\i-[:]][\c-[:]]*` | Java character classes with intersections | The XSD `NCName` form excludes `:`. |
| `[a-z-[aeiou]]+` | `[a-z&&[^aeiou]]+` | Lower-case consonants are accepted; vowels are rejected. |
| `\p{IsLatin-1Supplement}+` | `\p{InLatin-1Supplement}+` | `é` is accepted; `A` is rejected. |
| `^abc$` | `\^abc\$` | The literal string `^abc$` is accepted; `abc` is rejected. |

## Checked coverage

The compatibility suite does three things:

1. It compares representative accepted and rejected values against the JDK XML Schema validator and the translated Java pattern.
2. It compiles every XML Schema 1.0 Unicode general category and every block listed by that specification, including complements, in both engines.
3. It runs XJC, compiles the generated sources, and validates generated objects with Hibernate Validator.

This covers common grammar, shorthand classes, complements, XML name classes, Unicode categories and blocks, character-class subtraction, anchors, and generated runtime enforcement.

## Boundaries and reporting edge cases

This is broad characterization coverage, not a formal proof over every legal expression or every XML Schema processor. Unicode data versions and XML Schema implementations can differ. For example, the JDK `SchemaFactory` currently rejects U+2028 for XSD `a.b`, while the W3C definition of `.` includes that character and the generated Java expression accepts it. The plugin follows the W3C definition, leaving Java more permissive in that processor-specific case.

If a schema pattern and generated Java constraint disagree, please report a minimal schema together with values it should accept and reject. That case can be added to the compatibility matrix and used to refine the translator.
