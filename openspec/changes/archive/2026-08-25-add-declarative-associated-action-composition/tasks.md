## 1. Baseline and executable contract fixtures

- [x] 1.1 Record the current property and collection full-host rendering behavior, generated grid association markup, Petclinic adjacent workaround markup, and accepted foundation and browser baselines.
- [x] 1.2 Add DOM fixtures containing direct property actions, interleaved collection columns and actions, hidden and disabled owners, hidden and disabled actions, and parser-late declarations.
- [x] 1.3 Add failing tests proving current owner rendering destroys nested action declarations and documenting the required stable node-identity behavior.
- [x] 1.4 Add request-count instrumentation that distinguishes owner requirements, action requirements, action validation, and action invocation.
- [x] 1.5 Record the current public associated-member classes, data attributes, design variables, keyboard order, and narrow-viewport behavior that generated object layouts expose.

## 2. Shared member-composition lifecycle

- [x] 2.1 Add a toolkit-neutral member-composition helper that establishes one stable primary presentation region before declarative children.
- [x] 2.2 Classify only direct `<causeway-action>` children as associated declarations without recursively claiming descendant actions.
- [x] 2.3 Recognize parser-late and deliberately appended direct actions through bounded direct-child observation.
- [x] 2.4 Preserve each action node in place without cloning, serializing, relocating, or regenerating it during owner rendering.
- [x] 2.5 Preserve declaration order while adding stable associated-member and action-group hooks to the direct composition.
- [x] 2.6 Remove composition observers and transient references during owner disconnection without disconnecting children prematurely.
- [x] 2.7 Add unit tests for initial parsing, late insertion, ignored descendant actions, order, duplicate prevention, teardown, and reconnection.

## 3. Property-associated action composition

- [x] 3.1 Initialize the stable property primary region before property context rendering can replace declarative children.
- [x] 3.2 Route property loading, ready, error, unsupported, read-only, and null presentation updates into only the primary region.
- [x] 3.3 Route property editor, validation, failure, cancellation, reconciliation, and toolkit-fallback updates into only the primary region.
- [x] 3.4 Scope property click, input, change, reference, field, and focus handling to owner controls so nested action events retain standard behavior.
- [x] 3.5 Apply hidden and disabled property state only to owner presentation and controls while leaving associated actions under their own GraphQL authority.
- [x] 3.6 Preserve nested action node identity, context generation, focus, pending prompt state, and request counts across every property state transition.
- [x] 3.7 Add property tests for visible, hidden, disabled, editing, validating, failed, cancelled, reconciled, disconnected, and reconnected compositions.

## 4. Collection-associated action and column coexistence

- [x] 4.1 Initialize the stable collection primary region without consuming direct action or collection-column declarations.
- [x] 4.2 Filter collection-column and associated-action child vocabularies independently when declarations are interleaved.
- [x] 4.3 Keep retained collection-column elements hidden while preserving column configuration, attribute updates, row projection, and deterministic ordering.
- [x] 4.4 Route collection metadata, inactive, loading, error, hidden, disabled, empty, list, table, paging, and refresh presentation into only the primary region.
- [x] 4.5 Scope collection activation and row interaction handling so nested action controls cannot activate or reload the collection accidentally.
- [x] 4.6 Apply hidden and disabled collection state only to collection presentation and activation while leaving associated actions independently governed.
- [x] 4.7 Preserve nested action node identity, focus, pending prompt state, and request counts across activation, paging, errors, refresh, disconnect, and reconnect.
- [x] 4.8 Add collection tests for declaration interleaving, parser timing, column updates, zero rows, multiple pages, owner states, duplicate prevention, and projection isolation.

## 5. Shared presentation and generated-layout equivalence

- [x] 5.1 Add default light-DOM styles that place the owner primary region first and wrap direct associated actions in declaration and keyboard order.
- [x] 5.2 Expose documented `data-causeway-associated-member`, `data-causeway-action-group`, semantic classes, and `--causeway-*` spacing hooks for direct compositions.
- [x] 5.3 Ensure hidden declaration elements, owner descriptions, disabled reasons, labels, and action controls retain valid accessible relationships.
- [x] 5.4 Verify wide, narrow, reduced-motion, light, dark, and application-themed compositions without overlap, clipping, or horizontal overflow.
- [x] 5.5 Reuse shared composition markers or helpers in `object-layout.mjs` where practical without changing accepted grid parsing or member allocation.
- [x] 5.6 Extend object-layout and component-style tests to prove direct and grid-generated associations expose equivalent semantic ordering and hooks.
- [x] 5.7 Verify no application-facing Vaadin element, toolkit-specific slot, inline style, inline handler, or CSP relaxation enters the public contract.

## 6. Petclinic authored-page migration and browser qualification

- [x] 6.1 Nest `updateName` directly beneath the `name` property in `petclinic.PetOwner.html` and remove its adjacent workaround wrapper.
- [x] 6.2 Nest `addPet` and `removePet` beneath the `pets` collection while retaining all declared collection columns.
- [x] 6.3 Nest `bookVisit` beneath the `visits` collection while retaining all declared collection columns.
- [x] 6.4 Remove obsolete Petclinic association CSS without changing unrelated page layout or retained grid fallback resources.
- [x] 6.5 Extend integration assertions to verify literal nested source, private resource packaging, retained layout associations, and absence of workaround markup.
- [x] 6.6 Extend Playwright placement checks to assert one property-associated action and the ordered collection-associated action sets in final live DOM.
- [x] 6.7 Exercise nested property and collection action prompts, defaults, validation, cancellation, scalar results, object results, collection refresh, navigation, and history.
- [x] 6.8 Assert each activation produces at most one action request and owner rerendering produces no duplicate action requirement or live element.
- [x] 6.9 Verify keyboard focus restoration, tab order, disabled reasons, independent hidden state, mobile overflow, console output, CSP, and accessibility.
- [x] 6.10 Run the complete Petclinic browser matrix under Vaadin-default and explicit native toolkit policies over the same nested HTML.

## 7. Documentation, compatibility, and release evidence

- [x] 7.1 Document the direct-child association syntax, independent action authority, column coexistence, supported styling hooks, and adjacent-markup compatibility in foundation usage guidance.
- [x] 7.2 Update Petclinic documentation to identify nested HTML declarations as the application customization proof and retained grid XML as generated-layout equivalence evidence.
- [x] 7.3 Document that arbitrary descendant wrappers are not claimed and that association does not imply authorization, invocation ownership, or shared visibility.
- [x] 7.4 Run the complete foundation Node suite and HTMX route-policy suite and record exact pass counts.
- [x] 7.5 Run focused Petclinic integration tests, both Playwright policy matrices, RAT, packaging, strict OpenSpec validation, and whitespace checks.
- [x] 7.6 Verify GraphQL schema, action dispatch, value codecs, autocomplete, collection windows, route identity, Vaadin closures, CSP hashes, dependencies, and package locks remain unchanged.
- [x] 7.7 Record implementation, lifecycle, request-count, accessibility, responsive, compatibility, production-isolation, rollback, and final-gate evidence.
