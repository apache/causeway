## Why

The PetOwner pages still use application-specific grid, details, collections, and card wrappers around layout that `<cw-row>`, `<cw-column>`, `<cw-fieldset>`, and `<cw-collection>` can represent directly.
This obscures the intended declarative layout model and leaves HTMX and Vue examples carrying avoidable markup and CSS.

## What Changes

- Allow `<cw-column>` to contain a direct `<cw-tabgroup>` in addition to fieldsets, collections, and metadata, matching practical column composition and the conceptual `layout.xml` hierarchy.
- Replace the PetOwner macro grid and its details and collections wrapper divs with one `<cw-row>` containing `span="4"` and `span="8"` columns.
- Remove redundant full-width row/column wrappers around non-tabbed fieldsets.
- Remove application `<section>` card wrappers where `<cw-collection>` already supplies the semantic collection section and heading.
- Represent Agreement as a named `<cw-fieldset>` containing the agreement property.
- Preserve the Identity/Metadata tabs, authoritative member behavior, collection actions and previews, PDF reader, responsive order, two-column ratio, application shell, context, routing, and HTMX/Vue parity.
- Reduce Petclinic-specific CSS and tests to the behavior that is not already owned by foundation layout components.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Permit a tab group as a valid direct column child while retaining strict fail-closed layout grammar.
- `generic-htmx-web-component-viewer`: Simplify the HTMX PetOwner page to use foundation layout components for its macro layout and semantic collection panels.
- `generic-vue-web-component-viewer`: Apply the same simplified declarative PetOwner structure without Vue-owned layout behavior.

## Impact

The change affects foundation column validation and tests, shared layout documentation, HTMX and Vue PetOwner declarations and acceptance tests, Petclinic application CSS, and generated Vue assets.
It adds no GraphQL fields, domain behavior, host-framework state, dependency, or compatibility alias.
