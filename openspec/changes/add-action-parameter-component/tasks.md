## 1. Public parameter component

- [ ] 1.1 Add the `<cw-parameter>` element with normalized immutable `named`, `described-as`, `description-as`, and bounded `multi-line` configuration.
- [ ] 1.2 Register and export the element and extend public element, host, attribute, and configuration-event contracts.
- [ ] 1.3 Add synchronized structural styling and public usage documentation for the non-visual nested configuration component.

## 2. Action and prompt integration

- [ ] 2.1 Capture initial and dynamic parameter declarations in `<cw-action>` without making them authoritative action parameters.
- [ ] 2.2 Carry immutable parameter presentation hints through semantic action requests into standard prompt state.
- [ ] 2.3 Merge matching names, descriptions, label-or-tooltip presentation, and multiline rows into parameter markup and editor context while preserving canonical interaction state.
- [ ] 2.4 Add component and interaction tests for partial declarations, unknown ids, fallback behavior, accessibility, dynamic updates, and multiline qualification.

## 3. Petclinic demonstration

- [ ] 3.1 Add representative partial `<cw-parameter>` declarations to selected Petclinic parameterized actions while leaving other parameters and actions undeclared.
- [ ] 3.2 Extend Petclinic resource, integration, and applicable browser assertions for authored and canonical parameter presentation.

## 4. Verification

- [ ] 4.1 Run the complete foundation test suite.
- [ ] 4.2 Run the applicable Petclinic Maven tests.
- [ ] 4.3 Run strict OpenSpec validation and verify synchronized installable styles.
