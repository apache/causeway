## ADDED Requirements

### Requirement: Canonical property presentation metadata
The rich GraphQL property contract SHALL expose canonical friendly name, description, multiline row count, and label position metadata when those facets are available.
A `<cw-property>` read SHALL request the supported presentation metadata together with its existing semantic state and value projection.

#### Scenario: Property presentation facets are available
- **WHEN** a property declares `@PropertyLayout(named)`, `@PropertyLayout(describedAs)`, `@PropertyLayout(multiLine)`, or `@PropertyLayout(labelPosition)`
- **THEN** the rich property metadata exposes the corresponding canonical values
- **AND** a directly authored `<cw-property>` can render them without loading the effective-grid resource

#### Scenario: Presentation metadata field is unavailable
- **WHEN** an older compatible rich schema does not expose one or more presentation metadata fields
- **THEN** the property requests only supported fields
- **AND** missing values use the established fallback presentation without issuing an invalid GraphQL operation

### Requirement: Authored property presentation overrides
`<cw-property>` SHALL support `named`, `described-as`, `multi-line`, and `label-position` attributes as explicit overrides of canonical property presentation metadata.
Canonical authored attributes MUST take precedence over compatibility aliases, which MUST take precedence over metadata and fallback values.

#### Scenario: Property name is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" named="Given name">`
- **THEN** the visible and accessible property name is `Given name`
- **AND** another property without `named` continues to use canonical metadata or its humanized member ID

#### Scenario: Property description is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" described-as="The given or first name of this customer">`
- **THEN** that text is the property's effective visible and accessible description
- **AND** it takes precedence over any canonical property description

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
- **WHEN** a `LEFT` property cannot retain its configured columns at a narrow inline size
- **THEN** label, description, field, and controls stack in meaningful document order
- **AND** the property introduces no horizontal overflow, clipping, or overlap

### Requirement: Generated property presentation equivalence
`<cw-object>` SHALL carry supported effective-grid property name, description, multiline, and label-position hints into generated `<cw-property>` elements through the canonical public attributes.

#### Scenario: Effective grid supplies property presentation
- **WHEN** an effective-grid property reference supplies `named`, `describedAs`, `multiLine`, or `labelPosition`
- **THEN** the generated property receives the equivalent `named`, `described-as`, `multi-line`, or `label-position` attribute
- **AND** direct and generated property elements resolve presentation through the same component logic

#### Scenario: Effective grid presentation is partial
- **WHEN** an effective-grid property reference supplies only some supported presentation hints
- **THEN** supplied hints override their corresponding metadata values
- **AND** omitted hints continue to use canonical metadata or fallback values

### Requirement: Executable Petclinic property presentation examples
The Petclinic sample SHALL demonstrate annotation-derived and HTML-overridden property presentation selectively rather than applying every presentation form to every property.

#### Scenario: Maintainer inspects Petclinic property declarations
- **WHEN** a maintainer inspects the Petclinic domain and page resources
- **THEN** some but not all properties declare `@PropertyLayout(describedAs)`
- **AND** at least one property demonstrates annotation-derived `labelPosition=TOP`
- **AND** at least one property uses an authored `named` override
- **AND** some but not all appropriate properties use authored `described-as`, `multi-line`, or `label-position` overrides

#### Scenario: Browser renders Petclinic examples
- **WHEN** the Petclinic owner page reaches its ready state
- **THEN** the selected annotation-derived and HTML-overridden names, descriptions, multiline fields, and label positions are visible and accessible
- **AND** non-overridden properties retain their metadata-driven or fallback presentation
