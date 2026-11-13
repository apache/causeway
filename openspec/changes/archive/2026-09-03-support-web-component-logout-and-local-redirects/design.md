## Context

The shared action components publish a cancelable `causeway-action-request` before dispatch and a `causeway-action-result` after successful invocation.
HTMX and Vue both invoke service actions through the same foundation contexts, so the framework Logout menu currently reaches GraphQL in both viewers.
The invocation fails before either host receives a redirect, because `LogoutMenu.logout()` has the polymorphic Java return type `Object`, GraphQL advertises its result as `String`, and execution supplies no managed result for the `results` fetcher.

The existing authenticated HTMX SecMan integration already demonstrates the correct security boundary.
It removes the exact framework Logout action, blocks stale action requests, and submits a host-owned POST logout form with current CSRF evidence.
The generic HTMX runtime applies that exclusion only when authentication metadata is present, and Vue has no equivalent authentication integration.

`LocalResourcePath` is independently documented as a browser/client redirect value.
GraphQL preserves its `path` and `openUrlStrategy` in `LocalResourcePathValue`, but foundation normalization currently classifies every structured non-collection result as an object and the host bridges do not recognize this value type.

## Goals / Non-Goals

**Goals:**

- Ensure generic HTMX and Vue never invoke the exact framework Logout action through GraphQL unless a host has explicitly claimed a safe authentication operation.
- Hide or disable the unusable framework Logout menu affordance when no host logout capability is installed.
- Preserve the existing HTMX SecMan CSRF-protected logout flow.
- Provide an explicit pre-invocation host policy boundary suitable for a future Vue authentication integration.
- Preserve typed `LocalResourcePathValue` as a distinct semantic action result.
- Perform validated same-origin local-resource navigation according to `OpenUrlStrategy`.
- Keep action identity, result data, and host navigation policy explicit and testable.

**Non-Goals:**

- Do not change Wicket logout behavior, core `LogoutMenu`, Spring Security, SecMan, or OAuth implementations.
- Do not make GET `/logout` a universal authentication endpoint.
- Do not infer logout semantics from a path string or from an action label.
- Do not hide arbitrary application actions named Logout or all actions returning `LocalResourcePath`.
- Do not use Vue Router or HTMX fragment navigation for application-local resource paths.
- Do not add a Vue security implementation in this change.

## Decisions

### Use exact action identity for the reserved Logout operation

The reserved operation is identified only by service logical type `causeway.security.LogoutMenu` and action id `logout`.
A shared identity helper will prevent HTMX and Vue policies from drifting, while menu filtering and request interception remain host responsibilities.

Filtering by return type is rejected because Logout is declared as `Object`, its action metadata has no result logical type, and its GraphQL result is advertised as `String`.
Filtering every `LocalResourcePath` action is also rejected because those actions can represent legitimate application-local navigation.

### Fail closed before GraphQL invocation

An unclaimed framework Logout action will be absent from ordinary menu controls and any stale or custom `causeway-action-request` for that identity will be canceled before dispatch.
The action will not be invoked and no synthetic success result will be shown.
A bounded announcement or diagnostic may explain that logout requires a host authentication integration without disclosing session details.

Post-result error suppression is rejected because Logout has already crossed the wrong security boundary and can mutate authentication state before a result is available.

### Extend the existing host policy boundary rather than adding security to components

Foundation action controls will continue to publish canonical service/action identity through the cancelable request event.
The Vue viewer will expose a pre-invocation action policy alongside its navigation and result policies.
The HTMX host policy will expose equivalent claim handling while retaining its existing authentication-metadata interception.
A host claim means that the host owns the operation and MUST prevent ordinary GraphQL dispatch before performing its own behavior.

Foundation components will not know about CSRF, sessions, login pages, or security providers.
Authenticated hosts remain responsible for endpoint selection, HTTP method, current anti-forgery evidence, session cleanup, and post-logout navigation.

### Keep secure Logout distinct from local-resource navigation

A path equal to `/logout` has no intrinsic authentication meaning.
Hosts MUST NOT reinterpret a `LocalResourcePath` as logout merely because its text matches that path.
Secure logout commonly requires POST and CSRF evidence, and its effective endpoint can include an application context and viewer base path.

This deliberately rejects changing `LogoutMenu` to return a universal GET redirect.

### Introduce a distinct local-resource semantic result

Action-result normalization will recognize the exact GraphQL output type `LocalResourcePathValue` before applying generic structured-object classification.
The immutable semantic result will retain a distinct kind and a bounded value containing `path` and `openUrlStrategy`.
Object and service action contexts will share the normalization logic so their behavior cannot diverge.
Malformed values will fail closed as unsupported results rather than falling through to object navigation or string presentation.

### Resolve and validate navigation at the host boundary

Each host will resolve the supplied path against an application-local base under its current origin.
HTMX can receive the servlet context path from its server-rendered document contract.
Vue will accept an application-local resource base or resolver through viewer options, with a same-origin default suitable for root deployments.

The resolved URL MUST retain the current origin and MUST reject scheme-relative, cross-origin, credential-bearing, malformed, or otherwise unsupported targets.
`SAME_WINDOW` uses full-document navigation rather than the object router.
`NEW_WINDOW` uses a new browsing context with opener isolation, subject to browser popup policy.
Unknown strategies fail closed.

A host policy may claim a local-resource result to apply stricter deployment policy, but cannot alter the canonical action result.

### Test Logout and local-resource behavior independently

Logout acceptance will assert that the framework menu action is absent when unclaimed and that a synthetic stale request produces no GraphQL invocation.
The existing secured HTMX suite will continue proving CSRF-protected host logout.
Local-resource fixtures will use actions explicitly declared as `LocalResourcePath`, not the framework Logout action, and will verify same-window, new-window, context-path, malformed-target, and cross-origin behavior.

## Risks / Trade-offs

- **Risk: Suppressing Logout removes the only apparent sign-out affordance in an application without authentication integration.** → Such an affordance is currently broken and potentially unsafe; documentation will require authenticated hosts to supply an owned logout control.
- **Risk: Exact framework identity is a deliberate coupling.** → Centralize it as a reviewed compatibility constant and test that similarly named application actions remain unaffected.
- **Risk: Vue applications can be deployed beneath context paths unknown to a static bundle.** → Make the local-resource base explicit in viewer configuration and test root and nested deployments.
- **Risk: Browsers can block asynchronous `NEW_WINDOW` navigation.** → Document browser policy, preserve a host claim hook, and verify that the default uses opener isolation without promising popup bypass.
- **Risk: General result normalization changes both object and service actions.** → Centralize classification and retain regression coverage for scalar, object, collection, void, Blob, Clob, and unsupported structured results.
- **Risk: A permissive path validator could become an open redirect.** → Resolve with the URL API, require the current origin, reject credentials and scheme-relative input, and never derive logout semantics from the target.
