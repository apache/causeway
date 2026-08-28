## Context

The foundation library currently registers seventeen Causeway-owned custom elements under `causeway-*` names.
Those names appear in public authored markup, generated component markup, CSS type selectors, JavaScript selectors and local-name checks, server-rendered HTMX HTML, application custom pages, samples, tests, and documentation.
The same textual prefix also appears in contracts that are not element names, including semantic events, CSS classes, data attributes, CSS variables, and asset paths.
A safe migration must change only the custom-element namespace and references that identify those elements.

## Goals / Non-Goals

**Goals:**

- Make `cw-` the sole registered namespace for every Causeway-owned custom element.
- Update every producer and consumer of those tag names across foundation, HTMX, samples, tests, and documentation.
- Preserve behavior, element classes, attributes, events, styling hooks, routes, and assets.
- Provide a deterministic audit that catches stale old tags without rejecting intentionally retained non-element `causeway-*` contracts.

**Non-Goals:**

- Rename JavaScript or Java classes, packages, modules, Maven artifacts, or exported `Causeway*` symbols.
- Rename semantic events, CSS classes, `data-causeway-*` attributes, `--causeway-*` variables, test IDs, or `/causeway-*` resource paths.
- Register deprecated `causeway-*` aliases.
- Change GraphQL, HTMX routing, component behavior, or visual design.

## Decisions

Update the authoritative element-name constants and field-editor constant to these registrations:

[source,text]
----
cw-graphql-client
cw-object-context
cw-object
cw-object-header
cw-property
cw-value
cw-object-link
cw-action
cw-interaction-controller
cw-field-editor
cw-reference-editor
cw-collection
cw-collection-column
cw-menubars
cw-menubar-primary
cw-menubar-secondary
cw-menubar-tertiary
----

Keep the existing `CausewayElementName`, `Causeway*Element`, and `defineCausewayWebComponents` JavaScript API names.
They describe the project and JavaScript types rather than HTML namespace verbosity and changing them would create unrelated API churn.

Do not define aliases for old names.
The browser custom-element registry does not permit one constructor to be registered under multiple names without wrapper constructors, and alias wrappers would retain two public vocabularies, complicate selectors, lifecycle expectations, documentation, and removal timing.
This change is explicitly breaking and provides a direct mechanical migration from `<causeway-x>` to `<cw-x>`.

Migrate tag references by syntactic role rather than globally replacing the text prefix.
Change registration values, opening and closing tags, custom-element type selectors, element-name selectors, `localName` comparisons, and generated markup.
Retain class selectors such as `.causeway-property`, events such as `causeway-property-updated`, attributes such as `data-causeway-action`, variables such as `--causeway-space-2`, and paths such as `/causeway-webcomponents/`.

Add an audit over source, sample, test, and documentation files that detects stale `<causeway-*` opening or closing tags and quoted element-name literals from the known old registration set.
The audit must not flag unrelated retained Causeway contracts.

An unrestricted global replacement was rejected because it would silently rename stable non-element contracts.
Compatibility aliases were rejected because they would double the public vocabulary and make application migration indefinite.
Renaming only authored HTML was rejected because generated markup, CSS type selectors, and selectors would then disagree with registered names.

## Risks / Trade-offs

- [Risk] Existing application markup stops upgrading because old elements remain unknown HTML elements. → Mark the change breaking, document the mechanical migration, and update all repository samples and tests atomically.
- [Risk] A stale selector or CSS type selector silently stops matching. → Centralize registration names, run a known-old-name audit, and execute complete Node, Java integration, and Playwright suites.
- [Risk] Broad replacement corrupts events, classes, attributes, variables, or paths. → Use a known element-name map and post-change assertions that retained non-element contracts still use `causeway-*`.
- [Risk] Third-party authored custom pages are not updated automatically. → Document the one-to-one tag migration and require application source changes when adopting this breaking release.
- [Risk] Internal Vaadin elements could be renamed accidentally. → Restrict the map to the seventeen Causeway-owned registrations.

## Migration Plan

1. Replace every application opening and closing tag from `<causeway-X>` to `<cw-X>` using the one-to-one map.
2. Replace JavaScript and CSS selectors that identify those element names.
3. Leave classes, events, data attributes, CSS variables, and asset URLs unchanged.
4. Upgrade the Causeway Web Components artifacts and deploy the markup changes atomically.
5. Run application browser acceptance to catch custom pages or selectors outside the repository.

Rollback requires restoring the prior artifacts and application markup together.
Mixed old and new element vocabularies are unsupported.

## Open Questions

None.
