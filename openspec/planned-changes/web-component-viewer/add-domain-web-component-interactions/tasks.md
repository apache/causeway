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

## 7. Verification

- [ ] 7.1 Add end-to-end browser tests for property edits and representative safe and mutating actions against the rich GraphQL endpoint.
- [ ] 7.2 Run browser tests, relevant GraphQL viewer and Maven tests, accessibility checks, formatting checks, and strict OpenSpec validation, and resolve all failures.
