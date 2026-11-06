## Context

Maintenance `fb28fa67e06` retains nested specification requests and stabilizes mixin traversal to diagnose stack overflow before a later applicability prefilter fix.
V4 relocates its loader and specification implementations into `spec.impl`, uses REGISTER/TYPE_ONLY/FULL requests, has a legacy thread-safe state handler and an experimental facade, and already has phased/eager mixin bootstrap.
The port must preserve those transitions and caches rather than replay maintenance's older loader.

## Goals / Non-Goals

**Goals:** A useful retained type/request/state/caller chain on overflow, original error identity, deduplication during unwind, isolated leak-free state, deterministic mixin streams and representative real bootstrap coverage.

**Non-Goals:** Fixing recursion, prefiltering mixins (4061), changing stack size, changing loaders/state locks/cache/aliases, adding successful-load logs or telemetry/configuration APIs.

## Decisions

### Instrument the existing common nullable-load boundary

Wrap `SpecificationLoaderDefault.loadSpecificationNullable`, retaining the null bypass and existing substitution/classification/cache/phase-downgrade/introspection/alias behavior.
Record the requested Java type and request before loading; annotate the active frame with substituted type, effective request and a passive current-state snapshot once a cached/created spec is available.
Read legacy state through the existing package-private handler and facade phase through existing metadata variants, without forcing introspection or adding public state accessors.
An overflow during registration still has the requested type/request even when current state is unavailable.

### Keep diagnostics internal, best-effort and thread-local

Use a package-private helper in `spec.impl` with per-thread active frames and idempotent scopes.
Capture a compact caller through a lazily traversed StackWalker, excluding helper/loader plumbing and falling back to unavailable if capture fails.
Render retained frames only on overflow; successful requests do not log or build reports.
Deduplicate by error identity while the outer chain is active, report before unwinding loses frames, and release thread state once scopes and an optional test capture are gone.
Reporting or cleanup failure must not replace the original metamodel failure.
A package-private capture seam supports assertions about nesting without deliberately exhausting the JVM stack.
No application/instance data is captured.

### Stabilize only registry traversal

Sort `streamMixinTypes()` by `Class.getName()` at its existing shared registry boundary.
Both eager and on-the-fly discovery consume that stream; leave applicability, explicit layout/member ordering and later pipeline phases unchanged.
Do not add 4061's target-type prefilter.
Same-named classes loaded by different classloaders remain an existing unsupported/ambiguous ordering corner; the comparator guarantees ascending names, not a new classloader policy.

### Separate controlled failure tests from real bootstrap coverage

Focused tests exercise the real nullable-load method with controlled cached specifications/state handlers, injected nested overflows, phase downgrades, filtered/substituted/null paths and cleanup.
Helper tests verify complete ordered frames, caller capture, duplicate suppression, reporter failures, thread isolation and later independent work.
Registry tests permute input mixin insertion orders.
A small real domain-model fixture combines cyclic mixed-in collections, synthetic recording navigation and an action returning the other type, with explicit action order opposing class-name traversal.
Bootstrap it repeatedly and compare traversal/results without requiring v4 to reproduce maintenance's original overflow.
Record a non-gating warmed diagnostic-scope timing sample; do not claim a production-grade benchmark or introduce performance-sensitive test thresholds.

## Risks / Trade-offs

- Stack space may be scarce while reporting → Retain frames before failure, use compact formatting and suppress secondary diagnostic failures.
- Caller/frame capture adds successful-load overhead → Avoid normal report formatting/logging, measure a local sample and retain the source's diagnostic intent.
- Thread-local state could leak between bootstrap tasks → Remove it at the outermost close and test concurrent and failure paths.
- V4 may avoid the original recursion due to phased mixin loading → Use a real representative graph for behavior and controlled nested failures for reporting evidence.
- Fully qualified sorting could accidentally replace UI ordering → Assert explicit action sequence still wins.

## Migration Plan

No configuration, database or public API migration is required.
Overflow logs gain domain-type diagnostics and registry mixin traversal becomes reproducible.
Rollback removes the wrapper/helper and comparator without changing model declarations.

## Open Questions

None requiring a product decision.
