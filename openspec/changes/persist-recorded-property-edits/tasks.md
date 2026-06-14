## 1. Metamodel Facet Behavior

- [ ] 1.1 Locate the property command publishing facet creation path and identify where recording support can enable property publishing without bypassing `CommandPublishingFacet`.
- [ ] 1.2 Add a property-specific recording-support facet or branch that reports enabled when command-log recording support is `ENABLED`.
- [ ] 1.3 Ensure the enabled-recording behavior applies to unannotated properties, configured-none properties, and explicitly disabled properties according to the spec.
- [ ] 1.4 Ensure recording support does not install duplicate property command publishing behavior when the property is already command-published.

## 2. Suppression and Persistence Flow

- [ ] 2.1 Verify property edit execution still routes through the existing command publishing and command-log subscriber flow.
- [ ] 2.2 Verify command recording suppression marker behavior remains authoritative for property edit targets.
- [ ] 2.3 Confirm no persistence schema or command DTO shape changes are required.

## 3. Tests

- [ ] 3.1 Add or update metamodel tests proving recording support makes property command publishing metadata report enabled.
- [ ] 3.2 Add or update command-log regression tests proving an unannotated property edit is persisted when recording support is enabled.
- [ ] 3.3 Add or update regression tests proving an unannotated property edit is not persisted solely by command logging when recording support is disabled.
- [ ] 3.4 Add or update regression tests proving an already command-published property edit creates at most one command log entry when recording support is enabled.
- [ ] 3.5 Add or update regression tests proving a suppressed target property edit is not persisted solely through recording support.

## 4. Validation

- [ ] 4.1 Run focused metamodel and command-log regression tests for the changed behavior.
- [ ] 4.2 Run `openspec status --change persist-recorded-property-edits` and confirm the change is apply-ready.
