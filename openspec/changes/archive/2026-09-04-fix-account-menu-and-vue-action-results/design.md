## Context

The shared Petclinic menu layout currently declares a secondary `System` menu with `unreferencedActions="true"` and a tertiary `Account` menu with explicit utility actions.
Under SecMan, the catch-all captures `MeService#me`, while `Account` contains `UserMenu#me`, so both HTMX and Vue display two utility menus and duplicate the visible **Me** concept.
The intended sample information architecture is one utility menu containing the deliberately selected actions and labelled with the current username when authoritative host identity is available.
Exact Logout also needs a visually distinct treatment comparable to its former direct menubar presentation, without changing its semantic identity or invocation.

Vue also has two result-presentation layers during a menu action: the interaction source owns transient prompt/result chrome, and the host bridge owns canonical navigation or a selected page/shell result outlet.
The default Vue object-result branch currently returns into navigation before dismissing the source interaction result.
Unsupported result presentation in the persistent shell outlet is not cleared when a later object result navigates, which allows stale content to survive above the next route.
The generic `UserMenu#me` result is a view model and must be handled from its public semantic metadata rather than inferred from its label, action id, or current route.

## Goals / Non-Goals

**Goals:**

- Present one current-username utility menu in ordinary and secured HTMX and Vue Petclinic, with a neutral Account fallback only when authoritative display identity is unavailable.
- Preserve exact authorization and action identities for **Me**, **Configuration**, and **Sign out**.
- Give exact Logout a distinct accessible outlined treatment under native and Vaadin-backed presentation.
- Give every unclaimed Vue service-action result one deterministic host outcome.
- Navigate complete `UserMemento` identity through the canonical Vue object route.
- Dismiss transient source interaction chrome after host handoff.
- Clear obsolete host result presentation when a later result navigates or replaces it.

**Non-Goals:**

- Do not merge menu tiers generically for applications with different authored layouts.
- Do not make the generic component layer discover authentication state or derive a username from menu/action metadata.
- Do not infer Logout from a label, path, partial identity, or presentation class.
- Do not infer object identity from labels, JavaScript state, route state, or result type names.
- Do not add a `UserMemento`-specific Vue page or special-case its action identity.
- Do not change Logout interception, CSRF handling, SecMan authorization, or GraphQL invocation semantics.
- Do not make Vue and HTMX markup or pixels identical.

## Decisions

### Consolidate and label the Petclinic utility menu

The shared Petclinic menu layout will stop declaring the secondary `System` menu and its unreferenced-action catch-all.
The explicit tertiary section remains the sole utility-menu source for selected `UserMenu#me`, `ConfigurationMenu#configuration`, and Logout actions, but its displayed title is supplied by bounded host presentation policy rather than fixed to Account.
Secured HTMX and Vue hosts will supply the authenticated username from their existing authoritative authentication context, and an ordinary sample host may supply its established current-user identity without introducing an authentication dependency into generic packages.
If no trustworthy display identity is available, the section uses the neutral authored Account label rather than guessing or suppressing authorized actions.
The username is rendered as text, is not exposed in an anonymous shell, and does not alter menu identity, hierarchy, ordering, authorization, or action selection.
This is sample-owned information architecture plus a bounded label projection, so other applications retain their authored tiers and labels unless they opt into equivalent policy.

Keeping `System` but filtering only `MeService#me` was rejected because it would leave an empty or misleading menu and encode a sample cleanup as a host identity exception.
Moving the SecMan-only `MeService#me` into the utility section was rejected because the shared layout must also remain valid without SecMan and because one visible **Me** action is sufficient for this sample.
Having generic components query authentication state was rejected because foundation, HTMX, and Vue packages must remain authentication-neutral.

### Present exact Logout distinctly without changing behavior

