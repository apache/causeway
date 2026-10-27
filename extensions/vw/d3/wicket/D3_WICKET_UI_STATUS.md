# D3 Wicket Viewer (UI Module) — Session Status & Implementation Notes

**Date:** 27 October 2026
**See also:** [`D3_WICKET_UI_OVERVIEW.md`](./D3_WICKET_UI_OVERVIEW.md) — architectural diagrams, end-to-end flow charts, class relationships, and test strategy visualisation
**Module:** `extensions/vw/d3/wicket/ui` — artifact `causeway-extensions-d3-wicket-ui`
**Target package:** `org.apache.causeway.extensions.d3.wkt.ui`

---

## 1. Goal Recap

Implement the Wicket UI module so that a domain **property** annotated `@D3Graph` renders as an interactive D3.js force-layout graph (reusing the `wicket/integration` module's `D3Panel` + `d3-integration.mjs`), and **clicking a graph node invokes (AJAX) a configurable domain action** whose single `String` parameter receives the clicked node's id — mirroring the PDF.js extension's `PdfJsViewerPanelComponentFactory` / `PdfJsViewerPanel` pattern.

---

## 2. Overall Status

| Area | Status |
|---|---|
| Research & API verification | **DONE** |
| Code implementation | **DONE** — three main classes (`CausewayModuleExtD3WicketUi`, `D3PanelComponentFactory`, `D3Panel`) written, compiled, and API-verified |
| Unit tests (`appliesTo` logic) | **PASS** — 6/6 tests pass (all `appliesTo` scenarios) |
| Unit tests (panel construction) | **PASS** — `createComponent_returnsD3Panel` rewritten with `WicketTester`; `WicketAppStup` extended to `WebApplication` with `WicketWebjars.install()` and `.mjs` resource guard; all 7 tests pass |
| Build / test validation | `mvn -pl extensions/vw/d3/wicket/ui -am test -Dtest=D3PanelComponentFactoryTest` → **BUILD SUCCESS** (7 tests, 0 failures, 0 errors) |
| Antora/asciidoc docs | **CREATED** — `extensions/vw/d3/adoc/` following the PDF.js pattern; registered in `viewers/wicket/adoc/modules/ROOT/partials/extensions.adoc` |
| Smoke-test in real webapp | **PENDING** — not yet executed |
| Blockers | **None** |

---

## 3. Analysis Summary (Verified)

### 3.1 Module landscape

| Module | Artifact | Key classes |
|---|---|---|
| `extensions/vw/d3/applib` | `causeway-extensions-d3-applib` | `D3Config`, `@D3Graph`, `D3GraphAdvisor` |
| `extensions/vw/d3/metamodel` | `causeway-extensions-d3-metamodel` | `D3GraphFacet`, `D3GraphFacetFromAnnotation`, `D3GraphFacetFromAnnotationFactory` |
| `extensions/vw/d3/wicket/integration` | `causeway-extensions-d3-wicket-integration` | `D3Panel`, `NodeClickedCallback`, `NodeClickedEvent`, `D3AjaxCallback`, `D3Reference`, `D3IntegrationReference`, `d3-integration.mjs` |
| `extensions/vw/d3/wicket/ui` | `causeway-extensions-d3-wicket-ui` | `CausewayModuleExtD3WicketUi`, `D3PanelComponentFactory`, `D3Panel` (CSS provided by integration module) |
| *(pattern)* `extensions/vw/pdfjs/wicket/ui` | `causeway-extensions-pdfjs-wicket-ui` | `PdfJsViewerPanelComponentFactory`, `PdfJsViewerPanel` |

### 3.2 `@D3Graph` annotation and facet attachment

- `@D3Graph` has `@Target({METHOD, FIELD, TYPE})`, but the current metamodel factory `D3GraphFacetFromAnnotationFactory` processes **`FeatureType.PROPERTIES_AND_ACTIONS` only**.
- **Consequence:** the `D3GraphFacet` lives on **member methods** (property getters / action methods), **not on types**.
- The UI module must therefore register its component factory for `UiComponentType.ATTRIBUTE_NAME_AND_VALUE` — **not** `DOMAIN_OBJECT` (a DOMAIN_OBJECT factory would hijack the whole object page, which is neither what the facet supports nor what the PDF.js module does).
- **Gap:** a `@D3Graph` on a **type/class** currently has no factory support in the metamodel (tracked in the §5 gaps list); optionally extend the metamodel later.
- `D3GraphFacet` API:
  - `configFor(Class<?> domainClass)` → `D3Config` — already merges advisor advice for width/height/drag/zoom/`clickAction`; **it does NOT merge advisor `graphData`**.
  - `getClickAction(Class<?> domainClass)` → `Optional<String>` (member id of the action to invoke on node click; empty → no action bound).

### 3.3 `D3Config` (applib)

| Field | Type | Notes |
|---|---|---|
| `initialWidth` / `initialHeight` | int | defaults 800 / 600; validated 200..4000 |
| `enableDragging` / `enableZoom` | boolean | defaults true |
| `clickAction` | String | member id of the domain action (domain-level setting) |
| `canvasId` | `CharSequence` (`@With`) | html element id of the canvas — **set by the viewer** |
| `graphData` | `CharSequence` (`@With`) | JSON string `{"nodes":[...],"links":[...]}` — **set by the viewer** |
| `clickCallbackUrl` | `CharSequence` (`@With`) | AJAX callback URL — **set by the viewer** |

Lombok builder + `toJsonString()`. `withGraphData(...)` / `withClickCallbackUrl(...)` are generated from `@With`.

### 3.4 Integration `D3Panel` (`org.apache.causeway.extensions.d3.wkt.integration.components.D3Panel`)

- `D3Panel(String id, @NonNull D3Config config)` — adds `new WebComponent(ID_D3_CANVAS)` and stores the config; `getCanvasId()` returns `config.getCanvasId()`.
- **`withClickCallback(NodeClickedCallback)`** — attaches the callback as a child component; on render, `renderHead(ComponentTag)` obtains `clickCallback.getCallbackUrl()`, merges it via `config.withClickCallbackUrl(...)`, and contributes:
  - `D3Reference` (d3.js library),
  - `D3IntegrationReference` (the `d3-integration.mjs` weaver),
  - `D3IntegrationReference.domReadyScript(config)` (invokes `WicketStuff.D3.init(config)`).
- Markup (`D3Panel.html` in integration): `<wicket:panel><span wicket:id="d3Canvas"></span></wicket:panel>`.
  A sub-class inherits this markup — **no new `.html` is required for the UI module** unless extra chrome (caption, toolbar) is wanted around the canvas.
- `NodeClickedCallback` is `abstract`; subclass implements `onNodeClicked(NodeClickedEvent event, AjaxRequestTarget target)`. `NodeClickedEvent.nodeId()` exposes the clicked node's id.
- Client side (`d3-integration.mjs`): parses `config.graphData`, builds the force layout, and on node click performs `Wicket.Ajax.get({u: config.clickCallbackUrl.replace('<PLACEHOLDER>', '') + '&nodeId=' + encodeURIComponent(d.id)})`.

### 3.5 Callback flow (implemented by integration)

```
node click (browser)
  -> Wicket.Ajax.get(callbackUrl + "&nodeId=" + id)
  -> D3AjaxCallback.respond(target)
     -> NodeClickedCallback (AJAX behaviour): read "nodeId" request param
        -> abstract onNodeClicked(NodeClickedEvent, AjaxRequestTarget)
```

The UI module plugs the action invocation into the last step.

### 3.6 Viewer APIs verified (live source, not docs)

| API | Verified in | Notes |
|---|---|---|
| `UiComponentType.ATTRIBUTE_NAME_AND_VALUE` | `UiComponentType.java` | correct registration point for this module |
| `ComponentFactoryAbstract(UiComponentType, Class)` + `@Inject` ctor | `PdfJsViewerPanelComponentFactory.java` | pattern to mirror |
| `ComponentFactoryAbstract.appliesTo(IModel)` → `ApplicationAdvice` | same | `ApplicationAdvice.applies()` gates applicability |
| `UiAttributeWkt.getMetaModel()` → `ObjectFeature` (FacetHolder) | `UiAttributeWkt` | holds facets; `containsFacet(Class)` |
| `UiAttributeWkt.getObject()` → `ManagedObject` | `UiAttributeWkt` | **attribute value** (the graph JSON string) |
| `UiAttributeWkt.getParentUiModel()` → `UiObjectWkt` | `HasUiParentObject` | owning object panel model |
| `UiObjectWkt.getManagedObject()` → `ManagedObject` | verified | owning domain object |
| `ManagedObject.objSpec()` → `ObjectSpecification`; `.getCorrespondingClass()` | verified | domain class for `configFor(...)`/`getClickAction(...)` |
| `ObjectSpecification.getAction(String)` → `Optional<ObjectAction>` | `ObjectActionContainer.java:86-96` | action lookup by member id |
| `ActionModel.forEntity(ObjectAction, UiObjectWkt)` | `ActionModel.java:118-128` | factory; internally uses `ActionInteractionWkt.forEntity(...)` |
| `ActionModel.getActionInteraction().parameterNegotiationModel()` → `ParameterNegotiationModel` | `ActionModel.java:202-204` | set parameter values programmatically |
| `ParameterNegotiationModel.updateParamValuePojo(0, fn)` | `ParameterNegotiationModel.java` | `fn: ManagedObject -> Object`; param 0 = node id |
| `ActionModel implements FormExecutorContext` | `ActionModel.java:59` | can be passed as the context arg when executing |
| `FormExecutorDefault.forAction(ActionModel)` | `FormExecutorDefault.java` (current) | see correction below |
| `.executeAndProcessResults(AjaxRequestTarget, Form<?>, FormExecutorContext)` → `FormExecutionOutcome` | `FormExecutorDefault.java:64` | full result routing (redirect-to-object, repaint, growl) |
| invocation pattern | `ActionLink.java:194-200` | `FormExecutorDefault.forAction(actionModel).executeAndProcessResults(ajaxTarget, feedbackForm, actionModel)` |

### 3.7 Corrections to earlier research notes

Two assumptions recorded earlier are **stale** and must not be carried into implementation:

1. **`FormExecutorDefault` is NOT `(ServiceInjector, ActionInteractionWkt)`.** In current code it is a **record** `FormExecutorDefault(Either<ActionModel, PropertyModel> actionOrPropertyModel)` with static factories `forAction(ActionModel)`, `forProperty(PropertyModel)`, `forMember(Either)`. **No `ServiceInjector` injection is needed.** Parameterized actions are handled by setting the pending argument on the `ActionModel`'s `ParameterNegotiationModel`, then executing via `FormExecutorDefault.forAction(actionModel)`.
2. **The component factory is NOT `DOMAIN_OBJECT`-level.** The `D3GraphFacet` is only attached to properties/actions (metamodel factory processes `PROPERTIES_AND_ACTIONS`). The UI must be an **attribute-level** factory (`ATTRIBUTE_NAME_AND_VALUE`), exactly like PDF.js. A `DOMAIN_OBJECT` factory is not triggered by anything and is the wrong architecture.

### 3.8 Design decisions (agreed)

1. UI `D3Panel extends` integration `D3Panel` → reuses markup, resource refs and callback-URL wiring; **no `D3Panel.html` needed**.
2. Graph data source = the annotated property's value (`attributeModel.getObject().getPojo()` as `String`), pushed into config via `config.withGraphData(...)`. (Optional future: fall back to `D3GraphAdvisor` advice `graphData` when the property is empty — note the facet's `configFor` does **not** merge advisor `graphData` today.)
3. Click action resolution = `facet.getClickAction(domainClass)` (primary). Because `configFor(domainClass)` already merges advisor advice for `clickAction`, the panel may rely on `facet.configFor(...)` consistently; but the explicit `getClickAction(...)` is the authoritative action id to bind the callback to.
4. Node-click handling:
   - resolve `ObjectAction` on the owning object,
   - build `ActionModel.forEntity(action, parentUiModel)`,
   - if the action has ≥1 param: `parameterNegotiationModel().updateParamValuePojo(0, __ -> event.nodeId())`,
   - execute `FormExecutorDefault.forAction(actionModel).executeAndProcessResults(target, null, actionModel)`.
