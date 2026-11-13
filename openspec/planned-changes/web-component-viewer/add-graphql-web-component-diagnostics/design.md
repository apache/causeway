## Context

The foundation centralizes GraphQL execution behind its executor, client, object contexts, and semantic commands.
That boundary keeps page composition framework-neutral and prevents components from constructing generated rich-schema operations, but it also means a developer must open browser network tooling to understand the exact operation and response behind a component state.
The sample currently exposes semantic interaction diagnostics rather than transport-level request and response details.
This change adds optional development-time observability without changing the semantic component contract, persisting diagnostic data, or introducing remote telemetry.

## Goals / Non-Goals

**Goals:**

- Make every foundation GraphQL operation observable through one structured opt-in contract.
- Correlate request start, completion, failure, cancellation, and obsolete outcomes.
- Show exact GraphQL documents together with safely copied variables, response data, and errors.
- Protect hidden and sensitive values with redaction before diagnostic publication.
- Bound memory use and permit filtering, clearing, copying, and exporting local records.
- Provide an accessible framework-neutral diagnostic custom element and executable sample coverage.
- Preserve executor, client, object-context, and semantic-component behavior when diagnostics are disabled or enabled.

**Non-Goals:**

- Remote telemetry, analytics, tracing backends, or production monitoring.
- Persisting diagnostic records across page loads.
- Capturing authorization headers, cookies, or browser credentials.
- Replaying, editing, or resubmitting GraphQL requests.
- Replacing browser developer tools for low-level HTTP investigation.
- Making diagnostics mandatory for component-library or generic-viewer consumers.
- Revealing values hidden by Causeway semantics.

## Decisions

### Instrument the shared executor boundary

The diagnostic observer will attach where the executor has the endpoint, operation document, variables, operation name, abort signal, parsed GraphQL response, and transport outcome.
This captures targeted introspection, primary and secondary reads, validation, choices, autocomplete, property updates, safe actions, and mutations without adding instrumentation to each semantic command.

Instrumentation will not wrap global `fetch`, monkey-patch browser APIs, or require components to know about diagnostics.
A custom executor can publish equivalent records through the same observer contract.

### Publish correlated immutable lifecycle records

Each operation will receive a monotonic local correlation identifier.
A start record will describe the operation before transport, and one terminal record will classify success, partial GraphQL success, GraphQL failure, transport failure, cancellation, or an obsolete result known at the publishing layer.

Records will include timestamps and elapsed duration so the presentation can retain execution order even when operations complete out of order.
Diagnostic snapshots will be detached from live variables and results so observers cannot mutate execution state.

### Separate observation storage and presentation

The executor will publish to a small observer interface rather than directly managing UI state.
A reusable bounded store will subscribe to records, combine correlated lifecycle entries, evict oldest completed operations, filter views, clear records, and export redacted JSON.
The diagnostic custom element will consume that store or another compatible source.

Applications can therefore send records to a test harness, custom logger, or bespoke panel without importing the standard presentation.

### Keep diagnostics explicitly disabled by default

Existing clients will create no store and publish no payload snapshots unless diagnostics are configured.
The standard sample can enable diagnostics because it is an executable development fixture, while documentation will show production applications leaving the feature disabled.
Observer exceptions will be isolated and will never fail or delay the GraphQL operation.

### Redact before crossing the observer boundary

Authorization headers and browser credentials will never enter diagnostic records.
The default policy will recursively mask common sensitive key names and rich member payload values when the same wrapper reports `hidden: true`.
Large strings, blobs, lists, documents, variables, and response subtrees will be truncated according to configurable limits.

Redaction and truncation will operate on detached copies before publication, storage, rendering, copy, or export.
Applications may install a stricter policy and may explicitly relax a policy for controlled local development, but the sample and standard UI will use secure defaults.
The raw unredacted payload will not be retained by the standard store.

### Preserve exact operation structure

The record will retain the exact GraphQL document and operation name submitted by the executor.
Variable and response values will retain their JSON structure after redaction rather than being flattened to log strings.
The UI will distinguish operation kind, endpoint, variables, response data, GraphQL errors, and transport diagnostics so developers can verify query-versus-mutation placement and generated rich-schema argument shape.

### Provide a bounded local store

The standard store will use a configurable maximum operation count and per-record size limits.
Pending operations will remain visible while active, and completed records will be evicted oldest-first after the bound is exceeded.
Clearing the store will not cancel active GraphQL operations, and later lifecycle records may recreate an entry for an operation that completes after a clear.

The first version will remain memory-only and page-local.

### Render diagnostics as an optional semantic light-DOM element

`<cw-graphql-diagnostics>` will render an accessible list of operation summaries with status, type, name, and duration.
Users can filter by operation kind or outcome, expand request and response sections, copy one redacted operation, export the bounded set, and clear completed records.

The element will use native controls, keyboard navigation, labelled details, live lifecycle announcements, and documented light-DOM classes.
It will not depend on HTMX or another frontend framework.

### Demonstrate diagnostics without leaking the hidden fixture

The vanilla-HTML sample will explicitly create a diagnostic store and panel near its semantic diagnostics.
Its browser hooks will make initial introspection, coordinated reads, collection operations, property commands, safe actions, and mutations observable.
Security tests will assert that the hidden sample value and configured sensitive markers do not occur in rendered diagnostic text or exported records.

## Risks / Trade-offs

- [Request and response payloads can contain sensitive data] → Disable diagnostics by default, omit credentials, redact before publication, mask hidden wrappers, document local-only enablement, and test non-disclosure.
- [Capturing full payloads can consume memory] → Use bounded retention, detached snapshots, per-value and per-record truncation, and no persistence.
- [Snapshot copying can affect request latency] → Avoid all copying when disabled, keep observers synchronous only for small metadata, and perform bounded snapshot work without blocking semantic reconciliation more than necessary.
- [Observer failures could destabilize GraphQL execution] → Isolate observer exceptions and make diagnostic publication best-effort.
- [Concurrent operations can produce confusing ordering] → Use correlation identifiers, start and completion timestamps, operation state, and deterministic display ordering.
- [Executor-level diagnostics may not know object-context obsolete state] → Distinguish transport cancellation from higher-level obsolescence and allow context layers to annotate an existing correlation when that distinction is known.
- [A diagnostic panel can become a general developer console] → Limit the first version to passive inspection, filtering, copying, export, and clear behavior.

## Migration Plan

The observer options are additive and default to disabled.
Existing client and executor construction remains valid without configuration changes.
Applications can enable a bounded store and panel only in development profiles, and can remove them without changing semantic components or GraphQL operations.

## Open Questions

- Should higher-level object-context commands add semantic member and command labels to executor records, or should the first version remain transport-oriented?
- Should explicit local opt-in ever permit completely unredacted result values, or should at least hidden-wrapper masking remain mandatory?
- Which default limits best balance useful query and result inspection against memory and rendering cost?
- Should the generic HTMX viewer expose the same panel through a development-mode shell region once both planned changes exist?
