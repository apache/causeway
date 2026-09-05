## ADDED Requirements

### Requirement: Coordinated metadata-layout consumption

The object context SHALL allow `<cw-metadata>` to consume the current object's schema description and effective-grid structural resource through the established layout requirement and structural-resource policy.
The component MUST NOT create an independent GraphQL client, construct a structural URL, bypass context generation, or weaken same-origin resource controls.

#### Scenario: Metadata component requests effective layout

- **WHEN** a connected `<cw-metadata>` registers beneath the current object context
- **THEN** its layout requirement coalesces with other current semantic requirements
- **AND** the authoritative grid resource reference is selected only when supported by the schema and response

#### Scenario: Metadata component loads the referenced grid

- **WHEN** the current layout state contains an effective grid reference
- **THEN** the component loads it through `loadStructuralResource` with the established XML accept policy, credentials, redirect checks, same-origin checks, and byte bounds
- **AND** raw resource content remains internal to foundation parsing

#### Scenario: Metadata descendants connect

- **WHEN** the effective metadata fieldset yields property and action member identities
- **THEN** generated `<cw-property>` and `<cw-action>` descendants register their ordinary requirements on the same context
- **AND** batching, selection minimization, authorization, partial errors, invocation, and lifecycle isolation remain unchanged

#### Scenario: Context changes during metadata loading

- **WHEN** the parent context generation or identity changes before metadata preparation completes
- **THEN** the pending structural operation is aborted or ignored by generation
- **AND** no stale member component connects under the newer context
