## 1. Pin Resource Evidence

- [x] 1.1 Translate matrix entries `REF-RESOURCE-02`, `REF-RESOURCE-03`, and `REF-LAYOUT-01` into reduced deterministic tests.
- [x] 1.2 Record compatibility behavior for the existing global response-type setting and manually normalized clients.

## 2. Correct Resource References

- [x] 2.1 Replace slash concatenation with deployment-aware endpoint path construction.
- [x] 2.2 Verify exactly one same-origin path prefix under root, servlet-context, reverse-proxy, and non-default endpoint deployments.
- [x] 2.3 Preserve opaque encoded identities without double encoding or content disclosure.

## 3. Separate and Enforce Policy

- [x] 3.1 Add independent structural-metadata and value-content resource policy.
- [x] 3.2 Define migration behavior for the existing global resource response type.
- [x] 3.3 Omit or null forbidden resource capabilities without publishing unusable object-bearing references.
- [x] 3.4 Recheck authentication, authorization, target identity, member visibility, and category policy on every dereference.

## 4. HTTP and Security Verification

- [x] 4.1 Add grid, icon, Blob, and Clob success tests with media type, length, and cache behavior.
- [x] 4.2 Add malformed, stale, hidden, unauthorized, and forbidden tests proving bounded non-disclosing responses.
- [x] 4.3 Add diagnostics and logging tests proving resource content and unredacted identifiers are absent.

## 5. Compatibility and Documentation

- [x] 5.1 Select field omission or nullable-field migration behavior through schema compatibility tests.
- [x] 5.2 Document path resolution, policy categories, defaults, migration aliases, authorization, caching, and client treatment of opaque references.
- [x] 5.3 Run GraphQL model and viewer tests, resource-controller tests, reference-derived fixtures, documentation checks, formatting, and strict OpenSpec validation.
