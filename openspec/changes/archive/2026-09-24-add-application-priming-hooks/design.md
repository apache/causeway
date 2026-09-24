## Context

Causeway applications can avoid repeated lazy-loading queries by issuing application-specific bulk reads before an action traverses an object graph or before Wicket renders a large entity page.
The framework currently has no lifecycle hook that lets an application associate such reads with selected actions or entity types.
Applications could subscribe to action domain events or modify viewer components, but action events are configuration-dependent and viewer customization couples the optimization to internal Wicket code.

The hook is an opportunistic performance facility rather than a diagnostic or fetching abstraction.
The application remains responsible for choosing persistence queries that populate its ORM persistence context, and Causeway neither describes nor verifies the resulting object graph.

Registration requires the completed metamodel because a Java class must resolve to an exact Causeway logical type and a local action name must resolve to a domain-facing action identity.
Callbacks execute concurrently across requests as ordinary singleton application services, while their repository dependencies use the active request or interaction persistence context.

## Goals / Non-Goals

**Goals:**

- Let applications register synchronous cache-priming callbacks for selected domain actions and Wicket entity-page root types.
- Route callbacks through precomputed exact-match registries without requiring callbacks to inspect or parse logical identifiers.
- Resolve registration keys from domain classes and local action logical names through the initialized metamodel.
- Fail application startup for invalid registrations rather than silently leaving an intended optimization inactive.
- Invoke callbacks early enough that subsequent action or page access can reuse objects loaded into the current ORM persistence context.
- Keep the callback API independent of JDO, JPA, Wicket internals, and any particular repository implementation.

**Non-Goals:**

- Detect, diagnose, count, trace, or automatically repair N+1 queries.
- Define which object graph an application should load or provide an ORM-independent fetch-plan language.
- Prime collection rows individually or introduce a collection-view callback.
- Match registrations by wildcard, annotation, assignability, superclass, interface, or subtype traversal.
- Replace action domain events, lifecycle callbacks, or viewer extension APIs.
- Support asynchronous callbacks or callbacks that outlive the current invocation or rendering request.

## Decisions

### Introduce registrar, registry, and callback contracts in applib

Add a `PrimingRegistrar` SPI whose implementations receive a `PrimingRegistry` after the metamodel has loaded.
The registry exposes singular and plural action-registration operations and an entity-view registration operation conceptually equivalent to:

```java
<T> void action(
        Class<T> domainType,
        String actionLogicalName,
        ActionPrimer<? super T> primer);

<T> void actions(
        Class<T> domainType,
        Collection<String> actionLogicalNames,
        ActionPrimer<? super T> primer);

<T> void view(
        Class<T> domainType,
        ViewPrimer<? super T> primer);
```

`ActionPrimer<T>` receives a typed domain-facing target and immutable `ActionArguments`.
`ViewPrimer<T>` receives the typed root domain object.
The registration's `Class<T>` enables the registry to perform the target cast, so application callbacks do not need to cast from `Object`.

The alternative of defining one service implementation per identifier was rejected because every invocation would need to scan services or ask each service whether it applies.
The alternative of using annotations on primer methods was rejected because reflective argument binding and lifecycle discovery would add complexity without improving runtime routing.

### Derive exact keys from the initialized metamodel

For action registration, resolve the supplied class to its exact object specification, resolve each supplied local action logical name on that specification, and store the resulting domain-facing logical member identifier as the lookup key.
For view registration, resolve the supplied class to its exact object specification and store that specification's logical type name as the lookup key.
Registration therefore looks like:

```java
registry.actions(
        Invoice.class,
        Set.of("approve", "recalculate"),
        this::primeInvoice);

registry.view(
        Invoice.class,
        this::primeInvoiceView);
```

The application does not supply `estatio.Invoice#approve` or `estatio.Invoice`, and the callback receives neither value.
A contributed action is registered against its domain target or mixee class and its local action name, not against the mixin implementation class.

The registry rejects a class that has no exact metamodel specification and logical type, and it rejects an unknown or malformed local action name.
It does not search up or down the Java class hierarchy and does not expand a registration to related logical types.
This exact behavior avoids surprising activation and leaves hierarchy matching for a future independently specified enhancement.

The alternative of accepting full logical identifiers was rejected because it duplicates the logical type already represented by the class and makes registrations vulnerable to string construction errors.

### Populate once after metamodel loading and then freeze

The registry implementation listens for completion of metamodel loading, obtains all `PrimingRegistrar` services, and lets them contribute registrations.
It validates each contribution while the metamodel is available and then freezes two immutable multimaps:

