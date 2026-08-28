# reference-application-viewer-regression-suite Specification

## Purpose
Provide a pinned, viewer-neutral Apache Causeway Reference Application corpus and deterministic GraphQL, HTMX, Wicket, inventory, and browser qualification baseline for broad semantic viewer regression testing.
## Requirements
### Requirement: Pinned Reference Application corpus
The regression suite SHALL retain a repository-local copy of the approved Apache Causeway Reference Application source corpus at an exact upstream revision.
The copy MUST preserve Apache licensing, package structure, source comparability, provenance, included-path and omitted-path records, and deterministic file checksums.

#### Scenario: Maintainer verifies the copied corpus
- **WHEN** the provenance verification runs without network access
- **THEN** every retained file matches the reviewed manifest and checksum for the pinned revision
- **AND** the report identifies the upstream repository, revision, copied paths, omissions, and local adaptation boundaries

#### Scenario: Copied source drifts
- **WHEN** a retained source or resource changes without an accompanying reviewed provenance update
- **THEN** verification fails with the changed path identified
- **AND** ordinary builds do not silently fetch or substitute upstream content

#### Scenario: Upstream refresh is proposed
- **WHEN** a maintainer intentionally refreshes the copied corpus
- **THEN** the revision, manifests, checksums, licensing review, metamodel baseline, inventory, and affected browser journeys are reviewed together
- **AND** unrelated upstream deployment or viewer-specific content is not imported implicitly

### Requirement: Reusable viewer-neutral regression domain
The copied domain, deterministic fixture support, target catalogue, and inventory schema SHALL remain independent of any one presentation viewer.
Viewer-specific launchers and browser drivers MUST depend on the neutral regression modules rather than embedding private copies of the domain.

#### Scenario: A viewer launcher uses the corpus
- **WHEN** HTMX, Wicket, or a future viewer starts a Reference Application regression runtime
- **THEN** it uses the same effective metamodel, JPA fixture identities, application entries, menus, grids, and semantic target catalogue
- **AND** viewer-specific routing, rendering, and lifecycle code remains outside the neutral modules

#### Scenario: Regression modules are packaged
- **WHEN** the reactor builds the Reference Application modules
- **THEN** they are marked as non-release regression fixtures
- **AND** no production viewer module gains a runtime dependency on copied application code

### Requirement: Deterministic metamodel and GraphQL runtime
The suite SHALL compile and boot the retained corpus through JPA and the rich GraphQL viewer with deterministic fixtures and stable application entry points.
Startup checks MUST validate the effective metamodel, public rich schema, structural menu and grid resources, representative identities, and absence of undisclosed terminal errors.

#### Scenario: Ordinary regression build runs
- **WHEN** the applicable reactor test phase runs without a browser profile
- **THEN** the copied corpus compiles and its provenance, metamodel, GraphQL schema, structural resources, and fixture baseline are verified
- **AND** no browser download or network retrieval of upstream source is required

#### Scenario: Advertised schema cannot be built
- **WHEN** a retained domain feature causes metamodel or GraphQL schema construction to fail
- **THEN** the suite reports the responsible feature and failure boundary
- **AND** the source is not deleted or excluded merely to make the baseline appear successful

### Requirement: Complete capability inventory
The suite SHALL generate a deterministic machine-readable inventory of in-scope Reference Application objects, members, input and output value shapes, operation capabilities, and structural resources.
Every discovered item MUST receive exactly one reviewed support classification and no sensitive value or implementation exception detail may enter the report.

#### Scenario: Inventory baseline matches
- **WHEN** metamodel and GraphQL discovery run against the pinned fixture
- **THEN** stable identifiers and counts match the checked-in reviewed baseline
- **AND** each item is classified as `SUPPORTED`, `GRACEFUL_UNSUPPORTED`, `GRAPHQL_GAP`, `VIEWER_DEFECT`, `VIEWER_SPECIFIC`, or reasoned `NOT_EXERCISED`

#### Scenario: Capability changes unexpectedly
- **WHEN** a member, value shape, operation, menu entry, grid reference, classification, or stable fixture target is added, removed, or changed
- **THEN** verification fails with a bounded semantic diff
- **AND** acceptance requires an explicit baseline and journey review

