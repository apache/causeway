## 1. Establish the main implementation baseline

- [ ] 1.1 Refresh target refs, record the actual main SHA and toolchain/BOM versions, and inspect target AGENTS.md and existing observation specs for intervening changes.
- [ ] 1.2 Prepare a main-based branch/worktree, transfer these planning artifacts, and commit uncommitted proposal artifacts before application. Do not implement in the maintenance checkout.
- [ ] 1.3 Inventory main observation creation/cleanup sites, registry beans, handlers, exporter predicates and existing tests; record the main-only instrumentation that must be preserved.
- [ ] 1.4 Characterize the target Boot-managed configuration and agent integration requirements; record a concrete wiring plan with one SDK/export owner per supported configuration. Revise the design before any broader default-ownership migration.

## 2. Harden observation lifecycle

- [ ] 2.1 Add focused main-helper tests for repeated close, duplicate start, scope-open failure, scope-close failure and stop failure, including current-context restoration.
- [ ] 2.2 Adapt main's existing closure to clear ownership and attempt ordered cleanup once; retain tag/discard support and any necessary binary-compatible error overload.
- [ ] 2.3 Support Throwable recording and update affected consumers so work failures remain primary when reporting/cleanup also fails; test Error and suppressed-exception behavior.

## 3. Make activation and registry selection explicit

- [ ] 3.1 Add Spring configuration tests for inactive/active profiles with no application registry, one supplied registry, a primary registry and ambiguous candidates.
- [ ] 3.2 Adjust main's wiring so inactive Causeway observations use no-op integration without disabling application observations, while active integration honors Spring registry selection and a fallback only when absent.
- [ ] 3.3 Verify active operation without an agent/exporter and normal Boot-managed tracing; implement only the explicit agent adaptation identified in task 1.4.

## 4. Verify threshold and discard behavior

- [ ] 4.1 Reproduce threshold-wrapper behavior directly, through fluent composition and the closure, and through its JPA observation-provider usage; distinguish confirmed defects from suspected ones.
- [ ] 4.2 Preserve wrapper lifecycle/policy through composition, retain short failures, and test below/at/above threshold behavior without timing-sensitive sleeps where possible.
- [ ] 4.3 Verify actual discard export behavior for each supported telemetry configuration; where agent export cannot honor it, disable duration suppression explicitly and document/test retained spans.

## 5. Establish exported-trace regression evidence

- [ ] 5.1 Adapt the maintenance child-process/OTLP fixture to main dependencies and Java, retaining output draining, bounded waits and startup diagnostics without copying legacy version pins.
- [ ] 5.2 Assert HTTP/framework-action/JDBC ancestry and one framework span per expected invocation in the supported agent configuration; exercise the production integration rather than a substitute tracer.
- [ ] 5.3 Add fixture coverage for profile off, no agent/exporter, failed work and a subsequent request without stale context; verify Boot-managed export separately.
- [ ] 5.4 Run focused lifecycle/configuration and affected runtime/JPA tests, including interaction nesting, property modification, transactions and publishing; account for every baseline instrumentation boundary.

## 6. Document and prepare review

- [ ] 6.1 Write main-appropriate activation, Boot/agent ownership and compatibility guidance with exact validated versions, supported exclusions, discard limitations and error-retention behavior.
- [ ] 6.2 Record executed checks and fixture evidence, update roadmap status/source-to-target mapping, and preserve the metadata-policy follow-up as separate work.
- [ ] 6.3 Validate the OpenSpec change and review the final diff for unrelated maintenance artifacts, legacy dependency pins, accidental instrumentation loss and unplanned public API changes.
