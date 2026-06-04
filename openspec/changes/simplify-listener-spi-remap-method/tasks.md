## 1. SPI Simplification

- [ ] 1.1 Replace `CommandReplayMappingListener#remapTarget(...)` and `remapReferenceParameter(...)` with one default `remap(CommandLogEntry, Bookmark)` method.
- [ ] 1.2 Update listener Javadoc to describe common bookmark remapping for targets and reference parameters.
- [ ] 1.3 Keep `onReplayResultMapped(...)` unchanged.

## 2. Replay Remapping Flow

- [ ] 2.1 Update target remapping in `ReplayableCommand` to call the listener's common `remap(...)` method.
- [ ] 2.2 Update reference-parameter remapping in `ReplayableCommand` to call the same `remap(...)` method without passing parameter name or index.
- [ ] 2.3 Preserve no-op behavior when no listener returns a replacement or a listener throws.
- [ ] 2.4 Preserve replay-time DTO copy behavior so recorded command data remains unchanged.

## 3. Tests and Verification

- [ ] 3.1 Update `ReplayableCommandMappingTest` mocks and verifications for the single remap method.
- [ ] 3.2 Add or update assertions showing target and reference-parameter remapping both use the same SPI method.
- [ ] 3.3 Run the command log applib replay mapping tests.
- [ ] 3.4 Run `openspec validate --all --strict`.
