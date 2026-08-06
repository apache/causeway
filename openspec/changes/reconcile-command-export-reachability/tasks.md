## 1. Reachability Core

- [x] 1.1 Add the replayable-command participant-tracker contract and explicit manager-context construction path while preserving standalone memento construction.
- [x] 1.2 Implement the ordered known-participants validator for DTO targets, legacy target fallback, reference-valued action parameters, prior recorded results, and first-failure details.
- [x] 1.3 Add focused validator tests for roots, earlier and later results, baseline filtering, target and named or positional parameter failures, scalar parameters, and malformed or absent DTO data.

## 2. Causeway 4 Root Classification

- [x] 2.1 Extend replay context wiring with immutable command-log recording configuration, metamodel specification lookup, and the injected R1 reference-data classifier list while preserving test construction compatibility.
- [x] 2.2 Implement null-safe root classification from `ObjectSpecification.isDomainService()` and OR-composed reference-data classification without resolving bookmarked objects.
- [x] 2.3 Test domain-service roots, ordinary and unknown logical types, reference-data roots, disabled recording support, and the no-object-loading boundary.

## 3. Unified Manager Integration

- [x] 3.1 Make `CommandManager` provide participant reachability from its current baseline-, limit-, eligibility-, exclusion-, and timestamp-ordered command sequence.
- [x] 3.2 Add the derived non-persisted `knownParticipants` property to `ReplayableCommand`, returning false outside valid manager context and without changing replay state.
- [x] 3.3 Test first service commands, ordinary targets, property edits, reference parameters, reference data, earlier results across replay states, later and pre-baseline results, absent sequence entries, and standalone replayable commands.

## 4. Presentation and Boundaries

- [x] 4.1 Add `knownParticipants` immediately after `hasResult` in replayable-command and all unified-manager fallback column orders.
- [x] 4.2 Add layout and scope tests confirming R2 changes no memento, repository, replay-state, YAML export/import, legacy-manager action, workflow, replay-gate, background-gate, persistence-schema, or commandlog JDO behavior.

## 5. Verification

- [x] 5.1 Run focused commandlog applib tests for validator, root classification, manager integration, and presentation.
- [x] 5.2 Run the affected commandlog applib Maven verification and strict OpenSpec validation, documenting any intentionally omitted adapter coverage.

Verification scope: commandlog applib `verify` passed under JDK 21, and strict validation passed for the change and all OpenSpec items. Persistence-adapter verification is intentionally omitted because R2 adds no repository operation or storage semantic and relies on the P2-tested ordered repository contract; commandlog JDO remains removed on Causeway 4.
