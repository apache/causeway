## 1. Confirm Scope from Analysis

- [ ] 1.1 Map matrix entries `REF-VALUE-02`, `REF-VALUE-03`, and `REF-RESOURCE-01` to default support, application extension, output-only support, or explicit rejection.
- [ ] 1.2 Record compatibility exposure from current generic-string fallback and existing Blob or Clob shapes.
- [ ] 1.3 Verify prerequisites from object-interaction correctness and resource-link safety are complete.

## 2. Reversible Value Contracts

- [ ] 2.1 Select standard GraphQL scalar or structured shapes and canonical formats for every accepted standard datatype equivalence class.
- [ ] 2.2 Require reversible input coercion for every advertised property and action input.
- [ ] 2.3 Add canonical marshallers for the accepted temporal, URL, and related reference-app types.
- [ ] 2.4 Add actionable bounded diagnostics for output-only and unsupported values.
- [ ] 2.5 Define and test application marshaller plus GraphQL type-mapping registration.

## 3. Resource Value Contracts

- [ ] 3.1 Define common Blob, Clob, file, and other accepted resource metadata above the corrected resource-link contract.
- [ ] 3.2 Implement safe property read, update, action-parameter, and action-result behavior for supported resources.
- [ ] 3.3 Enforce accepted-file, media-type, size, authorization, and bounded-transfer policies.
- [ ] 3.4 Define explicit unsupported behavior for password, hidden, opaque, tree, and custom values lacking a registered strategy.

## 4. Compatibility and Verification

- [ ] 4.1 Add query, mutation, action, nullability, invalid-input, precision, timezone, and round-trip tests for every supported category.
- [ ] 4.2 Add security tests proving sensitive values cannot appear through output fallback, metadata, errors, resources, or diagnostics.
- [ ] 4.3 Add schema snapshots and migration tests for affected existing clients.
- [ ] 4.4 Document canonical formats, standard introspection, resource strategies, extension registration, compatibility behavior, and exclusions.
- [ ] 4.5 Run GraphQL model and viewer tests, reference-derived fixtures, documentation checks, formatting, and strict OpenSpec validation.
