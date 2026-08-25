## Context

Causeway grid XML already represents property- and collection-associated actions by nesting action nodes beneath the owning member.
`object-layout.mjs` preserves that relationship and renders the member and its actions inside a `data-causeway-associated-member` composition with a responsive action group.

Direct HTML currently cannot use the equivalent natural hierarchy.
`<causeway-property>` replaces its complete light DOM whenever property or editor state renders.
`<causeway-collection>` captures direct `<causeway-collection-column>` declarations and then replaces its complete light DOM for metadata, activation, loading, paging, error, empty, and ready states.
A nested `<causeway-action>` would therefore be disconnected and destroyed by the first owner render.

The newly archived HTML-authored Petclinic pages work around that limitation by placing associated action groups adjacent to their properties and collections.
This change makes the semantic hierarchy itself supported while retaining ordinary HTML composition, light-DOM styling, one object context, one interaction controller, and GraphQL authority.

## Goals / Non-Goals

**Goals:**

- Make direct `<causeway-action>` children a supported declarative vocabulary of `<causeway-property>` and `<causeway-collection>`.
- Preserve each declared action element, its declaration order, focus, pending interaction, and independent context subscription across owner rendering.
- Render owner presentation and associated actions as one responsive, accessible member composition with stable public styling and automation hooks.
- Keep collection columns and associated actions as disjoint child vocabularies that can coexist in either declaration order.
- Keep generated grid composition and direct HTML composition semantically and visually equivalent.
- Convert Petclinic's adjacent workaround markup into executable nested declarations.

**Non-Goals:**

- Inferring associations from action naming, GraphQL metadata, Java methods, or DOM proximity outside the supported parent-child relationship.
- Allowing an owner property or collection to authorize, hide, disable, validate, invoke, cancel, or reconcile an associated action.
- Associating one action declaration with multiple owners or introducing action references separate from ordinary `<causeway-action>` elements.
- Adding GraphQL fields, changing effective grid XML, changing action dispatch, or changing interaction result semantics.
- Adding raw toolkit elements, framework-specific slots, Shadow DOM, a page template language, or designer diagnostics.
- Supporting arbitrary nested wrapper elements as action declarations; only direct semantic children participate.

## Decisions

### Direct semantic children define association

A direct `<causeway-action>` child of `<causeway-property>` or `<causeway-collection>` declares presentation association with that parent.
Descendant actions inside arbitrary wrappers remain ordinary composition and are not captured as associated declarations.
This keeps ownership deterministic, preserves source order, avoids selector heuristics, and gives future authoring snippets one canonical syntax.

For collections, direct `<causeway-collection-column>` children remain column declarations and direct `<causeway-action>` children become associated-action declarations.
Each vocabulary is filtered independently, so interleaving columns and actions cannot add action members to row projections or treat columns as action controls.

Alternatives considered were association attributes on adjacent elements and a new `<causeway-associated-actions>` wrapper.
Adjacent attributes repeat the workaround that the change is intended to remove, while a wrapper adds vocabulary without improving semantic ownership.

### Owner rendering uses a stable primary region

Property and collection rendering will stop replacing the entire custom-element light DOM.
A shared member-composition helper will establish one stable owner-presentation region before the direct declarative children and route all existing loading, hidden, disabled, error, editor, empty, table, and row markup updates into that region.

Declared action nodes will remain the same connected DOM objects after initial parsing.
They will not be cloned, serialized, moved into a replacement subtree, or regenerated during owner updates.
A bounded direct-child observer will recognize parser-late or deliberately appended action declarations and update only composition markers and presentation state.
The observer will not recursively scan descendants or create additional action elements.

This approach preserves action context generations, pending prompts, focus, event listeners, and stale-request protection while avoiding duplicate metadata reads or action requests caused by owner rerendering.
It also lets collection-column declarations remain connected and hidden rather than relying on their accidental destruction after initial capture.

