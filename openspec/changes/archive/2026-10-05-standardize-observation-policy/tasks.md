## 1. Configuration and policy plumbing

- [x] 1.1 Add typed observation policy with independent identity flags, default-disabled duration filtering and validated JPA duration threshold; update configuration metadata and constructor compatibility.
- [x] 1.2 Wire the policy through auto-configuration, integration and interaction tagging without changing registry selection or activation; test default/explicit flags, malformed/negative/zero durations and inactive-profile behavior.

## 2. Existing observation names and metadata

- [x] 2.1 Apply the JPA operation mapping and safe static contextual metadata, removing bookmark/query-description values; source the threshold from configuration.
- [x] 2.2 Separate operation names from static contextual member names in action/property execution, and move publishing subscriber count out of names and low-cardinality dimensions.
- [x] 2.3 Apply independent opt-ins to username and tenancy tagging; test all option combinations, absent values and preservation of non-identity metadata.
- [x] 2.4 Add focused observation-context tests for the name mapping and sentinel-value exclusion across names/tags, including multiple instances, named-query arguments and missing static metadata; verify existing instrumentation boundaries and invoked member identity remain intact.

## 3. Exported behavior and regression evidence

- [x] 3.1 Extend the Boot/agent compatibility fixture to assert actual exported contextual names, identity defaults/opt-ins, absence of duplicate framework spans and default retention of short JPA parents with JDBC children under deterministic sampling.
- [x] 3.2 Cover explicit Boot filtering at below/equal/above configurable thresholds, zero and short failures; retain agent-mode filtering-disabled coverage and fluent lifecycle guarantees.
- [x] 3.3 Run affected module tests, both export fixture modes and relevant interaction regressions on main's supported toolchain; record commands, versions, results and any exporter limitations in validation.md.

## 4. Operator guidance

- [x] 4.1 Update the observability guide with the operation/contextual-name migration table, identity options, threshold examples, default volume impact, lossy-filter parentage limitation and supported Boot/agent configurations.
- [x] 4.2 Validate configuration examples against binding tests and build the affected documentation; retain later semantic naming, correlation and collection budgets as separate roadmap work.
