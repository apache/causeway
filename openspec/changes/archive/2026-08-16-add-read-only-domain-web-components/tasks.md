## 1. Public Component Contracts

- [x] 1.1 Finalize semantic custom-element names, attributes, structured properties, slots, host classes, and custom-event payloads for objects, members, collections, columns, and links.
- [x] 1.2 Extend schema fixtures and semantic descriptors for enums, object references, collections, action affordances, Blob, Clob, null, and unsupported output shapes.
- [x] 1.3 Add interoperability tests for plain HTML lifecycle behavior and standard bubbling and composed events.

## 2. Value Rendering

- [x] 2.1 Implement the deterministic value-renderer registry and application registration API.
- [x] 2.2 Implement standard renderers for supported GraphQL scalars, enums, null values, and object references.
- [x] 2.3 Implement supported read-only Blob and Clob presentations using the rich-schema resource representations.
- [x] 2.4 Implement the explicit unsupported-value renderer and diagnostic type reporting.
- [x] 2.5 Test renderer precedence, application overrides, formatting, null handling, and unsupported shapes.

## 3. Properties and Object Navigation

- [x] 3.1 Expand `<causeway-property>` to render semantic labels, descriptions, loading, visible, hidden, disabled, null, value, and member-error states.
- [x] 3.2 Implement `<causeway-object-link>` with title and bookmark presentation plus semantic navigation events.
- [x] 3.3 Implement object-valued property presentation through object links and verify nested metadata selections are coordinated by the object context.
- [x] 3.4 Add accessibility and lifecycle tests for property and object-link behavior.

## 4. Action Affordances

- [x] 4.1 Implement `<causeway-action>` read-only visibility, usability, label, description, and disabled-reason presentation.
- [x] 4.2 Implement semantic action-request events without adding prompt or invocation behavior.
- [x] 4.3 Test visible, hidden, disabled, loading, error, pointer, and keyboard action states.

## 5. Collections and Row Contexts

- [x] 5.1 Extend the object context with lazy context-owned secondary read operations suitable for collection content.
- [x] 5.2 Implement `<causeway-collection>` activation, loading, empty, row, and error states without eager content loading.
- [x] 5.3 Implement default identity-and-title row projection and declarative `<causeway-collection-column>` requirement contribution.
- [x] 5.4 Implement hydrated nested row object contexts that reuse returned data and the parent GraphQL client.
- [x] 5.5 Test inactive collections, default rows, declared columns, empty results, partial row errors, hydration reuse, and missing-field delta reads.

## 6. Styling and Accessibility

- [x] 6.1 Define stable light-DOM semantic markup, host classes, slots, and minimal default styles for the read-only component vocabulary.
- [x] 6.2 Verify labels, roles, focus order, keyboard activation, disabled reasons, loading announcements, empty states, and error announcements.
- [x] 6.3 Add an application-theme example proving that host design-system styles can customize the components.

## 7. Demonstration and Verification

- [x] 7.1 Add plain-HTML examples for an object summary, object reference, action affordance, and lazy collection with declared columns.
- [x] 7.2 Add integration tests against representative rich GraphQL responses for all supported read-only member kinds and value renderers.
- [x] 7.3 Document semantic composition, renderer extension, collection activation, row hydration, navigation events, action-request events, and known collection-size limitations.
- [x] 7.4 Run browser tests, relevant Maven tests, accessibility checks, formatting checks, and strict OpenSpec validation, and resolve all failures.

## 8. Executable Vanilla-HTML Acceptance Application

- [x] 8.1 Extend the deterministic `sample-html` JPA domain while preserving `s_sample-1`, adding enum, null, object-reference, enabled-action, disabled-action, hidden-action, populated-collection, empty-collection, and stable row-column semantics.
- [x] 8.2 Extend the real-endpoint GraphQL contract tests to cover GraphQL Java-compatible one-type introspection and deterministic object, action-state, collection, row-hydration, and empty-collection responses.
- [x] 8.3 Expand `/sample-html/index.html` to compose the public value, object-link, action, collection, and collection-column components while retaining all foundation selectors and readiness behavior.
- [x] 8.4 Add the documented stable `data-testid` hooks and plain-JavaScript navigation and action-request event diagnostics without adding routing, prompting, invocation, HTMX, Playwright, or a frontend build.
- [x] 8.5 Extend random-port integration tests to verify the expanded page, packaged ECMAScript modules, stable hooks, and deterministic rich GraphQL responses.
- [x] 8.6 Update the sample manual-verification documentation with the added data, selectors, expected read-only states, collection activation, and event-observation steps.
- [x] 8.7 Run the packaged sample in a real browser, verify `data-state="ready"`, visible, disabled, hidden, collection, and semantic-event behavior without console errors, then run final Maven, formatting, and strict OpenSpec verification.

## 9. Illustrative Sample Presentation

- [x] 9.1 Extend the deterministic root domain with `summary`, `capacity`, and `featured` text, numeric, and boolean values, and enable ordinary property semantics while preserving the deliberate disabled members.
- [x] 9.2 Restructure `/sample-html/index.html` into labelled object-summary, property, action, collection, and event-diagnostic sections with all existing selectors and readiness behavior preserved.
- [x] 9.3 Add responsive page-specific light and dark application-theme styling for cards, typography, focus, state explanations, diagnostics, controls, and collection tables without moving presentation policy into the component library.
- [x] 9.4 Add a visible coverage guide that explains enabled, disabled, null, reference, empty, and intentionally hidden semantics without revealing or synthesizing the hidden value.
- [x] 9.5 Expand the event diagnostics panel and add collection row-count reporting while preserving `[data-testid="sample-event"]` and avoiding routing, prompting, invocation, or generic composition.
- [x] 9.6 Extend random-port integration tests, stable-selector documentation, deterministic GraphQL assertions, and manual verification instructions for the richer reference presentation.
- [x] 9.7 Run the packaged reference page in narrow and wide browser viewports and light and dark modes, verify keyboard events, collection and hidden states, and diagnostics, then pass Lighthouse accessibility, Maven, formatting, and strict OpenSpec validation.
- [x] 9.8 Add and verify a reactor-safe Maven profile that starts the sample with `spring-boot:run`, including the foundation and GraphQL runtime, and update the sample README to make it the preferred manual launch path.
