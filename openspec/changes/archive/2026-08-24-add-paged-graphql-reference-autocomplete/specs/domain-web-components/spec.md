## ADDED Requirements

### Requirement: Semantic autocomplete window consumption
Semantic property and action-parameter editors SHALL prefer an advertised rich GraphQL autocomplete window while preserving Causeway-owned pending values, validation, semantic events, cancellation, and toolkit neutrality.
They MUST retain a bounded honest fallback when the server exposes only legacy autocomplete.

#### Scenario: Windowed property search begins
- **WHEN** an editable reference property advertises `autoCompleteWindow` and the search reaches its minimum length
- **THEN** the property context requests offset zero with a bounded page size
- **AND** publishes the returned items and continuation metadata only for the current filter generation

#### Scenario: Windowed action parameter depends on earlier values
- **WHEN** an object or service action parameter requests a page using current preceding arguments
- **THEN** the semantic context sends only arguments declared by the advertised window field
- **AND** changing an earlier value invalidates outstanding pages and restarts the affected search at offset zero

#### Scenario: Later page is requested
- **WHEN** the active semantic editor requests an offset not yet loaded for the current filter
- **THEN** the context obtains that authoritative server window without downloading or locally slicing the complete result
- **AND** semantic identities deduplicate items at their requested positions

#### Scenario: Window request becomes obsolete
- **WHEN** filter text, dependent arguments, prompt state, route generation, connection, or component lifetime supersedes an outstanding page
- **THEN** the request is aborted or ignored
- **AND** stale items, totals, validation, selection, focus, and errors cannot replace current state

#### Scenario: Native editor receives a partial first window
- **WHEN** native fallback presentation has items with `hasNext` true
- **THEN** it exposes the bounded current choices and an accessible additional-results or refine-search indication
- **AND** does not claim the first window is the complete result

#### Scenario: Server has only legacy autocomplete
- **WHEN** targeted introspection finds `autoComplete` but no `autoCompleteWindow`
- **THEN** the context uses the existing single-response operation within its configured bound
- **AND** an over-bound response retains the established visible limitation state without silent truncation
