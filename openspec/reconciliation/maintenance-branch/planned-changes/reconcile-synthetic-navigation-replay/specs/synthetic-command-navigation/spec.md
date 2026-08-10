<!--
DRAFT DELTA — re-validate on promotion into openspec/changes/.
The MODIFIED requirement below restates the EXISTING requirement
"Framework synthesizes selector actions for parented collections"
with only the collection action-id changed from
  __causeway_navigate_to_<collectionId>
to
  __causeway_navigate_to_one_of_<collectionId>
On promotion, carry over the full existing requirement body verbatim and change ONLY:
  (1) the id-statement line, and
  (2) the "Synthetic action id is deterministic" scenario's prefix assertion.
All other statement lines and scenarios of that requirement are unchanged.
-->

## MODIFIED Requirements

### Requirement: Framework synthesizes selector actions for parented collections

The system SHALL provide the `causeway.extensions.command-log.recording-support` configuration property that
controls command-log recording support behavior. The `recording-support` property SHALL be an enum with values
`ENABLED` and `DISABLED`, defaulting to `DISABLED`. When `recording-support` is `ENABLED`, the system SHALL
synthesize a safe metamodel `ObjectAction` for each eligible parented collection association whose owning type
does not implement the command recording suppression marker interface; when `DISABLED`, or when the owning type
implements that marker, the system MUST NOT synthesize the selector action.

The synthetic action SHALL have a deterministic identifier that does not collide with developer-authored actions.
**The synthetic parented-collection selector action identifier SHALL use the reserved prefix
`__causeway_navigate_to_one_of_` followed by the associated parented collection id** (distinguishing it from the
scalar-reference navigation identifier, which uses `__causeway_navigate_to_` followed by the reference id). The
remaining synthesis behavior — eligibility, layout association, display name `Navigate To`, CSS/Font-Awesome
styling, and marker suppression — is unchanged.

> Promotion note: the existing requirement's other SHALL statements and all its scenarios are preserved
> verbatim; only the collection id form and the deterministic-id scenario's prefix assertion change to
> `__causeway_navigate_to_one_of_`.

#### Scenario: Synthetic collection selector action id uses the one-of infix

- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** command-log recording support is `ENABLED`
- **AND** `Lease` does not implement the command recording suppression marker interface
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action identifier is `__causeway_navigate_to_one_of_items`

#### Scenario: Scalar-reference navigation id is unchanged

- **GIVEN** an entity type has a scalar reference property `owner`
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework synthesizes the navigate-to action for `owner`
- **THEN** the action identifier is `__causeway_navigate_to_owner`

## ADDED Requirements

### Requirement: Recorded synthetic collection-navigation commands remain replayable

The system SHALL replay a command recorded against a synthetic parented-collection selector action even when the
collection's filter columns — and therefore the selector action's parameters — have been added, removed, or
reordered between recording and replay. For a synthetic collection-navigation action, the system SHALL bind each
recorded command DTO parameter to the current action parameter that has the same parameter id, falling back to
the same friendly name, rather than by position. Any current selector parameter that has no corresponding
recorded DTO parameter SHALL be padded with an empty/no-op filter value. Ordinary (non-synthetic) action replay
SHALL continue to bind arguments positionally.

The system SHALL derive the selector's filter parameters excluding any filter property hidden at
`Where.REFERENCES_PARENT`, and SHALL order those parameters by member-order sequence and then by id, so that a
recorded parameter set aligns deterministically with the current metamodel.

#### Scenario: Collection-navigation command recorded on an earlier metamodel replays

- **GIVEN** a command DTO recorded against a synthetic collection selector with id `__causeway_navigate_to_one_of_items`
- **WHEN** the command is replayed on a system that synthesizes the same selector id
- **THEN** the action is resolved and executed
- **AND** replay does not fail with an unknown-action error

#### Scenario: Replay tolerates a changed filter-column set

- **GIVEN** a recorded collection-navigation command whose DTO parameters differ from the current selector's
  parameters because the collection's columns were added, removed, or reordered
- **WHEN** the command is replayed
- **THEN** each recorded parameter binds to the current parameter with the same id or friendly name
- **AND** any current parameter absent from the DTO is padded with an empty filter value
- **AND** the correct child object is selected

#### Scenario: A parent-referencing filter property is excluded from selector parameters

- **GIVEN** a parented collection whose element type has a property hidden at `Where.REFERENCES_PARENT`
- **WHEN** the framework synthesizes the selector action
- **THEN** that property is not a selector filter parameter
