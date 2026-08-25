## ADDED Requirements

### Requirement: Common internal editor toolkit policy
The HTMX viewer SHALL expose `causeway.viewer.webcomponents.htmx.editor-toolkit` with the bounded values `vaadin` and `native` and SHALL default effectively to `vaadin`.
An explicitly configured common policy MUST take precedence over deprecated reference-widget and field-family properties.

#### Scenario: No toolkit property is configured
- **WHEN** an application starts without the common or deprecated toolkit properties
- **THEN** the resolved policy enables qualified reference, basic, numeric, and local-temporal Vaadin adapters
- **AND** unsupported shapes and failed closures retain native or explicit unsupported presentation

#### Scenario: Native policy is explicit
- **WHEN** `editor-toolkit=native` is configured
- **THEN** the shell explicitly disables reference and field Vaadin adapters and emits no Vaadin CSP hashes
- **AND** routes request no reference or field-family closure

#### Scenario: Common policy overrides deprecated properties
- **WHEN** the common property and one or both deprecated properties are configured with conflicting values
- **THEN** the resolved common policy determines references, every qualified field family, and CSP hashes
- **AND** the deprecated values cannot create a mixed or broadened policy

#### Scenario: Only deprecated properties are configured
- **WHEN** the common property is absent and either deprecated property is explicitly configured
- **THEN** the viewer preserves the former complete policy in which references default false and field families default empty unless their corresponding old value is supplied
- **AND** shell diagnostics identify compatibility policy without changing application markup

#### Scenario: Toolkit value is invalid
- **WHEN** configuration supplies a value other than `vaadin` or `native`
- **THEN** configuration binding rejects it with a bounded error
- **AND** the viewer does not silently select a broader policy

### Requirement: Default route-lazy toolkit delivery
The generic HTMX viewer SHALL enable every qualified packaged adapter by default without eagerly importing any reference or field-family closure.
A closure MUST load only after an eligible connected Causeway editor selects its internal adapter.

#### Scenario: Route contains one eligible family
- **WHEN** the first editor eligible for one default family connects
- **THEN** only that family's same-origin closure is requested and upgraded
- **AND** route readiness and other families do not wait for it

#### Scenario: Route contains no eligible editor
- **WHEN** a landing, menu-only, read-only, custom, or other unaffected route renders
- **THEN** it requests zero reference, basic, numeric, and local-temporal Vaadin assets
- **AND** default CSP hash permission does not cause a network request

#### Scenario: One family fails
- **WHEN** a default family closure fails to load or define its controls
- **THEN** its existing Causeway failure boundary activates the matching native implementation
- **AND** other family closures remain independently eligible and lazy

### Requirement: Supported exact-hash toolkit CSP
The HTMX response CSP SHALL include only the generated reviewed style-hash union for the resolved internal toolkit policy.
It MUST retain `style-src-attr 'none'`, same-origin script and connection sources, and no `unsafe-inline` source.

#### Scenario: Default policy renders CSP
- **WHEN** the effective policy is the supported Vaadin default
- **THEN** `style-src` and `style-src-elem` contain the deterministic deduplicated reference and field-family hash union
- **AND** every hash corresponds to pinned generated policy metadata

#### Scenario: Deprecated subset policy renders CSP
- **WHEN** compatibility mode enables only a subset of old adapters
- **THEN** CSP contains exactly the reviewed union required by that resolved subset
- **AND** disabled-family-only hashes are absent where generated policy distinguishes them

#### Scenario: Native policy renders CSP
- **WHEN** the effective common policy is native
- **THEN** CSP contains no Vaadin style hash
- **AND** route, GraphQL, application stylesheet, and canonical identity policy remains unchanged

### Requirement: Supported default and native release qualification
The viewer SHALL treat default-Vaadin and explicit-native modes as supported release configurations rather than sample-scoped pilot modes.
Petclinic, the vanilla sample, the pinned Reference Application, deterministic packaging, strict CSP, accessibility, browser isolation, bundle budgets, licenses, vulnerabilities, and ordinary Maven packaging MUST remain passing gates.

#### Scenario: Default release matrix runs
- **WHEN** release qualification runs with no toolkit override
- **THEN** eligible reference and field journeys use internal Vaadin adapters and preserve authoritative outcomes
- **AND** unexpected CSP, accessibility, console, page, external-request, stale-state, focus, overlay, or overflow failures fail the gate

#### Scenario: Native release matrix runs
- **WHEN** the same journeys run with `editor-toolkit=native`
- **THEN** native controls preserve the same GraphQL values, routes, interactions, and classifications
- **AND** all Vaadin closure requests and style hashes are absent

## REMOVED Requirements

### Requirement: Opt-in route-lazy reference widget delivery
**Reason**: The qualified reference adapter is now enabled through the common supported-default policy while remaining route-lazy.
**Migration**: Use the common native policy for rollback instead of relying on omitted pilot configuration.

### Requirement: Production CSP compatibility for candidate widgets
**Reason**: Exact-hash CSP is now a supported release requirement for the resolved common toolkit policy rather than candidate-only evidence.
**Migration**: Regenerate and review the same pinned policy metadata when dependencies change.

### Requirement: Sample-scoped pilot qualification
**Reason**: Petclinic and vanilla qualification now form part of the supported default and native release matrix.
**Migration**: Run both common policy modes instead of a sample-specific pilot toggle.

### Requirement: Explicit route-lazy field-family delivery
**Reason**: All qualified field families are enabled by the common Vaadin default while retaining independent lazy imports.
**Migration**: Use `editor-toolkit=native` for complete rollback or deprecated family configuration only during the compatibility period.

### Requirement: Exact-hash field-family CSP
**Reason**: Reference and field hashes are governed together by the resolved common exact-hash toolkit CSP requirement.
**Migration**: Verify the resolved default, native, and deprecated subset hash unions through the common policy tests.
