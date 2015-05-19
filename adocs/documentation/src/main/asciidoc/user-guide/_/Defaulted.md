Title: @Defaulted

[//]: # (content copied to _user-guide_xxx)

> This annotation has partial/incomplete support.

The concept of "defaulted" means being able to provide a default value
for the type by way of the
`org.apache.isis.applib.adapters.DefaultsProvider` interface. Generally
this only applies to value types, where the `@Value` annotation implies encodability through the `ValueSemanticsProvider` interface.

For these reasons the `@Defaulted` annotation is generally never applied
directly, but can be thought of as a placeholder for future enhancements
whereby non-value types might also have a default value provided for
them.
