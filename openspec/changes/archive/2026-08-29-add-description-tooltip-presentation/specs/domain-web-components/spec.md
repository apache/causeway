## ADDED Requirements

### Requirement: Shared member description tooltip presentation
`<cw-property>` and `<cw-collection>` SHALL provide a framework-neutral tooltip presentation for effective descriptions without changing their metadata source or precedence.
Tooltip content MUST be escaped, bounded, pointer-discoverable, keyboard-reachable, responsive, and associated with its member through accessible description semantics.

#### Scenario: Tooltip mode is selected
- **WHEN** a property or collection with a non-blank effective description declares `description-as="tooltip"` ignoring case
- **THEN** the description does not consume visible layout space
- **AND** the semantic property label, property field when its label is suppressed, or collection heading exposes the description by pointer and keyboard
- **AND** assistive technology retains access to the effective description

#### Scenario: Label mode is selected or implied
- **WHEN** `description-as` is absent, blank, `label`, or unsupported
- **THEN** the effective description retains the established visible label-adjacent presentation
- **AND** unsupported input does not create an unknown presentation state

#### Scenario: Description and disabled reason share a tooltip
- **WHEN** a property or collection has both an effective description and a disabled reason
- **THEN** one tooltip presents a bounded description section above a distinct bounded disabled-reason section
- **AND** accessible description references preserve the same description-then-reason order

#### Scenario: Only a disabled reason is available
- **WHEN** a disabled property or collection has no effective non-redundant description
- **THEN** its semantic tooltip trigger exposes the bounded disabled reason without an empty description section
- **AND** the reason is not added as ordinary visible page text

#### Scenario: Presentation attribute changes
- **WHEN** `description-as` changes while the component is connected
- **THEN** the current member rerenders using the newly resolved presentation
- **AND** member data is not independently reloaded solely for the presentation change

## MODIFIED Requirements

### Requirement: Authored property presentation overrides
`<cw-property>` SHALL support `named`, `described-as`, `description-as`, `multi-line`, and `label-position` attributes as explicit overrides of canonical property presentation metadata or its presentation.
Canonical authored attributes MUST take precedence over compatibility aliases, which MUST take precedence over metadata and fallback values.

#### Scenario: Property name is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" named="Given name">`
- **THEN** the visible and accessible property name is `Given name`
- **AND** another property without `named` continues to use canonical metadata or its humanized member ID

#### Scenario: Property description is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" described-as="The given or first name of this customer">`
- **THEN** that text is the property's effective visible or tooltip description according to `description-as`
- **AND** it takes precedence over any canonical property description

#### Scenario: Description presentation is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" description-as="tooltip">`
- **THEN** the effective description uses tooltip presentation regardless of whether its text came from `described-as` or canonical metadata
- **AND** changing presentation does not change description-text precedence

#### Scenario: Multiline rows are overridden
- **WHEN** authored HTML contains `<cw-property id="notes" multi-line="5">`
- **THEN** a supported string view or editor uses five multiline rows
- **AND** canonical metadata does not replace the authored row count

#### Scenario: Invalid multiline override
- **WHEN** `multi-line` is malformed, no greater than one, or exceeds the supported maximum
- **THEN** the component ignores the malformed or non-multiline value or caps an excessive value at the supported maximum
- **AND** unrelated property rendering remains available

#### Scenario: Label position is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" label-position="TOP">`
- **THEN** the property uses the `TOP` presentation regardless of its canonical label-position facet

#### Scenario: Invalid label-position override
- **WHEN** an authored `label-position` is not `LEFT`, `TOP`, or `NONE` ignoring case
- **THEN** the component falls back to canonical metadata and then `LEFT`
- **AND** exposes no broken or unknown layout mode

#### Scenario: Compatibility aliases remain usable
- **WHEN** existing markup uses `label` or `multiline`
- **THEN** the component preserves the established override behavior
- **AND** a simultaneously supplied canonical `named` or `multi-line` attribute wins deterministically

### Requirement: Property label-position presentation
`<cw-property>` SHALL honour the effective property label position `LEFT`, `TOP`, or `NONE` in view, loading, error, disabled, and edit states.
Properties using `LEFT` in the same field-set-like container MUST use a consistent configurable label-to-field ratio.
Effective multiline presentation from canonical HTML, compatibility HTML, or member metadata MUST drive the same explicit responsive shell layout.
Description layout MUST follow the effective `description-as` mode without weakening accessible naming or description.

