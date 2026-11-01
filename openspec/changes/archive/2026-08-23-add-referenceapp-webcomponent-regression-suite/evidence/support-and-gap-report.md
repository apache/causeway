# Initial Reference Application support and gap report

## Reviewed baseline

The regression corpus is copied from `apache/causeway-app-referenceapp` revision `29b43bfe4f77d525fb345394e5a52bd7d85a91ba`.
The retained boundary contains 1,900 upstream files and 6,007,107 bytes before local build, launcher, inventory, test, and report files.
The corpus preserves the upstream `demoapp` packages and is protected by deterministic per-file checksums.
The launcher combines the copied JPA application, rich GraphQL viewer, generic HTMX viewer, and Wicket comparison viewer over one deterministic fixture.

## Capability inventory

The reviewed rich-schema inventory contains 4,286 uniquely classified items.

| Classification | Count |
| --- | ---: |
| `SUPPORTED` | 3,354 |
| `GRACEFUL_UNSUPPORTED` | 210 |
| `GRAPHQL_GAP` | 0 |
| `VIEWER_DEFECT` | 67 |
| `VIEWER_SPECIFIC` | 1 |
| `NOT_EXERCISED` | 654 |

The zero `GRAPHQL_GAP` count reflects the current identifier-based classifier and does not mean that runtime GraphQL gaps were absent.
Runtime journeys exposed contract combinations that introspection-item classification alone cannot infer, and those gaps are recorded below.
The 654 `NOT_EXERCISED` items remain bounded by checked-in reasons and representative substitutes rather than being treated as support.
Four of those items normalize conditionally advertised abstract `demo.ValueHolder` members so clean and incremental metamodel builds produce one reviewed inventory.

## Representative supported behavior

Application startup builds the copied JPA metamodel and advertises more than 9,000 GraphQL introspection types.
The secured Wicket viewer accepts the deterministic `sven` fixture account and shares object identity with the HTMX and GraphQL routes.
The generic viewer renders the application home, service menus, canonical object routes, history transitions, ordinary scalar properties, editable controls, cancellation, deterministic validation errors, tabs, and responsive shells.
Boolean, enum, local-date, offset-date-time, zoned-date-time, Blob, Clob, decimal, integer, reference, and custom-value families have stable checked-in targets.
A Java string property mutation succeeds through the public rich GraphQL mutation and is restored in a `finally` block.
The same mutation test runs twice against one application context to prove order independence.

## Graceful and classified limitations

Unsupported value semantics remain visible rather than being silently coerced into successful-looking values.
The bounded Reference Application target catalogue is validated against the effective rich schema on every inventory test run.
Inactive collection tabs remain idle until activated.
Configured collection layouts, hidden property members, disabled controls, and partial collection outcomes are asserted explicitly.
Wicket-only panels and UI extensions are classified as viewer-specific and are not copied into the viewer-neutral domain module.

## Runtime GraphQL and viewer gaps

### Missing version metadata on view models and view-model rows

The generic viewer requests `_meta.version` while preparing several view-model actions and collection rows.
Rich GraphQL metadata types such as `DependentArgsDemoItem`, `ActionSemanticsVm`, and `CollectionLayoutPagedChildVm` do not advertise that field.
The result is a visible action error or a collection `partial-error` or `error` state rather than a usable editor or row window.
This blocks representative choices, autocomplete, parameterless actions, successful parameterized invocation, and some polymorphic collections.

### Service and object action invocation mismatch

Parameterless service actions and valid parameterized object actions can end with the visible message `Action invocation failed` or the missing-version error.
The effective schema exposes safe nested invocation and flat mutation shapes, but the semantic interaction controller does not dispatch every advertised shape correctly.
The suite retains the actions and asserts the visible failure instead of deleting them from the menu or counting them as support.

### Polymorphic union metadata mismatch

The generic collection projection requests `_meta` directly from `rich__demo_ValueHolder__gqlv_union`.
That union does not define `_meta`, so affected property and collection pages return GraphQL validation errors.
The errors are bounded as known gaps and the corresponding components remain visibly failed.

### Value scalar declaration and runtime coercion mismatch

The mutation `demo_BigDecimalEntity__readWriteProperty` advertises a GraphQL `String` argument.
Supplying the advertised string reaches a runtime value-type check that requires `java.math.BigDecimal` and fails with an incompatible-type exception.
The suite therefore uses a Java string entity for restoration testing and records decimal mutation as a GraphQL/value-semantics correctness gap.

### Composite view-model route rejection

The composite-value action returns a valid rich object identity with a long opaque identifier.
Navigating to the corresponding generic route produces the visible `invalid-route` state.
The corpus and action remain present so route parsing can be fixed by focused viewer work.

### Reference autocomplete remains bounded

The current public operation supports bounded non-paged candidate retrieval only.
The regression launcher retains the 50-result maximum and does not imply server paging, total counts, or unbounded search.

## Content and application exceptions

The copied menu model advertises invalid action references from retained source families.
The application structural metadata reports those references through `INVALID_ACTION_REFERENCE` issues.
Two retained framework-extension grids also contain metadata errors.
These are upstream content exceptions and remain separate from generic-viewer defects.

## Browser outcomes

Targeted headless journeys pass for menus, canonical routes, history, property editing and cancellation, invalid validation, representative value families, collection partial errors, route replacement, Wicket comparison, and deterministic mutation restoration.
The browser harness rejects unclassified GraphQL errors, console errors, page errors, CSP violations, failed non-GraphQL requests, external requests, overflow, and leaked Vaadin overlays.
Known GraphQL gaps are matched narrowly by field and affected metadata type and are counted separately from unexpected failures.
Light, dark, narrow, reduced-motion, and forced-colors modes retain semantic accessibility checks.
The final browser profile completed 9 test invocations with zero failures, errors, or skips in 23.06 seconds of test time and 33.69 seconds of Maven wall time.

## Reactor and package cost

The measured pre-copy clean regression package baseline was 19.27 seconds.
The initial like-for-like post-change clean package measurement was approximately 27.09 seconds.
The initial like-for-like post-change incremental package measurement was approximately 17.72 seconds.
The final Reference Application clean package with all accepted validation was 30.84 seconds.
The complete 19-module regression-test reactor clean package, including its existing test workload, completed in 111.71 seconds.
No production viewer source file was changed.
Ordinary launcher packaging disables the inherited Spring Boot repackage execution and produces a 23,909-byte lightweight JAR.
The optional `package-referenceapp-htmx` profile remains available when an executable launcher is specifically required and produced a verified 333,618,156-byte executable JAR.

## Acceptance

This baseline accepts the copied corpus, deterministic inventory, representative successful behavior, and explicitly classified gaps together.
It does not treat discovered correctness or scalability defects as fixed.
Remediation is assigned to separately scoped follow-on changes in `follow-on-recommendations.md`.
