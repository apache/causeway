## ADDED Requirements

### Requirement: Collection summary view can be disabled by configuration

The system SHALL provide a boolean configuration property `causeway.viewer.wicket.summary-view-disabled` that defaults to `false`. When the property is `true`, the Wicket collection summary view MUST NOT apply to any collection, even one whose element type has a `BigDecimal` property; the summary-view factory SHALL report that it does not apply. When the property is `false` or unset, the summary view SHALL continue to apply to a collection whose element type has a `BigDecimal` property, exactly as before. The capability SHALL be provided through the framework configuration system and MUST NOT rely on an environment-variable lookup.

#### Scenario: Summary view applies by default

- **GIVEN** `causeway.viewer.wicket.summary-view-disabled` is unset or `false`
- **AND** a collection whose element type has a `BigDecimal` property
- **WHEN** the summary-view factory evaluates applicability
- **THEN** it applies the summary view

#### Scenario: Summary view is disabled by configuration

- **GIVEN** `causeway.viewer.wicket.summary-view-disabled` is `true`
- **AND** a collection whose element type has a `BigDecimal` property
- **WHEN** the summary-view factory evaluates applicability
- **THEN** it does not apply the summary view

#### Scenario: Capability uses configuration, not an environment variable

- **WHEN** an operator disables the summary view
- **THEN** they set the `causeway.viewer.wicket.summary-view-disabled` configuration property
- **AND** no environment-variable lookup is used to determine the setting
