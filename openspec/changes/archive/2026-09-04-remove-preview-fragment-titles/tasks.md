## 1. HTMX Preview Composition

- [x] 1.1 Remove `<cw-object-header>` from all Petclinic runtime-type default preview resources while preserving their selected detail members.
- [x] 1.2 Remove `<cw-object-header>` from the PetOwner inline Pet preview while preserving editable notes, actions, and nested visits.
- [x] 1.3 Update HTMX source and browser tests to verify title-free live previews and retained useful content.

## 2. Vue Preview Parity

- [x] 2.1 Remove `<cw-object-header>` from Owner and Visit previews on the Vue Home page.
- [x] 2.2 Remove `<cw-object-header>` from Pet and Visit previews on the Vue PetOwner page.
- [x] 2.3 Update Vue browser assertions and regenerate packaged frontend assets through the established build lifecycle.

## 3. Documentation and Verification

- [x] 3.1 Update the foundation preview example and Petclinic documentation to recommend concise preview bodies without repeated titles.
- [x] 3.2 Run complete foundation, HTMX, Vue, native/Vaadin, Maven packaging, OpenSpec, IDE, and whitespace validation.
