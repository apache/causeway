## Why

Property presentation currently relies on a limited mix of GraphQL metadata and the legacy `label`/`multiline` HTML attributes, so authored pages cannot consistently apply Causeway's property name, description, multiline, and label-position semantics.
The semantic property component should honour these facets while allowing explicit HTML composition to override them.

## What Changes

- Render property descriptions from `@PropertyLayout(describedAs)` as smaller explanatory text below the property label.
- Honour `@PropertyLayout(labelPosition)` values `LEFT`, `TOP`, and `NONE`, with consistent field-set alignment and accessible description behavior.
- Add authored `<cw-property>` overrides through `named`, `described-as`, `multi-line`, and `label-position` attributes.
- Define authored override precedence over descriptor or effective-grid metadata and bounded handling for invalid multiline and label-position values.
- Extend unit and browser acceptance coverage for presentation facets and overrides.
- Extend the Petclinic sample with selective annotation-driven and HTML-authored examples, including a `TOP` example, without overriding every property.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Extend semantic property presentation with descriptor-driven descriptions and label positioning plus explicit HTML overrides for name, description, multiline rows, and label position.

## Impact

The change affects the foundation `<cw-property>` implementation and styles, its public attribute contract, metadata selection or introspection where required, generated property markup, foundation tests, and the Petclinic domain, HTML pages, styling, and browser acceptance tests.
No third-party dependency or breaking removal is expected; the existing `label` and `multiline` behavior may remain as compatibility aliases where tests or generated markup still depend on it.
