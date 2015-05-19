Title: @Encodable

[//]: # (content copied to _user-guide_xxx)

> This annotation has partial/incomplete support.

> In particular, the Wicket viewer uses Apache Wicket's Converter API instead.

Encodability means the ability to convert an object to-and-from a
string, by way of the `org.apache.isis.applib.adapters.EncoderDecoder`
interface. Generally this only applies to value types, where the `@Value`
annotation implies encodability through the `ValueSemanticsProvider` interface.

For these reasons the `@Encodable` annotation is generally never applied
directly, but can be thought of as a placeholder for future enhancements
whereby non-value types might also be directly encoded.

