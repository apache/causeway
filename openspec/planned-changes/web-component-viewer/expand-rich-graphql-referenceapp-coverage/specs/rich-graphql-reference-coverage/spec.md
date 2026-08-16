## ADDED Requirements

### Requirement: Pinned reference-application coverage matrix
The rich GraphQL viewer SHALL maintain a pinned reference-derived coverage matrix that classifies representative Causeway domain features as supported, intentionally viewer specific, explicitly unsupported, or requiring protocol capability.

#### Scenario: Coverage baseline is reviewed
- **WHEN** the pinned reference-application revision changes
- **THEN** representative object, service, property, action, parameter, collection, value, layout, and application-entry examples are reclassified
- **AND** contract fixtures are updated deliberately

#### Scenario: Normal verification runs offline
- **WHEN** the GraphQL viewer build executes its coverage tests
- **THEN** reduced deterministic fixtures in this repository exercise the classified contracts
- **AND** the build does not clone the external reference application

### Requirement: Explicit reversible value representations
Every rich GraphQL value shape advertised for mutation or action input SHALL have a reversible marshaller for its declared Causeway type.

#### Scenario: Supported standard value is submitted
- **WHEN** a client submits a canonical value for a supported temporal, numeric, URL, UUID, primitive, wrapper, enum, or textual type
- **THEN** GraphQL reconstructs the declared Java value
- **AND** a subsequent output round trip uses the documented canonical representation

#### Scenario: No reversible marshaller exists
- **WHEN** a property or action input uses a value type without a reversible marshaller
- **THEN** schema capability discovery or construction reports that input representation as unsupported
- **AND** the viewer does not silently pass a raw GraphQL string as the domain value

### Requirement: Reference temporal and URL coverage
The default scalar marshaller set SHALL support the reference application's standard temporal and URL types with documented canonical formats.

#### Scenario: Reference temporal fixtures are mapped
- **WHEN** the schema includes `LocalDate`, `LocalDateTime`, `LocalTime`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `java.util.Date`, `java.sql.Date`, or `java.sql.Timestamp`
- **THEN** each supported type has deterministic input coercion, output serialization, null behavior, and invalid-input diagnostics

#### Scenario: URL fixture is mapped
- **WHEN** a property or parameter declares `java.net.URL`
- **THEN** the canonical GraphQL representation round-trips a valid URL
- **AND** rejects malformed input with a typed coercion error

### Requirement: Discoverable value semantics
A rich datatype descriptor SHALL identify the logical type, representation category, GraphQL input and output shape, canonical format, relevant constraints, and resource behavior needed by a framework-neutral client.

#### Scenario: Client discovers an editor-neutral value contract
- **WHEN** targeted introspection reaches a property or action-parameter datatype descriptor
- **THEN** the client can distinguish textual, temporal, numeric, boolean, enum, object-reference, resource, composite, and opaque categories
- **AND** no HTML control or frontend framework is prescribed

#### Scenario: Application registers a custom marshaller
- **WHEN** an application supplies a reversible scalar marshaller and value-semantics descriptor for its custom value
- **THEN** the rich schema advertises that registered representation
- **AND** uses it for supported reads and inputs

### Requirement: Structured known-member metadata
Existing rich property, action, parameter, and collection wrappers SHALL expose additive structured metadata required by generic clients without adding a duplicate member-list endpoint.

#### Scenario: Client requests member presentation
- **WHEN** a client already knows a semantic member identifier and requests its metadata fields
- **THEN** the wrapper can return canonical friendly name and description independently
- **AND** returns applicable constraints and presentation hints

#### Scenario: Client discovers members
- **WHEN** a client needs the member identifiers for a logical type
- **THEN** it continues to use standard targeted GraphQL introspection
- **AND** does not call a separate metadata member-list API

### Requirement: Property constraints and hints
A rich property wrapper SHALL expose framework-neutral constraint and input hints represented by the Causeway metamodel where applicable.

#### Scenario: Constrained text property is inspected
- **WHEN** a property declares optionality, maximum length, regular-expression intent, multiline, or typical-length semantics
- **THEN** those semantics are available as structured metadata
- **AND** server validation remains authoritative

