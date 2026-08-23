## Why

The current semantic editor registry converts `Long`, `BigInteger`, and `BigDecimal` through JavaScript `Number`, supports only three temporal input names, and cannot represent nullable boolean or the complete resource-input contract.
Those behaviors can lose valid domain state or present advertised GraphQL inputs as unsupported, so they must be corrected before broad Vaadin editor adoption or default selection.

## What Changes

- Preserve exact decimal and integer lexical forms through editing, validation, GraphQL variables, mutation, reconciliation, and display without binary floating-point coercion.
- Support the complete accepted GraphQL temporal input catalogue, including offset and zoned semantics where advertised, with explicit timezone and precision behavior.
- Represent nullable boolean inputs without conflating `null` and `false`.
- Complete safe URL, password or protected-value, Blob, Clob, and resource-input capability handling according to public GraphQL metadata and size or media constraints.
- Classify custom value types by reversible advertised input strategy and fail closed when no strategy exists.
- Add Reference Application inventory and browser coverage for valid, invalid, null, boundary, precision, timezone, protected, and resource cases.
- Preserve public Causeway semantic elements and events while allowing native or later Vaadin controls to share the corrected codecs.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `domain-web-components`: Requires exact, reversible, null-preserving semantic editor behavior across the accepted value catalogue.
- `rich-graphql-value-semantics`: Clarifies client-visible lexical, temporal, nullable, protected, and resource-input contracts required by generic viewers.

## Impact

The change affects foundation editor codecs and renderers, GraphQL variable construction, property and action reconciliation, value metadata consumption, Reference Application classifications, and browser tests.
It may intentionally reject previously accepted lossy numeric or implicit string conversions.
It does not select Vaadin by default or add new toolkit components.
