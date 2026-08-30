## Context

The standard interaction controller currently derives every action-parameter label by humanizing the GraphQL parameter id, renders the canonical description visibly, and selects an editor solely from the introspected parameter shape.
Custom pages can author presentation attributes on `<cw-property>`, `<cw-action>`, and `<cw-collection>`, but an authored `<cw-action>` has no declarative way to refine selected parameter presentation.
`<cw-action>` replaces its light-DOM content when rendering, so nested configuration elements must be captured before ordinary action markup replaces them.
The existing collection-column capture and configuration pattern provides a precedent for declarative, non-visual child components.

## Goals / Non-Goals

**Goals:**

- Register and export a public `<cw-parameter>` configuration component.
- Let an authored action declare zero, some, or all parameter presentation overrides by semantic parameter id.
- Support property-consistent `named`, `described-as`, `description-as="label|tooltip"`, and bounded `multi-line` behavior.
- Preserve canonical parameter semantics for omitted declarations and omitted attributes.
- Keep the standard controller authoritative for prompt lifecycle and the object context authoritative for GraphQL operations.
- Demonstrate partial declarations on selected Petclinic actions.

**Non-Goals:**

- Declare, add, remove, reorder, hide, disable, default, constrain, validate, or invoke parameters from HTML.
- Change GraphQL parameter metadata or operation planning.
- Require `<cw-parameter>` children for parameterized actions.
- Add label-position overrides for action parameters.
- Expose application-authored raw toolkit controls.

## Decisions

### Model `<cw-parameter>` as a non-visual configuration component

`CausewayParameterElement` will extend the environment-safe HTMLElement base used by other configuration elements and expose normalized immutable configuration.
It will observe `id`, `named`, `described-as`, `description-as`, and `multi-line`, remain hidden, and notify its owning action when connected or changed.
The public element name, host contract, attribute constants, registration, module export, and documentation will be updated.

A visible standalone parameter component was rejected because action parameters require an active prompt, pending argument set, and controller-owned validation lifecycle.

### Capture declarative children before action rendering

Registration will capture pre-upgrade `<cw-parameter>` children, and `CausewayActionElement` will capture already-connected or dynamically added parameter configurations before replacing ordinary action markup.
The action will retain immutable configurations keyed by non-empty semantic parameter id and include them in the existing action presentation carried by the semantic action-request event.
Unknown ids remain harmless presentation data and are ignored when no authoritative parameter matches.
Duplicate ids resolve deterministically to the latest accepted configuration so dynamic attribute updates replace rather than multiply hints.

Preserving the configuration elements as visible prompt children was rejected because it would give two elements ownership of editor and validation state.
Constructing parameter GraphQL requirements from declarations was rejected because declarations are optional presentation hints, not semantic authority.

### Merge hints only at prompt presentation boundaries

The interaction controller will retain normalized parameter presentation hints separately from GraphQL-authoritative parameter state.
For each prepared parameter it will resolve:

- label from explicit `named`, otherwise the established humanized id;
- description from explicit `described-as`, otherwise the authoritative parameter description;
- description presentation from explicit `description-as`, otherwise visible label presentation;
- multiline rows from a valid explicit `multi-line`, otherwise ordinary editor selection.

The effective label and description will feed editor accessibility ids and names.
Tooltip descriptions will use the established bounded member-tooltip presentation while retaining visually hidden accessible description text.
The effective multiline value will be passed into the standard editor registry, which already selects textarea presentation for supported strings.
Hidden, disabled, default, choices, autocomplete, validity, parameter order, and invocation values continue to come exclusively from each prepared GraphQL parameter.

Mutating the prepared parameter objects was rejected because recomputation replaces those objects and because presentation should remain distinguishable from authoritative interaction state.

### Demonstrate selective Petclinic overrides

Petclinic will add representative nested declarations to selected existing parameterized actions while deliberately leaving sibling parameters and other actions undeclared.
The sample will cover an authored name, authored description in visible and tooltip forms, and multiline string editing without changing domain methods.
Resource-page, foundation interaction, and browser or integration assertions will verify declarative structure and effective prompt presentation.

## Risks / Trade-offs

- [Risk] Action rendering can erase declarative children before configuration is read. → Capture before custom-element registration and again during action connection, following the collection-column precedent.
- [Risk] Presentation hints could accidentally become semantic parameter declarations. → Match only ids returned by authoritative preparation and never alter parameter arrays or operation values.
- [Risk] Recomputed parameter state could lose presentation. → Store normalized hints in prompt state independently and resolve them for every render.
- [Risk] Tooltip descriptions could disappear from accessibility APIs. → Keep a visually hidden description node and wire editor `aria-describedby` while exposing the label tooltip by pointer and keyboard.
- [Risk] Multiline could be applied to an incompatible type. → Pass the bounded hint through existing editor qualification so unsupported shapes retain their normal editor.

## Migration Plan

No migration is required.
Existing actions without `<cw-parameter>` children retain byte-equivalent semantic behavior and established prompt presentation.
Removing nested declarations restores canonical presentation without changing domain code or GraphQL metadata.

## Open Questions

None.
