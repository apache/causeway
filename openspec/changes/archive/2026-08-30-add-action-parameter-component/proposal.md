## Why

Custom HTML pages can override action-level presentation but cannot currently refine individual action-parameter labels, descriptions, description presentation, or multiline editing.
A declarative `<cw-parameter>` child gives applications selective presentation control while retaining GraphQL-authoritative parameter semantics and allowing unspecified parameters to use canonical metadata.

## What Changes

- Add a public framework-neutral `<cw-parameter id="parameterName">` custom element that may be nested directly beneath `<cw-action>`.
- Treat nested parameter elements as optional presentation hints matched by semantic parameter id; actions do not require every parameter to be declared.
- Support `named`, `described-as`, `description-as="label|tooltip"`, and bounded `multi-line` attributes with behavior consistent with property presentation overrides.
- Apply matching hints when the standard interaction controller renders and edits action parameters without changing hidden, disabled, default, choice, autocomplete, validation, ordering, or invocation semantics.
- Preserve canonical metadata and editor behavior for undeclared parameters and for attributes not explicitly overridden.
- Extend selected Petclinic parameterized actions with representative, partial `<cw-parameter>` declarations and regression coverage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Add the semantic action-parameter component vocabulary and selective parameter presentation override contract.
- `generic-htmx-web-component-viewer`: Extend Petclinic custom-page and browser qualification with representative partial action-parameter declarations.

## Impact

- Foundation custom-element contracts, registration, exports, action rendering, action-request presentation payload, and interaction prompt rendering.
- Shared presentation normalization for names, descriptions, tooltip presentation, and multiline editor hints.
- Foundation structural styles, documentation, and component/interaction tests.
- Selected Petclinic HTML pages and integration or browser regression coverage.
- No GraphQL schema, operation placement, parameter identity, parameter ordering, validation, or invocation API changes.
