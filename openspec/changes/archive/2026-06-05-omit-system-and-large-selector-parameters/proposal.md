## Why

Synthetic parented collection selector actions currently risk exposing child properties that are poor equality filters or internal framework identifiers.
Blob and clob values are expensive to compare, while `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, and `datanucleusVersionTimestamp` are technical metadata that should not be presented as selector action parameters.

## What Changes

- Exclude blob and clob child properties from synthetic selector action parameters.
- Exclude child properties named `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, and `datanucleusVersionTimestamp` from synthetic selector action parameters.
- Preserve the existing parent parameter and eligible scalar child filter parameters.
- Keep selector action matching based only on the remaining eligible parameters supplied by the caller.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: refine which child properties are eligible to become synthetic selector action filter parameters.

## Impact

- Affects synthetic selector action parameter generation in the metamodel.
- Affects command DTO shape for synthetic selector action invocations by removing expensive or technical child filter parameters.
- Requires focused metamodel tests for blob, clob, and framework metadata property exclusion.