Alternatives considered were cloning action configuration on every render, moving action nodes into a generated wrapper, and adopting Shadow DOM slots.
Cloning loses independent action state, moving an already connected custom element creates avoidable disconnect and reconnect cycles, and Shadow DOM would break the established light-DOM styling and automation contract.

### Association is presentation-only

The owner component will classify and arrange action children but will not inspect their GraphQL descriptors or interaction state.
Each `<causeway-action>` continues to register its own semantic requirement against the nearest object context and to publish its own action request to the existing interaction controller.

Owner loading, error, disabled, hidden, editing, validation, activation, paging, and refresh state affect only the stable primary region.
A visible associated action remains available when the owner member is hidden or disabled unless that action's own GraphQL state hides or disables it.
Owner disconnection still disconnects all descendants as part of normal route disposal.

The property component's editor event handling and the collection component's activation handling will be scoped to controls in the primary region so events from nested action controls cannot be mistaken for owner controls.
Semantic action events continue to bubble through the owner without interception.

### One shared presentation contract covers both composition paths

Direct member hosts and generated object-layout wrappers will share the established `data-causeway-associated-member`, `data-causeway-action-group`, and associated-action styling vocabulary.
The direct host will expose a full-width primary region followed by a wrapping sequence of direct action elements without relocating those elements.

The default CSS will keep actions in declaration order, wrap them at narrow widths, preserve visible focus, avoid horizontal overflow, and expose `--causeway-*` variables for spacing and presentation.
Applications may style documented hosts and classes but do not need raw Vaadin elements or inline styles.

`object-layout.mjs` may reuse shared marker or markup helpers where that reduces divergence, but its accepted grid parser and effective-grid semantics will not change.
Semantic equivalence is required; byte-identical DOM between generated and directly authored pages is not.

### Petclinic is the browser-level acceptance composition

`petclinic.PetOwner.html` will nest `updateName` beneath `name`, `addPet` and `removePet` beneath `pets`, and `bookVisit` beneath `visits`.
The adjacent Petclinic-only association wrappers will be removed.
Retained layout XML continues to prove that generated grid composition expresses the same associations.

Foundation tests will cover parser timing, declaration order, column coexistence, owner state transitions, hidden and disabled independence, action state, no duplicate nodes or requests, focus, teardown, and reconnection.
Petclinic integration and Playwright tests will cover literal nested source, final DOM placement, action prompts and results, editing, paging, history, responsive overflow, default Vaadin policy, and native rollback.

## Risks / Trade-offs

- [Risk] Refactoring full-host `innerHTML` writes could leave stale owner markup or break existing selectors. → Use one explicit primary-region render primitive, retain existing markup inside it, and rerun the complete foundation and Petclinic suites.
- [Risk] Parent event delegation could consume events from nested action controls. → Scope owner control handling to the primary region and add bubbling-action regression tests.
- [Risk] Parser timing could connect a parent before all declarations exist. → Observe bounded direct-child changes and test declarations parsed and appended after parent connection.
- [Risk] A hidden owner could accidentally hide independently visible actions through the host `hidden` attribute. → Apply hidden state to the primary region whenever associated actions exist and test independent GraphQL visibility explicitly.
- [Risk] Retained collection-column nodes change an implementation detail that previously disappeared after capture. → Keep them hidden, exclude them from presentation layout, and test unchanged row projections and attribute updates.
- [Risk] Host-level action grouping is less structurally explicit than moving actions into a wrapper. → Provide stable data hooks and direct-child styling while prioritizing node identity and lifecycle correctness.

## Migration Plan

Existing adjacent action composition remains valid ordinary HTML and requires no immediate migration.
Applications may move an action element directly beneath its property or collection to opt into the association contract.
Generated `<causeway-object>` pages require no source migration.

Rollback restores full-host rendering and the adjacent Petclinic markup without changing GraphQL, routes, grid XML, action semantics, or persisted state.

## Open Questions

None.
