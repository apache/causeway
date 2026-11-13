## Why

Causeway already models an object's navigable parent through `@PropertyLayout(navigable = Navigable.PARENT)`, but the rich GraphQL and web-component stack does not expose that hierarchy to custom pages.
Applications therefore cannot provide consistent, metadata-driven breadcrumbs without duplicating parent relationships and navigation logic in application-specific code.

## What Changes

- Add a bounded rich GraphQL metadata projection for the current object's navigable ancestor chain, derived from the existing navigable-parent facet.
- Add a semantic breadcrumbs read requirement to the shared object context so descendant components do not construct GraphQL selections.
- Introduce the framework-neutral `<cw-breadcrumbs>` custom element with accessible root-to-current navigation and standard semantic navigation events.
- Bound ancestor traversal, detect cycles, omit unavailable identities safely, and preserve GraphQL authorization and partial-error behavior.
- Add stable component names, classes, styling, documentation, fixtures, and automated tests.
- Mark Petclinic's `Pet.petOwner` and `Visit.pet` properties as navigable parents and compose `<cw-breadcrumbs>` into its HTML-authored pages.
- Extend Petclinic integration and Playwright coverage for owner → pet → visit breadcrumb hierarchy and navigation.

## Capabilities

### New Capabilities

- `rich-graphql-navigable-breadcrumbs`: Expose a bounded root-to-parent identity and title chain from Causeway's navigable-parent facet through rich object metadata.

### Modified Capabilities

- `graphql-web-component-context`: Add a semantic breadcrumbs requirement to coordinated rich object reads.
- `domain-web-components`: Add the accessible framework-neutral `<cw-breadcrumbs>` component and its semantic navigation behavior.
- `generic-htmx-web-component-viewer`: Demonstrate metadata-driven breadcrumbs and canonical navigation in the Petclinic HTML-authored pages.

## Impact

The change affects rich GraphQL common object metadata and tests, web-component schema discovery, object-context projection and component registration, component styles and documentation, package fixtures and browser tests, Petclinic domain annotations and HTML pages, HTMX route navigation coverage, and the generated GraphQL contract.
No new third-party dependency, route format, application-specific breadcrumb API, or direct browser access to metamodel services is introduced.
