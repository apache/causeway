## MODIFIED Requirements

### Requirement: HTMX Petclinic declarative layout demonstration

The HTMX Petclinic application SHALL demonstrate application-authored `<cw-row>`, `<cw-column>`, `<cw-fieldset>`, `<cw-tabgroup>`, `<cw-tab>`, and `<cw-metadata>` composition beneath its existing object context and interaction controller.
The demonstrated tab group SHALL contain an initial **Identity** tab followed by a **Metadata** tab and SHALL NOT retain the artificial **Layout help** tab or placeholder content.
HTMX templates MUST NOT duplicate foundation span, tab, metadata-discovery, or action-menu behavior.

#### Scenario: Petclinic layout renders wide and narrow

- **WHEN** the demonstrated exact page is opened at documented wide and narrow viewports
- **THEN** fieldsets and collections use authored column spans at wide width and stack in semantic order at narrow width
- **AND** page navigation, member behavior, collection presentation, and document overflow remain correct

#### Scenario: PetOwner tab composition renders

- **WHEN** the PetOwner exact page reaches its presentable state
- **THEN** the current Identity tab contains the owner's identity fields and the following Metadata tab contains `<cw-metadata>`
- **AND** no Layout help tab or artificial placeholder panel is present

#### Scenario: User operates the HTMX tab group

- **WHEN** the user changes between Identity and Metadata by pointer or keyboard
- **THEN** foundation updates the accessible current white panel without an HTMX request or route replacement
- **AND** the application shell and object context remain stable

#### Scenario: HTMX metadata menu resolves

- **WHEN** the demonstrated object's effective metadata fieldset contains associated actions
- **THEN** `<cw-metadata>` shows exactly its authoritative properties and a heading ellipsis menu containing the authorized actions
- **AND** HTMX does not provide hard-coded metadata member ids, action grouping, menu state, or menu positioning
