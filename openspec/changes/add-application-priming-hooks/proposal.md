## Why

Applications can suffer N+1 persistence access when an action or a large Wicket entity page traverses an object graph that has not been loaded efficiently into the ORM persistence context.
Causeway needs an opt-in application hook that can bulk-load the likely-needed graph immediately before that work, without coupling framework code to application-specific persistence queries.

## What Changes

- Add an application registrar SPI for associating action and view priming callbacks with metamodel identities during startup.
- Register action primers using a domain class and one or more local action logical names, without requiring callbacks to parse logical member identifiers.
- Register entity-view primers using only a domain class, whose logical type is resolved from the initialized metamodel.
- Build and freeze exact-match action and view registries after validating every registration.
- Invoke matching action primers within the current persistence context immediately before ordinary domain action execution.
- Invoke matching view primers before preparing and rendering a Wicket entity page during each relevant rendering request.
- Pass action primers the domain-facing target and immutable positional action arguments, and pass view primers the page's root domain object.
- Fail startup when a registered class has no exact logical type or when a registered local action name cannot be resolved.
- Propagate callback failures while leaving unregistered actions and views unchanged.

## Capabilities

### New Capabilities

- `application-priming-hooks`: Defines registration, metamodel validation, action and Wicket entity-view callback invocation, argument access, and failure behavior for application-defined ORM cache priming.

### Modified Capabilities

None.

## Impact

The change adds public applib registration and callback contracts, a runtime registry and action-execution integration, and Wicket entity-page integration.
It requires focused metamodel, runtime, and Wicket tests plus application-facing documentation.
It adds no ORM-specific API or external dependency, performs no automatic fetching, and introduces no collection-row priming, tracing, diagnostics, wildcard registration, or class-hierarchy matching.
