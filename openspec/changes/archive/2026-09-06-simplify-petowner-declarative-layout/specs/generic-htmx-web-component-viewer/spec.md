## MODIFIED Requirements

### Requirement: HTMX Petclinic declarative layout demonstration

The HTMX Petclinic application SHALL demonstrate application-authored `<cw-row>`, `<cw-column>`, `<cw-fieldset>`, `<cw-tabgroup>`, `<cw-tab>`, and `<cw-metadata>` composition beneath its existing object context and interaction controller.
The PetOwner macro layout SHALL use one direct row containing details and collections columns with spans 4 and 8 rather than application grid, details, or collections wrapper divs.
The demonstrated tab group SHALL contain an initial **Identity** tab followed by a **Metadata** tab and SHALL NOT retain the artificial **Layout help** tab or placeholder content.
HTMX templates MUST NOT duplicate foundation span, column spacing, responsive stacking, tab, metadata-discovery, collection-section, or action-menu behavior.

#### Scenario: Petclinic layout renders wide and narrow

- **WHEN** the demonstrated exact page is opened at documented wide and narrow viewports
- **THEN** the details and collections columns use authored 4/8 spans at wide width and stack in semantic order at the shared narrow breakpoint
- **AND** page navigation, member behavior, collection presentation, and document overflow remain correct

#### Scenario: PetOwner macro composition is inspected

- **WHEN** the HTMX PetOwner page source or connected direct-child tree is inspected
- **THEN** one macro row directly owns the details and collections columns
- **AND** no application-specific object-grid, object-details, or object-collections wrapper div is present

#### Scenario: PetOwner tab composition renders

- **WHEN** the PetOwner exact page reaches its presentable state
- **THEN** the current Identity tab contains the owner's identity fields and the following Metadata tab contains `<cw-metadata>`
- **AND** Contact and Details are direct fieldset children of the details column without redundant full-width row and column wrappers

#### Scenario: Semantic collection panels render

- **WHEN** the Pets and Visits collections render in the collections column
- **THEN** each direct `<cw-collection>` supplies its own labelled semantic section, heading, description, actions, preview, rows, and paging
- **AND** no outer application section duplicates that collection panel

#### Scenario: Agreement panel renders

- **WHEN** the Agreement panel renders in the collections column
- **THEN** a direct named fieldset contains the existing agreement property and PDF reader
- **AND** no standalone property violates the column grammar

#### Scenario: User operates the HTMX tab group

- **WHEN** the user changes between Identity and Metadata by pointer or keyboard
- **THEN** foundation updates the accessible current white panel without an HTMX request or route replacement
- **AND** the application shell and object context remain stable

#### Scenario: HTMX metadata menu resolves

- **WHEN** the demonstrated object's effective metadata fieldset contains associated actions
- **THEN** `<cw-metadata>` shows exactly its authoritative properties and a heading ellipsis menu containing the authorized actions
- **AND** HTMX does not provide hard-coded metadata member ids, action grouping, menu state, or menu positioning
