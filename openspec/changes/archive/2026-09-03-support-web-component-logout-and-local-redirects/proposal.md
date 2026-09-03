## Why

The framework Logout service is a viewer-specific abstraction that fails when HTMX or Vue invokes it through GraphQL, leaving both generic viewers with a visible action that produces a server-side error instead of ending a browser session.
`LocalResourcePath` is already a public redirect value and has a structured GraphQL representation, but the web-component result contract and host viewers do not yet interpret it as safe browser navigation.

## What Changes

- Treat the exact framework `causeway.security.LogoutMenu#logout` action as a host-owned authentication operation rather than an ordinary GraphQL domain action.
- Suppress that framework action by default in generic HTMX and Vue menu presentation unless an installed host authentication integration explicitly claims it.
- Prevent stale or custom requests for the framework Logout action from reaching GraphQL when no safe host logout handler is available.
- Preserve the existing authenticated HTMX SecMan behavior, which uses a CSRF-protected POST logout control outside domain invocation.
- Add a bounded host interception contract so future Vue or other authentication integrations can claim Logout without coupling foundation components to Spring Security, SecMan, Wicket, or OAuth.
- Classify typed `LocalResourcePathValue` action results distinctly from navigable domain objects and other structured values.
- Interpret valid same-origin local-resource results using `SAME_WINDOW` or `NEW_WINDOW` behavior while rejecting malformed, cross-origin, or unsupported targets without navigation.
- Keep logout independent from `LocalResourcePath` result handling because secure logout can require POST, current CSRF evidence, a configured viewer/context path, and host-specific session cleanup.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `domain-web-components`: Add semantic classification for local-resource action results and a host-claimable pre-invocation boundary for reserved host operations.
- `generic-htmx-web-component-viewer`: Suppress unsafe framework Logout invocation by default and perform bounded local-resource navigation.
- `generic-vue-web-component-viewer`: Suppress unsafe framework Logout invocation by default, expose host policy interception, and perform bounded local-resource navigation.

## Impact

The change affects action-request dispatch, menu projection, semantic action-result normalization, HTMX result handling, Vue host policies, and browser acceptance coverage under `viewers/webcomponents`.
It reuses the existing GraphQL `LocalResourcePathValue` shape and existing authenticated HTMX logout form rather than changing core security, the Logout service, GraphQL schema authority, or Wicket behavior.
Applications with similarly named logout actions remain unaffected because matching uses the exact framework service and action identity.
