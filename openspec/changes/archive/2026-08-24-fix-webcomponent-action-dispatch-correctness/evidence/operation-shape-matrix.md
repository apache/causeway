# Reviewed action operation-shape matrix

## Authoritative Reference Application shapes

Targeted introspection was recorded against the pinned Reference Application with secured user `sven`.
The public schema was not modified to obtain these shapes.

| Target | Action wrapper | Executable placement | Arguments | Result |
|---|---|---|---|---|
| `demo.ActionChoicesMenu.choices` | `rich__demo_ActionChoicesMenu__choices__gqlv_action` advertises `hidden`, `disabled`, and `validate`, but no nested invocation field | Mutation `demo_ActionChoicesMenu__choices` | None | Direct `rich__demo_ActionChoices` |
| `demo.ActionChoices.selectTvCharacter` | Wrapper advertises parameter state and validation, but no nested invocation field | Mutation `demo_ActionChoices__selectTvCharacter` | `_target: rich__demo_ActionChoices__gqlv_input`, `tvCharacter: rich__demo_DependentArgsDemoItem__gqlv_input` | Direct `rich__demo_ActionChoices` |
| `demo.ActionChoices.selectTvCharacterByShow` | Wrapper advertises parameter state and validation, but no nested invocation field | Mutation `demo_ActionChoices__selectTvCharacterByShow` | `_target`, `tvShow`, `tvCharacter` | Direct `rich__demo_ActionChoices` |
| `demo.ActionChoices.selectTvCharacters` | Wrapper advertises parameter state and validation, but no nested invocation field | Mutation `demo_ActionChoices__selectTvCharacters` | `_target`, `tvCharacters` | Direct `rich__demo_ActionChoices` |
| `demo.ActionChoices.selectTvCharactersByShow` | Wrapper advertises nested `invoke` | Nested safe query | `tvShow`, `tvCharacters` through the nested field; object identity through the enclosing lookup | Envelope with `target`, `args`, and `results`; `results` is `rich__demo_ActionChoices` |
| `demo.ActionChoices.selectTvCharactersByShows` | Wrapper advertises nested `invoke` | Nested safe query | `tvShows`, `tvCharacters` through the nested field; object identity through the enclosing lookup | Envelope with `target`, `args`, and `results`; `results` is `rich__demo_ActionChoices` |

## Root cause confirmed

`demo.ActionChoicesMenu.choices` is not a nested safe invocation.
It is a parameterless flat mutation returning a view model directly.
The returned `rich__demo_ActionChoices` metadata type advertises identity, logical type, and title but not every entity concurrency field.
The previous generic result selector requested `_meta.version` unconditionally, so operation rendering failed before GraphQL execution and the interaction controller displayed only `Action invocation failed`.

The corrected selector derives metadata children from the effective metadata type.
The corrected dispatch planner also prevents an advertised legacy nested `invokeNonIdempotent` field from hiding an available top-level mutation.

## Closed foundation matrix

Foundation fixtures cover:

- nested `invoke` and `invokeIdempotent` placement;
- direct top-level object and service mutation placement;
- top-level mutation preference over `invokeNonIdempotent`;
- legacy nested mutation fallback when no root mutation exists;
- object target argument discovery by generated input type;
- absence of manufactured service targets;
- direct scalar and direct versionless object results;
- `results` envelopes;
- required-argument and missing-target planning errors;
- unsupported action plans;
- omission of unadvertised metadata fields.

General polymorphic union fragments remain outside this change.
Versionless action preparation cases not solved by effective result selection remain candidates for the next identity-focused change.
