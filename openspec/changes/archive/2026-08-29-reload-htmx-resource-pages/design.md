## Context

`HtmxClasspathPageLoader` currently discovers every private page resource, reads and validates its bytes, and creates an `HtmxPageDefinition` containing the resulting immutable HTML string.
`HtmxPageFragmentRegistry` then combines those definitions with Java fragment factories in one immutable logical-type map, and `HtmxPageRenderer` reads the stored string for every route.
This provides deterministic startup failure and efficient production rendering, but an IntelliJ resource build changes the classpath output file without changing the stored string.

The desired development loop is:

```text
edit existing HTML -> IntelliJ copies resource -> browser refresh -> current HTML renders
```

The change must not make private pages publicly fetchable, weaken startup conflict detection, silently substitute generic layout after an invalid edit, or add development-only dependencies to production applications.

## Goals / Non-Goals

**Goals:**

- Allow an application to opt into request-time content reload for existing startup-registered HTML pages.
- Preserve current cached immutable behavior as the default.
- Preserve startup discovery, identity, count, conflict, and initial-content validation in both modes.
- Reuse exactly the same bounded decoding rules during reload.
- Make the Petclinic sample usable with IntelliJ build and browser refresh without manual Spring restart.

**Non-Goals:**

- Discover newly added pages without restarting.
- Notice renamed or deleted registrations and rebuild the registry dynamically.
- Reload Java fragment factories, Java classes, application properties, CSS, JavaScript, grids, or menu resources through this mechanism.
- Read directly from a source-tree directory or expose a filesystem path through an HTTP endpoint.
- Add Spring Boot DevTools or an application-wide restart mechanism.

## Decisions

### Add an explicit resource-page mode

`HtmxViewerProperties` will expose a `ResourcePageMode` enum through `causeway.viewer.webcomponents.htmx.resource-page-mode` with values `CACHED` and `RELOAD`.
`CACHED` remains the default and retains the current startup-decoded immutable string behavior.
`RELOAD` reopens and validates the registered resource whenever its page definition renders.

An enum is preferred to an inverted cache Boolean because the operational behavior is visible in configuration and can be extended later without ambiguous double negatives.
Automatic detection of an exploded classpath is rejected because an application's behavior must not change implicitly between launch mechanisms.

### Keep the registry immutable

Startup will continue to enumerate the bounded classpath pattern and register an immutable set of logical-type definitions before accepting requests.
Every resource will be decoded and validated once during startup even in `RELOAD` mode, preserving fail-fast detection for initially defective resources.
Java factory definitions and resource definitions will continue to share the same duplicate check.

Reload therefore changes content ownership, not registration ownership:

```text
                      startup                         render
CACHED   Resource -> validate -> immutable HTML  -> stored HTML
RELOAD   Resource -> validate -> Resource handle -> read + validate -> current HTML
```

Classpath rescanning on every request is rejected because it would move page-count, duplicate, addition, deletion, and factory-conflict semantics into concurrent request handling.
A filesystem watch service is rejected because classpaths can contain directories and jars, watcher events can be missed, and lifecycle complexity is unnecessary for editing existing files.

### Centralize bounded decoding

The loader's current byte limit, non-empty check, UTF-8 decoder, NUL rejection, and bounded safe diagnostics will be extracted or exposed as one reusable resource-decoding operation.
Both startup validation and request-time reload will call that operation.
The reload path will not retain last-known-good HTML after a read or validation failure because stale fallback would conceal the developer's edit.
It will also not use generic object layout as fallback because a registered custom page remains authoritative.

### Retain resource handles only in reload mode

A resource page definition will retain either cached HTML or a reloadable resource-backed content provider according to the configured mode.
The provider will create a fresh input stream for each render and will not cache by timestamp.
Timestamp caching is rejected because classpath resources do not uniformly support reliable modification times and coarse timestamp resolution can miss rapid edits.

The immutable registry and page definitions remain safe for concurrent requests because reload uses request-local streams and state.

### Opt Petclinic into reload mode

The Petclinic sample will set `causeway.viewer.webcomponents.htmx.resource-page-mode=reload` in its application configuration.
This makes the sample's normal IntelliJ launch demonstrate the intended workflow without requiring an extra active profile.
Production applications remain cached unless they explicitly opt in.

The documentation will explain that IntelliJ must first copy the edited source resource into the running module's classpath output.
It will also describe IntelliJ automatic build and the setting that permits auto-make while the application is running as an optional way to remove the explicit build action.

## Risks / Trade-offs

- [Risk] Reload mode performs resource I/O for every custom object-page render. → Keep it opt-in, retain `CACHED` as the production default, and read only the selected registered page rather than rescanning all pages.
- [Risk] IntelliJ can expose a resource briefly while copying it. → Apply bounded validation on every read, fail the affected request safely, and allow the next refresh to retry.
- [Risk] Developers may expect newly added or renamed pages to appear immediately. → Document that registration identity remains startup-bound and test that reload affects content only.
- [Risk] A reload failure could accidentally reveal an absolute path or resource description. → Continue using bounded filename-based source identifiers and safe diagnostic codes.
- [Risk] Concurrent requests can observe different file generations. → Treat each request as an independent development-time read and retain no shared mutable content state.
- [Risk] Petclinic tests may assume immutable content. → Cover both modes explicitly and keep general HTMX test fixtures in the default cached mode unless a test opts in.

## Migration Plan

Add the configuration enum with `CACHED` as its default, refactor decoding without changing cached behavior, add reload-backed definitions, and then opt the Petclinic sample into `RELOAD`.
Applications require no migration.
Rollback consists of removing the Petclinic override or explicitly setting `resource-page-mode=cached`.

## Open Questions

None.