Host presentation policy will map only the exact identity `causeway.security.LogoutMenu#logout` to a semantic sign-out appearance token.
Native and Vaadin-backed projections will render **Sign out** with a visible outlined or equivalently bounded treatment, including accessible contrast and focus indication that does not rely on color alone.
The appearance token changes presentation only; host-owned POST interception, CSRF evidence, authorization, ordering, action identity, and fail-closed behavior remain unchanged.
Renderer-specific selectors based only on the visible **Sign out** text were rejected because labels are mutable and cannot establish Logout identity.

### Complete source-to-host result handoff before returning

The Vue result listener will treat source dismissal as part of default result handoff for local-resource, object, scalar, collection, and void outcomes.
Object navigation will no longer bypass source dismissal through an early return.
Dismissal remains scoped to the event source's public `dismissResult` operation and does not reach into renderer-specific DOM.

### Use normalized public object identity

A normalized object result with non-empty `_meta.logicalTypeName` and `_meta.id` will navigate through the existing canonical router policy regardless of whether the object is an entity or view model.
If `UserMemento` reaches Vue without that advertised identity, implementation will trace the semantic result selection and restore the missing public metadata at the shared query/normalization boundary rather than inventing an identity in Vue.
The same regression will cover another object result such as Configuration to prove the behavior is generic.

### Make host result ownership replacement-based and generation-safe

Before committing a new default result outcome, Vue will clear obsolete host-owned presentation that cannot belong to the new outcome.
Object and local-resource navigation will clear persistent page/shell fallback content after the event is accepted and before navigation handoff.
Scalar, collection, void, and unsupported outcomes will replace the selected destination rather than accumulate alongside an earlier result.
Asynchronous completion will remain guarded by the current shell and route generation so an obsolete event cannot clear or repopulate a newer route.

The source interaction result and host destination are distinct owners; clearing one must not arbitrarily clear unrelated application-authored content or a result claimed by application policy.

### Test semantics through visible controls and public state

Ordinary and secured browser suites will open the one visible utility menu under native and Vaadin-backed policies, assert its expected current-username label where authoritative identity is available, and assert that no separate System or Account menu remains.
They will also verify that exact Logout is labelled **Sign out** and receives its distinct accessible appearance while adjacent actions do not.
Vue tests will activate **Me**, verify canonical `UserMemento` route presentation, confirm source result chrome is absent, then activate **Configuration** and confirm that no Me result remains.
Tests will inspect public route, semantic component state, accessible text, and bounded diagnostics rather than private toolkit timing where a visible control is available.
HTMX tests will retain comparison coverage for the consolidated menu and successful object navigation.

## Risks / Trade-offs

- [Risk] Removing the catch-all can hide a newly added unreferenced framework action in Petclinic. → Require deliberate placement in the shared layout and cover the intended utility set explicitly.
- [Risk] A username label can leak identity or display untrusted markup. → Supply it only from established host context after authentication and render it as text, with Account as the non-identifying fallback.
- [Risk] Vaadin overlay styling can diverge from native styling. → Project a semantic appearance token into each adapter and test accessible computed treatment rather than identical pixels.
- [Risk] Clearing result outlets too broadly can erase application-owned results. → Clear only viewer-selected outlets during unclaimed default handling and preserve claimed policy behavior.
- [Risk] Source dismissal can race asynchronous component rendering. → Centralize handoff ordering and add browser coverage for both immediate and navigational outcomes.
- [Risk] `UserMemento` metadata can differ across GraphQL schema evolution. → Test the public normalized result contract and fail closed when identity is genuinely absent.

## Migration Plan

No persistent data migration is required.
Applications using the generic Vue package receive corrected default result lifecycle behavior unless they claim the result through their own policy, in which case existing application ownership remains authoritative.
The menu change is confined to the shared Petclinic sample layout.
Rollback restores the System catch-all, the fixed Account label and undifferentiated Logout appearance, and the prior Vue result listener, though doing so reintroduces the reported duplicate and stale presentations.

## Open Questions

None.