#### Scenario: Unsupported capability is encountered
- **WHEN** the public schema cannot express a feature or a semantic viewer intentionally does not support it
- **THEN** the inventory records the applicable non-success classification and safe user-visible behavior
- **AND** does not count a hidden failure, malformed control, lossy value, or browser error as support

### Requirement: Staged representative browser coverage
The suite SHALL provide opt-in headless browser journeys across representative Reference Application feature families without requiring exhaustive invocation of every action.
Journeys MUST cover ordinary, invalid, disabled, cancelled, superseded, and route-replaced states where the public capability exists.

#### Scenario: Broad HTMX journey runs
- **WHEN** the Reference Application browser profile executes
- **THEN** it covers application and service menus, generic objects and grids, properties, actions, references, choices, autocomplete, defaults, validation, values, collections, navigation, and semantic results
- **AND** it fails on unexpected GraphQL failures, console or page errors, CSP violations, external requests, lost focus, residual overlays, stale updates, or page overflow

#### Scenario: Mutating behavior is exercised
- **WHEN** a journey invokes a mutating or destructive member
- **THEN** it uses disposable fixture data or restores deterministic state
- **AND** later journeys do not depend on execution order

#### Scenario: Heavy browser profile is inactive
- **WHEN** an ordinary reactor build runs
- **THEN** browser dependencies and browser binaries are not required
- **AND** non-browser provenance, compilation, schema, fixture, and inventory checks remain active

### Requirement: Cross-viewer semantic comparison
The first regression runtime SHALL expose Wicket and generic HTMX routes over the same metamodel, security context, persistence state, and deterministic fixture.
Comparison SHALL evaluate shared semantic outcomes and accessibility rather than requiring identical DOM, styling, or viewer-specific extensions.

#### Scenario: Representative object is compared
- **WHEN** automation opens the same authorized object and member family in Wicket and HTMX
- **THEN** visible identity, member availability, disabled reasons, accepted values, validation, and interaction outcomes are semantically consistent
- **AND** route, markup, theme, and lifecycle differences remain internal to each viewer

#### Scenario: Feature is viewer-specific
- **WHEN** the corpus contains a Wicket panel, extension UI, or another intentionally viewer-specific capability
- **THEN** the inventory classifies it as `VIEWER_SPECIFIC` with a representative semantic fallback where applicable
- **AND** the generic viewer is not failed for omitting unsupported private Wicket APIs

### Requirement: Gap-preserving acceptance report
The suite SHALL publish a bounded report of support, graceful limitations, GraphQL gaps, viewer defects, excluded viewer-specific features, build cost, and browser outcomes for the pinned corpus.
The report MUST distinguish discovery from remediation and MUST identify focused follow-on work rather than expanding this change without review.

#### Scenario: Initial baseline is accepted
- **WHEN** the pinned corpus, inventory, and representative journeys complete
- **THEN** the report records reproducible commands, reviewed classifications, measured reactor cost, and prioritized gaps
- **AND** hard correctness or scalability gaps are assigned to separately scoped changes

#### Scenario: Regression is introduced later
- **WHEN** a framework, GraphQL, viewer, dependency, or corpus update changes a previously accepted outcome
- **THEN** the suite identifies the semantic category and representative target that regressed
- **AND** a maintainer can reproduce it without the external Reference Application repository

### Requirement: Versionless identity regression coverage
The pinned Reference Application suite SHALL exercise representative concrete view models and collection rows whose rich metadata omits `version` while preserving stable semantic identity.
It MUST distinguish corrected versionless projection behavior from retained polymorphic-union and opaque-route gaps.

#### Scenario: Versionless preparation target is exercised
- **WHEN** the browser prepares representative action or property interaction semantics that return concrete versionless values
- **THEN** defaults, choices, autocomplete, validation, and successful submission remain usable without a missing-version GraphQL error
- **AND** invalid, cancelled, and stale interactions retain their established behavior

#### Scenario: Versionless property or action result is rendered
- **WHEN** a representative concrete view model is returned as a property value or typed action outcome
- **THEN** its advertised identity and title remain renderable and navigable through existing semantic contracts
- **AND** no `_meta.version` selection is submitted for its versionless metadata type

