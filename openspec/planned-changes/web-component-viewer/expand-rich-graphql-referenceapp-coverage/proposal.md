## Why

The Apache Causeway reference application is the broadest executable catalogue of Causeway domain-model and presentation semantics and therefore provides a stronger coverage target than the deliberately small web-component sample.
A source audit of `apache/causeway-app-referenceapp` at commit `04cff95802de44d0ff5ac0802857f3bb2ffe8c3a` found 1,271 Java files covering domain services, entities, view models, mixins, bulk actions, custom values, rich supporting methods, layout annotations, resource values, temporal values, numeric values, collections, and application entry points.
The current rich GraphQL schema already represents core objects, services, mixins, dynamic hidden and disabled state, property validation and choices, action parameter negotiation, collection-valued parameters, and safe or mutating invocation placement.
However, several reference-application semantics are lossy, unavailable, or only indirectly represented, which would force a generic viewer to guess, fetch excessive collection data, or accept string fallback behavior that cannot round-trip valid domain values.
These protocol gaps should be closed or explicitly classified before a generic HTMX viewer treats the rich schema as a complete application protocol.

## What Changes

- Add an executable reference-coverage matrix that classifies representative reference-application features as supported, intentionally presentation-only, explicitly unsupported, or requiring rich GraphQL work.
- Add reversible scalar marshalling for reference-application temporal, URL, and other standard value types that currently fall through to the generic string marshaller.
- Define an extensible value-semantics descriptor for custom and Causeway value types so clients can distinguish supported textual, temporal, numeric, resource, composite, and opaque representations.
- Make unsupported input and output value shapes fail schema construction or targeted capability discovery with actionable diagnostics instead of silently accepting a non-reversible string fallback.
- Expose structured member presentation and input semantics through the already-addressed rich property, action, parameter, collection, service, and object metadata wrappers without adding a duplicate member-list API.
- Include friendly names, descriptions, constraints, editing and navigation hints, action semantics and prompt hints, collection presentation and ordering hints, and the subset of layout information required by non-Wicket clients.
- Add bounded collection reads with deterministic windowing, total-count information, and supported ordering behavior so clients do not need to materialize every row.
- Define GraphQL contracts for Blob, Clob, accepted-file constraints, and other resource-valued property or action inputs and results where a safe JSON or resource-reference representation is available.
- Expose enough application-entry metadata to identify service/menu grouping, ordering, and the home-page action without coupling clients to Wicket.
- Add deterministic server-side tests derived from representative reference-application fixtures and document the remaining intentional differences from Wicket presentation behavior.

## Capabilities

### New Capabilities

- `rich-graphql-reference-coverage`: Defines the rich GraphQL protocol coverage required for representative Causeway reference-application domain, value, collection, presentation, and application-entry semantics.

### Modified Capabilities

None.

## Impact

- Primarily affects `viewers/graphql/model`, scalar marshallers, schema construction, rich query and mutation wrappers, integration fixtures, and GraphQL documentation.
- May add fields and arguments to existing rich wrapper types while preserving current field names and operation placement.
- Provides capabilities that later component refinement and the generic HTMX viewer can consume through targeted introspection.
- Does not require GraphQL to reproduce every Wicket-only visual decoration or lifecycle demonstration.
- Does not add a duplicate object-member listing endpoint or expose Causeway metamodel internals directly.
- Requires compatibility tests for existing GraphQL clients and a migration note for value types that previously used non-reversible string fallback behavior.
