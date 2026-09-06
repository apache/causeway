## MODIFIED Requirements

### Requirement: Vue Petclinic declarative layout parity

The Vue Petclinic application SHALL author the same representative `<cw-row>`, `<cw-column>`, `<cw-fieldset>`, `<cw-tabgroup>`, `<cw-tab>`, and `<cw-metadata>` structure as the HTMX reference where the corresponding exact page is reconciled.
The PetOwner macro layout SHALL use one direct row containing details and collections columns with spans 4 and 8 rather than Vue grid, details, or collections wrapper divs.
The reconciled tab group SHALL contain an initial **Identity** tab followed by a **Metadata** tab and SHALL NOT retain artificial placeholder content.
Vue MUST treat those elements as custom elements and MUST NOT own their responsive, column-spacing, tab, metadata-discovery, collection-section, or action-menu state.

#### Scenario: Vue layout renders

- **WHEN** the reconciled exact page is opened at wide or narrow viewport width
- **THEN** shared foundation elements preserve equivalent 4/8 columns, fieldset, tab, collection, agreement, and metadata relationships to HTMX
- **AND** Vue wrappers do not duplicate visible members or alter semantic ordering

#### Scenario: Vue PetOwner macro composition is inspected

- **WHEN** the Vue PetOwner template or connected direct-child tree is inspected
- **THEN** one macro row directly owns the details and collections columns
- **AND** no Vue-specific object-grid, object-details, or object-collections wrapper div is present

#### Scenario: Vue PetOwner tab composition renders

- **WHEN** the PetOwner route page reaches its presentable state
- **THEN** the current Identity tab contains the owner's identity fields and the following Metadata tab contains `<cw-metadata>`
- **AND** Contact and Details are direct fieldset children of the details column without redundant full-width row and column wrappers

#### Scenario: Vue semantic collection panels render

- **WHEN** Pets and Visits render in the collections column
- **THEN** direct `<cw-collection>` elements supply the same labelled semantic sections, actions, previews, rows, and paging as HTMX
- **AND** no Vue section wrapper duplicates either collection panel

#### Scenario: Vue Agreement panel renders

- **WHEN** Agreement renders in the collections column
- **THEN** a direct named fieldset contains the existing agreement property and PDF reader
- **AND** Vue does not introduce a layout wrapper or PDF behavior

#### Scenario: User operates the Vue-hosted tabs

- **WHEN** pointer or keyboard input changes between Identity and Metadata
- **THEN** foundation updates selection, focus, and the current white panel without Vue component state or route navigation
- **AND** the application-owned route outlet and object context remain stable

#### Scenario: Vue-hosted metadata resolves

- **WHEN** `<cw-metadata>` consumes the current Vue route's object context with associated actions
- **THEN** the same effective metadata properties and heading ellipsis action menu are presented as in HTMX
- **AND** generated member components retain ordinary foundation behavior while Vue owns no menu state