```text
logical member identifier -> action primer registrations
logical type name          -> view primer registrations
```

A registration attempt after freezing fails rather than mutating behavior while requests are active.
Invalid registration aborts startup with a message identifying the registrar, class, and action name where applicable.

Multiple callbacks may be registered for one key and all of them execute.
Callbacks must not depend on their relative order, although registrar discovery and callback storage remain deterministic for repeatable tests.

The alternative of lazy registration on first invocation was rejected because invalid registrations would escape startup validation and could first fail under production load.

### Route action priming immediately before domain method execution

Integrate dispatch with ordinary `ActionExecutor` execution after action validation and any `EXECUTING` domain-event argument adjustment, but before invoking the domain method.
The lookup uses the domain-facing logical member identifier, including the mixee identity for a contributed action.
The callback target is the interaction owner or mixee rather than the transient mixin implementation instance.

The primer executes synchronously in the same interaction, transaction, thread, and ORM persistence context as the action.
Normal nested or wrapper-mediated action executions participate when they pass through ordinary action execution, while pass-through invocation and synthetic mixed-in property or collection access do not introduce separate priming calls.

The alternative of passing `ActionInvocation` directly was rejected because its mixin target can be the transient mixin and its argument list need not represent post-event effective arguments.
The alternative of invoking before validation was rejected because unavailable or invalid actions must not perform speculative application reads.

### Represent arguments as an immutable positional value

`ActionArguments` preserves action arity and exposes `size()`, positional `get(int)`, typed `get(int, Class<T>)`, and an unmodifiable list view.
A parameter whose value is itself an array remains one positional argument rather than being flattened as it could be by a varargs callback signature.
Typed access validates the requested type and reports an argument error with the index and expected type.

Arguments remain positional because registration already identifies the exact action and the application therefore knows its parameter contract.
Named or reflectively bound primer parameters were rejected as unnecessary additional API and metamodel coupling.

### Prime Wicket entity pages by exact root logical type

At the beginning of each relevant Wicket entity-page rendering request, resolve the page's root managed object and use its exact logical type name to select view callbacks.
Invoke matching callbacks after object visibility authorization but before expensive title, layout, property, or collection preparation that can traverse the object graph.
Use a request-local guard so each callback runs at most once for the same entity page during one rendering request, while allowing it to run again on later full or Ajax requests that have a new ORM persistence context.

The callback receives the root domain object and can issue bulk repository queries for all collections and related objects likely to be used by the heavy page.
No row-level callback is needed because the intended optimization is to prime the graph reachable from the page root.

The alternative of invoking only during initial Wicket page construction was rejected because a stateful page can be rendered in a later request with a different persistence context.
The alternative of invoking from generic model resolution was rejected because it would activate outside entity-page rendering and could recurse during callback queries.

### Treat primers as application-owned synchronous reads

Primer implementations must be synchronous, read-only, idempotent, and safe for concurrent invocation as singleton services.
A callback exception propagates through the enclosing action or Wicket request because persistence failures can invalidate the active transaction and must not be hidden.
When no callback is registered, dispatch performs only an exact map lookup and changes no application behavior.
The framework does not add primer-specific tracing, metrics, caching, retries, or exception suppression.

## Risks / Trade-offs

- [Risk] A primer can over-fetch and make a request slower. → Keep registration opt-in and leave graph selection entirely under application control.
- [Risk] A primer can perform writes or other side effects despite the read-only contract. → Document the contract prominently and propagate failures, while avoiding an unenforceable ORM-specific runtime check.
- [Risk] A Wicket lifecycle path can prepare components before the callback runs. → Add focused lifecycle tests proving that initial and subsequent request callbacks precede representative expensive member access.
- [Risk] Registering against a superclass may appear to imply subtype matching. → Validate and document exact logical-type matching and perform no hierarchy traversal.
- [Risk] Multiple callbacks for one key can duplicate reads or develop ordering dependencies. → Invoke all callbacks deterministically, document that they must be independently idempotent, and test multi-registration behavior.
- [Trade-off] Application code still handles positional action arguments. → Provide bounded typed access without introducing reflective binding or action-specific generated types.
- [Trade-off] The view hook is currently Wicket-specific. → Keep the callback contract free of Wicket types so another viewer can adopt it later under a separate lifecycle specification.

## Migration Plan

The change is additive and requires no application migration.
Applications opt in by adding a `PrimingRegistrar` service and registering selected callbacks.
Rollback consists of removing the registrar or deploying the previous framework version, with no persisted data or configuration migration.

## Open Questions

None.
