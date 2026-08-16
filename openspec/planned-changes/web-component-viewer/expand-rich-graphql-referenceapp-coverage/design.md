## Context

The reference application intentionally exercises far more Causeway semantics than the current `sample-html` fixture.
The audit used `apache/causeway-app-referenceapp` commit `04cff95802de44d0ff5ac0802857f3bb2ffe8c3a`, which contains 1,271 Java files and extensive examples of domain annotations, programmatic supporting methods, standard and custom value types, layout hints, services, entities, view models, mixins, and application entry points.

The current rich GraphQL model already has strong behavioral coverage.
It exposes contributing domain services at the top level, bookmark-addressable entities and view models, mixin members, property and collection reads, dynamic hidden and disabled state, property choices, autocomplete and validation, action defaults, choices, autocomplete and validation, collection-valued action parameters, safe query invocation, and top-level mutation placement.
It also exposes object identity, logical type, version, title, CSS class, icon, layout, grid, and save-as resources through object metadata.

The audit nevertheless found protocol gaps that matter before generic page composition.
`RichCollectionGet` returns an unargumented list with no response window or total-count contract.
Rich property, action, parameter, and collection wrappers expose behavioral state but not structured friendly-name, constraint, prompt, ordering, or presentation metadata.
The default scalar marshaller set omits reference-app types such as `LocalDateTime`, `URL`, `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp`, and the last-resort object marshaller maps output to GraphQL String while returning raw GraphQL input without reconstructing the target value.
Blob and Clob have specialized property reads but do not yet form a uniform property-update and action-input or result contract.
Service actions are discoverable, but service grouping, ordering, and the home-page designation are not represented as application-entry semantics.

## Goals / Non-Goals

**Goals:**

- Use the reference application as an explicit protocol coverage catalogue.
- Distinguish already-supported semantics from true GraphQL gaps and Wicket-only concerns.
- Make every supported input-capable value representation reversible.
- Make unsupported value shapes explicit rather than silently lossy.
- Expose structured semantics needed by framework-neutral generic clients through targeted rich-wrapper fields.
- Add bounded collection access suitable for large associations.
- Define resource-valued interaction contracts.
- Represent application entry points sufficiently for later generic viewers.
- Preserve existing rich GraphQL operations and the no-duplicate-member-list constraint.

**Non-Goals:**

- Reproducing every Wicket visual decoration in GraphQL.
- Exposing Causeway metamodel objects or annotation instances directly.
- Adding a second endpoint that lists all object members.
- Implementing the web-component editors or generic HTMX viewer in this change.
- Requiring the normal build to clone or execute the external reference application.
- Guaranteeing efficient database-level pagination for domain collections whose programming model materializes the complete association.
- Defining authentication pages or viewer-specific navigation policy.

## Coverage Findings

### Already represented

- Domain services exposed as top-level GraphQL fields.
- Entity and view-model identity through bookmarks and object metadata.
- Mixin properties, collections, and actions through the Causeway metamodel.
- Dynamic hidden and disabled state.
- Property get, set or mutation, choices, autocomplete, validation, and datatype.
- Action parameter hidden, disabled, default, choices, autocomplete, validation, and datatype semantics.
- One-to-many action parameters used by bulk actions.
- Safe, idempotent, and mutating action placement through configured query and mutation variants.
- Scalar, object, collection, and void action results where the underlying value type is representable.
- Blob and Clob property reads.
- Object title, icon, CSS class, layout, and grid resource metadata.

### Missing or lossy

- Canonical friendly name is conflated with field description and cannot be queried independently.
- Property constraints and editor hints such as maximum length, regular expression, file acceptance, multiline, typical length, label position, and navigation intent are not structured rich-wrapper fields.
- Action prompt, redirect, association, position, sequence, icon, CSS, and related presentation semantics are not structured rich-wrapper fields.
- Collection default view, page size, configured ordering, table decoration, and sequence are not structured rich-wrapper fields.
- Collection content has no windowing, continuation, or count contract.
- Several reference-app temporal and URL types fall through to non-reversible string behavior.
- Causeway and application custom values lack a discoverable representation category and round-trip contract unless an application supplies bespoke scalar infrastructure.
- Blob, Clob, file-accept, image, markup, password, local-resource-path, tree, composite, and embedded values do not have one uniform and explicit interaction policy.
- Service grouping, ordering, and home-page designation are not represented as application entry points.

### Intentionally separate

- Domain events, execution publishing, command publishing, lifecycle events, and entity-change publishing are server execution concerns rather than client rendering metadata.
- Wicket-specific repainting, table decorators, CSS classes, and redirect behavior may be exposed as optional hints but must not become mandatory client behavior.
- Authorization remains enforced by Causeway hidden, disabled, and invocation semantics rather than disclosed as policy rules.

