## Why

The web-component foundation deliberately hides GraphQL document construction behind semantic clients and object contexts, which is the right application contract but makes development-time troubleshooting harder.
Developers currently need browser network tools to understand which targeted introspection, object-read, validation, mutation, collection, or action operation ran and what GraphQL response drove the resulting component state.
An opt-in structured diagnostic stream and reusable diagnostic panel will make that behavior observable without coupling the semantic components to a frontend framework or enabling remote telemetry.

## What Changes

- Add an opt-in GraphQL diagnostic observer at the foundation executor boundary so every introspection, read, secondary projection, validation, autocomplete, property update, and action invocation can publish a correlated lifecycle record.
- Record operation name and kind, endpoint, exact GraphQL document, redacted variables, start time, duration, HTTP outcome, redacted response data, GraphQL errors, transport failures, cancellation, and obsolete completion state where available.
- Add a bounded in-memory diagnostic store with filtering, retention limits, clear, subscription, and export contracts.
- Add configurable redaction and truncation policies that run before records reach observers or stores, mask hidden rich-wrapper values and common sensitive keys by default, and omit authorization material.
- Add an optional framework-neutral `<causeway-graphql-diagnostics>` light-DOM element for listing, filtering, expanding, copying, and clearing diagnostic records accessibly.
- Extend `sample-html` with an explicitly enabled developer diagnostics region that demonstrates requests and results without exposing the hidden sample value.
- Add deterministic executor, store, DOM, real-endpoint, browser, security, cancellation, and accessibility verification.

## Capabilities

### New Capabilities

- `graphql-web-component-diagnostics`: Provides opt-in, correlated, redacted GraphQL operation diagnostics and a reusable framework-neutral diagnostic presentation over the web-component foundation.

### Modified Capabilities

None.

## Impact

- Extends the web-component foundation executor and client configuration with optional observer hooks while preserving existing execution results and semantic component APIs.
- Adds a small bounded diagnostic store and an optional custom element to the packaged ESM artifact.
- Extends the vanilla-HTML sample and its stable automation hooks with developer-only diagnostics.
- Does not add remote telemetry, persistent logging, GraphQL replay, request mutation, or framework-specific tooling.
- Diagnostics remain disabled unless an application explicitly enables them.
- Redacted diagnostic copies, rather than live request or result objects, cross the observer boundary by default.
