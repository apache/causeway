## 1. Public Component Contracts

- [ ] 1.1 Finalize semantic custom-element names, attributes, structured properties, slots, host classes, and custom-event payloads for objects, members, collections, columns, and links.
- [ ] 1.2 Extend schema fixtures and semantic descriptors for enums, object references, collections, action affordances, Blob, Clob, null, and unsupported output shapes.
- [ ] 1.3 Add interoperability tests for plain HTML lifecycle behavior and standard bubbling and composed events.

## 2. Value Rendering

- [ ] 2.1 Implement the deterministic value-renderer registry and application registration API.
- [ ] 2.2 Implement standard renderers for supported GraphQL scalars, enums, null values, and object references.
- [ ] 2.3 Implement supported read-only Blob and Clob presentations using the rich-schema resource representations.
- [ ] 2.4 Implement the explicit unsupported-value renderer and diagnostic type reporting.
- [ ] 2.5 Test renderer precedence, application overrides, formatting, null handling, and unsupported shapes.

## 3. Properties and Object Navigation

- [ ] 3.1 Expand `<causeway-property>` to render semantic labels, descriptions, loading, visible, hidden, disabled, null, value, and member-error states.
- [ ] 3.2 Implement `<causeway-object-link>` with title and bookmark presentation plus semantic navigation events.
- [ ] 3.3 Implement object-valued property presentation through object links and verify nested metadata selections are coordinated by the object context.
- [ ] 3.4 Add accessibility and lifecycle tests for property and object-link behavior.

## 4. Action Affordances

- [ ] 4.1 Implement `<causeway-action>` read-only visibility, usability, label, description, and disabled-reason presentation.
- [ ] 4.2 Implement semantic action-request events without adding prompt or invocation behavior.
- [ ] 4.3 Test visible, hidden, disabled, loading, error, pointer, and keyboard action states.

## 5. Collections and Row Contexts

- [ ] 5.1 Extend the object context with lazy context-owned secondary read operations suitable for collection content.
- [ ] 5.2 Implement `<causeway-collection>` activation, loading, empty, row, and error states without eager content loading.
- [ ] 5.3 Implement default identity-and-title row projection and declarative `<causeway-collection-column>` requirement contribution.
- [ ] 5.4 Implement hydrated nested row object contexts that reuse returned data and the parent GraphQL client.
- [ ] 5.5 Test inactive collections, default rows, declared columns, empty results, partial row errors, hydration reuse, and missing-field delta reads.

## 6. Styling and Accessibility

- [ ] 6.1 Define stable light-DOM semantic markup, host classes, slots, and minimal default styles for the read-only component vocabulary.
- [ ] 6.2 Verify labels, roles, focus order, keyboard activation, disabled reasons, loading announcements, empty states, and error announcements.
- [ ] 6.3 Add an application-theme example proving that host design-system styles can customize the components.

## 7. Demonstration and Verification

- [ ] 7.1 Add plain-HTML examples for an object summary, object reference, action affordance, and lazy collection with declared columns.
- [ ] 7.2 Add integration tests against representative rich GraphQL responses for all supported read-only member kinds and value renderers.
- [ ] 7.3 Document semantic composition, renderer extension, collection activation, row hydration, navigation events, action-request events, and known collection-size limitations.
- [ ] 7.4 Run browser tests, relevant Maven tests, accessibility checks, formatting checks, and strict OpenSpec validation, and resolve all failures.
