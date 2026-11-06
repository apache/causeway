## Context

Maintenance `473454f2447` observes individual callbacks inside the priming registry, avoiding public SPI or call-site changes.
V4's priming registry atomically publishes immutable snapshots, and its existing action/Wicket hooks already enforce effective arguments, authorization and request-local admission.
The observation integration and robust closure are shared by Boot and Java-agent ownership.

## Goals / Non-Goals

**Goals:** One semantic observation per actual callback, natural JDBC ancestry, bounded private metadata, unchanged deterministic/fail-fast/no-op behavior and exported evidence under both owners.

**Non-Goals:** Aggregate primer spans, per-query instrumentation, callback labels/implementation identities, registration or public API changes, synthetic parent spans, new configuration or dependencies.

## Decisions

### Wrap the registry's individual callbacks

Resolve the optional active observation integration only after a matching key is found, retaining a direct callback path when the integration is absent/no-op.
Use `causeway.priming.action` and `causeway.priming.view` with existing naming helpers for `prime action <type>#<name>` and `prime view <type>` displays.
Preserve the registration snapshot, list order and existing action/view admission; do not instrument the enclosing `primeAction`/`primeView` loop.
Multiple callbacks therefore produce sequential siblings and unmatched calls produce no spans.

### Retain validated canonical metadata

Capture logical target type, signature-free display member key and full canonical action identifier when validating registrations.
Rebuild the canonical action identifier with the exact registered domain class/logical type, resolved local action name and resolved metamodel parameter type names.
This adapts maintenance's placeholder `key + "()"` to v4's existing full-signature action metadata while keeping contributed action identity domain-facing.
Add only canonical type/action and framework bean/module tags; do not expose callback classes, lambda names, sequence/counts, targets, arguments, values, users or transaction/interaction IDs.

### Preserve ownership and primary failures

Use the existing provider/registry and v4's robust `ObservationClosure`, recording callback runtime exceptions/errors and rethrowing the identical failure after cleanup.
Keep failure-first callback short-circuiting; secondary reporting/cleanup failures must not replace the callback failure.
Do not install tracing infrastructure or manipulate parents: current action/request/interaction scopes and automatic JDBC instrumentation determine ancestry.

### Validate registry behavior and real exports

Focused tests cover action/view single and multiple callbacks, unmatched keys, bounded names, complete signatures, contributed identities, exact tag sets, callback ordering, runtime/error cleanup and absent/no-op integration.
Retain all prior action and Wicket hook tests to verify no admission or lifecycle changes.
Extend the existing HTTP/Wicket export fixture with a real finalized primer registry and controlled H2 callbacks.
Exercise the production authorized page boundary before preparation, and dispatch action primers under the existing observed action; use the 10a real-executor tests to establish the before-domain-method hook independently.
Test view/action failure followed by success, request admission, JDBC descendants, sibling order and inactive/absent-exporter behavior under Boot and agent ownership.

## Risks / Trade-offs

- Multiple callbacks share names → Keep them as separate spans distinguished by timing/children, not implementation labels.
- Signatures differ from maintenance placeholder metadata → Retain v4's canonical conventions and explicitly test complete static identifiers.
- A callback failure could leak scope or mask its original cause → Reuse the shared robust closure and test suppression/restoration.
- View observations might accidentally fall beneath preparation → Exercise the actual shared authorization/admission boundary before preparation in the request fixture.
- Registry no-op behavior could depend on tracing beans → Keep the optional absent/no-op integration path and test it explicitly.

## Migration Plan

No application, configuration, schema or data migration is needed.
Active observation deployments gain one additional span per invoked primer and should account for that volume.
Inactive/no-exporter deployments retain callback execution without primer exports.
Rollback removes the wrappers without altering registrations or persistence work.

## Open Questions

None requiring a product decision.
