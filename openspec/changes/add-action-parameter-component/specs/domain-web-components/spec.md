## ADDED Requirements

### Requirement: Declarative action-parameter presentation
The component library SHALL provide a framework-neutral `<cw-parameter>` configuration component that MAY be nested directly beneath `<cw-action>` to supply presentation hints for the authoritative action parameter with the same semantic id.
Nested parameter declarations MUST NOT add, remove, reorder, hide, disable, default, validate, or invoke parameters.

#### Scenario: Selected parameter has authored presentation
- **WHEN** an authored action contains `<cw-parameter id="firstName" named="Given name" described-as="The person's given name" description-as="label" multi-line="3">`
- **AND** authoritative action preparation returns a compatible parameter with id `firstName`
- **THEN** the standard prompt uses the authored name and description and a bounded multiline editor
- **AND** hidden, disabled, default, choice, autocomplete, validity, order, and invocation semantics remain authoritative

#### Scenario: Parameter declaration is partial
- **WHEN** an action declares presentation for only some authoritative parameters
- **THEN** matching declared parameters use only their explicitly supplied overrides
- **AND** undeclared parameters and non-overridden presentation fields retain established canonical behavior

#### Scenario: Declared parameter is not authoritative
- **WHEN** a nested parameter id is absent from authoritative action preparation
- **THEN** the controller ignores that declaration
- **AND** it does not create an editor, argument, validation request, or invocation value for that id

#### Scenario: Action declares no parameters
- **WHEN** an existing authored or generated action has no nested `<cw-parameter>` children
- **THEN** parameter preparation, prompt presentation, validation, and invocation remain unchanged

### Requirement: Action-parameter name and description overrides
`<cw-parameter>` SHALL support optional `named`, `described-as`, and `description-as` attributes with normalization and accessibility behavior consistent with property presentation.
`description-as` MUST accept `label` and `tooltip`, MUST default to `label`, and MUST fall back safely for blank or unsupported values.

#### Scenario: Visible authored description
- **WHEN** a matching declaration supplies `described-as` and `description-as="label"`
- **THEN** the effective description appears in the parameter layout
- **AND** the editor is associated with that description accessibly

#### Scenario: Tooltip authored description
- **WHEN** a matching declaration supplies `described-as` and `description-as="tooltip"`
- **THEN** the effective description is available from the parameter label by pointer and keyboard
- **AND** hidden accessible description text remains associated with the editor

#### Scenario: Authored description duplicates effective name
- **WHEN** the effective description equals the effective parameter name ignoring case and surrounding whitespace
- **THEN** the prompt suppresses redundant visible and tooltip description presentation

### Requirement: Action-parameter multiline override
`<cw-parameter>` SHALL support an optional bounded `multi-line` attribute that requests the established multiline string editor without changing parameter value parsing or validation.

#### Scenario: Valid multiline hint
- **WHEN** a matching string parameter declaration supplies an integer `multi-line` value greater than one within or above the supported bound
- **THEN** the standard editor uses a textarea with the bounded effective row count
- **AND** pending-value parsing, focus, validation, and invocation remain identical to an ordinary string parameter

#### Scenario: Invalid or incompatible multiline hint
- **WHEN** `multi-line` is absent, malformed, non-positive, one, or incompatible with the authoritative input shape
- **THEN** the parameter retains its established qualified editor
- **AND** unrelated parameter presentation remains available
