## ADDED Requirements

### Requirement: Vue Petclinic declarative layout parity

The Vue Petclinic application SHALL author the same representative `<cw-row>`, `<cw-column>`, `<cw-fieldset>`, `<cw-tabgroup>`, `<cw-tab>`, and `<cw-metadata>` structure as the HTMX reference where the corresponding exact page is reconciled.
Vue MUST treat those elements as custom elements and MUST NOT own their responsive, tab, metadata-discovery, or action-dropdown state.

#### Scenario: Vue layout renders

- **WHEN** the reconciled exact page is opened at wide or narrow viewport width
- **THEN** shared foundation elements preserve equivalent fieldset, column, tab, and metadata relationships to HTMX
- **AND** Vue wrappers do not duplicate visible members or alter semantic ordering

#### Scenario: User operates the Vue-hosted tabs

- **WHEN** pointer or keyboard input changes the current tab
- **THEN** foundation updates selection, focus, and panels without Vue component state or route navigation
- **AND** the application-owned route outlet and object context remain stable

#### Scenario: Vue-hosted metadata resolves

- **WHEN** `<cw-metadata>` consumes the current Vue route's object context
- **THEN** the same effective metadata properties and associated Actions dropdown are presented as in HTMX
- **AND** generated member components retain ordinary foundation behavior
