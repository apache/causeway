## ADDED Requirements

### Requirement: Petclinic action prompt-style demonstration
The Petclinic HTMX sample SHALL demonstrate `INLINE`, `DIALOG_MODAL`, and `DIALOG_SIDEBAR` parameter prompts through ordinary `<cw-action prompt-style>` declarations and standard semantic interactions.
The demonstration MUST NOT require application-specific prompt rendering, event handling, or mutation code.

#### Scenario: Property-associated inline action opens
- **WHEN** the authored owner-page action associated with the name property declares `prompt-style="INLINE"` and is activated
- **THEN** its standard parameter prompt temporarily replaces the name property composition
- **AND** cancellation restores the property value and associated action control with focus returned

#### Scenario: Modal action opens
- **WHEN** a representative authored Petclinic action declares `prompt-style="DIALOG_MODAL"` and is activated
- **THEN** its standard parameter prompt opens as a centred movable modal dialog
- **AND** pointer movement of the heading changes the bounded dialog position without changing parameter values

#### Scenario: Sidebar action opens
- **WHEN** a representative authored Petclinic action declares `prompt-style="DIALOG_SIDEBAR"` and is activated
- **THEN** its standard parameter prompt opens as a vertical panel at the viewport's inline end
- **AND** cancellation closes the panel and restores originating-action focus

#### Scenario: Petclinic prompt submits
- **WHEN** browser automation enters and submits valid values through any demonstrated style
- **THEN** the established GraphQL preparation, validation, mutation, result, refresh, and routing contracts remain authoritative
- **AND** no unexpected console, page, resource, GraphQL, focus, or overflow failure occurs

### Requirement: Petclinic prompt-style regression coverage
Petclinic integration and opt-in browser acceptance SHALL verify canonical rich GraphQL prompt-style metadata, authored override precedence, the three effective prompt surfaces, responsive behavior, and unchanged invocation semantics.

#### Scenario: Rich GraphQL action metadata is queried
- **WHEN** integration coverage reads representative object or service action metadata
- **THEN** `promptStyle` exposes the resolved canonical enum name
- **AND** existing metadata and action state remain unchanged

#### Scenario: Browser exercises styled prompts
- **WHEN** browser automation opens inline, modal, and sidebar prompts at desktop and narrow viewport widths
- **THEN** each prompt uses the declared placement, accessible semantics, focus behavior, and cancellation restoration
- **AND** modal dragging remains bounded while inline and sidebar presentation remain free of horizontal overflow
