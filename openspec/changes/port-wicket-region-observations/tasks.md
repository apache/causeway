## 1. Main lifecycle and static identity

- [ ] 1.1 Add lifecycle characterization tests for DomainObjectPage initialization, reused-page configuration, descendant before-render work and Ajax subtree rendering; map maintenance component/model hooks to main.
- [ ] 1.2 Implement serializable region descriptors and required core formatter extensions; test operation/display distinctions, complete contributed action signatures, default fieldsets, casing, long-name collisions and instance-data privacy.

## 2. Request ownership and region instrumentation

- [ ] 2.1 Implement shared render behavior and request-local tracking with idempotent LIFO cleanup, error preservation and continued unwinding after a close failure; integrate cleanup before existing request/interaction parent closure.
- [ ] 2.2 Add domain-object page preparation and render observations at the characterized main hooks, including construction and descendant preparation without duplicate phases or forced model resolution.
- [ ] 2.3 Attach fieldset, regular domain property, parented collection and object-action control regions to main's components, preserving hidden-region and parameter/table exclusions.
- [ ] 2.4 Add contributed action-prompt regions in ActionParametersPanel with full domain identity and natural prompt/member ancestry.

## 3. Lifecycle and export evidence

- [ ] 3.1 Test full-page/Ajax nesting, preparation/render failures, skipped callbacks, detach, repeated completion, cleanup failures and failure-followed-by-success on a reused worker.
- [ ] 3.2 Test serialization/deserialization during and after observation; confirm descriptors survive without live registry/tracer/scope/request state and subsequent renders start fresh observations.
- [ ] 3.3 Extend real Wicket lifecycle exported-trace fixtures for Boot and agent modes; assert semantic names/full identities, one region per render, HTTP/framework/interaction ancestry, errors and inactive/missing integration behavior without competing telemetry infrastructure.

## 4. Guidance and delivery checks

- [ ] 4.1 Integrate verified Petclinic Wicket page/collection/prompt examples into the M3 otel-howto and observability guide, preserving the current Boot-first/agent flow and distinguishing HTMX and later table/root features without before/after comparisons or new launcher flags.
- [ ] 4.2 Run relevant Wicket/core tests, exported tracing compatibility checks and documentation rendering; record baseline versions, main lifecycle adaptations, commands, results and any limits in validation.md.
