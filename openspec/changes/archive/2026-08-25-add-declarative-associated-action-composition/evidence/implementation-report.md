# Implementation report

## Stable direct-child composition

`member-composition.mjs` owns one weakly referenced composition record per property or collection host.
It inserts one primary presentation element before authored children, observes only direct child-list changes, marks direct actions, preserves action nodes in source order, and removes its observer on disconnection.

Owner rendering writes only the primary element.
The helper leaves direct action and collection-column nodes connected, so owner state changes cannot restart action requirements, prompts, or event listeners.
When an owner is hidden, the primary element is hidden while a host containing associated actions remains available.
Removing the final action from a hidden owner restores normal host hiding.

Property event delegation rejects events whose direct host child is an associated action before considering edit, save, cancel, input, reference, field, or toolkit-fallback controls.
Semantic action events continue bubbling to the existing interaction controller.

Collection activation handling similarly ignores events from associated action descendants.
Column capture now filters direct collection-column elements before reading member configuration, so interleaved actions cannot enter row projections.
The existing hidden column elements remain connected and continue publishing deterministic configuration updates.

## Presentation equivalence

Direct property and collection hosts with actions expose the established associated-member and action-group data hooks.
The stable primary region occupies the full first row, and direct actions wrap after it through ordinary light-DOM flex styling.
Source order remains keyboard order because CSS does not assign order or relocate controls.

The existing effective-grid parser and generated object-layout renderer remain byte-identical.
Generated layouts retain their section and action-group markup, while direct HTML uses the same semantic hooks without requiring byte-identical DOM.

## Petclinic migration

`petclinic.PetOwner.html` now nests `updateName` beneath `name`, `addPet` and `removePet` beneath `pets`, and `bookVisit` beneath `visits`.
All property, collection, and column declarations remain literal private packaged HTML.
Petclinic-only adjacent association wrappers and their obsolete CSS were removed.
The retained `PetOwner.layout.xml` continues to express equivalent generated-grid associations.

Integration tests verify literal nested source, private packaging, retained grid actions, ordering, and absence of workaround classes.
Playwright verifies direct-child placement, ordered collection actions, responsive gap, prompts, validation, cancellation, invocation results, navigation, history, focus, collection refresh, and one mutation per activation.

## Lifecycle and request evidence

Foundation fixtures instrument property, collection, and action requirement registrations and releases independently.
They cover initial declarations, parser-late insertion, ignored descendant actions, interleaved columns, hidden and disabled owner/action combinations, property editing and cancellation, owner rerendering, action bubbling, removal, disconnection, and reconnection.

A connected action registers once and remains the same node across owner states.
A route disconnection releases owner and action requirements once.
A later route reconnection registers each existing semantic child once for the new connection.
Browser mutation counters verify exactly one GraphQL mutation for each successful `updateName`, `addPet`, `bookVisit`, and `removePet` activation.