#### Scenario: Versionless collection row is rendered
- **WHEN** a representative concrete view-model collection is activated
- **THEN** its row window reaches the supported ready or documented partial state with stable identities and requested columns
- **AND** a missing version field is not the cause of any row error

#### Scenario: Unrelated retained gaps remain classified
- **WHEN** the same qualification run reaches an incompatible raw union row or a long opaque composite bookmark
- **THEN** the suite retains the corresponding bounded known-gap assertion until its focused change is implemented
- **AND** does not count it as evidence that concrete versionless identity failed

### Requirement: Polymorphic projection regression coverage
The pinned Reference Application suite SHALL distinguish valid concrete and union projection from a runtime object that is not a member of its advertised union.
It MUST preserve deterministic concrete-row evidence, bounded schema mismatch behavior, and the separate opaque-route gap.

#### Scenario: Declared type-of collection is activated
- **WHEN** the browser activates `demo.CollectionTypeOfPage.children`, whose `typeOf` metadata supplies the concrete row type
- **THEN** concrete `demo.CollectionTypeOfChildVm` rows render their semantic identities through the ordinary concrete projection
- **AND** union probing is not used for that collection

#### Scenario: Raw collection exposes incompatible runtime rows
- **WHEN** the companion raw collection is advertised as `rich__demo_ValueHolder__gqlv_union` but returns `demo.CollectionTypeOfChildVm`, which does not implement `demo.ValueHolder`
- **THEN** GraphQL and the viewer retain a bounded local schema/runtime mismatch
- **AND** the viewer does not manufacture union membership or an application-specific fragment

#### Scenario: Generated union membership is completed incrementally
- **WHEN** the Reference Application schema repeatedly discovers concrete `demo.ValueHolder` implementations
- **THEN** introspection advertises the deterministic merged set rather than only the first registration
- **AND** every advertised fragment name is a valid member of the completed union

#### Scenario: Invalid direct union metadata is rejected
- **WHEN** a reproduction selects `_meta` directly from `rich__demo_ValueHolder__gqlv_union`
- **THEN** GraphQL rejects that operation as invalid
- **AND** valid viewer planning uses typename and concrete fragments only

#### Scenario: Existing collection behavior remains stable
- **WHEN** the full collection journey also reaches concrete versionless rows, configured collections, lazy tabs, stale windows, partial errors, and route replacement
- **THEN** those established behaviors retain their passing assertions
- **AND** polymorphic planning does not make inactive collections eager

#### Scenario: Opaque composite route remains separate
- **WHEN** a projected concrete result carries the long opaque identifier retained by the corpus
- **THEN** the suite continues to assert the focused `invalid-route` gap
- **AND** does not attribute route rejection to union projection

### Requirement: Opaque bookmark route regression coverage
The pinned Reference Application suite SHALL exercise the authoritative long memento identifier returned for `demo.CompositeValuesPage` through the generic HTMX canonical route.
It MUST preserve the exact identifier, distinguish corrected routing from object-read failures, and retain unrelated bounded invalid-route behavior.

#### Scenario: Composite-values result is routed
- **WHEN** `demo.CompositeValueTypeMenu.compositeValueTypes` returns the `demo.CompositeValuesPage` identity
- **THEN** its exact opaque identifier produces a canonical HTMX object route within the supported encoded bound
- **AND** the route reaches ready or documented partial object state instead of `invalid-route`

#### Scenario: Composite-values route reconstructs identity
- **WHEN** the server renders the canonical composite-values route
- **THEN** the route object context contains the same logical type and byte-for-byte identifier returned by GraphQL
- **AND** representative composite value content remains visible through the authoritative object read

#### Scenario: Composite-values history is traversed
- **WHEN** navigation replaces the active object route with the composite-values route and the browser traverses back and forward
- **THEN** each history entry restores its exact canonical logical identity and route state
- **AND** stale work from the replaced route cannot overwrite the restored context

#### Scenario: Malformed route remains bounded
- **WHEN** the same qualification run requests malformed escapes, separators, controls, non-canonical encoding, or a segment beyond the supported encoded bound
- **THEN** the viewer retains its non-disclosing `invalid-route` presentation
- **AND** successful long opaque routing does not weaken route validation