#### Scenario: Label is positioned left
- **WHEN** the effective label position is `LEFT`
- **THEN** the visible label is placed to the left of the property field using the container's consistent label-column ratio
- **AND** a label-presented description appears in smaller text below the label
- **AND** the field uses the remaining width

#### Scenario: Label is positioned top
- **WHEN** the effective label position is `TOP`
- **THEN** the visible label is placed above the property field
- **AND** a label-presented description appears in smaller text below the label
- **AND** the field uses the full available width

#### Scenario: Label is suppressed
- **WHEN** the effective label position is `NONE`
- **THEN** no visible label or label-presented description is rendered
- **AND** the field uses the full available width
- **AND** interactive controls retain a meaningful accessible name
- **AND** a tooltip-presented description remains available from the property field by pointer and keyboard

#### Scenario: Property description comes from the facet
- **WHEN** `@PropertyLayout(describedAs)` supplies a non-redundant description and no HTML text override is present
- **THEN** the description appears in smaller text immediately below the visible label by default or in the tooltip when selected
- **AND** the value or editor is associated with it through accessible description semantics

#### Scenario: Narrow presentation stacks safely
- **WHEN** a multiline `LEFT` property resolved from `multi-line`, legacy `multiline`, or `metadata.multiLine` cannot retain its configured columns at a narrow inline size
- **THEN** label and any label-presented description occupy explicit successive rows followed by an explicit full-width field row in meaningful document order
- **AND** the field's value and bounded edit control align at the start of that row without overlap or implicit-grid displacement
- **AND** the property introduces no horizontal overflow or clipping

### Requirement: Semantic collection headings
The `<cw-collection>` component SHALL render one semantic collection heading from an explicit HTML name, canonical member metadata, or a safe member-id fallback.
It SHALL render or tooltip-present a distinct available description according to `description-as` and associate the description with every collection presentation state.

#### Scenario: HTML supplies text and presentation overrides
- **WHEN** a collection declares non-blank `named` and `described-as` attributes and a supported `description-as` value
- **THEN** those text values are the effective collection name and description
- **AND** they override canonical friendly-name and description metadata
- **AND** the description uses the selected visible-label or tooltip presentation

#### Scenario: Canonical metadata supplies heading text
- **WHEN** no applicable HTML text override exists and the collection response supplies canonical `metadata.friendlyName` or `metadata.description`
- **THEN** each available value is used independently
- **AND** a missing value falls back to a humanized member id for the name or no description for the description

#### Scenario: Legacy label remains present
- **WHEN** a collection declares the existing `label` attribute without `named`
- **THEN** the label remains the effective name for backward compatibility
- **AND** `named` takes precedence when both are non-blank

#### Scenario: Description duplicates resolved name
- **WHEN** the candidate description equals the resolved name after trimming and case-insensitive comparison
- **THEN** no duplicate description is rendered, tooltip-presented, or referenced

#### Scenario: Heading attributes change
- **WHEN** `named`, `described-as`, `description-as`, `label`, or the member id changes while the component is connected
- **THEN** the current collection shell rerenders with the newly resolved accessible heading and description presentation
- **AND** collection data is not independently reloaded solely for the text or presentation change

### Requirement: Quiet read-only collection presentation
Collection wrappers SHALL remain read-only and authorization-aware without rendering their disabled or unmodifiable boolean or reason as ordinary visible explanatory text or a label.
A non-blank collection-level reason MUST remain available on demand from the semantic heading tooltip and through accessible description semantics.

#### Scenario: Mixed-in collection is unmodifiable
- **WHEN** a readable mixed-in collection reports a disabled reason such as “Cannot edit a mixed-in collection”
- **THEN** its rows, heading, description, paging, and applicable associated actions remain available
- **AND** the collection-level reason is absent from ordinary visible layout but available from the heading tooltip

#### Scenario: Collection has description and unmodifiable reason
- **WHEN** a collection has both an effective description and a disabled or unmodifiable reason
- **THEN** the heading tooltip presents the description above the reason as distinct sections
- **AND** the description remains visibly rendered as well when `description-as` resolves to `label`

#### Scenario: Member or associated action is disabled
- **WHEN** an individual row member or associated action has its own disabled state and reason
- **THEN** that control retains its established semantic state and explanation
- **AND** collection-level tooltip presentation does not broaden authorization or enable mutation
