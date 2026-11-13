## ADDED Requirements

### Requirement: Petclinic reference-modal Escape regression coverage
Petclinic browser verification SHALL exercise Escape cancellation for the `removePet` reference-first modal action prompt.

#### Scenario: User dismisses Remove Pet with Escape
- **WHEN** the owner page opens `removePet` with effective style `DIALOG_MODAL` and its pet reference editor is ready
- **THEN** focus is within the reference editor
- **AND** pressing Escape with its dropdown closed removes the modal prompt
- **AND** no removal mutation occurs and focus returns to the Remove Pet action

#### Scenario: User reopens the cancelled action
- **WHEN** the same action is activated after Escape cancellation
- **THEN** its authoritative parameter values and choices are prepared normally
- **AND** the earlier cancellation has not invoked or corrupted the action lifecycle
