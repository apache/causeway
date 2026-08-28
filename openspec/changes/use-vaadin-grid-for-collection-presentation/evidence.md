# Implementation evidence

## Pre-implementation collection baseline

The implementation baseline is planning commit `f572e07df86` on top of archived member-presentation commit `c6465fe82e8` and archive commit `b2ac18b083c`.
An inactive `<cw-collection>` registers metadata and renders one native activation button without requesting rows.
An active collection requests one bounded window through the object-context controller, aborts its prior single load when superseded, rejects stale revision or disconnected results, creates hydrated row contexts, and publishes one semantic collection-state snapshot.
A collection with declared columns renders one native light-DOM table with a leading semantic object link and Causeway-rendered cells.
A collection without declared columns renders one native list of semantic object links.
Empty, loading, disabled, hidden, partial-error, terminal-error, row-relative error, associated-action, and disconnect behavior remain Causeway-owned.
Current collection rendering does not expose Grid, a data provider, an application sorting or filtering contract, or persistence query pushdown.
The bounded GraphQL `window` operation reports requested offset and size, returned count, configured maximum, previous and next availability, nullable total, ordering classification, rows, partial errors, and row selection.
The current server may materialize the complete domain collection before ordering and slicing, and this remains outside Grid semantics.

The existing foundation, vanilla, Petclinic, and Reference Application suites establish keyboard-operable object links and actions, semantic labels and descriptions, route-generation cancellation, dark theme, reduced motion, forced colors, responsive containment, strict exact-hash CSP, zero unexpected external requests, and no page overflow.
The pre-change default toolkit may request qualified reference, field, and action closures, while collection routes request no Grid asset because no Grid closure exists.
Explicit native mode requests no Vaadin closure and contributes no Vaadin style hash.

## Accepted Grid qualification matrix

| Condition | Presentation | Loading model |
|---|---|---|
| Active, visible, wider than 48rem, bounded window, deterministic ordering, safe stable total, supported columns and renderers | Virtual Grid | Grid callbacks map to bounded Causeway ranges using only the reported total. |
| Active, visible, wider than 48rem, bounded window, deterministic ordering, total unavailable, supported columns and renderers | Bounded Grid | Current bounded window plus Causeway-owned previous and next controls. |
| Safe zero total or terminal empty first window | Native accessible empty state | No Grid installation or speculative request. |
| At or below 48rem | Native responsive list or table presentation | Existing current window only. |
| Window operation absent | Native backward-compatible presentation | Existing collection loading only. |
| Encounter or unstable ordering | Native presentation | No speculative cross-window Grid request. |
| Unsupported column, renderer, interaction, policy, lifecycle, module, definition, CSP, or family state | Native presentation | Current authoritative Causeway state and failure boundary. |
| Explicit `component-toolkit=native` | Native presentation | Zero Grid bytes and hashes. |

Supported cells retain Causeway object links and value-renderer output for standard scalars, exact and machine numbers, local and excluded temporal values, Boolean, enum, null, references, resources, hidden state, disabled reasons, row-relative errors, unsupported values, and explicit application renderers where recycling remains lifecycle-safe.
A collection falls back wholly rather than mixing approximate Grid cells when one accepted renderer cannot preserve semantics.
Sorting, filtering, selection, editing, details, drag, resizing, reordering, row actions, Grid Pro, and direct Grid navigation remain excluded.
Associated `<cw-action>` children remain Causeway siblings outside Grid.

## Deterministic qualification targets

The vanilla sample retains `relatedObjects` as a populated declarative-column collection and `emptyRelatedObjects` as the empty classification.
Petclinic retains `petclinic.PetOwner#pets` and `petclinic.PetOwner#visits` as custom-page declarative collections with references, temporal and text cells, empty and populated states, paging metadata, associated actions, route navigation, and narrow responsive journeys.
The Reference Application catalogue retains `demo_CollectionLayoutPagedPage#children` for paged columns, versionless rows, refresh, stale partial results, repeated ranges, and membership changes.
It retains `demo_CollectionLayoutPagedPage#moreChildren` for empty and populated outcomes.
It retains `demo_CollectionTypeOfPage#children` for polymorphic rows and `demo_CollectionTypeOfPage#otherChildren` for associated actions and error behavior.
The `entities` collections under wrapper Boolean, BigDecimal, BigInteger, local temporal, offset and zoned temporal, enum, URL, Password, Blob, and Clob pages provide deterministic semantic cell families and exclusions.
Composite-value and application-renderer fixtures provide custom and unsupported classifications.

## Accepted budgets and security boundary

The Grid closure gzip cap is 196608 bytes.
The previously accepted all-closure aggregate cap is 336896 bytes, and the Grid-inclusive aggregate cap is 533504 bytes.
An unaffected, inactive-only, narrow-only, unqualified, former-pilot, or explicit-native route has a zero-byte Grid budget.
The Grid closure must use pinned free-core inputs, deterministic output, same-origin delivery, exact generated style hashes, `style-src-attr 'none'`, no `unsafe-inline`, no external request, and zero reviewed production vulnerabilities.
No Flow, Binder, server-side Vaadin state, Grid Pro, commercial package, Menu Bar, telemetry, CDN content, external asset, public data provider, or persistence pushdown is eligible.
Failure events and diagnostics may contain family, phase, bounded classification, and revision only and must exclude row values, protected values, GraphQL variables, serialized snapshots, and full server errors.

