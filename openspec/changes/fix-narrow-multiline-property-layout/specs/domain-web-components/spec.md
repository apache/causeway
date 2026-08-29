## MODIFIED Requirements

### Requirement: Property label-position presentation
`<cw-property>` SHALL honour the effective property label position `LEFT`, `TOP`, or `NONE` in view, loading, error, disabled, and edit states.
Properties using `LEFT` in the same field-set-like container MUST use a consistent configurable label-to-field ratio.
Effective multiline presentation from canonical HTML, compatibility HTML, or member metadata MUST drive the same explicit responsive shell layout.

#### Scenario: Label is positioned left
- **WHEN** the effective label position is `LEFT`
- **THEN** the visible label is placed to the left of the property field using the container's consistent label-column ratio
- **AND** any description appears in smaller text below the label
- **AND** the field uses the remaining width

#### Scenario: Label is positioned top
- **WHEN** the effective label position is `TOP`
- **THEN** the visible label is placed above the property field
- **AND** any description appears in smaller text below the label
- **AND** the field uses the full available width

#### Scenario: Label is suppressed
- **WHEN** the effective label position is `NONE`
- **THEN** no visible label or description is rendered
- **AND** the field uses the full available width
- **AND** interactive controls retain a meaningful accessible name

#### Scenario: Property description comes from the facet
- **WHEN** `@PropertyLayout(describedAs)` supplies a non-redundant description and no HTML override is present
- **THEN** the description appears in smaller text immediately below the visible label
- **AND** the value or editor is associated with it through accessible description semantics

#### Scenario: Narrow presentation stacks safely
- **WHEN** a multiline `LEFT` property resolved from `multi-line`, legacy `multiline`, or `metadata.multiLine` cannot retain its configured columns at a narrow inline size
- **THEN** label, description, field, and controls occupy explicit successive grid rows in meaningful document order
- **AND** the edit control aligns with its field without overlap or implicit-grid displacement
- **AND** the property introduces no horizontal overflow or clipping