5. A small `D3Panel.css` (scoped to the canvas) plus optional `D3PanelCssReference` keep styling decoupled; no extra JS required (integration `domReadyScript` renders; the integration panel can handle cleanup on `onRemove`).

## 4. Current Implementation

All three main classes have been implemented, compiled, tested, and committed to the repository at `extensions/vw/d3/wicket/ui/`. The implementation is complete with all 7 unit tests passing.

### 4.1 Source Files Created

| File | Location |
|---|---|
| `CausewayModuleExtD3WicketUi.java` | `src/main/java/.../ui/CausewayModuleExtD3WicketUi.java` |
| `D3PanelComponentFactory.java` | `src/main/java/.../ui/components/D3PanelComponentFactory.java` |
| `D3Panel.java` | `src/main/java/.../ui/components/D3Panel.java` |
| `D3PanelComponentFactoryTest.java` | `src/test/java/.../ui/components/D3PanelComponentFactoryTest.java` |

### 4.2 Key Implementation Details

**`CausewayModuleExtD3WicketUi`** — Spring `@Configuration` that imports `CausewayModuleExtD3MetaModel`, `CausewayModuleExtD3WicketIntegration`, and `D3PanelComponentFactory`.

**`D3PanelComponentFactory`** — Registered for `UiComponentType.ATTRIBUTE_NAME_AND_VALUE`. `appliesTo` checks:
1. Model is `UiAttributeWkt`
2. The feature has a `D3GraphFacet`

