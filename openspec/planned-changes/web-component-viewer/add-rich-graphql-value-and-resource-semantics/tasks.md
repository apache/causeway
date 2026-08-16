## 1. Confirm Scope from Analysis

- [ ] 1.1 Map accepted value and resource matrix entries to standard support, application extension, output-only support, or explicit rejection.
- [ ] 1.2 Record compatibility exposure from current generic-string fallback and existing Blob or Clob shapes.

## 2. Value Descriptor and Marshalling Contracts

- [ ] 2.1 Define the rich datatype descriptor and targeted-introspection shape.
- [ ] 2.2 Require reversible input coercion for every advertised property and action input.
- [ ] 2.3 Add canonical marshallers for the confirmed missing standard temporal, URL, and related reference-app types.
- [ ] 2.4 Add actionable schema or capability diagnostics for unsupported values.
- [ ] 2.5 Define and test application marshaller and descriptor registration.

## 3. Resource Contracts

- [ ] 3.1 Define common Blob, Clob, file, image, markup, and other confirmed resource metadata.
- [ ] 3.2 Implement safe property read, update, action-parameter, and action-result behavior for supported resources.
- [ ] 3.3 Enforce accepted-file, media-type, size, authorization, and bounded-transfer policies.
- [ ] 3.4 Define explicit unsupported behavior for password, hidden, opaque, tree, composite, and custom values lacking a registered strategy.

## 4. Compatibility and Verification

- [ ] 4.1 Add query, mutation, action, nullability, invalid-input, precision, timezone, and round-trip tests for every supported category.
- [ ] 4.2 Add security tests proving sensitive values cannot appear through output fallback, metadata, errors, or diagnostics.
- [ ] 4.3 Add schema snapshots and migration tests for affected existing clients.
- [ ] 4.4 Document canonical formats, descriptors, resource policies, extension registration, compatibility behavior, and exclusions.
- [ ] 4.5 Run GraphQL model and viewer tests, reference-derived fixtures, documentation checks, formatting, and strict OpenSpec validation.
