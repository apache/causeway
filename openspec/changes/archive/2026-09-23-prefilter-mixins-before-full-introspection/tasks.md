## 1. Characterize type-stage mixin metadata

- [x] 1.1 Add tests proving supported action, property, collection, and `@DomainObject` mixin styles expose applicability and main-method metadata at `TYPE_INTROSPECTED`.
- [x] 1.2 Add a diagnostic-chain test showing the current production-shaped fixture requests full introspection for inapplicable mixins before the correction.
- [x] 1.3 Confirm malformed registered mixins remain covered by metamodel validation independently of contribution to a particular target.

## 2. Implement applicability prefiltering

- [x] 2.1 Add a shared internal helper that type-introspects a candidate and returns it only when its `MixinFacet` applies to the target specification.
- [x] 2.2 Update mixed-in association discovery to fully introspect only candidates accepted by the helper.
- [x] 2.3 Update mixed-in action discovery to fully introspect only accepted candidates while preserving the domain-service `Object_` exclusion.
- [x] 2.4 Preserve deterministic class-name traversal and existing member filtering and construction after a candidate is accepted.

## 3. Verify functional compatibility

- [x] 3.1 Add tests proving inapplicable action, property, and collection mixins are not fully introspected or contributed.
- [x] 3.2 Add tests proving applicable action, property, and collection mixins are fully introspected and contribute the expected identifiers and facets.
- [x] 3.3 Verify explicit member ordering still overrides class-name traversal order.
- [x] 3.4 Verify applicable parented mixed-in collections still synthesize navigation actions when command recording is enabled.

## 4. Verify recursion-depth reduction

- [x] 4.1 Extend the production-shaped fixture with many deterministic inapplicable mixins followed by applicable candidates.
- [x] 4.2 Use the introspection diagnostic seam to assert inapplicable candidates receive type-level checks without mixed-in-discovery requests for full introspection.
- [x] 4.3 Run focused metamodel and full-boot recording-navigation tests with a constrained `-Xss256k` stack.
- [x] 4.4 Document the before-and-after nested full-introspection depth and any remaining dominant recursion path for production verification.