### Requirement: Multi-window reference autocomplete regression coverage
The pinned Reference Application suite SHALL exercise deterministic rich autocomplete results larger than its configured qualification page for property or action-parameter reference selection.
It MUST cover GraphQL metadata, later-window selection, argument dependencies, cancellation, legacy compatibility, and toolkit-neutral outcomes.

#### Scenario: GraphQL autocomplete spans several windows
- **WHEN** a deterministic search returns more references than the configured page size
- **THEN** adjacent windows report accurate items, offsets, counts, totals, maximum, ordering, and continuation state
- **AND** their combined authoritative encounter order contains no omitted or duplicated position

#### Scenario: Invalid window is requested
- **WHEN** qualification requests a negative offset, non-positive size, or size above the configured maximum
- **THEN** GraphQL returns a bounded non-disclosing error
- **AND** existing legacy autocomplete remains executable

#### Scenario: Browser selects a later-window reference
- **WHEN** the reference editor searches and requests an item absent from the first window
- **THEN** the internal adapter obtains the later server window and selection preserves the item's semantic identity
- **AND** validation or submission uses the existing Causeway interaction contract

#### Scenario: Search generation is superseded
- **WHEN** the browser changes filter or dependent parameter values while a page is in flight or replaces the route
- **THEN** obsolete pages cannot alter suggestions, total count, selection, error, focus, or current route state
- **AND** no unexpected GraphQL, console, page, CSP, external-request, accessibility, or overflow failure occurs

#### Scenario: Native and candidate modes are qualified
- **WHEN** the same semantic autocomplete target runs with native fallback and explicitly enabled Vaadin reference widgets
- **THEN** both modes preserve authoritative values and honest continuation or refinement behavior
- **AND** unaffected routes retain zero Vaadin asset requests

### Requirement: Vaadin semantic field-family regression coverage
The pinned Reference Application suite SHALL provide deterministic editable property and action-parameter targets for every adopted Vaadin field family and representative unqualified values.
It MUST execute the same authoritative outcomes in default configuration and explicit native rollback modes.

#### Scenario: Basic families are exercised
- **WHEN** default and native qualification edits text, multiline, protected, nullable and required Boolean, enum, and bounded scalar-choice targets
- **THEN** both modes preserve requiredness, nullability, choices, validation, redaction, submission, and semantic events
- **AND** protected values appear in none of the captured markup, errors, diagnostics, or route evidence

#### Scenario: Numeric families are exercised
- **WHEN** default and native qualification edits boundary machine numbers and exact `Long`, `BigInteger`, and `BigDecimal` lexicals
- **THEN** both modes preserve the established GraphQL codec result
- **AND** no exact value passes through lossy JavaScript numeric coercion

#### Scenario: Local temporal families are exercised
- **WHEN** default and native qualification edits local date, local time, and local date-time values including supported fractional precision
- **THEN** both modes preserve the authoritative lexical value
- **AND** browser time zone or locale does not shift the submitted local value

#### Scenario: Unqualified values remain visible
- **WHEN** the corpus advertises offset-bearing, zoned, resource, custom, or other unqualified inputs
- **THEN** inventory and browser evidence retain their reviewed native, graceful-unsupported, or not-exercised classification
- **AND** no default adapter turns them into a successful-looking fallback

#### Scenario: Family route isolation is measured
- **WHEN** default qualification visits routes containing no eligible member for a packaged family
- **THEN** those routes request zero assets for that family
- **AND** CSP, accessibility, console, page, external-request, and overflow checks remain clean

### Requirement: Field adapter lifecycle regression coverage
The Reference Application browser suite SHALL exercise default adapter upgrade and replacement around real HTMX route, property, and action prompt lifecycles and SHALL compare explicit native rollback.

#### Scenario: Route changes during upgrade
- **WHEN** an eligible default field begins asynchronous upgrade and HTMX replaces the route
- **THEN** disconnected work cannot restore the old control, focus, pending value, error, or route state
- **AND** the current route remains authoritative

#### Scenario: Family load failure is injected
- **WHEN** a default family module fails to load
- **THEN** its active editor rerenders to the matching native semantic editor with pending state preserved
- **AND** other family and reference closures remain independently usable

