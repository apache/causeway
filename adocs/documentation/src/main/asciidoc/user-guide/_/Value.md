Title: @Value

[//]: # (content copied to _user-guide_xxx)

> This annotation has partial/incomplete support.

The `@Value` annotation indicates that a class should be treated as a
value type rather than as a reference (or entity) type. It does this
providing an implementation of a
`org.apache.isis.applib.adapters.ValueSemanticsProvider`.

For example:

    @Value(semanticsProviderClass=ComplexNumberValueSemanticsProvider.class)
    public class ComplexNumber {
        ...
    }

The `ValueSemanticsProvider` allows the framework to interact with the
value, parsing strings and displaying as text, and encoding/decoding
(for serialization).

