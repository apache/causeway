## MODIFIED Requirements

### Requirement: Generic action-result outlet
The component library SHALL provide `<cw-action-results>` as a passive accessible outlet for host-owned action-result presentation with declarative `INLINE`, `DIALOG`, and `SIDEBAR` surfaces.
The outlet MUST NOT globally subscribe to action results, invoke actions, choose application policy, navigate, resolve fragments, or independently construct domain presentation.

#### Scenario: Outlet is empty
- **WHEN** no current host-owned result presentation is mounted
- **THEN** `<cw-action-results>` contributes no misleading result content, focus target, occupied layout space, dialog, or sidebar
- **AND** it remains available as a semantic placement boundary

#### Scenario: Presentation style is absent or invalid
- **WHEN** `presentation-style` is absent, blank, or unsupported
- **THEN** the outlet normalizes to `INLINE`
- **AND** existing direct-child layout, replacement, clear, and accessibility behavior remains compatible

#### Scenario: Host mounts an inline result
- **WHEN** the owning viewer places a current scalar, void-status, or standalone collection presentation into an `INLINE` outlet
- **THEN** the outlet exposes an accessible in-page result-region relationship for that content
- **AND** the Dismiss control is placed below the result content without reducing its available inline size
- **AND** long result content scrolls within a bounded content area while Dismiss remains visible and operable below it
- **AND** result rendering, announcements, links, values, and lifecycle remain owned by their established host and semantic components

#### Scenario: Host mounts a dialog result
- **WHEN** the owning viewer places a current presentation into a `DIALOG` outlet
- **THEN** the outlet opens one bounded labelled modal surface with a backdrop
- **AND** long result content scrolls independently above a visible Dismiss control placed below that content
- **AND** presentation nodes remain identity-stable light-DOM descendants of the route-owned outlet

#### Scenario: Keyboard user operates a dialog result
- **WHEN** a dialog result is open
- **THEN** initial focus enters the modal surface, Tab and Shift+Tab remain contained, and Escape requests dismissal
- **AND** dismissal restores focus to the connected eligible originating control supplied by the host

#### Scenario: Host mounts a sidebar result
- **WHEN** the owning viewer places a current presentation into a `SIDEBAR` outlet
- **THEN** the outlet opens one labelled non-modal panel at the viewport inline end without making the underlying page inert
- **AND** the panel is reachable through ordinary keyboard navigation and its bounded content scrolls above a visible Dismiss control without horizontal document overflow

#### Scenario: Keyboard user operates a sidebar result
- **WHEN** focus is within an open sidebar result
- **THEN** Escape requests dismissal without trapping ordinary Tab navigation
- **AND** explicit dismissal restores focus to the connected eligible originating control supplied by the host

#### Scenario: Narrow viewport or reduced motion applies
- **WHEN** a dialog or sidebar result is presented on a narrow viewport or under reduced-motion preference
- **THEN** content, heading, and dismissal remain visible and operable within the visual viewport
- **AND** the surface introduces neither horizontal document overflow nor required animation

#### Scenario: Result is replaced
- **WHEN** the host accepts a newer successful result generation
- **THEN** prior result nodes, surface structure, focus containment, and transient toolkit state are retired before the newer presentation becomes current
- **AND** stale nodes cannot remain interactive or overwrite the replacement

#### Scenario: Result is cleared
- **WHEN** the host clears or dismisses the current result
- **THEN** any open dialog or sidebar closes and the outlet returns to hidden-empty state
- **AND** focus restoration occurs only for a connected eligible origin and never targets stale route content

#### Scenario: Outlet disconnects
- **WHEN** route replacement or page lifecycle disconnects the outlet
- **THEN** its mounted presentation and styled surface cannot receive later asynchronous state
- **AND** modal state, backdrop, focus containment, and document-overflow effects are fully retired
- **AND** the viewer remains free to use its current deterministic fallback destination
