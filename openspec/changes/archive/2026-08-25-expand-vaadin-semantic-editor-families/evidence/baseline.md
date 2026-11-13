# Accepted baseline

The pre-change native registry implements bounded choices, autocomplete, nullable and required Boolean, exact and machine numeric, local and offset temporal, multiline, text, protected, URL, enum, reference, and explicit unsupported editors through toolkit-neutral codecs.
The new field adapter must preserve those codec results rather than reinterpret raw values.

The independently packaged reference closure remains unchanged:

- Asset: `vaadin-reference.js`.
- Raw bytes: 191,342.
- Gzip bytes: 48,684.
- SHA-256: `40ef3cecd641b14b7212759d45035991d9eb12550c00be64d2d7a786bf8f8a81`.
- Production packages: 19.
- Entry points: `@vaadin/combo-box` and `@vaadin/multi-select-combo-box`.
- Accepted exact style hashes: four.
- Unaffected routes: zero reference asset requests.

The field change must not alter the reference checksum, bundle size, policy, selection precedence, or route-lazy behavior.