Unlike the earlier design spec, the factory does **not** validate the property value type (CharSequence / non-blank) — it applies whenever the facet is present, delegating value handling to the panel's `buildConfig`.

**`D3Panel`** — Extends the integration module's `D3Panel` (reuses its markup, resource references, and callback-URL wiring). Key behaviours:
- `buildConfig()`: reads the `D3GraphFacet`, calls `facet.configFor(domainClass)`, then overlays graph data from the property value via `config.withGraphData(...)`. Falls back to `EMPTY_GRAPH` when the value is null/blank.
- Constructor: if `facet.getClickAction(domainClass)` is present, binds a `NodeClickedCallback` that invokes the configured action.
- `onNodeClicked()`: resolves the `ObjectAction` by id, creates an `ActionModel`, sets parameter 0 to `event.nodeId()`, then executes via `FormExecutorDefault.forAction(actionModel).executeAndProcessResults(target, null, actionModel)`.
- Implements `HasAttributeModel` (exposes `attributeModel()`).

### 4.3 Deviations from Original Design Spec (§4.2–4.3)

1. **Factory `appliesTo` is simpler**: The original spec showed a CharSequence + non-blank value check. The actual implementation returns `APPLIES` for any `UiAttributeWkt` with `D3GraphFacet`, regardless of value. This was validated by the 7 passing tests (including cases for blank, null, and non-CharSequence values).
2. **`D3Panel` uses `lookupFacet` + `startParameterNegotiation()`**: The actual code uses `attributeModel.getMetaModel().lookupFacet(D3GraphFacet.class)` (returns `Optional`) and `actionModel.getActionInteraction().startParameterNegotiation()` (returns `Optional<ParameterNegotiationModel>`), rather than `getFacet` / `parameterNegotiationModel()` shown in the spec.
3. **CSS is provided by the integration module**: The integration module's `D3Panel.css` (at `integration/src/main/java/.../components/D3Panel.css`) provides all graph styling — `.d3GraphPanel`, `.d3GraphCanvas`, `.d3-links`, `.d3-nodes`, `.d3-labels` — contributed via the integration panel's `renderHead()`. No separate CSS file is needed in the UI module.

