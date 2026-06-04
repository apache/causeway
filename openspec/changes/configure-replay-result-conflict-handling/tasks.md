## 1. Configuration Model

- [ ] 1.1 Add a command log replay result mapping conflict policy enum with strict and log-and-continue values.
- [ ] 1.2 Add a `CausewayConfiguration` property for the default listener conflict policy with strict throwing behavior as the default.
- [ ] 1.3 Document the property name and accepted values in code comments or generated configuration metadata if applicable.

## 2. Default Listener Behavior

- [ ] 2.1 Update `CommandReplayMappingListenerDefault` construction to receive the configured conflict policy.
- [ ] 2.2 Preserve the existing exception-throwing conflict behavior when the policy is strict.
- [ ] 2.3 Implement the log-and-continue policy so conflicts are logged and the existing mapping is retained without throwing.
- [ ] 2.4 Update the default listener bean factory to obtain the policy from `CausewayConfiguration`.

## 3. Tests and Validation

- [ ] 3.1 Update default listener unit tests to verify strict conflict handling remains the default.
- [ ] 3.2 Add unit tests for the log-and-continue policy, including retained original mapping and no thrown exception.
- [ ] 3.3 Add or update bean factory tests to verify configuration is passed into the default listener.
- [ ] 3.4 Run the command log applib test suite covering `CommandReplayMappingListenerDefaultTest` and `ReplayableCommandMappingTest`.
- [ ] 3.5 Run `openspec validate configure-replay-result-conflict-handling --type change --strict`.
