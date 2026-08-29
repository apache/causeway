## Context

`<cw-property>` currently derives its default label by humanizing the member ID, uses the GraphQL member field description as explanatory metadata, and accepts legacy authored `label` and `multiline` attributes.
The rich GraphQL model already exposes canonical member metadata such as `friendlyName`, `description`, and `multiLine`, but the web-component property read does not select that nested metadata and the metadata does not yet expose `labelPosition`.
Effective-grid parsing also carries only the multiline presentation hint into generated property markup.

The Petclinic sample directly authors low-level property elements, making it suitable for demonstrating both annotation-derived facets and selective HTML overrides.

## Goals / Non-Goals

**Goals:**

- Define one effective presentation model for property name, description, multiline rows, and label position.
- Preserve semantic metadata as the default while giving explicit HTML attributes precedence.
- Render `LEFT`, `TOP`, and `NONE` accessibly in view and edit states.
- Keep labels consistently aligned within the same field-set-like container without application-specific per-property widths.
- Verify the public contract in foundation tests and Petclinic acceptance coverage.

**Non-Goals:**

- Rework collection or action label presentation.
- Add arbitrary label-position values beyond `LEFT`, `TOP`, and `NONE`.
- Infer that all properties in an authored page should receive overrides.
- Replace the existing effective-grid parser or object layout system.

## Decisions

### Use canonical rich member metadata as the facet transport

The rich GraphQL member `metadata` selection will expose `friendlyName`, `description`, `multiLine`, and `labelPosition` for properties.
The web-component client will describe the metadata type and property reads will select only the supported presentation fields.
This keeps facet semantics GraphQL-authoritative for directly authored `<cw-property>` elements instead of requiring them to load and parse an effective grid.

Using only GraphQL field names and descriptions was rejected because it cannot carry multiline and label-position facets reliably.
Loading effective-grid XML from each property was rejected because it duplicates `<cw-object>` layout responsibilities and adds unnecessary resource coupling.

### Define explicit authored override precedence

The effective values will be resolved in this order:

1. The authored `named`, `described-as`, `multi-line`, or `label-position` attribute when present and valid.
2. Canonical property metadata returned with the current state.
3. Existing fallback behavior, including humanizing the member ID and defaulting label position to `LEFT`.

The existing `label` and `multiline` attributes will remain compatibility aliases below the new canonical authored names but above metadata, avoiding an abrupt break for existing samples and generated markup.
Empty `described-as` and `named` values are explicit text overrides, allowing an author to suppress metadata where appropriate, while `label-position="NONE"` is the preferred way to suppress both label and description.

### Normalize bounded values at the component boundary

`multi-line` accepts an integer greater than one and caps it at the existing maximum of 50 rows.
Invalid values fall back to metadata or single-line behavior rather than breaking sibling content.
`label-position` is case-insensitively normalized to `LEFT`, `TOP`, or `NONE`; invalid authored values fall back to metadata and then `LEFT`.

### Render a stable semantic property grid

The property wrapper will expose a normalized `data-label-position` hook and use a CSS grid with a shared configurable label-column width for `LEFT`.
Properties in the same field list therefore use the same ratio through a container-level CSS variable rather than measuring labels in JavaScript.
`TOP` places the label and description above a full-width value/editor region.
`NONE` omits label and description markup, gives the value/editor the full width, and uses an off-screen accessible name where a native control still requires one.

Descriptions remain associated through `aria-describedby` and use a smaller muted presentation below the visible label.
Disabled reasons remain independently available and do not replace descriptions.

### Carry presentation through generated layouts

Effective-grid property parsing will retain `named`, `describedAs`, `multiLine`, and `labelPosition` in the member presentation plan and emit the canonical authored attribute names when generating `<cw-property>`.
This makes generated and directly authored properties use the same component boundary and precedence rules.

## Risks / Trade-offs

- [Risk] Adding nested metadata to every property read increases GraphQL response size. → Select only supported scalar presentation fields and merge them into the existing coordinated object read.
- [Risk] Removing visible labels for `NONE` can weaken accessibility. → Preserve an accessible name for value and editor controls without rendering visible label or description content.
- [Risk] Existing themes assume the current implicit inline flow. → Keep stable host classes, add normalized data hooks, and use additive CSS variables with responsive stacking.
- [Risk] Compatibility aliases can make precedence confusing. → Document and test deterministic canonical-attribute, alias, metadata, and fallback precedence.

## Migration Plan

Introduce metadata and component behavior additively, update generated markup to canonical attributes, then update selected Petclinic examples and tests.
Rollback can revert the additive metadata field and component changes without domain-data migration.

## Open Questions

None.
