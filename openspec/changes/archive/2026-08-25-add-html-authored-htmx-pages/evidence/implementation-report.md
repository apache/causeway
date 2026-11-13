# Implementation report

## Discovery and qualification

`HtmxClasspathPageLoader` uses Spring's `classpath*:` resource pattern support to discover private HTML pages from exploded application directories and dependency jars.
It validates the exact filename, finite page count, bounded bytes, strict UTF-8, non-empty content, and absence of NUL characters before constructing immutable resource definitions.
Diagnostics use bounded logical names and filenames rather than classpath URLs or page content.
I/O exception details are not retained in the public startup exception.

Unit fixtures exercise accepted literal content, exploded directories, jars, invalid names, empty content, malformed UTF-8, NUL content, unreadable resources, oversized pages, discovery failure, and page-count overflow.

## Unified registry and fallback

`HtmxPageDefinition` adapts resource pages and existing Java factories behind one internal representation.
`HtmxPageFragmentRegistry` merges both sources into one immutable exact-logical-type map.
Resource/resource, resource/factory, and factory/factory conflicts fail before routing.
Existing factory route delivery remains compatible.
Resource HTML is returned literally and never receives the route object.

`HtmxPageRenderer` retains `data-page-kind="custom"` compatibility and adds safe `data-page-source="resource|factory|generic"` evidence.
Every route still has one escaped `<causeway-object-context>` and one interaction controller.
An absent definition still renders `<causeway-object editable>` beneath that context.

## Petclinic conversion

Petclinic now packages exact-type HomePage, PetOwner, Pet, and Visit HTML files under the private resource root.
The files visibly compose ordinary application regions, headings, properties, actions, references, collections, and collection columns.
All use the existing route context, GraphQL client, action interaction controller, application stylesheet, and semantic events.

`PetClinicHomeFragmentFactory` was removed.
An executable classpath assertion proves the class is absent.
Direct HTTP assertions prove private page resources are not served as static files.

The existing grid, column-order, menu, provenance, domain, fixture, Wicket, configuration, and route resources remain.
Fallback is therefore an absence policy rather than recovery from a defective registration.

## Browser behavior

The existing Playwright suite now enters all four resource pages and verifies exact resource classification.
It retains direct entry, home replacement, object links, back and forward history, focus restoration, menus, property editing, validation, action prompts, cancellation, collections, semantic results, and responsive overflow checks.
Both Vaadin-default and explicit native matrices pass over identical HTML pages.
No public Vaadin element appears in application markup.