## Accepted closure and implementation checkpoints

The pinned free-core Grid closure uses `@vaadin/grid` 25.2.8, esbuild 0.27.4, Playwright 1.61.0, and axe-core 4.10.3.
Two regeneration runs produced byte-identical `vaadin-grid.js` output with SHA-256 `33af4e23100ca0c6af7bb2fd492077c7e4973584cf389c177549fa89b93a9441`.
The accepted closure is 187581 raw bytes and 50558 gzip bytes, below the 196608-byte Grid cap.
The generated transitive inventory contains 18 packages and the production npm audit reports zero vulnerabilities.
Representative empty, populated, virtual, bounded, error, focus, theme, and forced-colors states require the one exact style source `sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=`.
The enforcing closure browser audit recorded two provider calls, 24 rendered cells, zero CSP violations, zero axe violations, zero console errors, zero page errors, zero external requests, and no overflow.
Ordinary Maven validation verifies and packages the checked-in asset and legal metadata without invoking npm, while `regenerate-vaadin-grid-assets` remains explicit.

The foundation now owns immutable qualification and semantic projection descriptors, a three-request and six-window bounded range broker, capped retained rows and errors, independent request keys, stale-generation guards, exact-once context cleanup, container-aware 48rem switching, semantic focus identity, bounded paging, and family-scoped recovery.
The initial accepted window seeds the broker without a duplicate request, identical cached and in-flight ranges deduplicate, overlapping ranges remain independently cancellable, and object, member, column, renderer, policy, width, refresh, or disconnect changes retire obsolete work.
The foundation Node suite passes 241 tests after the initial Grid integration checkpoint.
The targeted Grid closure verification and browser audit pass.
The foundation-plus-HTMX Maven reactor tests pass under Java 21, and the packaged Grid asset retains the accepted checksum.
Source audits find no application-facing raw Grid markup, data provider, renderer callback, persistence query, or enabled sorting, filtering, selection, editing, details, resizing, reordering, or drag API.

## Final qualification results

The final foundation Node suite passes 243 of 243 tests with one-at-a-time execution.
The complete Web Components Maven reactor passes under Java 21, including foundation, HTMX, security, samples, packaging, and generated-asset verification.
Reference, field, action, and Grid deterministic verification passes, and every production npm audit reports zero vulnerabilities.
The vanilla sample passes Maven integration and manual browser checks for virtual, bounded unavailable-total, empty, disabled, narrow, encounter-ordered, renderer-failure, explicit-native, zero-external-request, and zero-overflow states.
The Petclinic Playwright suite passes 4 of 4 tests in default mode and 4 of 4 tests in explicit-native mode.
The Reference Application Playwright suite passes 13 of 13 tests in default mode and 13 of 13 tests in explicit-native mode.
The Reference Application clean package, integration, capability inventory, overlapping-range, unavailable-total, membership-refresh, route-replacement, and family-failure gates pass.
Clean and incremental capability inventory resources are byte-identical with SHA-256 `2c2a629db29693f59531de5f2559e8fb756687b45448fc88bcdf9d88cb94112d`.
The reviewed capability inventory contains 4288 items with item hash `4b1e27305892e04d2ad848dfb137405fb456dc1a4742878f400792dcf19b3789`, zero viewer defects, and exactly two additive supported `ValueHolder` properties relative to the previous baseline.
Exact-hash CSP, `style-src-attr 'none'`, no `unsafe-inline`, dark, reduced-motion, forced-colors, keyboard, pointer, focus, paging, responsive switching, route isolation, family recovery, no external request, and no overflow gates pass.
Applicable Web Components and Reference Application RAT checks pass.
JavaScript syntax checks pass for every changed or added module, the production-isolation audit passes, strict OpenSpec validation passes for 38 items, and `git diff --check` passes.
IntelliJ compilation reports no problems for the modified HTMX controller, while the complete Maven builds provide the authoritative multi-module compilation gate.

Supported collection outcomes are virtual Grid for deterministic safe-total windows, bounded Grid with Causeway paging for deterministic unavailable-total windows, and complete native presentation for explicit native policy or any responsive, ordering, capability, semantic, lifecycle, or family disqualification.
Operational rollback remains `causeway.viewer.webcomponents.htmx.component-toolkit=native` and requires no GraphQL, route, persisted-data, or application-markup migration.
Persistence query pushdown, sorting, filtering, selection, editing, details, drag, resizing, reordering, Grid Pro, Flow, Binder, server-side Vaadin state, telemetry, CDN resources, Menu Bar, raw Grid APIs, and application-owned data providers remain excluded.
