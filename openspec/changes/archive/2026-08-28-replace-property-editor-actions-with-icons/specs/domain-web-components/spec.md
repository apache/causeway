## MODIFIED Requirements

### Requirement: Property editing semantics
An editable `<causeway-property>` SHALL provide view, preparing, editing, validating, saving, success, and failed interaction states driven by its rich-schema capabilities.
Its view-state edit affordance MUST be a compact conventional icon control adjacent to the property value with a property-specific accessible name and pointer description.
Its active editor MUST present Save and Cancel as compact conventional icon controls with property-specific accessible names and pointer descriptions while preserving native button, gating, keyboard, and focus behavior.
During stable ordinary editing, the component MUST communicate edit mode through the focused editor and its controls without rendering a redundant “Editing” status label or empty status row.
Meaningful preparing, validating, saving, correction-required, and unsupported states SHALL retain their bounded status presentation.

#### Scenario: User starts editing
- **WHEN** a visible and enabled property advertises a supported update operation and the user activates edit
- **THEN** the component selects an editor from the semantic editor registry
- **AND** lazily obtains the supported editor semantics needed for the current property
- **AND** focuses and presents the editor with compact Save and Cancel icon controls without a separate “Editing” status label

#### Scenario: Editable property is presented
- **WHEN** a visible and enabled property can offer editing in view state
- **THEN** the component presents a compact pencil icon control adjacent to the property value
- **AND** the control's accessible name and pointer description identify the property that will be edited
- **AND** the decorative icon is not a separate focus or accessibility target

#### Scenario: Property editor actions are presented
- **WHEN** a property is in active edit mode
- **THEN** Save is presented as a compact tick icon control and Cancel as a compact cross icon control
- **AND** each control's accessible name and pointer description identify both its action and property
- **AND** each decorative icon is outside the focus and accessibility trees
- **AND** Save retains validation gating and primary treatment while Cancel retains cancellation and focus behavior

#### Scenario: User cancels editing
- **WHEN** the user cancels before a successful update
- **THEN** the component restores its authoritative context value
- **AND** does not execute an update command

#### Scenario: Property is not editable
- **WHEN** the property is hidden, disabled, or lacks an update capability
- **THEN** the component does not expose an enabled edit affordance

#### Scenario: Property edit changes state
- **WHEN** an active property editor is preparing, validating, saving, failed, or unsupported
- **THEN** the component presents the corresponding bounded meaningful status
- **AND** does not replace that status with an ordinary “Editing” label
