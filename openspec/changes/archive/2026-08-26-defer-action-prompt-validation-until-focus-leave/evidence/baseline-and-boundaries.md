# Baseline and authority boundaries

## Reproduced defect

The screenshot shows mandatory reasons for Name and Species immediately after the Add Pet prompt opens, before either field has been completed.
With the pre-change controller restored against the new focused tests, 17 interaction tests ran, 14 passed, and 3 failed.
The failures demonstrated eager prepared-validity presentation, immediate malformed-value feedback, and protected-parameter preparation while input focus remained active.

## Accepted interaction boundary

Initial `prepareAction` remains necessary and GraphQL-authoritative for parameter order, type, hidden state, disabled state, defaults, choices, autocomplete capabilities, and dependency metadata.
The viewer retains that prepared state but withholds validity and parameter-error presentation until the corresponding field loses focus or Invoke is activated.
Input and change events retain codec-produced pending values locally without requesting parameter recomputation.
Focus departure commits the field through existing preparation and stale-generation protection.
Invoke refreshes authoritative parameter state and validates the complete latest pending argument set, including an unblurred current value.
Autocomplete search remains independently debounced because it is suggestion retrieval rather than validity presentation.

## Preserved security and architecture boundaries

- GraphQL remains authoritative for validation, dependencies, invocation, visibility, usability, choices, and results.
- Protected pending values remain excluded from markup, semantic prompt events, errors, diagnostics, and evidence.
- No GraphQL schema, operation shape, codec, route, CSP, toolkit policy, or public Causeway element changes.
- Property-editor validation timing remains unchanged.
- Focusout recomputation must preserve the browser-selected next prompt control across rerendering.
