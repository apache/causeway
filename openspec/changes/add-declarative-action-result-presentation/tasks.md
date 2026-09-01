## 1. Authoritative Action Result Metadata

- [ ] 1.1 Add nullable action-only `resultElementLogicalTypeName` to `RichMemberMetadata` using static metamodel collection element semantics.
- [ ] 1.2 Extend rich GraphQL metadata unit, schema-growth, targeted-introspection, authorization-safety, and end-to-end compatibility coverage.
- [ ] 1.3 Request supported result-element metadata for object and service actions and carry it through normalized action presentation without breaking older schemas.

## 2. Collection Presentation Resources

- [ ] 2.1 Add bounded classpath discovery for `META-INF/causeway/webcomponents/collections/*.html` with logical-type naming, UTF-8, count, size, duplicate, cache, and reload protections.
- [ ] 2.2 Add a collection-presentation registry and reserved same-origin resolution route that cannot collide with object routing or expose arbitrary classpath resources.
- [ ] 2.3 Parse resolved fragments inertly into bounded standalone attributes and direct-child column configuration while rejecting unsupported roots, elements, executable markup, and malformed declarations.
- [ ] 2.4 Add loader, registry, route, cache/reload, malformed-resource, duplicate, absence, and traversal-safety tests.

## 3. Declarative Action Result Presentation

- [ ] 3.1 Extend `<cw-action>` parser-safe declaration capture to preserve at most one direct-child `<cw-standalone-collection>` and its column nodes across registration and rerenders.
- [ ] 3.2 Keep nested declarations hidden and inert, normalize immutable inline presentation snapshots, and expose deterministic duplicate or inapplicable diagnostics.
- [ ] 3.3 Implement precedence across valid inline declarations, type-default resources, and generic presentation with generation-safe asynchronous resolution.
- [ ] 3.4 Add Foundation tests for pre-upgrade capture, node identity, zero-column overrides, complete replacement rather than merging, invalid declarations, repeated invocation, and superseded resolution.

## 4. Original Invocation Result Projection

- [ ] 4.1 Refactor the existing collection-row GraphQL selection builder for safe reuse by object and service action invocation planning.
- [ ] 4.2 Pass bounded resolved column selection into `invokeAction`, merge authoritative `_meta` identity and supported property wrappers, and omit unsupported or incompatible columns diagnostically.
- [ ] 4.3 Publish the exact immutable resolved presentation snapshot additively with successful normalized action-result detail.
- [ ] 4.4 Add object-action and service-action tests proving selected columns arrive in the single invocation response, empty results use declared type, and rejected columns cause no hydration or follow-up GraphQL request.

## 5. Generic Action Result Outlet

- [ ] 5.1 Add, register, export, style, and document the passive accessible `<cw-action-results>` public component.
- [ ] 5.2 Replace the shell result boundary with the shared outlet contract while retaining a deterministic stable-shell fallback.
- [ ] 5.3 Snapshot one unique active-route outlet and navigation generation when interaction begins, and fall back safely for absence, duplicates, disconnection, or supersession.
- [ ] 5.4 Route collection, scalar, and void-status presentation into the current resolved destination while preserving object navigation, application result claims, announcements, focus, dismissal, refresh, and missing-object recovery.
- [ ] 5.5 Rehome preserved current void status after route refresh and retire stale outlet nodes, Grid state, and asynchronous fragment work on replacement.
- [ ] 5.6 Add Foundation and HTMX tests for passive behavior, accessibility, unique and duplicate outlet resolution, shell fallback, application overrides, route replacement, service-action completion, result replacement, and preserved void refresh.

## 6. Application Demonstration and Qualification

- [ ] 6.1 Add a default PetOwner collection presentation resource and an authored page outlet demonstrating reusable type-default columns.
- [ ] 6.2 Add or adapt a same-element-type action with a nested standalone override proving heading and column replacement rather than default merging.
- [ ] 6.3 Extend Petclinic integration and Playwright journeys to verify original GraphQL selection, empty and populated results, links, icons, announcements, outlet placement, replacement, fallback, and canonical navigation.
- [ ] 6.4 Run equivalent default and inline journeys under Vaadin and explicit native toolkit policies with no follow-up row request or raw application result list.
- [ ] 6.5 Run Foundation Node and Maven suites, GraphQL and HTMX reactor tests, browser CSP/axe/console/network/overflow audits, Petclinic integration and Playwright tests, IDE build/lint, strict OpenSpec validation, and `git diff --check` using Java 21 where the complete reactor requires it.
