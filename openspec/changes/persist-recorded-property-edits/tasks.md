## 1. Metamodel Facet Behavior

- [x] 1.1 Locate the property command publishing facet creation path and identify where recording support can enable property publishing without bypassing `CommandPublishingFacet`.
- [x] 1.2 Add a property-specific recording-support facet branch that installs an enabled facet when command-log recording support is `ENABLED` during property facet creation.
- [x] 1.3 Ensure the enabled-recording behavior applies to unannotated properties, configured-none properties, and explicitly disabled properties according to the spec.
- [x] 1.4 Ensure recording support does not install duplicate property command publishing behavior when the property is already command-published.

## 2. Suppression and Persistence Flow

- [x] 2.1 Verify property edit execution still routes through the existing command publishing and command-log subscriber flow.
- [x] 2.2 Verify command recording suppression marker behavior remains authoritative for property edit targets.
- [x] 2.3 Confirm no persistence schema or command DTO shape changes are required.

## 3. Tests

- [x] 3.1 Add or update metamodel tests proving recording support makes property command publishing metadata report enabled.
- [x] 3.2 Add JDO and JPA recording-support integration tests proving a property edit without command-publishing annotation is persisted when recording support is enabled for the application context.
- [x] 3.3 Add or update regression tests proving a property edit without command-publishing annotation is not persisted solely by command logging when recording support is disabled.
- [x] 3.4 Add JDO and JPA recording-support integration tests proving an already command-published property edit creates at most one command log entry when recording support is enabled.
- [x] 3.5 Preserve suppression coverage for targets that implement the command recording suppression marker.

## 4. Validation

- [x] 4.1 Run focused metamodel and command-log regression tests for the changed behavior.
- [x] 4.2 Run `openspec status --change persist-recorded-property-edits` and confirm the change is apply-ready.