#### Scenario: Default and native outcomes are compared
- **WHEN** the same deterministic property and action journeys run with no override and with `editor-toolkit=native`
- **THEN** authoritative GraphQL outcomes, semantic events, route identities, and reviewed classifications match
- **AND** native mode requests no Vaadin assets

### Requirement: Vaadin read-only field and action regression coverage
The pinned Reference Application suite SHALL provide deterministic read-only property and ordinary action targets for every adopted Vaadin presentation family and representative excluded values and controls.
It MUST execute the same authoritative outcomes in default component configuration and explicit native component rollback modes.

#### Scenario: Read-only field families are exercised
- **WHEN** default and native qualification display text, multiline, Boolean, enum, bounded-choice, exact and machine numeric, local date, local time, and local date-time targets
- **THEN** both modes preserve authoritative values, labels, descriptions, disabled reasons, nullability, alignment, wrapping, local semantics, and accessible relationships
- **AND** application renderer precedence and supported fractional precision remain unchanged

#### Scenario: Excluded values remain authoritative
- **WHEN** the corpus displays protected, null, reference, resource, LOB, offset-bearing, zoned, legacy temporal, custom, collection, unsupported, or otherwise excluded values
- **THEN** their reviewed native, application-renderer, or explicit-unsupported presentations remain visible
- **AND** no default adapter turns them into an approximate successful-looking field

#### Scenario: Ordinary action states are exercised
- **WHEN** default and native qualification renders and activates visible enabled, visible disabled, hidden, object, service, parameterless, and parameterized ordinary actions
- **THEN** both modes preserve labels, descriptions, disabled reasons, visibility, exact single request publication, parameter preparation, validation, invocation, result handling, and focus restoration
- **AND** property editor, action-prompt, menu, and shell buttons remain on their reviewed native contracts

#### Scenario: View and edit transitions are compared
- **WHEN** an eligible property enters edit, validates, cancels, saves, fails, and reconciles authoritatively in default and native modes
- **THEN** family selection does not change pending or authoritative values, GraphQL variables, validation, semantic events, or focus intent
- **AND** the final read-only presentation contains no stale editor or duplicate control

### Requirement: Presentation adapter lifecycle and delivery regression coverage
The Reference Application browser suite SHALL exercise read-only field and action adapter loading, replacement, failure, routing, theming, responsiveness, and policy precedence against real HTMX lifecycles.

#### Scenario: Route changes during read-only upgrade
- **WHEN** an eligible default read-only field or action begins asynchronous upgrade and HTMX replaces the route
- **THEN** disconnected work cannot restore the old control, listener, focus, value, error, or route state
- **AND** the current route remains authoritative

#### Scenario: Read-only family failure is injected
- **WHEN** one default field-family module fails while displaying an eligible value
- **THEN** that value rerenders through its matching authoritative native renderer
- **AND** editors, references, actions, other families, values, descriptions, errors, and recoverable focus remain correct

#### Scenario: Action-button failure is injected
- **WHEN** the default action-button module fails before or after an ordinary action connects
- **THEN** ordinary actions rerender as their established native buttons and remain singly operable
- **AND** no duplicate listener, request, control, stale focus target, or toolkit error escapes the bounded failure state

#### Scenario: Route asset isolation is measured
- **WHEN** default qualification visits routes containing distinct combinations of eligible read-only families, editors, references, ordinary actions, and unaffected content
- **THEN** each route requests only the closures selected by connected eligible components
- **AND** unused closures, external requests, unexpected CSP violations, and route-readiness dependencies are absent

#### Scenario: Component policy precedence is exercised
- **WHEN** browser profiles cover explicit component policy, deprecated editor policy, deprecated pilot subsets, conflicting properties, and the no-property default
- **THEN** adapter selection, shell diagnostics, CSP hashes, and requested closures match documented precedence
- **AND** explicit `component-toolkit=native` produces no Vaadin request or hash

#### Scenario: Presentation accessibility matrix runs
- **WHEN** default and native controls are exercised by keyboard at wide and narrow viewports with theme switching, reduced motion, forced colors, disabled reasons, validation, route replacement, and representative errors
- **THEN** there are no unexpected accessibility, console, page, external-request, duplicate-control, overlay, focus, clipping, or overflow failures
- **AND** accessible names, descriptions, state, order, and authoritative outcomes remain equivalent
