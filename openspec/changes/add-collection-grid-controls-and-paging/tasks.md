## 1. Public collection contract

- [ ] 1.1 Add observed, reactive `paged`, `resizable-columns`, and `reorderable-columns` collection attributes with bounded normalization and default-off Grid controls.
- [ ] 1.2 Apply valid page sizes to initial and subsequent loads, reset changed paging to offset zero, and force Causeway-owned bounded presentation without weakening stale-request protection.
- [ ] 1.3 Document the new collection attributes and the deliberate sorting and filtering exclusion.

## 2. Private Grid adapter

- [ ] 2.1 Thread normalized control options through the frozen adapter presentation contract.
- [ ] 2.2 Map resizing to generated Vaadin columns and reordering to the Vaadin Grid while preserving private events, native fallback, and declarative column authority.
- [ ] 2.3 Add adapter and collection tests for defaults, opt-in mapping, bounded paging, reactivity, invalid values, and fallback behavior.

## 3. Petclinic demonstration

- [ ] 3.1 Replace inert collection range attributes with selective `paged` overrides on the global owner list and owner visit history while leaving upcoming visits and pets unpaged.
- [ ] 3.2 Update Petclinic resource composition, integration, and Playwright assertions for configured page size and accessible paging without changing associated actions.

## 4. Verification

- [ ] 4.1 Run foundation Node and Maven tests, Petclinic integration and browser tests, RAT, IntelliJ inspections or compilation, diff checks, and strict OpenSpec validation.
