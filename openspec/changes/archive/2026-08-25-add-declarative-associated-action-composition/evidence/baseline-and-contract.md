# Baseline and accepted contract

## Planning baseline

Planning commit `ced901615359` records the pre-implementation behavior.
`<causeway-property>` wrote every loading, value, hidden, error, and editor state to `this.innerHTML`, replacing all light-DOM declarations.
`<causeway-collection>` captured collection-column configuration and then wrote every metadata, activation, loading, empty, error, list, table, paging, and refresh state to `this.innerHTML`.
A direct action child was therefore removed by the first owner render.

Generated `<causeway-object>` composition already retained grid-nested actions in a `data-causeway-associated-member` section followed by a `causeway-object-associated-actions` group marked with `data-causeway-action-group`.
The generated parser and renderer baseline SHA-256 was `94fb83fc6d9bc8c0e651bac39bb356797ffd342895ada2c7c0888e8130f9e8db` and remains unchanged.

`petclinic.PetOwner.html` used adjacent `petclinic-member-composition` and `petclinic-associated-actions` wrappers.
Its baseline was 4,267 bytes with SHA-256 `eb73a48873498781f6a47496628305a8a47b53537b20074c78bd92be601e0b09`.
The Petclinic stylesheet baseline was 3,215 bytes with SHA-256 `abe338010af0d1048f80616d2cd127b81f5762eb37bddff9e583ae224e7b4b0f`.

The accepted pre-change Maven foundation suite had 163 passing tests, the HTMX route-policy suite had 5 passing tests, the Petclinic integration suite had 5 passing tests, and each Petclinic Playwright policy matrix had 4 passing tests.

## Accepted public contract

Only a direct `<causeway-action>` child of `<causeway-property>` or `<causeway-collection>` declares presentation association.
Arbitrary descendant actions remain independent ordinary composition.

The owner creates one stable `causeway-member-primary` region before declarations and updates only that region.
Direct action nodes remain physically in place, preserve source order, and are neither cloned nor moved by owner rendering.
A direct-child observer recognizes parser-late and deliberately appended actions without recursive scanning.

Collections filter direct collection-column and action declarations independently.
Actions never contribute row fields, and columns never become action controls.
Retained column declaration elements remain hidden while their configuration continues to drive bounded row projection.

Association controls placement only.
Each action registers its own requirement, consumes the nearest object context, and retains independent GraphQL visibility, usability, parameters, validation, invocation, concurrency, results, announcements, and focus.
A hidden or disabled owner affects only its primary region and cannot hide or disable an independently visible or enabled action.

Direct compositions expose `data-causeway-associated-member`, `data-causeway-action-group`, `data-causeway-associated-action`, `causeway-member-primary`, `--causeway-associated-action-gap`, and `--causeway-associated-action-margin-block-end`.
Generated grid composition retains its accepted semantically equivalent hooks and parser.

## Accepted implementation resources

| Resource | Baseline SHA-256 | Implemented SHA-256 |
|---|---|---|
| `member-composition.mjs` | absent | `fc472f6304658b8a3e13f5bb9d04e3906e2b6fa4386d65e95eef229288296977` |
| `property-element.mjs` | `13a5220a25c1682f739d654ff241fe12be2a637e39dec893714368c86a0f4115` | `3984f5c6108c943cd43edd0020ac33f865af218f7769dbbdc4513946606f41d3` |
| `collection-element.mjs` | `1bc1e1bb8b55744043caa462785d7abaf111971d4fa84f73653ee62e86eee173` | `16b12c321022ceed6cc7fe13219d22aa266d274ec71c0c2ae4fc3c99ea3d921d` |
| `component-styles.css` | `a9c766004578c90c853e970c158fb786d66787d4a02d7f4bc94bb93b6f6c7476` | `efc75ffe6be075355f1252efbd7127f16b773e690a11daec352898f037aa98b9` |
| `petclinic.PetOwner.html` | `eb73a48873498781f6a47496628305a8a47b53537b20074c78bd92be601e0b09` | `25b869e6756ab1998045edfdf741ceccc4aa43b734bf717555fd4c39fb8561c2` |
| `application.css` | `abe338010af0d1048f80616d2cd127b81f5762eb37bddff9e583ae224e7b4b0f` | `d4b0c06a5779fcc898d28cd95435f9a4276108e0539184ba8b8794a7a9feb326` |
