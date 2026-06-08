## 1. API Definition

- [x] 1.1 Add the public `RefData` marker interface in the command-log applib API.
- [x] 1.2 Document the marker contract that implementations must represent stable, well-known replay environment data.

## 2. Default Classifier

- [x] 2.1 Add a default `CommandReplayReferenceDataService` implementation for `RefData` classes.
- [x] 2.2 Inject `SpecificationLoader` into the default implementation.
- [x] 2.3 Resolve bookmarks by logical type name to `ObjectSpecification` and corresponding class.
- [x] 2.4 Return true only when the corresponding class implements `RefData`.
- [x] 2.5 Return false for unknown logical types, missing specifications, null bookmarks, or non-marker classes.
- [x] 2.6 Register the default classifier so it participates in the existing list of `CommandReplayReferenceDataService` implementations.

## 3. Tests

- [x] 3.1 Add tests showing a bookmark whose logical type resolves to a `RefData` class is classified as reference data.
- [x] 3.2 Add tests showing a bookmark whose logical type resolves to a non-marker class is not classified as reference data.
- [x] 3.3 Add tests showing an unknown logical type is not classified as reference data.
- [x] 3.4 Add tests showing the default marker classifier composes with custom SPI implementations through existing OR semantics.
- [x] 3.5 Add tests or assertions showing entity instances are not loaded during marker classification.

## 4. Verification

- [x] 4.1 Run the relevant command-log applib test suite.
- [x] 4.2 Run `openspec status --change default-refdata-marker-service` and confirm the change remains apply-ready.
