## 1. Public Reference-Data Contracts

- [ ] 1.1 Add the dependency-neutral `RefData` marker to applib with API documentation for its cross-environment identity contract.
- [ ] 1.2 Add the public bookmark-based `CommandReplayReferenceDataService` SPI and null-safe OR-composition helper.
- [ ] 1.3 Add focused composition tests covering accepted, rejected, multiple-service, null-service, empty-list, and null-bookmark cases, including normal propagation of classifier failures.

## 2. Default Metamodel Classifier

- [ ] 2.1 Implement `CommandReplayReferenceDataServiceForRefData` using `SpecificationLoader` and corresponding-class assignability without object resolution.
- [ ] 2.2 Register the default classifier in `CausewayModuleExtCommandLogApplib` and add a module-registration test.
- [ ] 2.3 Add focused tests for marker and non-marker classes, unknown logical types, null bookmarks, custom-classifier composition, and metamodel-only access.

## 3. Built-In SecMan Reference Data

- [ ] 3.1 Make the Causeway 4 `ApplicationUser`, `ApplicationRole`, `ApplicationTenancy`, and `ApplicationPermission` abstractions extend `RefData`.
- [ ] 3.2 Add focused contract tests confirming exactly the designated SecMan abstractions carry the marker and that no persistence mapping changes are introduced.

## 4. Scope and Documentation

- [ ] 4.1 Document the public marker, custom SPI extension point, OR semantics, conservative fallback, metamodel-only default, and SecMan provisioning responsibility.
- [ ] 4.2 Add scope guards confirming R1 does not change command exportability, known participants, manager collections, YAML, replay mappings, workflow actions, background gates, datastore schemas, or commandlog JDO support.

## 5. Verification

- [ ] 5.1 Run focused applib, commandlog applib, and SecMan applib tests covering the new contracts and module wiring.
- [ ] 5.2 Run the affected aggregate Maven verification suite under the repository's supported JDK and resolve compilation, formatting, and inspection failures.
- [ ] 5.3 Run strict OpenSpec validation and confirm all R1 scenarios are represented by tests or explicit structural checks.
