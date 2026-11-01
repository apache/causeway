## 1. Rich GraphQL Metadata

- [x] 1.1 Add nullable `areYouSure` to shared rich member metadata using canonical action semantics and null for non-action wrappers.
- [x] 1.2 Extend GraphQL model and Petclinic integration tests for true, false, null, and backward-compatible action metadata reads.

## 2. Web Components Confirmation Flow

- [x] 2.1 Select optional `areYouSure` metadata for object and service action state and carry strict canonical confirmation presentation in action-request events.
- [x] 2.2 Add confirming interaction state and invocation gating for parameterless and successfully validated parameterized actions.
- [x] 2.3 Render and style the accessible confirmation dialog with deterministic focus, Confirm, Cancel, Escape, decline, and repeated-activation behavior.
- [x] 2.4 Add foundation unit tests covering canonical metadata compatibility, ordinary actions, both confirmation entry paths, retained parameter values, cancellation, focus, stale generations, and at-most-once invocation.
- [x] 2.5 Document the standard are-you-sure behavior and stable confirmation hooks in foundation usage guidance.

## 3. Petclinic Demonstration

- [x] 3.1 Replace the fixture-specific Delete disable rule with the authoritative visit-count veto and correct the action description.
- [x] 3.2 Extend Petclinic integration tests for visit-based disabled state, enabled no-visit state, and canonical confirmation metadata.
- [x] 3.3 Extend opt-in Playwright coverage to cancel and confirm deletion of disposable eligible owners while checking focus, route recovery, and browser errors.

## 4. Verification

- [x] 4.1 Run focused GraphQL model, foundation, and Petclinic Maven tests.
- [x] 4.2 Run the foundation direct Node test suite and compile Playwright tests.
- [x] 4.3 Run the focused Petclinic Playwright destructive-action journey with an available browser.
- [x] 4.4 Run strict OpenSpec validation and record all applicable checks passing.
