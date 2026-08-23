## 1. Establish failing value-semantics baselines

- [ ] 1.1 Add foundation codec tests reproducing `Long`, `BigInteger`, and `BigDecimal` precision loss, scale loss, malformed lexical input, and null handling.
- [ ] 1.2 Add foundation tests reproducing nullable Boolean collapse and required Boolean behavior.
- [ ] 1.3 Add foundation tests for local, offset, zoned, legacy, fractional-second, and malformed temporal inputs.
- [ ] 1.4 Inventory effective GraphQL scalar names, input shapes, nullability, resource metadata, protected-value metadata, and custom-value strategies used by the editor contexts.
- [ ] 1.5 Add GraphQL integration tests reproducing advertised exact-numeric strings reaching property or action invocation as incompatible Java values.

## 2. Introduce toolkit-neutral value codecs

- [ ] 2.1 Add a value-codec registry with deterministic priority, named-type discovery, authoritative normalization, control conversion, parsing, and GraphQL-variable conversion contracts.
- [ ] 2.2 Add a fail-closed unsupported codec that never converts display output into input implicitly.
- [ ] 2.3 Refactor editor selection so native and optional toolkit editors consume codec contexts without changing public Causeway elements or semantic events.
- [ ] 2.4 Route property validation and mutation values through the selected codec while retaining pending lexical state after rejection.
- [ ] 2.5 Route action defaults, dependent preparation, validation, and invocation values through the selected codec with stale-response and cancellation protection.
- [ ] 2.6 Export documented codec registration hooks for application-specific reversible values without exposing internal widget APIs.

## 3. Correct numeric and Boolean semantics

- [ ] 3.1 Implement exact lexical codecs for `Long`, `BigInteger`, and `BigDecimal` without JavaScript `Number` conversion.
- [ ] 3.2 Validate exact numeric sign, digits, decimal, exponent, range, required, and null rules with bounded field errors.
- [ ] 3.3 Preserve decimal scale and trailing zeros through pending edit, validation, GraphQL variables, failure, cancellation, and authoritative reconciliation.
- [ ] 3.4 Restrict JavaScript-number codecs to explicitly representable scalar and range contracts and test their boundaries.
- [ ] 3.5 Implement distinct required two-state and nullable three-state Boolean editor behavior.
- [ ] 3.6 Add property and action tests proving exact numeric and nullable Boolean defaults, choices, validation, invocation, cancellation, and stale-result handling.

## 4. Complete temporal semantics

- [ ] 4.1 Implement local date, local time, and local date-time codecs that preserve local meaning and accepted fractional precision.
- [ ] 4.2 Implement offset date-time and offset time codecs that validate and preserve explicit offsets without browser-timezone conversion.
- [ ] 4.3 Implement zoned date-time codec behavior using the effective advertised or configured format while preserving required zone information.
- [ ] 4.4 Classify legacy temporal inputs explicitly and provide reversible codecs only where the public GraphQL contract is complete.
- [ ] 4.5 Add property and action tests for timezone boundaries, daylight-saving transitions, fractional seconds, malformed values, nulls, cancellation, and authoritative reconciliation.

## 5. Enforce protected, resource, and custom capabilities

- [ ] 5.1 Implement safe URL input validation and preserve the existing resource-link output policy.
- [ ] 5.2 Implement write-only password or protected-value handling that never exposes prior or submitted secrets in markup, events, errors, logs, or reconciliation.
- [ ] 5.3 Inventory Blob, Clob, and local-resource input constraints and enable editors only when authorization, media, size, and representation metadata are complete.
- [ ] 5.4 Add bounded Blob, Clob, and local-resource tests for accepted input, media mismatch, size rejection, cancellation, and unsupported metadata.
- [ ] 5.5 Require custom value codecs to opt into reversible input explicitly and keep output-only custom values visibly unsupported for editing.
- [ ] 5.6 Add extension tests proving registered custom codecs round-trip declared domain values and unregistered strategies fail closed.

## 6. Align rich GraphQL schema and runtime conversion

- [ ] 6.1 Add scalar marshaller unit tests for exact numeric, nullable Boolean, URL, local, offset, zoned, protected, resource, and custom input representations.
- [ ] 6.2 Fix shared property mutation unmarshalling so the scalar accepted by introspection is converted to the declared domain Java type before validation and invocation.
- [ ] 6.3 Fix shared action argument unmarshalling through the same type-mapper path and reject incompatible values before domain invocation.
- [ ] 6.4 Preserve exact numeric scale, temporal offset or zone, nullable state, and bounded validation errors through rich GraphQL end-to-end tests.
- [ ] 6.5 Verify simple-schema behavior and existing compatibility configuration remain explicit and do not regain implicit lossy input.
- [ ] 6.6 Add regression tests proving malformed or unsupported input does not invoke domain code or expose implementation exceptions.

## 7. Qualify the corrected baseline

- [ ] 7.1 Expand the Reference Application target catalogue and Playwright journeys for exact numeric, nullable Boolean, temporal, URL, protected, resource, and custom-value cases.
- [ ] 7.2 Regenerate and review capability inventory classifications only after focused successful and rejected journeys pass.
- [ ] 7.3 Run foundation Node tests, GraphQL model and integration tests, HTMX Maven tests, Petclinic browser tests, and Reference Application browser tests.
- [ ] 7.4 Run strict CSP, accessibility, keyboard, light, dark, narrow, reduced-motion, forced-colors, external-request, console-error, page-error, and overflow gates.
- [ ] 7.5 Document accepted lexical contracts, intentional compatibility tightening, extension registration, reproduction commands, and rollback.
- [ ] 7.6 Verify production dependencies, public GraphQL operation names, Causeway elements, semantic events, routes, assets, and Vaadin default-selection policy remain unchanged.
