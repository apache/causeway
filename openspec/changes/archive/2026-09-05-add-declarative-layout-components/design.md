## Context

The foundation already exposes semantic member elements and can parse the authoritative effective Causeway grid for `<cw-object>`, but application-authored exact pages use generic `<div>` and `<section>` wrappers for equivalent layout concepts.
The effective grid parser already normalizes rows, columns, tabs, fieldsets, properties, collections, actions, and authoritative member references into a toolkit-neutral plan.
The object context already coordinates schema description and same-origin structural-resource loading, so `<cw-metadata>` can reuse those boundaries rather than introduce another GraphQL or resource client.

## Goals / Non-Goals

**Goals:**

- Provide framework-neutral declarative elements for fieldsets, twelve-column rows, columns, tab groups, and tabs.
- Enforce the confirmed direct-child contracts with bounded diagnostics and safe unsupported-child handling.
- Provide accessible fieldset and tab semantics, responsive layout, and keyboard operation.
- Populate `<cw-metadata>` exclusively from the current object's effective fieldset id `metadata`.
- Render metadata properties as ordinary `<cw-property>` elements and associated actions under one accessible panel dropdown.
- Preserve existing context coalescing, authorization, validation, interaction, and lifecycle behavior.

**Non-Goals:**

- Automatically translating an entire `layout.xml` into authored `<cw-row>` and related declarations.
- Letting layout wrappers infer or select arbitrary domain members.
- Allowing hosts to inspect raw GraphQL, effective-grid XML, or toolkit internals.
- Replacing `<cw-object>` automatic effective-grid rendering.
- Adding arbitrary Bootstrap classes, nested column recursion, or a general-purpose design system.

## Decisions

### Use light-DOM custom elements with owned semantic chrome

`<cw-row>` and `<cw-column>` remain light-DOM layout hosts so semantic member descendants retain normal context discovery and shared styling.
`<cw-fieldset>` owns a generated heading and group semantics while preserving explicitly authored `<cw-property>` children.
`<cw-tabgroup>` owns the tablist controls and coordinates direct `<cw-tab>` panels; each tab preserves its authored `<cw-row>` children.
Names come from `name`, with bounded human-readable fallbacks where a name is optional.

Using host-framework wrappers was rejected because it would duplicate behavior between HTMX and Vue.
Using autonomous shadow trees for all containers was rejected because it would complicate shared styling and focus inspection without improving authority boundaries.

### Enforce direct-child contracts with one bounded diagnostic event

A shared layout-container base validates direct element children at connection and after child-list mutation.
Allowed authored relationships are fieldset → property, row → column, column → fieldset/collection/metadata, tabgroup → tab, and tab → row.
Unsupported direct children are hidden from presentation and produce `causeway-layout-component-diagnostic`; valid children remain connected and authoritative.
Whitespace and comments are ignored.

Throwing during custom-element upgrade was rejected because one malformed region must not break the page or disconnect valid semantic members.
Silently accepting unsupported children was rejected because it would make the declared grammar unreliable.

### Normalize column spans at the component boundary

`<cw-column span="x">` accepts decimal integers from 1 through 12 and defaults to 12 when absent.
Invalid values produce a bounded diagnostic and use span 12 without reflecting invented authored input.
A twelve-track CSS grid applies the span at wide widths and stacks every column at the established responsive breakpoint.

Relying on host CSS classes was rejected because span semantics belong to the shared component contract.

### Keep tab state local, accessible, and deterministic

The first enabled tab is selected initially unless one direct tab has `selected`.
Exactly one tab may be selected; selection synchronizes tab buttons, panel `hidden`, `aria-selected`, `tabindex`, and `aria-controls`.
Click, ArrowLeft/ArrowRight, Home, and End update selection and focus, with RTL-aware horizontal direction.
Removal or insertion selects the first available current tab when necessary.

A host router or Vue state dependency was rejected because tabs are local presentation state with no domain or route authority.

### Derive metadata from the authoritative effective grid

`<cw-metadata>` extends the ordinary object-context consumer and registers the existing layout requirement.
For each current context generation it obtains the schema description and authorized effective-grid URL from the context, loads that same-origin XML through `loadStructuralResource`, parses it with `parseCausewayGridXml`, and extracts the normalized fieldset whose id is exactly `metadata`.
An object-layout helper returns ordered metadata property and action member nodes without exposing raw XML.
The component renders ordinary `<cw-property>` descendants in the fieldset body and ordinary `<cw-action>` descendants inside a native details/summary panel dropdown labelled **Actions**.
Those descendants independently register their usual requirements, so hidden, disabled, validation, invocation, and authorization semantics remain canonical.

Hard-coded property ids such as `id` and `version` were rejected because applications may add metadata properties and framework mixins may contribute metadata actions.
Re-querying or interpreting annotations in the browser was rejected because the effective grid is authoritative.
Rendering all described object members was rejected because only exact metadata-fieldset membership is in scope.

### Fail closed and retire asynchronous metadata work

If no context, effective grid, usable metadata fieldset, or safe structural resource exists, `<cw-metadata>` renders a bounded empty/error status and invents no members.
Abort controllers and generation checks prevent disconnected or superseded work from mutating a newer context.
Structural-resource CSP, same-origin, byte, redirect, media-type, and credential policy remains unchanged.

### Demonstrate host equivalence without transferring ownership

HTMX and Vue Petclinic exact pages will use representative declarative rows, columns, fieldsets, tabs, and metadata panels while retaining equivalent information architecture.
Both hosts author only element structure; all component behavior remains in foundation.
Browser tests cover wide/narrow spans, keyboard tabs, metadata membership, dropdown actions, and host parity.

## Risks / Trade-offs

- [Effective-grid parsing is repeated when `<cw-object>` and `<cw-metadata>` coexist] → Object-context structural-resource caching/coalescing remains the optimization boundary; the component does not add an independent client.
- [Malformed child markup could remain connected while hidden] → Emit a bounded diagnostic and exclude it from layout/tab coordination without altering valid siblings.
- [Metadata actions inside a closed dropdown remain connected] → Native `details` removes them from sequential focus while closed; ordinary action authorization still controls visibility and usability.
- [Automatic metadata rendering can duplicate explicitly authored members elsewhere] → Document that `<cw-metadata>` owns the effective metadata fieldset; the framework does not infer or rewrite unrelated authored declarations.
- [Responsive custom elements could alter sample geometry] → Reuse established breakpoints and verify both wide and narrow HTMX/Vue acceptance invariants.
