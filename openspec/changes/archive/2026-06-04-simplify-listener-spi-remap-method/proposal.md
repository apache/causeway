## Why

The recently introduced command replay mapping listener exposes separate target and reference-parameter remapping methods, but callers only need to provide a replacement bookmark for a recorded bookmark.
The parameter-specific API adds names and indexes that are not required for the intended SPI use and makes implementations noisier than necessary.

## What Changes

- **BREAKING**: Replace the listener's separate target and reference-parameter remapping methods with a single `remap(...)` method used for both command targets and reference parameters.
- Keep replay result mapping notification behavior unchanged.
- Keep replay-time input remapping behavior unchanged except for the simplified listener callback shape.
- Update tests and documentation/specification to describe the single remapping callback.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-replay-result-mapping`: Simplify the command replay mapping SPI requirements so target and reference-parameter remapping use one common remapping method.

## Impact

- Affects the command log applib SPI `CommandReplayMappingListener`.
- Affects replay input remapping implementation in the command log applib replay flow.
- Affects tests and any application implementations of the recently introduced listener SPI.
