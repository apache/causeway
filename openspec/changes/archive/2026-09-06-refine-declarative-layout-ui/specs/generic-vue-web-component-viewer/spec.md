## MODIFIED Requirements

### Requirement: Vue Petclinic declarative layout parity

The Vue Petclinic application SHALL author the same representative `<cw-row>`, `<cw-column>`, `<cw-fieldset>`, `<cw-tabgroup>`, `<cw-tab>`, and `<cw-metadata>` structure as the HTMX reference where the corresponding exact page is reconciled.
The reconciled tab group SHALL contain an initial **Identity** tab followed by a **Metadata** tab and SHALL NOT retain the artificial **Layout help** tab or placeholder content.
Vue MUST treat those elements as custom elements and MUST NOT own their responsive, tab, metadata-discovery, or action-menu state.

#### Scenario: Vue layout renders

- **WHEN** the reconciled exact page is opened at wide or narrow viewport width
- **THEN** shared foundation elements preserve equivalent fieldset, column, tab, and metadata relationships to HTMX
- **AND** Vue wrappers do not duplicate visible members or alter semantic ordering

#### Scenario: Vue PetOwner tab composition renders

- **WHEN** the PetOwner route page reaches its presentable state
- **THEN** the current Identity tab contains the owner's identity fields and the following Metadata tab contains `<cw-metadata>`
- **AND** no Layout help tab or artificial placeholder panel is present

#### Scenario: User operates the Vue-hosted tabs

- **WHEN** pointer or keyboard input changes between Identity and Metadata
- **THEN** foundation updates selection, focus, and the current white panel without Vue component state or route navigation
- **AND** the application-owned route outlet and object context remain stable

#### Scenario: Vue-hosted metadata resolves

- **WHEN** `<cw-metadata>` consumes the current Vue route's object context with associated actions
- **THEN** the same effective metadata properties and heading ellipsis action menu are presented as in HTMX
- **AND** generated member components retain ordinary foundation behavior while Vue owns no menu state
