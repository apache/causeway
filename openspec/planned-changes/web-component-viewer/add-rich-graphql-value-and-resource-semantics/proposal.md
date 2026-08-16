## Why

The preliminary reference-app audit found standard, Causeway, custom, and resource value types that are not uniformly reversible through the rich GraphQL schema.
The generic object marshaller can make output appear readable as a string while returning raw GraphQL input without reconstructing the declared Java value, and specialized Blob and Clob property reads do not yet define a uniform update and action contract.
Clients need explicit, discoverable value semantics rather than accidental string coercion.

## What Changes

- Add canonical reversible marshalling for confirmed missing standard reference-app datatypes.
- Replace silent input-capable object-string fallback with explicit supported or unsupported capability behavior.
- Add an editor-neutral rich datatype descriptor covering logical type, representation category, input and output shape, canonical format, constraints, and resource behavior.
- Define extension contracts for Causeway and application custom values.
- Define consistent Blob, Clob, file, and other supported resource contracts across property reads, updates, action parameters, and action results.
- Preserve strict non-disclosure for passwords, hidden values, and unsupported opaque values.
- Add round-trip, invalid-input, size, media-type, compatibility, and targeted-introspection tests.

## Capabilities

### New Capabilities

- `rich-graphql-value-semantics`: Defines reversible and discoverable rich GraphQL contracts for standard, Causeway, custom, and resource values.

### Modified Capabilities

None.

## Impact

- Affects GraphQL scalar marshallers, type mapping, rich datatype wrappers, property mutation, action invocation and results, resource policies, tests, and documentation.
- Depends on the completed reference-app analysis and may be narrowed by its evidence.
- May require a compatibility mode for applications relying on arbitrary raw-string fallback input.
- Does not implement browser editors or resource widgets.
