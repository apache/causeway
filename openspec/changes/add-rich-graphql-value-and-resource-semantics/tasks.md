## 1. Confirm Scope from Analysis

- [x] 1.1 Map matrix entries `REF-VALUE-02`, `REF-VALUE-03`, and `REF-RESOURCE-01` to default support, application extension, output-only support, or explicit rejection.
- [x] 1.2 Record compatibility exposure from current generic-string fallback and existing Blob or Clob shapes.
- [x] 1.3 Verify prerequisites from object-interaction correctness and resource-link safety are complete.

## 2. Reversible Value Contracts

- [x] 2.1 Select standard GraphQL scalar or structured shapes and canonical formats for every accepted standard datatype equivalence class.
- [x] 2.2 Require reversible input coercion for every advertised property and action input.
- [x] 2.3 Add canonical marshallers for the accepted temporal, URL, and related reference-app types.
- [x] 2.4 Add actionable bounded diagnostics for output-only and unsupported values.
- [x] 2.5 Define and test application marshaller plus GraphQL type-mapping registration.

## 3. Resource Value Contracts

- [x] 3.1 Define common Blob, Clob, file, and other accepted resource metadata above the corrected resource-link contract.
- [x] 3.2 Implement safe property read, update, action-parameter, and action-result behavior for supported resources.
- [x] 3.3 Enforce accepted-file, media-type, size, authorization, and bounded-transfer policies.
- [x] 3.4 Define explicit unsupported behavior for password, hidden, opaque, tree, and custom values lacking a registered strategy.

## 4. Compatibility and Verification

- [x] 4.1 Add query, mutation, action, nullability, invalid-input, precision, timezone, and round-trip tests for every supported category.
- [x] 4.2 Add security tests proving sensitive values cannot appear through output fallback, metadata, errors, resources, or diagnostics.
- [x] 4.3 Add schema snapshots and migration tests for affected existing clients.
- [x] 4.4 Document canonical formats, standard introspection, resource strategies, extension registration, compatibility behavior, and exclusions.
- [x] 4.5 Run GraphQL model and viewer tests, reference-derived fixtures, documentation checks, formatting, and strict OpenSpec validation.

## 5. Fail-Closed Built-In Coverage Delta

- [x] 5.1 Inventory and classify every framework-provided `ValueSemanticsProvider` as reversible, structured, protected, output-only, or unsupported.
- [x] 5.2 Make `ScalarMarshaller` input support an explicit opt-in while keeping existing implementations binary-safe and output-capable.
- [x] 5.3 Replace implicit unknown-value `toString()` output with a non-disclosing default and an explicit temporary `LEGACY_STRING` migration policy.
- [x] 5.4 Add canonical mappings for the remaining selected scalar, password, markup, and local-resource-path built-ins while explicitly rejecting composite, image, DTO, and tree values.
- [x] 5.5 Add closed-inventory, migration, introspection, round-trip, and non-disclosure tests plus corresponding configuration and user documentation.
- [x] 5.6 Regenerate the schema and rerun GraphQL model, viewer, reference-derived, documentation, formatting, approval, and strict OpenSpec validation checks.
