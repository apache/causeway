## 1. Investigate Existing Command Publishing Path

- [x] 1.1 Inspect how normal safe actions receive `CommandPublishingFacet` metadata.
- [x] 1.2 Inspect current synthetic selector action facet installation and identify why the command publishing facet is absent or ineffective.
- [x] 1.3 Identify the lightest command-log or export test fixture that can exercise a synthetic selector action invocation.

## 2. Selector Action Publishing Implementation

- [x] 2.1 Ensure synthetic selector actions expose command publishing facet metadata through the normal action facet model.
- [x] 2.2 Ensure the facet respects the existing safe action command publishing configuration.
- [x] 2.3 Ensure no selector action command log entry is created when safe action command publishing is disabled.

## 3. Command Log and Export Coverage

- [x] 3.1 Add focused metamodel coverage that verifies a synthetic selector action has command publishing metadata when safe action publishing is enabled.
- [x] 3.2 Add coverage that verifies a synthetic selector action invocation creates a command log entry with a selector command DTO when both opt-in properties are enabled.
- [x] 3.3 Add coverage that verifies a logged synthetic selector action is included by command export with returned object metadata when available.

## 4. Validation

- [x] 4.1 Run the focused selector action and command publishing tests.
- [x] 4.2 Run `openspec validate publish-selector-actions-for-export --strict`.
