# Baseline and resource contract

## Previous application customization

Before implementation, `HtmxPageFragmentRegistry` accepted only injected `HtmxPageFragmentFactory` beans.
`PetClinicHomeFragmentFactory` embedded the HomePage Causeway markup in a Java text block.
PetOwner, Pet, and Visit routes selected generic `<causeway-object editable>` composition.
The stable shell, exact route codec, strict CSP, Vaadin-default policy, native rollback, layout resources, application stylesheet, and Playwright matrices were already qualified.

## Accepted resource contract

The private discovery pattern is `classpath*:/META-INF/causeway/webcomponents/pages/*.html`.
The basename before `.html` is the exact logical type.
Accepted pages are loaded once during startup as immutable literal strings.
The per-page limit is 262,144 UTF-8 bytes and the aggregate limit is 512 pages.
Empty content, malformed UTF-8, NUL content, invalid names, unreadable resources, count overflow, and duplicate resource or factory definitions fail with bounded classified errors.
Only absence selects generic `<causeway-object editable>` fallback.

## Accepted Petclinic pages

| Page | Bytes | SHA-256 |
|---|---:|---|
| `petclinic.HomePage.html` | 2,011 | `44afaf83d8d434fabc1c92dc506253ff6f9ba50e100223bacfd1685697c479fb` |
| `petclinic.Pet.html` | 1,608 | `96eb6f9988abad5701c168fe4c4787e4544fdbd66aadc884cf9998d22fe46798` |
| `petclinic.PetOwner.html` | 4,267 | `eb73a48873498781f6a47496628305a8a47b53537b20074c78bd92be601e0b09` |
| `petclinic.Visit.html` | 1,635 | `25552ecf5619e7cd12cbabe6de391493ec9dfa18223e3de21f7f5b809d32098e` |

Every page is well beneath the fixed ceiling.
No page contains a script element, inline style attribute, inline click handler, or raw Vaadin element.

## Retained fallback resources

The following SHA-256 values are byte-identical to planning commit `7883e6baf3e`:

| Resource | SHA-256 |
|---|---|
| `menubars.layout.xml` | `893e6e8de2598e47a089d2952008a6a7c1008143a3b2d0d9646fbb78c92ea6e2` |
| `Pet.layout.xml` | `294d1f9b594d17f5273fbe2df9c858a630cc72d5d73413a9f545e4641fc87df8` |
| `PetClinicHomePage.layout.xml` | `3c93c7302ff03da12d7524bf8ee898db3a9e17cb91ecc204dc5dbde902b3930b` |
| `PetOwner.layout.xml` | `6d0858bee0eacf4ac55bc3281693f4e791602e7ff41c799885a7aa0f91b87d1a` |
| `Visit.layout.xml` | `0c7ba2173b67a196036dbd5edf0b620ad776c2c03251e454a33deac316f15ef1` |
| `HomePage#futureVisits.columnOrder.txt` | `1f66e4d17f50c2a8c8e7a64b45e6b15187507b66bbc85c95422e9d85a02edad5` |
| `HomePage#petOwners.columnOrder.txt` | `10ed9ce4f449a9e56e1e0507239e017d19044a8560ddf998a36faaa7041563b3` |
| `PetOwner#pets.columnOrder.txt` | `af770713f41495e526c9a8c9a51b31b968cd832b167582fe3ea42ee59010a801` |
| `PetOwner#visits.columnOrder.txt` | `1f66e4d17f50c2a8c8e7a64b45e6b15187507b66bbc85c95422e9d85a02edad5` |

The application stylesheet changed intentionally from SHA-256 `cb89814d22663c46878c68f3136062cfaf23f03740849769db67349efc75614a` to `abe338010af0d1048f80616d2cd127b81f5762eb37bddff9e583ae224e7b4b0f` to style authored cards, action groups, and responsive grids.
