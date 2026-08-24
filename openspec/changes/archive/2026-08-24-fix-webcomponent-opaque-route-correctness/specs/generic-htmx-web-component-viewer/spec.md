## MODIFIED Requirements

### Requirement: Canonical bookmark routing
The viewer SHALL map a public logical type and opaque object identifier to one documented round-trippable route grammar beneath a configurable base path.
Each browser-generated and server-parsed segment MUST use the same canonical UTF-8 percent encoding and MUST remain within the documented encoded-segment bound without interpreting or altering identifier content.

#### Scenario: Direct object route is requested
- **WHEN** `<base-path>/object/<logical-type>/<identifier>` contains independently canonical percent-encoded route segments
- **THEN** the router renders the object page for that exact logical route identity
- **AND** browser history represents the same canonical route

#### Scenario: Semantic navigation is requested
- **WHEN** a component publishes an object navigation or object-result event
- **THEN** default route policy constructs the same canonical route from the exact advertised identity
- **AND** HTMX replaces only the route region while pushing that route into browser history

#### Scenario: Long authoritative identifier remains within the route bound
- **WHEN** GraphQL returns a valid opaque identifier longer than the former decoded-character limit whose canonical encoded segment remains within the documented bound
- **THEN** browser and server codecs preserve and round-trip the identifier exactly
- **AND** direct load, HTMX replacement, history restoration, back, and forward reconstruct the same object context

#### Scenario: Route encoding is invalid
- **WHEN** a route contains malformed escapes, empty values, encoded separators, control characters, dot segments, malformed Unicode, overlong encoded segments, or a non-canonical encoding
- **THEN** the viewer presents a bounded invalid-route state
- **AND** does not disclose object state, authorization rules, submitted route content, or raw decoder exceptions

#### Scenario: Viewer is mounted elsewhere
- **WHEN** an application configures a valid non-root base path
- **THEN** shell, object, asset, home, and history URLs consistently use that path
- **AND** no route assumes deployment at the origin root
