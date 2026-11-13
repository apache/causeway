# Commit and Branch Naming Guidance

These guidelines define the preferred branch naming and commit message format.


## Feature Branch Naming

Feature branches typically follow this pattern:

`PROJKEY-123456`

but are sometimes longer:

`PROJKEY-123456-short-description-lower-case`

Rules:

1. `PROJKEY` is the ServiceDesk project key
2. `1234567` is the ServiceDesk work item number within that project
3. `short-description-lower-case` is a short lowercase description, using hyphens between words.

Example:

`PROJKEY-1234567-add-commit-message-standards`


## Commit Summary Format

When the current branch is a feature branch (a work item, eg `PROJKEY-1234567`), the commit summary begins with the matching work item id:

```
<branchname>: <present-tense description>
```

- `<branchname>` is the current git branch (typically the ticket id, e.g. `PROJKEY-1234567`).
- The description completes the sentence *"This patch, if applied, will ..."*, so use the
  third-person present tense: **"makes"**, **"adds"**, **"fixes"**, **"removes"**, **"bumps"**.
- Do not use past tense ("made", "added") or imperative ("make", "add"); keep it lower-case after
  the colon and omit a trailing period.

When the current branch does **not** match that feature branch convention, omit the prefix and use only the summary text.


### Summary Writing Rules

1. Write the summary in **present tense** so it reads naturally as a completion of:  
   `this patch, if applied, ...`
2. Keep the summary line within **72 characters**, including any `PROJKEY-1234567: ` prefix.
3. Make the summary specific enough that future automation or reviewers can understand the change at a glance.

Examples:

```
JDOJPA-373: makes MinioClient timeouts configurable
ESP-360: adds @ParameterLayout to explicitly set the ParameterNamedFacet
JDOJPA-322: bumps pipeline resources
```

## Commit Body

The commit body is optional, but if present, it should provide additional context or explanation for the change.

When a body is present:

1. Leave a **blank line** between the summary and the body.
2. Use the body to explain useful context, what changed, or why the change was needed.
3. Keep the body focused on information that does not fit cleanly in the summary.

Example:

```text
PROJKEY-1234567: adds commit naming guidance

Documents the feature branch pattern and the commit summary prefix rule
so repositories consuming this library can apply the same convention.
```



