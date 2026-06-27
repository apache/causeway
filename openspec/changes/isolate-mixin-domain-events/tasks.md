## 1. Characterize the shared-facet regression

- [x] 1.1 Add a focused recording-support-enabled metamodel test with a generic mixed-in action contributed to two unrelated domain types where only one domain type declares an action domain-event default.
- [x] 1.2 Assert the mixed-in action on the annotated domain type can resolve that domain type's event default without mutating the shared mixin method facet.
- [x] 1.3 Assert the mixed-in action on the unrelated domain type does not expose the annotated domain type's event default.
- [x] 1.4 Include generated parented collection navigation action synthesis in the regression setup so the test exercises the recording-support post-processing path.
- [x] 1.5 Include generated scalar reference navigation action synthesis in the regression setup or add a parallel focused test for the scalar reference path.

## 2. Isolate mixee-specific domain-event facets in post-processing

- [x] 2.1 Update `SynthesizeDomainEventsForMixinPostProcessor` so mixee-specific action domain-event defaults are applied as local object-type-specific facets on the mixed-in member rather than by mutating the shared mixin method facet.
- [x] 2.2 Add the smallest helper, factory method, or facet subtype needed to create the local action domain-event overlay without changing normal action facet installation.
- [x] 2.3 Preserve explicit action domain-event annotations declared on application-authored mixins.
- [x] 2.4 Review property and collection branches in the same postprocessor and either apply the same local-overlay approach or document why the action-only change is sufficient for this regression.
- [x] 2.5 Keep production code changes out of command logging, command DTO generation, replay, export, viewers, and application-specific mixins.

## 3. Keep generated navigation actions neutral and unchanged

- [x] 3.1 Assert generated parented collection navigation actions do not expose an owner type's action domain-event default.
- [x] 3.2 Assert generated scalar reference navigation actions do not expose an owner type's action domain-event default.
- [x] 3.3 Verify generated parented collection navigation action ids, names, layout association metadata, styling, validation, invocation, and command publishing behavior remain unchanged.
- [x] 3.4 Verify generated scalar reference navigation action ids, names, layout association metadata, styling, usability, invocation, and command publishing behavior remain unchanged.
- [x] 3.5 Verify command-log recording support still gates generation of both navigation action families.

## 4. Validate

- [x] 4.1 Run the focused metamodel tests for mixed-in domain-event isolation.
- [x] 4.2 Run the focused metamodel tests for synthetic navigation action synthesis.
- [x] 4.3 Run any existing regression test that boots recording-support navigation, such as `RecordingNavigation_IntegTest`, if the focused tests do not cover the full post-processing sweep.
- [x] 4.4 Run `openspec validate isolate-mixin-domain-events --strict`.
