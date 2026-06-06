## 1. DTO and Import/Export Implementation

- [ ] 1.1 Rename `CommandDtoUtils.CommandExportDto` result metadata property and accessors from `returnedObject` to `result`.
- [ ] 1.2 Update command replay import to read `ImportedCommandDto#getResult()` or equivalent renamed API and set `CommandLogEntry#setResult(...)` from that value.
- [ ] 1.3 Remove support for the old `returnedObject` YAML field; do not add Jackson aliases or compatibility setters for it.

## 2. Tests and Fixtures

- [ ] 2.1 Update command export YAML tests and approval fixtures to emit `result:` and not `returnedObject:`.
- [ ] 2.2 Update command replay import tests to import result bookmark metadata from `result`.
- [ ] 2.3 Add or update coverage showing old `returnedObject` input is not treated as result metadata.
- [ ] 2.4 Search source, tests, fixtures, docs, and active OpenSpec artifacts for current-behavior `returnedObject` references and update them to `result` where appropriate.

## 3. Validation

- [ ] 3.1 Run focused applib command DTO YAML tests.
- [ ] 3.2 Run focused command replay import tests.
- [ ] 3.3 Run `openspec validate rename-returned-object-to-result --strict`.
