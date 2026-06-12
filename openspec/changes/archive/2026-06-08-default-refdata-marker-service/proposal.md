## Why

Applications can now provide custom `CommandReplayReferenceDataService` implementations, but each application must write boilerplate to recognize ordinary reference-data classes.
A built-in marker-interface approach gives applications a simple default path while preserving the existing SPI extension point for more specialized classification.

## What Changes

- Add a public marker interface named `RefData` for domain classes whose bookmarked instances are stable replay reference data.
- Provide a default `CommandReplayReferenceDataService` implementation that classifies bookmarks by logical type.
- Have the default implementation look up the bookmark logical type through `SpecificationLoader`, obtain the corresponding class from the `ObjectSpecification`, and accept classes assignable to `RefData`.
- Keep custom `CommandReplayReferenceDataService` implementations supported and composable with the default implementation.

## Capabilities

### New Capabilities

- `command-export-refdata-marker`: Defines the built-in marker-interface classifier for replay reference data.

### Modified Capabilities

- `command-export-reference-data-participants`: Reference-data SPI behavior now includes the built-in marker-interface implementation as a default classifier.

## Impact

- Adds a public `RefData` marker interface to the command-log applib API.
- Adds a default domain service implementation of `CommandReplayReferenceDataService` in the command-log applib module.
- Requires integration with `SpecificationLoader` and tests for marker matches, non-marker misses, unknown logical types, and continued composition with custom classifiers.
