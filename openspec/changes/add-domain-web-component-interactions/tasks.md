## 1. Interaction Contracts and Fixtures

- [ ] 1.1 Define semantic object commands, interaction states, result types, custom events, and application override contracts for property and action interactions.
- [ ] 1.2 Extend schema and response fixtures for editable properties, choices, autocomplete, validation, action parameters, safe invocation, mutations, and all result shapes.
- [ ] 1.3 Add contract coverage for query-only, query-and-mutation, and non-spec-compliant rich-schema API variants.

## 2. Semantic Command Execution

- [ ] 2.1 Extend rich-schema discovery to describe property update, action parameter semantic, validation, invocation, and top-level mutation capabilities.
- [ ] 2.2 Implement object-context commands for property choices, autocomplete, validation, and update using semantic member identifiers.
- [ ] 2.3 Implement object-context commands for action parameter defaults, choices, autocomplete, disabled and hidden state, validation, and invocation.
- [ ] 2.4 Implement API-variant-aware operation selection that prefers top-level mutations for mutating properties and actions.
- [ ] 2.5 Implement per-object mutation serialization and complete active-projection reconciliation after successful commands.

## 3. Editor Registry and Property Interaction

- [ ] 3.1 Implement the deterministic semantic editor registry and application override API.
- [ ] 3.2 Implement standard editors for supported scalar, enum, object-reference, choice, and autocomplete input shapes.
- [ ] 3.3 Implement property view, preparing, editing, validating, saving, success, failure, and cancel transitions.
- [ ] 3.4 Implement property choices, debounced autocomplete, validation, save blocking, update execution, and server-authoritative reconciliation.
- [ ] 3.5 Test hidden, disabled, unsupported, valid, invalid, cancelled, successful, failed, and stale-response property interactions.

## 4. Action Prompt and Parameter Negotiation

- [ ] 4.1 Implement the standard nearest action interaction controller while preserving claimed or cancelled action-request overrides.
- [ ] 4.2 Implement accessible parameterless invocation and parameterized prompt presentation.
- [ ] 4.3 Build prompt parameters in schema order using the semantic editor registry and introspected parameter wrappers.
- [ ] 4.4 Implement hidden, disabled, default, choice, autocomplete, and validation state using current pending preceding arguments.
- [ ] 4.5 Implement invalidation and recomputation of later parameter semantics when preceding arguments change.
- [ ] 4.6 Test parameterless, parameterized, dependent-choice, autocomplete, invalid, disabled, hidden, cancelled, and application-overridden flows.

## 5. Invocation Results and Errors

- [ ] 5.1 Implement safe-query and mutating-action invocation through the object-context command API.
- [ ] 5.2 Normalize object, collection, scalar, and void outcomes and publish framework-neutral semantic result events.
- [ ] 5.3 Implement parameter-path, property-path, invocation-level, and transport-error mapping while retaining successful sibling state.
- [ ] 5.4 Implement request generations, cancellation, debounce, and obsolete-response rejection for validation, choices, defaults, and autocomplete.
- [ ] 5.5 Test mutation ordering, context invalidation, returned-object hydration, broad refresh, action results, partial errors, and out-of-order responses.

## 6. Accessibility and Customization

- [ ] 6.1 Verify property editors and the standard action prompt provide labels, descriptions, focus management, keyboard control, pending announcements, validation associations, and error summaries.
- [ ] 6.2 Add examples of application-provided editors, claimed action requests, custom prompt presentation, and host-controlled result navigation.
- [ ] 6.3 Document semantic commands, interaction events, API-variant behavior, refresh guarantees, customization obligations, and unsupported interaction shapes.

## 7. Executable Interaction Sample

- [ ] 7.1 Extend the deterministic `sample-html` domain with representative editable scalar and enum properties, validation, choices or autocomplete, safe and mutating actions, parameter semantics, and result shapes while preserving `s_sample-1` and the existing disabled and hidden members.
- [ ] 7.2 Extend `/sample-html/index.html` with explicit property interaction enablement, the standard action interaction controller, accessible prompt and result presentation, semantic interaction diagnostics, and additive stable hooks while preserving all read-only selectors and readiness behavior.
- [ ] 7.3 Extend random-port integration tests with targeted introspection and real GraphQL assertions for property validation and mutation, safe and mutating action operation placement, parameter negotiation, deterministic outcomes, and authoritative object refresh.
- [ ] 7.4 Document the deterministic interaction data, stable selectors, expected editor and prompt states, semantic result events, Maven run profile, and troubleshooting probes for `/graphql` and terminal interaction errors.

## 8. Automated Verification

- [ ] 8.1 Add dependency-free DOM and fixture coverage for representative property edits, parameterless actions, parameterized prompts, safe and mutating invocation, semantic outcomes, focus restoration, cancellation, validation, and stale responses.
- [ ] 8.2 Run the foundation Node suite, sample and GraphQL Maven tests, syntax, formatting, strict OpenSpec validation, and configured accessibility checks, and resolve all failures.

## 9. Manual Browser Verification

- [ ] 9.1 Launch the sample with `mvn -f viewers/webcomponents/pom.xml -Prun-sample-html`, verify `/graphql` with a direct probe, open `/sample-html/index.html`, and confirm `sample-app[data-state="ready"]` before testing interactions.
- [ ] 9.2 Verify with pointer input that a representative text or numeric property can enter edit mode, cancel without a command, reject an invalid pending value with an accessible reason, save a valid value, and render the authoritative refreshed value; also verify enum choices and advertised autocomplete behavior.
- [ ] 9.3 Verify parameterless safe and parameterized mutating actions, prompt defaults and choices, invalid argument blocking, cancel behavior, successful invocation, disabled and hidden actions, semantic result diagnostics, and representative object, collection, scalar, and void outcomes without automatic navigation.
- [ ] 9.4 Repeat property and action flows with keyboard-only input, including Tab order, Enter and Space activation, Escape cancellation, prompt focus containment, validation announcements, and focus restoration to the originating control.
- [ ] 9.5 Repeat the interaction smoke test at narrow and wide viewports in light and dark color schemes, inspect GraphQL requests for correct query-versus-mutation placement and HTTP success, and confirm context refresh, stable read-only content, no console errors, and no configured Lighthouse accessibility failures.
- [ ] 9.6 Record the manual results, then rerun final Maven, Node, formatting, and strict OpenSpec validation before marking the interaction change complete.
