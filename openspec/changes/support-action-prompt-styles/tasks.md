## 1. Rich GraphQL Metadata

- [x] 1.1 Add nullable action-only `promptStyle` to shared rich member metadata using the resolved canonical `ObjectAction` value.
- [x] 1.2 Extend GraphQL metadata tests for action values, non-action null behavior, and unchanged shared metadata.

## 2. Action Presentation Contract

- [x] 2.1 Add prompt-style normalization and the reflected `<cw-action prompt-style>` property with authored-over-canonical precedence.
- [x] 2.2 Select optional `promptStyle` metadata for object, service, and application-menu actions with older-schema compatibility.
- [x] 2.3 Carry the effective style through semantic action requests and controller state without changing action semantics.
- [x] 2.4 Extend fixtures and focused action, object-context, service, menu, and presentation tests.

## 3. Prompt Surfaces

- [x] 3.1 Refactor the interaction controller to render and query one active prompt surface across controller and inline portal locations.
- [x] 3.2 Implement reversible inline replacement for authored and effective-grid property associations with modal fallback when no property is associated.
- [x] 3.3 Implement movable bounded modal dialogs and vertical sidebar dialogs with responsive styling.
- [x] 3.4 Preserve style-specific focus containment, Escape, validation, confirmation, cancellation, success, disconnect, and stale-generation behavior in focused tests.
- [x] 3.5 Keep canonical are-you-sure confirmation modal and return cancelled parameter confirmation to the original style with retained values.

## 4. Petclinic Demonstration

- [x] 4.1 Declare representative inline, modal, and sidebar prompt styles in Petclinic authored HTML.
- [x] 4.2 Extend Petclinic integration coverage for canonical prompt-style metadata.
- [x] 4.3 Extend focused Playwright coverage for placement, modal movement, accessibility, focus restoration, responsive overflow, and successful invocation.

## 5. Documentation and Verification

- [x] 5.1 Document `prompt-style`, precedence, normalization, property-association constraints, and the three prompt surfaces in foundation usage guidance.
- [x] 5.2 Run focused GraphQL, foundation, Petclinic integration, Playwright compilation, and prompt-style browser checks.
- [x] 5.3 Run full foundation Node and Maven tests, strict OpenSpec validation, and formatting checks.
