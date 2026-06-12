## Context

The command replay mapping SPI already lets applications remap replay targets and reference parameters, and it notifies listeners after a successful replay maps a recorded result bookmark to an actual result bookmark.
The common replay workflow needs those result mappings to be remembered so that later commands can remap inputs that refer to objects created or otherwise mapped by earlier replayed commands.
Today the SPI provides the hooks, but applications must supply their own listener before replay input remapping can use mappings observed earlier in the same replay session.

## Goals / Non-Goals

**Goals:**

- Provide a default `CommandReplayMappingListener` implementation that stores recorded-to-actual replay result mappings in memory.
- Make the default implementation available through autoconfiguration only when an application has not defined another listener bean.
- Keep the listener behavior simple, deterministic, and aligned with the existing SPI methods.
- Preserve custom application listeners as the extension point for persistent, distributed, or domain-specific mapping behavior.

**Non-Goals:**

- Persist replay mappings across JVM restarts or application nodes.
- Change the `CommandReplayMappingListener` SPI method signatures.
- Replace user-defined listener beans or combine multiple default mapping stores.
- Resolve bookmarks to live domain objects before storing or remapping them.

## Decisions

- Implement the default listener as a command log extension class that implements `CommandReplayMappingListener` directly.
  This keeps the behavior close to the SPI and avoids adding a new service abstraction for simple map-backed state.
  An alternative was to add a separate mapping store service, but that would add indirection without current persistence or distribution requirements.

- Use a simple in-memory `HashMap<Bookmark, Bookmark>` keyed by the recorded bookmark and valued by the actual replay bookmark.
  `onReplayResultMapped(...)` will update the map only when the actual bookmark differs from the recorded bookmark, and `remap(...)` will return the mapped actual bookmark when present.
  Equal recorded and actual bookmarks do not need saved mappings because replay can keep using the recorded bookmark unchanged.
  An alternative was to store identity mappings too, but that would grow the map without changing later remapping behavior.
  Another alternative was to use a concurrent map, but command replay is expected to use this listener in a straightforward replay flow and the requested implementation explicitly calls for a simple hashmap.

- Register the default listener with Spring Boot autoconfiguration guarded by `@ConditionalOnMissingBean(CommandReplayMappingListener.class)`.
  This lets applications override the default by declaring their own listener bean while giving simpler applications out-of-the-box remapping behavior.
  An alternative was unconditional registration, but that would interfere with custom listener implementations and could create multiple mapping listeners with overlapping behavior.

- Keep the default map lifecycle bound to the Spring bean lifecycle.
  This matches the requirement for a simple default implementation and avoids adding clearing, scoping, or persistence semantics in this change.
  Applications that need different lifecycle semantics can provide their own listener bean.

## Risks / Trade-offs

- In-memory mappings are lost on restart or bean recreation → Document the default as an in-memory convenience and leave durable behavior to custom listener beans.
- A singleton map can retain mappings longer than a replay batch → Keep the implementation simple for this change and rely on custom listeners where tighter scoping is required.
- Multiple replay batches in the same JVM could observe previous mappings → Use recorded bookmark keys so repeated mappings overwrite previous values, and avoid claiming batch isolation for the default implementation.
- `HashMap` is not thread-safe → Treat the default as suitable for simple replay use and allow applications with concurrent replay requirements to replace it with their own bean.
