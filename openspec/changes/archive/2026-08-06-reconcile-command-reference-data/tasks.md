## 1. Public Reference-Data Contracts

- [x] 1.1 Add the dependency-neutral `RefData` marker to applib with API documentation for its cross-environment identity contract.
- [x] 1.2 Add the public bookmark-based `CommandReplayReferenceDataService` SPI and null-safe OR-composition helper.
- [x] 1.3 Add focused composition tests covering accepted, rejected, multiple-service, null-service, empty-list, and null-bookmark cases, including normal propagation of classifier failures.

## 2. Default Metamodel Classifier

- [x] 2.1 Implement `CommandReplayReferenceDataServiceForRefData` using `SpecificationLoader` and corresponding-class assignability without object resolution.
- [x] 2.2 Register the default classifier in `CausewayModuleExtCommandLogApplib` and add a module-registration test.
- [x] 2.3 Add focused tests for marker and non-marker classes, unknown logical types, null bookmarks, custom-classifier composition, and metamodel-only access.

## 3. Built-In SecMan Reference Data

- [x] 3.1 Make the Causeway 4 `ApplicationUser`, `ApplicationRole`, `ApplicationTenancy`, and `ApplicationPermission` abstractions extend `RefData`.
- [x] 3.2 Add focused contract tests confirming exactly the designated SecMan abstractions carry the marker and that no persistence mapping changes are introduced.

## 4. Scope and Documentation

- [x] 4.1 Document the public marker, custom SPI extension point, OR semantics, conservative fallback, metamodel-only default, and SecMan provisioning responsibility.
- [x] 4.2 Add scope guards confirming R1 does not change command exportability, known participants, manager collections, YAML, replay mappings, workflow actions, background gates, datastore schemas, or commandlog JDO support.

## 5. Verification

- [x] 5.1 Run focused applib, commandlog applib, and SecMan applib tests covering the new contracts and module wiring.
- [x] 5.2 Run the affected aggregate Maven verification suite under the repository's supported JDK and resolve compilation, formatting, and inspection failures.
- [x] 5.3 Run strict OpenSpec validation and confirm all R1 scenarios are represented by tests or explicit structural checks.
