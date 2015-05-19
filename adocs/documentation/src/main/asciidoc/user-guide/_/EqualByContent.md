Title: @EqualByContent

[//]: # (content copied to _user-guide_xxx)

> This annotation has partial/incomplete support.

Equal-by-content is a characteristic of value types, and is implied by
any class annotated with the the `@Value` annotation (see ? and also ?).

The `@EqualByContent` annotation exists so that this semantic could, if
required, by applied directly to an object without it necessarily also
being annotated as a value.

That said, this annotation is only really for completeness. Moreover,
the semantic captured by this annotation is currently used by the
framework (not in any object store nor viewer).
