## Context

The command-log applib exposes `CommandReplayReferenceDataService` so applications can classify stable replay reference data from bookmarks.
The common case is simpler than a custom classifier: a domain class itself can declare that its instances are reference data.
The implementation needs to classify from a bookmark without loading the entity instance, so it should use metamodel lookup rather than repository lookup.

## Goals / Non-Goals

**Goals:**

- Add a public `RefData` marker interface for domain classes whose instances are stable replay reference data.
- Register a default `CommandReplayReferenceDataService` implementation that recognizes bookmarked classes assignable to `RefData`.
- Use `SpecificationLoader` to resolve a bookmark logical type to an `ObjectSpecification` and then inspect its corresponding class.
- Preserve the existing multi-implementation SPI behavior so custom classifiers still participate.

**Non-Goals:**

- Infer reference data from annotations, packages, persistence tables, or local database rows.
- Load or instantiate bookmarked entities during export validation.
- Replace custom `CommandReplayReferenceDataService` implementations.
- Change command export YAML or replay remapping semantics.

## Decisions

### Use a marker interface named `RefData`

Add an empty public marker interface in the command-log applib API, most likely under the `spi` package alongside `CommandReplayReferenceDataService`.
Applications opt in by implementing this marker on domain classes whose identifiers are stable across replay environments.

Alternative considered: add an annotation.
A marker interface is sufficient, cheap to test with `isAssignableFrom`, and avoids adding annotation processing rules.

### Implement classification through `SpecificationLoader`

The default service receives a `Bookmark`, extracts `logicalTypeName`, asks `SpecificationLoader` for the corresponding `ObjectSpecification`, obtains the corresponding class, and returns true when that class implements `RefData`.
Missing logical types or specifications are treated as non-reference-data classifications.

Alternative considered: use `MetaModelService.lookupLogicalTypeByName` directly.
That can identify Java classes for known logical types, but `SpecificationLoader` is the requested integration point and provides the object specification used by the metamodel.

### Register the default classifier as a normal SPI implementation

The default marker classifier should itself implement `CommandReplayReferenceDataService` and be registered as a domain service or component in the command-log applib module.
This lets the existing list-injection and OR composition include marker-based classification without special cases in export validation.

Alternative considered: hard-code marker lookup in `CommandExportManager`.
That would bypass the SPI composition model and make future classifiers harder to reason about.

## Risks / Trade-offs

- Marker interface is applied too broadly → Replay may rely on mutable data existing in target environments; document the marker's intended stability contract.
- Specification lookup cannot resolve the logical type → The default service returns false so existing validation remains protective.
- Default service registration changes behavior for applications that add the marker → This is the intended opt-in; unmarked classes keep current behavior.
- Interface package choice affects API discoverability → Place it near `CommandReplayReferenceDataService` and document the relationship.
