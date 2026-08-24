## MODIFIED Requirements

### Requirement: Honest bounded autocomplete behavior
The pilot SHALL prefer advertised public GraphQL autocomplete windows and SHALL connect its internal lazy data-provider callback to bounded server response pages.
It MUST retain an explicit maximum search policy for legacy non-paged autocomplete, and local slicing MUST NOT be represented as server paging.

#### Scenario: Window capability is advertised
- **WHEN** an eligible reference editor advertises `autoCompleteWindow`
- **THEN** the internal Vaadin data provider requests bounded offset pages using current filter text and Causeway argument context
- **AND** supplies returned items and authoritative total count to the control without exposing a Vaadin API to the application

#### Scenario: User scrolls beyond the first page
- **WHEN** Vaadin requests a later page for the current filter generation
- **THEN** the adapter obtains that page from the semantic Causeway context
- **AND** does not locally slice a previously downloaded complete result

#### Scenario: Filter changes while pages are in flight
- **WHEN** newer filter text, dependent arguments, route navigation, fragment replacement, prompt closure, or disconnection supersedes page requests
- **THEN** the adapter cancels or ignores stale callbacks and items
- **AND** retains Causeway-owned selection, focus, validation, and pending-value state

#### Scenario: Only bounded legacy autocomplete is available
- **WHEN** the server does not advertise a window field and GraphQL returns a stable choice set within the configured bound
- **THEN** the adapter may use the complete finite result while retaining cancellation and generation checks
- **AND** records that the response is legacy and not server paging

#### Scenario: Legacy autocomplete bound is exceeded
- **WHEN** a legacy server response exceeds the configured maximum or cannot provide stable identities
- **THEN** the editor presents a Causeway-owned limitation state or activates the documented fallback
- **AND** does not truncate the choices while claiming a complete match set
