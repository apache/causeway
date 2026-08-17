## 1. Catalogue Contract and Tool Selection

- [ ] 1.1 Inventory every public element, module, attribute, property, event, slot, context requirement, state, and styling hook.
- [ ] 1.2 Evaluate current Storybook web-component support and one viable alternative against direct ESM, accessibility, play-test, and static-build requirements.
- [ ] 1.3 Define deterministic Custom Elements Manifest generation and any namespaced semantic extensions.

## 2. Manifest Generation and Verification

- [ ] 2.1 Generate versioned declarations for all public elements and source modules.
- [ ] 2.2 Include documented attributes, properties, events, slots, CSS custom properties, light-DOM hooks, and semantic contract references.
- [ ] 2.3 Add drift tests against public registrations, exports, and documentation.
- [ ] 2.4 Publish the manifest with the ESM package under documented compatibility policy.

## 3. Interactive Workbench

- [ ] 3.1 Configure the selected web-component-native explorer without per-element framework wrappers.
- [ ] 3.2 Add deterministic fixture executors, context decorators, viewport controls, themes, reduced motion, and bounded diagnostics.
- [ ] 3.3 Add representative stories for every public component and applicable lifecycle, value, interaction, result, error, empty, and unsupported state.
- [ ] 3.4 Add an explicitly enabled local real-endpoint mode for selected sample integration stories.

## 4. Accessibility, Security, and Publication

- [ ] 4.1 Add pointer and keyboard play tests, focus assertions, responsive checks, and automated accessibility checks.
- [ ] 4.2 Verify published stories and manifests contain no hidden values, credentials, authorization rules, or unredacted resource content.
- [ ] 4.3 Produce a static workbench build and document local development, fixture, integration, and publication workflows.
- [ ] 4.4 Document catalogue versioning and its potential use by later semantic designer tooling.
- [ ] 4.5 Run Node, workbench, browser, accessibility, formatting, packaging, and strict OpenSpec validation checks.
