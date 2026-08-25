# Resolved toolkit policy matrix

| Explicit common property | Explicit deprecated reference | Explicit deprecated fields | Source | Effective reference | Effective fields | CSP |
|---|---|---|---|---|---|---|
| absent | absent | absent | supported default | Vaadin | basic, numeric, local-temporal | Complete reviewed union |
| `vaadin` | any | any | common | Vaadin | basic, numeric, local-temporal | Complete reviewed union |
| `native` | any | any | common | native | none | No Vaadin hashes |
| absent | `true` | absent | compatibility | Vaadin | none | Reference hashes only |
| absent | `false` | absent | compatibility | native | none | No Vaadin hashes |
| absent | absent | subset | compatibility | native | normalized subset | Selected field hashes only |
| absent | absent | empty | compatibility | native | none | No Vaadin hashes |
| absent | `true` | subset | compatibility | Vaadin | normalized subset | Deduplicated selected union |

An explicitly configured common policy always wins regardless of setter or binding order.
Any deprecated property activates the complete old-policy baseline when the common property is absent, so an omitted deprecated counterpart retains its former false or empty default rather than acquiring a new adapter unexpectedly.
The shell reports `vaadin`, `native`, or `compatibility` as the bounded resolved policy source and separately emits explicit reference and field values consumed by foundation modules.
Invalid toolkit enum values and unknown, blank, or repeated deprecated family values fail binding or normalization instead of broadening selection.