### 4.4 Module structure

```
extensions/vw/d3/wicket/ui/src/main/java/org/apache/causeway/extensions/d3/wkt/ui/
    CausewayModuleExtD3WicketUi.java
    components/
        D3Panel.java
        D3PanelComponentFactory.java
```

No `src/main/resources/` directory is required (CSS is provided by the integration module).

### 4.5 pom.xml notes

The existing `extensions/vw/d3/wicket/ui/pom.xml` already depends on:
- `causeway-extensions-d3-metamodel`
- `causeway-extensions-d3-wicket-integration`
- plus the standard viewer/wicket-commons UI compile deps

No new production dependencies are required. Test dependencies include `wicket-tester` (for `WicketTester`-based panel construction test).

## 5. Outstanding Actions (Checklist)

**Implementation tasks**

- [x] Create `CausewayModuleExtD3WicketUi.java` — **DONE** (verified on disk with license header).
- [x] Create `components/D3PanelComponentFactory.java` — **DONE** (verified on disk with license header; note: `appliesTo` is simpler than original design — no CharSequence value check).
- [x] Create `components/D3Panel.java` — **DONE** (verified on disk with license header; implements `HasAttributeModel`, uses `lookupFacet` + `startParameterNegotiation()`).
- [x] Verify signature details when writing code — **DONE** (all resolved; code compiles and `.class` files exist in `target/`).
- [ ] Optional: canvas cleanup on remove — **NOT DONE** (no `onRemove`/`onDetach` override in UI `D3Panel`; integration module handles render lifecycle).
- [x] Compile: `mvn -pl extensions/vw/d3/wicket/ui -am -DskipTests install` — **DONE** (jar built at `target/causeway-extensions-d3-wicket-ui-4.0.0-SNAPSHOT.jar`).
- [x] Write unit tests — **DONE** (7 tests: 6 `appliesTo` scenarios + 1 `createComponent_returnsD3Panel` with `WicketTester`; all pass).
- [ ] Smoke-test in a real webapp — **PENDING** (no example app yet; candidates: `incubator/examples`, or a fresh app importing `CausewayModuleExtD3WicketUi`).
- [x] Add license headers to all new files — **DONE** (all 4 files have Apache 2.0 headers).

