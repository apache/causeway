## Context

The semantic menu pipeline projects authorized Causeway menu resources into native or Vaadin controls and currently offers only an exclusion predicate to host viewers.
The generic HTMX and Vue hosts use that predicate to suppress exact framework Logout because invoking it through GraphQL is unsafe.
Their secured Petclinic variants compensate with a separate visible shell form, but that removes the Logout entry from the System menu and can collapse the tertiary bar when no other effective action remains.

Logout endpoint selection, POST method, current CSRF evidence, session invalidation, cookie cleanup, and redirect behavior must remain owned by the installed security integration.
The foundation and generic viewer packages must remain authentication-neutral, and exact action identity rather than label must continue to distinguish framework Logout.

## Goals / Non-Goals

**Goals:**

- Preserve authoritative menu hierarchy and placement while allowing a host to replace presentation text for one exact action.
- Show **Sign out** in the existing tertiary System menu for secured HTMX and Vue.
- Route that menu activation to the existing native CSRF-protected logout form before GraphQL validation or invocation.
- Keep ordinary unsecured viewers fail closed by excluding framework Logout.
- Support both native and Vaadin menu projections through one shared mechanism.
- Avoid visible authentication chrome outside the menu.

**Non-Goals:**

- Changing the framework Logout metamodel, annotation, GraphQL schema, authorization, or presentation in other viewers.
- Adding authentication or CSRF knowledge to foundation, generic HTMX, or generic Vue modules.
- Displaying the current username in another shell location.
- Treating similarly named actions or `/logout` resource paths as authentication operations.
- Creating a general mechanism for hosts to inject arbitrary menu hierarchy or alter action state.

## Decisions

### Add a presentation-only menu-action mapper

The menu projection will accept an optional host callback for an action's presentation label.
The callback receives a frozen bounded descriptor containing exact service logical type, action id, and authoritative label, and can return a replacement label or leave it unchanged.
The returned label is bounded and text-safe through the same projection rules as resource labels.
Identity, role, menu, section, order, visibility, disabled state, description, icon, prompt metadata, and semantic activation remain authoritative and cannot be changed by this hook.

The callback will be available per `<cw-menubars>` or `<cw-menubar-*>` instance and through the existing global widget configuration so native and Vaadin projections consume the same result.

A DOM post-processing alternative was rejected because Vaadin overlay content and native light DOM have different lifecycles and could expose stale or briefly incorrect labels.
Changing `LogoutMenu` metadata was rejected because it would change other viewers globally.
Arbitrary host action injection was rejected because secured mode already has an authorized exact framework entry and injection would manufacture menu structure and action state.

### Treat an installed logout claim differently from unavailable Logout

Generic HTMX and Vue continue to exclude exact framework Logout unless the application explicitly supplies a host presentation/claim contract.
Secured integrations opt that exact action into projection, map its label to **Sign out**, and preserve the existing pre-invocation interception.
A missing or failed claim remains fail closed and cannot fall through to GraphQL.

For HTMX, current authentication metadata identifies the exact host-managed action.
Bootstrap uses that identity to retain and relabel the menu action and to submit the native form when activated.
For Vue, the application configures an explicit menu-action policy alongside the existing action policy.

Using the presence of any action policy as implicit permission was rejected because unrelated host policies must not make framework Logout visible.
Matching the label was rejected because labels are mutable and non-authoritative.

### Keep the secure form but make it non-visual

Each secured shell retains one native POST form containing the current CSRF parameter and token.
The form is hidden from visual and accessibility navigation and exists only as the submission target for exact menu activation.
The separate username and Sign out shell block is removed.
The visible and accessible control is the semantic tertiary menu action.

Direct form submission behavior remains browser-native, preserving server-controlled redirects and full session cleanup.
Creating a `fetch` logout request was rejected because native navigation already provides the required full-document lifecycle and simpler CSRF/session semantics.

### Preserve menu hierarchy rather than synthesize a secured bar

The exact framework action remains in its GraphQL-authoritative menu, section, and order.
The host changes only its visible label and activation destination.
If authorization hides the framework action, the security integration does not manufacture a replacement menu or reveal its existence.

This keeps secured and unsecured effective-menu semantics aligned and avoids a security feature overriding application menu structure.

## Risks / Trade-offs

- [Risk] A host mapper throws or returns unsupported content. → Apply bounded text normalization and fail closed for that action without exposing exception content.
- [Risk] Framework Logout becomes visible without a functioning host form. → Visibility requires an explicit host-managed identity contract, and activation still cancels before GraphQL while reporting bounded unavailability if submission cannot occur.
- [Risk] Native and Vaadin presentations drift. → Apply the mapper during their shared immutable projection and test both families.
- [Risk] Removing username chrome changes existing screenshots or selectors. → Update secured acceptance tests and documentation to assert the menu-based contract; ordinary shell geometry remains unchanged.
- [Risk] An empty tertiary bar still collapses when authorization removes Logout. → This is intentional because the host does not manufacture unauthorized structure.

## Migration Plan

1. Add and test the bounded action-label mapper in foundation menu projection and elements.
2. Update generic HTMX and Vue policy wiring so explicit secured ownership retains and relabels exact framework Logout while absent ownership still excludes it.
3. Replace visible secured-shell authentication blocks with hidden native logout forms.
4. Update secured integration and browser tests for native and Vaadin menu behavior and CSRF-safe logout.
5. Regenerate and verify committed Vue assets and update documentation.

Rollback restores exclusion and visible shell chrome without changing server security endpoints or sessions.

## Open Questions

None.