## Decisions

### Maintain a pinned reference-derived coverage matrix

The change will record the audited reference revision and map each representative feature to GraphQL schema fields, configured resources, explicit unsupported diagnostics, or a documented viewer-specific exclusion.
Reduced fixtures in this repository will encode the contract so builds remain deterministic and offline.
Updating the pinned audit later will be a deliberate review rather than an implicit dependency update.

### Eliminate silent lossy input fallback

The generic object scalar may remain useful for explicitly output-only diagnostic text, but it will not claim reversible input support for an arbitrary Causeway value.
Schema construction or capability discovery will report an actionable unsupported representation when no reversible marshaller exists.

Standard types exercised by the reference application will receive canonical documented formats and round-trip tests.
Application value types will use the existing marshaller extension mechanism augmented by discoverable value-semantics metadata.

### Add value-semantics descriptors rather than frontend editor names

Rich datatype metadata will describe representation category, logical type, GraphQL input and output shape, canonical format, constraints, and resource behavior.
It will not prescribe an HTML control or frontend framework component.
Web-component editor registries and other clients can map those semantics to presentation independently.

### Extend known-member wrappers instead of listing members again

Presentation and constraint metadata will be additive fields beneath the existing property, action, parameter, collection, object-meta, and service wrappers.
Clients will continue to discover member identifiers through standard GraphQL introspection and request metadata only for members they already address.

GraphQL field descriptions remain useful documentation but will no longer be overloaded as the only carrier of friendly names and descriptions.

### Treat grid resources as structure and wrapper metadata as local semantics

`_meta.grid` and `_meta.layout` remain the source for page regions, grouping, and complete layout structure where available.
Wrapper fields expose the local name, constraints, interaction hints, and fallback ordering needed when a member is used outside a grid or when a resource is absent.
The protocol will avoid duplicating an entire serialized grid beneath every object read.

### Add compatible bounded collection reads

Existing `get` without arguments remains available during compatibility migration.
An additive windowed form will accept a deterministic start position or cursor and requested size and return rows plus continuation and count metadata.

Configured collection ordering will be applied where Causeway provides a stable comparator.
The contract will state when response windowing does not imply database-level lazy loading.

### Separate resource metadata from content transfer

Blob and Clob metadata, accepted media types, filenames, and size limits will be queryable without transferring content.
Small bounded content may use an inline representation, while larger or forbidden content uses secured resource references consistent with existing resource response policy.

Password and hidden values will never gain a readable fallback representation.
Unsupported image, tree, composite, or application value operations will return explicit capability diagnostics until a marshaller and policy are registered.

### Add application-entry metadata without prescribing navigation

Top-level domain-service fields remain the executable service contract.
Additive metadata identifies grouping, ordering, friendly presentation, visibility, and the configured home-page action.
A client decides how to render menus, landing pages, and navigation.

## Risks / Trade-offs

- [The reference application contains Wicket-specific demonstrations] → Classify each feature and expose only framework-neutral semantics or optional hints.
- [A large proposal can become an attempt at complete viewer parity] → Stage work by value types, metadata, collections, resources, and entry points with explicit exclusions.
- [New wrapper fields enlarge introspection and object reads] → Keep fields optional and targeted, and avoid aggregate member metadata payloads.
- [Changing fallback scalar behavior can break clients relying on strings] → Preserve output compatibility where safe, add diagnostics and migration notes, and gate stricter input behavior through compatibility policy if required.
- [Collection windows may still materialize full associations] → Document the distinction and measure server behavior before claiming persistence-level pagination.
- [Metadata can leak sensitive policy] → Expose presentation and capability outcomes, never authorization rules, hidden values, or password data.
- [Custom value semantics vary by application] → Keep an extension contract and explicit unsupported result rather than guessing serialization.

## Migration Plan

All new metadata and collection contracts are additive.
Existing rich reads and mutations remain valid.
New standard marshallers are selected ahead of the fallback marshaller and use canonical formats documented for clients.
If stricter unsupported-input behavior is not source compatible, introduce it behind a configuration mode, deprecate arbitrary fallback input, and change the default in a later release.

## Open Questions

- Should bounded collections use offset and limit, opaque cursors, or expose both over one internal window abstraction?
- Which presentation hints are sufficiently framework-neutral to belong in GraphQL rather than only in the grid resource?
- Should canonical temporal scalars remain named strings or use dedicated GraphQL scalar names with format descriptions?
- Which Causeway value types should be supported by default versus requiring application scalar marshallers?
- Can secured resource references support upload as well as download without adding multipart GraphQL transport?
- Should menu and home-page metadata be part of the rich root schema or a separate optional application-entry root field?
