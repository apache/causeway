## Why

Replay export validation currently requires every action target and reference action parameter to be established by the dotted path, unless it is a menu service root.
This is too strict for application reference data, whose identities are expected to be stable across replay environments and can safely be resolved independently of the exported command path.

## What Changes

- Introduce an application SPI for deciding whether a bookmarked entity is replay reference data.
- Allow multiple SPI implementations to participate, accepting an entity as reference data when any implementation says it is.
- Treat reference data entities as known export participants for action targets and reference action parameters, even when they were not produced by an earlier command in the dotted path.
- Keep existing dotted-path validation for non-reference-data entities.

## Capabilities

### New Capabilities

- `command-export-reference-data-participants`: Defines how application-declared reference data entities participate in command export validation.

### Modified Capabilities

- `command-export-known-targets`: Known target and known reference-parameter validation now also accepts application-declared reference data entities.

## Impact

- Adds a public SPI for command replay/export reference data classification.
- Updates command export validation and exportability calculation to consult all registered reference data classifiers.
- Requires tests for target validation, reference-parameter validation, multiple SPI implementations, and non-reference-data fallback behavior.
