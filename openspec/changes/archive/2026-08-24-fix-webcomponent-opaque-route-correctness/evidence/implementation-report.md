# Opaque-route implementation report

## Corrected policy

The Java and browser codecs no longer reject identity based on the former 1,024 UTF-16-character ceiling.
Both codecs bound source UTF-8 to 4,096 bytes and final canonical percent-encoded representation to 4,096 ASCII characters.
The server additionally retains its complete raw-path bound and strict decode-then-reencode equality.

Route generation rejects unpaired surrogates, separators, controls, dot segments, excessive UTF-8, and excessive encoded expansion before returning a path.
Route parsing rejects malformed UTF-8 and escapes, encoded separators, controls, alternate percent spellings, excess segments, and excess complete paths.
Every failure uses the unchanged generic route error and does not echo submitted content.

## Reference Application outcome

The pinned composite-values identifier safely measures 1,177 Java UTF-16 characters and 1,179 canonical encoded characters.
It now survives GraphQL result identity, semantic navigation, browser canonicalization, HTMX replacement, server parsing, HTML attribute escaping, object-context reconstruction, GraphQL lookup, and browser back and forward restoration.
The `complexNumber` property remains visible after reconstruction.
A malformed encoded separator still reaches the non-disclosing `invalid-route` state.

## Compatibility and rollback

The route grammar, public navigation event, result event, logical type, identifier, base path, context path, response headers, custom-page precedence, generic fallback, and history policy are unchanged.
Ordinary Petclinic and Reference Application routes continue through the same producer and parser.
Rollback restores conservative rejection of long memento routes without data migration or persisted-state impact.

## Retained scope

Raw collection rows incompatible with an advertised union remain bounded and separate.
Paged GraphQL reference autocomplete remains the next planned change.