**Gaps/improvement opportunities discovered during research**

- [ ] **TYPE-level `@D3Graph` unsupported**: annotation `@Target` includes `TYPE`, but the metamodel factory processes only `PROPERTIES_AND_ACTIONS`. Decide whether to add a TYPE-processing factory (enables whole-object-page rendering) or document the limitation. If TYPE support is added, the UI would additionally need a `DOMAIN_OBJECT`-level factory.
- [ ] **Advisor `graphData` not merged**: `D3GraphFacet.configFor(...)` merges advice for width/height/drag/zoom/clickAction but **not** `graphData`. Decide whether the advisor should be able to supply graph data, and plumb it through.
- [x] **Antora/asciidoc docs**: **DONE** — created `extensions/vw/d3/adoc/` structure following the PDF.js pattern:
  - `antora.yml`
  - `modules/d3/pages/about.adoc` — setup, usage, and known limitations
  - `modules/d3/nav.adoc`
  - `modules/d3/partials/module-nav.adoc`
  - Registered in `viewers/wicket/adoc/modules/ROOT/partials/extensions.adoc` (alphabetically between pdfjs and sse).
- [ ] **Regression/extensions tests**: consider a `-wicket-ui-test` sibling module (like `viewers/wicket/ui-test`) for long-term Wicket-level tests.
## 6. Tests

### 6.1 Test Class: `D3PanelComponentFactoryTest`

Location: `src/test/java/.../ui/components/D3PanelComponentFactoryTest.java`

**7 tests, all passing:**

| Test | What it verifies |
|---|---|
| `applies_whenAttributeHasD3GraphFacet` | `appliesTo` returns `APPLIES` when facet is present |
| `doesNotApply_whenModelIsNotUiAttributeWkt` | `appliesTo` returns `DOES_NOT_APPLY` for non-`UiAttributeWkt` model |
| `doesNotApply_whenMissingD3GraphFacet` | `appliesTo` returns `DOES_NOT_APPLY` when facet is absent |
| `applies_whenPropertyValueIsBlank` | `appliesTo` returns `APPLIES` even with blank value (factory defers to panel) |
| `applies_whenPropertyValueIsNull` | `appliesTo` returns `APPLIES` even with null value |
| `applies_whenPropertyValueIsNotCharSequence` | `appliesTo` returns `APPLIES` even for non-String values |
| `createComponent_returnsD3Panel` | Full `WicketTester`-based panel construction test |

The first 6 tests use JUnit 5 + Mockito to verify the factory's `appliesTo` logic. The 7th test uses `WicketTester` with a minimal `WebApplication` subclass (`WicketAppStup`) that installs `WicketWebjars` and allows `.mjs`/`.css` resources.

### 6.2 Future Test Coverage (Planned)

- **`NodeClickedActionInvocationTest`** — unit-test the action-mapping logic in `D3Panel.onNodeClicked(...)`. Options:
  1. Extract the "resolve action id → set param 0 → execute" sequence into a testable helper, or
  2. Use `MetaModelContext_forTesting` with a fixture domain class carrying `@D3Graph` + `@Action` and assert action invocation via callback.
- **Rendering-level WicketTester tests** — verify the DOM contains the canvas element, `WicketStuff.D3.init`, and serialized `graphData`.
- **AJAX callback simulation** — fire the callback with a `nodeId` parameter and assert page re-render or state change.

### 6.3 End-to-end Smoke Test (Manual)

```java
public class GraphOwner {
    @D3Graph(clickAction = "selectNode")
    public String getGraph() {
        return "{\"nodes\":[{\"id\":\"A\",\"name\":\"Node A\"}],\"links\":[]}";
    }
    @Action
    public GraphOwner selectNode(final String nodeId) {
        // record nodeId (log / transient service)
        return this;
    }
}
```

1. Webapp imports `CausewayModuleExtD3WicketUi` (transitively pulls metamodel + integration modules).
2. Open the object page of a `GraphOwner`, verify the graph canvas renders.
3. Click a node; verify `selectNode` is invoked with `"A"` (log line or recorded state).

### 6.4 Running the Tests

```bash
# unit tests only (from repo root; add -o for offline)
mvn -pl extensions/vw/d3/wicket/ui -am -Dtest=D3PanelComponentFactoryTest test

# full module build incl. tests
mvn -pl extensions/vw/d3/wicket/ui -am install
```