#### Scenario: Resource-valued property is inspected
- **WHEN** a property declares accepted-file or resource constraints
- **THEN** the client can discover accepted media or filename patterns and the supported transfer representation

### Requirement: Action and parameter hints
A rich action and its parameter wrappers SHALL expose framework-neutral invocation and presentation hints represented by the Causeway metamodel where applicable.

#### Scenario: Action metadata is inspected
- **WHEN** an action declares semantics, prompt style, redirect policy, field-set association, position, sequence, icon, or CSS hints
- **THEN** the corresponding supported metadata is queryable independently from dynamic hidden and disabled state

#### Scenario: Client ignores an optional hint
- **WHEN** a client does not implement a presentation hint
- **THEN** action validation and invocation behavior remains correct

### Requirement: Collection semantics and bounded reads
A rich collection wrapper SHALL expose collection presentation and ordering semantics and SHALL support bounded deterministic content retrieval.

#### Scenario: Client requests a collection window
- **WHEN** a client supplies a valid start position or cursor and requested size
- **THEN** the response contains only that deterministic row window
- **AND** reports returned size, continuation state, and total count when available

#### Scenario: Existing client requests unargumented get
- **WHEN** a compatibility client uses the established unargumented collection field
- **THEN** the request remains valid during the documented migration period

#### Scenario: Collection has configured ordering
- **WHEN** Causeway supplies a supported stable collection ordering
- **THEN** every response window applies that ordering consistently

### Requirement: Explicit resource-value interaction
Blob, Clob, and other supported resource values SHALL have consistent query, mutation, action-parameter, and action-result capability contracts.

#### Scenario: Client requests resource metadata
- **WHEN** a resource-valued member is available
- **THEN** filename, media type, size policy, and transfer mode can be requested without downloading the content

#### Scenario: Client submits supported resource input
- **WHEN** an update or action accepts a resource representation within configured limits
- **THEN** GraphQL reconstructs the declared domain resource value
- **AND** enforces accepted-file and size constraints

#### Scenario: Sensitive or unsupported resource is encountered
- **WHEN** a password, hidden value, image, tree, composite, or custom value has no safe registered representation
- **THEN** the schema reports the operation as unsupported
- **AND** does not serialize the value through generic string fallback

### Requirement: Application-entry metadata
The rich GraphQL root SHALL expose enough metadata to identify visible domain-service grouping and ordering and the configured home-page action without prescribing viewer navigation.

#### Scenario: Client composes service entry points
- **WHEN** visible contributing domain services are available
- **THEN** a client can discover their friendly grouping, ordering, and actions through GraphQL
- **AND** action execution reuses the established parameter and invocation contracts

#### Scenario: Application defines a home page
- **WHEN** Causeway has a configured home-page action
- **THEN** GraphQL identifies that semantic action and its service
- **AND** the client decides how and when to navigate to its result

### Requirement: Backward-compatible rich-schema expansion
Reference-coverage additions SHALL preserve established rich object reads, service actions, property and action interactions, and generated naming unless a separately documented compatibility mode is required.

#### Scenario: Existing client schema contract is exercised
- **WHEN** compatibility tests execute established query and mutation documents
- **THEN** their fields, arguments, operation placement, and response shapes remain valid

#### Scenario: Lossy fallback input is deprecated
- **WHEN** an existing configuration relied on arbitrary raw-string fallback input
- **THEN** migration diagnostics identify the affected logical types
- **AND** the compatibility policy and replacement marshaller path are documented

### Requirement: Reference-derived executable verification
The GraphQL viewer build SHALL exercise representative reference-application coverage through deterministic schema, query, mutation, and compatibility tests.

#### Scenario: Representative coverage suite runs
- **WHEN** verification executes the reduced reference-derived fixtures
- **THEN** it covers standard and custom values, constraints, member metadata, collections, services, home-page actions, resources, bulk parameters, mixins, entities, and view models
- **AND** compares each outcome with the pinned coverage classification

#### Scenario: New capability is introspected
- **WHEN** a client uses targeted one-type introspection for added wrapper fields and arguments
- **THEN** capability discovery succeeds without broad repeated `__Type.fields` requests
