# semantic-member-span-naming Specification

## Purpose
TBD - created by archiving change port-semantic-member-names. Update Purpose after archive.
## Requirements
### Requirement: Action observations use domain-facing contextual names

Causeway SHALL preserve `causeway.member.action` as the main action operation name and assign contextual names `act <logical-type>#<member>` using domain-facing action identity. It SHALL add `causeway.action.id` containing the complete canonical logical action identifier, including declared parameter signatures. Physical receiver, invoked identifier, rule state and publishing/DTO behavior SHALL remain unchanged.

#### Scenario: Declared action
- **WHEN** a declared action is observed
- **THEN** its display name begins with act and identifies the domain type/member, while its operation remains causeway.member.action and its semantic attribute retains the complete signature

#### Scenario: Contributed action
- **WHEN** a mixin implementation method act contributes Customer#updateName
- **THEN** the contextual name and causeway.action.id identify Customer#updateName rather than the mixin method, without changing the actual invocation identity

### Requirement: Mixed-in association accesses have distinct semantic categories

Causeway SHALL recognize mixed-in property/collection access by facet and metamodel association linkage. Resolved singular access SHALL use `causeway.property.access`, contextual prefix prop and `causeway.property.id`. Resolved collection access SHALL use `causeway.collection.access`, contextual prefix coll and `causeway.collection.id`. Both SHALL use the full domain-facing association identifier. Only the applicable semantic identifier key SHALL be emitted; association spans SHALL NOT be labelled causeway.action.id. Existing generic metadata SHALL remain available.

#### Scenario: Property access through a mixin
- **WHEN** a mixin implements a domain property's getter
- **THEN** one prop span identifies the domain property in its property-access category, not as an action invocation

#### Scenario: Collection access through a mixin
- **WHEN** a mixin implements a domain collection's getter
- **THEN** one coll span identifies the domain collection in its collection-access category

#### Scenario: Unresolved association
- **WHEN** the association facet is present but its domain association cannot be recovered safely
- **THEN** ordinary action observation remains available without inventing association identity or dropping instrumentation

### Requirement: Compact display names preserve canonical identity and privacy

Contextual names SHALL contain only operation prefixes and static logical model identifiers, preserving declared casing. The formatter SHALL retain a full name when it fits within 50 characters, otherwise retry with a namespace-free type name, then truncate to 50 characters if necessary. The semantic identity attribute SHALL retain its complete identifier independently of compaction. Names and semantic attributes SHALL NOT include instance IDs, bookmarks, titles, argument values, usernames, tenancy values or localized labels.

#### Scenario: Short full name
- **WHEN** an act, prop or coll name fits the display budget
- **THEN** its complete logical type/member name and casing are retained

#### Scenario: Long name
- **WHEN** a name exceeds 50 characters
- **THEN** its namespace is shortened before truncation, while its full semantic identity attribute is unchanged

#### Scenario: Collision or overload
- **WHEN** different canonical members have the same compact display name or actions have different declared parameter signatures
- **THEN** their full semantic attributes distinguish them

#### Scenario: Instance-specific inputs
- **WHEN** the same logical operation runs for different instances or arguments
- **THEN** display name and semantic attributes remain identical and contain none of those instance-specific values

### Requirement: Both tracing owners preserve lifecycle and semantic names

Semantic naming SHALL work under supported Boot-managed and agent-managed tracing, preserving casing, parentage, scopes, attributes and errors. It SHALL create no competing registry, SDK/exporter or duplicate observations, and inactive observations SHALL remain inert. Root interaction, HTTP, JDBC and unrelated instrumentation boundaries SHALL remain unchanged by this feature.

#### Scenario: Boot and agent exports
- **WHEN** representative declared/mixed-in action, property and collection work is collected in each mode
- **THEN** exported spans show the expected contextual names and canonical attributes with existing ancestry and exactly one member observation per invocation

#### Scenario: Failure followed by success
- **WHEN** observed member work fails and later work succeeds
- **THEN** the original failure is preserved and the later execution inherits no stale scope or name

#### Scenario: Observation disabled
- **WHEN** the observation profile is inactive
- **THEN** invocation behavior is unchanged and no Causeway semantic spans are emitted

### Requirement: Operators can inspect semantic member spans

The M3 how-to and observability guide SHALL integrate the new capability into their existing content, explaining operation versus display identity, action/property/collection categories, canonical attributes and compact-name collisions. They SHALL present the capability as new M3 functionality without before/after comparisons or migration tables. They SHALL identify the Wicket/root-naming features that remain out of scope and SHALL NOT require new naming configuration flags.

#### Scenario: Reader inspects semantic spans
- **WHEN** a reader follows the updated guide
- **THEN** they can recognize representative act/prop/coll spans and search by full semantic identifier rather than relying on potentially shortened display names

