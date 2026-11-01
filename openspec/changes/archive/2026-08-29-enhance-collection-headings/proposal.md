## Why

Collection cards currently cannot express application-authored names and descriptions using the domain vocabulary requested by HTML page authors, and their visible unmodifiable reasons add noise without helping users interact with read-only collection contents.
Collection headings should consistently present either canonical metamodel text or explicit HTML overrides, while Petclinic demonstrates both paths selectively.

## What Changes

- Add `named` and `described-as` attributes to `<cw-collection>` for application-authored heading overrides.
- Load canonical collection `friendlyName` and `description` metadata through the existing targeted object context.
- Resolve collection heading text with explicit HTML overrides taking precedence over canonical metadata and safe fallbacks.
- Render a non-duplicate collection description directly below its label using subdued, smaller typography.
- Stop rendering collection-level disabled or unmodifiable reasons as visible labels or tooltips while retaining read-only collection behavior.
- Add selective Petclinic collection names and descriptions, using both `@CollectionLayout(describedAs)` and HTML `described-as` examples.
- Add component, context, GraphQL integration, Petclinic integration, and browser regression coverage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Extend collection heading semantics, HTML overrides, description presentation, and quiet read-only behavior.
- `graphql-web-component-context`: Select supported canonical collection heading metadata in targeted member reads.
- `generic-htmx-web-component-viewer`: Demonstrate and verify selective collection names and descriptions in Petclinic.

## Impact

Affected code is limited to the framework-owned collection component and styles, targeted GraphQL selection state, existing test fixtures, and Petclinic domain/page examples.
The change adds no dependency, route, toolkit, persistence, or shared application-shell behavior.
Existing `label` markup remains compatible, while `named` is the preferred collection-name override.
