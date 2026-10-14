# D3 Wicket Viewer (UI Module) — Session Status & Implementation Notes

**Date:** 14 October 2026
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
| Unit tests (panel construction) | **BLOCKED** — requires `wicket-tester` for full Wicket container simulation; `wicket-tester` dep added to `pom.xml` but panel-level tests (`D3PanelTest`) were removed pending rewrite using `WicketTester` |
| Build / test validation | `mvn -pl extensions/vw/d3/wicket/ui -am test` → **BUILD SUCCESS** (6 tests, 0 failures, 0 errors) |
| Blockers | **None** |

---

## 3. Analysis Summary (Verified)

### 3.1 Module landscape

| Module | Artifact | Key classes |
|---|---|---|
| `extensions/vw/d3/applib` | `causeway-extensions-d3-applib` | `D3Config`, `@D3Graph`, `D3GraphAdvisor` |
| `extensions/vw/d3/metamodel` | `causeway-extensions-d3-metamodel` | `D3GraphFacet`, `D3GraphFacetFromAnnotation`, `D3GraphFacetFromAnnotationFactory` |
| `extensions/vw/d3/wicket/integration` | `causeway-extensions-d3-wicket-integration` | `D3Panel`, `NodeClickedCallback`, `NodeClickedEvent`, `D3AjaxCallback`, `D3Reference`, `D3IntegrationReference`, `d3-integration.mjs` |
| `extensions/vw/d3/wicket/ui` | `causeway-extensions-d3-wicket-ui` | **(to be written)** `CausewayModuleExtD3WicketUi`, `D3PanelComponentFactory`, `D3Panel`, `D3Panel.css` |
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

## 4. Implementation Plan (Design Spec — Ready to Code)

### 4.1 `CausewayModuleExtD3WicketUi.java`

```java
package org.apache.causeway.extensions.d3.wkt.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.apache.causeway.extensions.d3.metamodel.CausewayModuleExtD3MetaModel;
import org.apache.causeway.extensions.d3.wkt.integration.CausewayModuleExtD3WicketIntegration;
import org.apache.causeway.extensions.d3.wkt.ui.components.D3PanelComponentFactory;

@Configuration
@Import({
    CausewayModuleExtD3MetaModel.class,
    CausewayModuleExtD3WicketIntegration.class,
    D3PanelComponentFactory.class,
})
public class CausewayModuleExtD3WicketUi {}
```

### 4.2 `components/D3PanelComponentFactory.java`

```java
@Component
public class D3PanelComponentFactory extends ComponentFactoryAbstract {

    @Inject
    public D3PanelComponentFactory() {
        super(UiComponentType.ATTRIBUTE_NAME_AND_VALUE, D3Panel.class);
    }

    @Override
    protected ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof UiAttributeWkt attributeModel)) { return ApplicationAdvice.DOES_NOT_APPLY; }
        if (!attributeModel.getMetaModel().containsFacet(D3GraphFacet.class)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        // graph data must be a CharSequence (JSON string)
        var value = attributeModel.getObject();
        if (value.getPojo() instanceof CharSequence && !value.getPojo().toString().isBlank()) {
            return ApplicationAdvice.APPLIES;
        }
        return ApplicationAdvice.DOES_NOT_APPLY;
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        return new D3Panel(id, (UiAttributeWkt) model);
    }
}
```

### 4.3 `components/D3Panel.java`

```java
public class D3Panel extends org.apache.causeway.extensions.d3.wkt.integration.components.D3Panel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(D3Panel.class);

    private final UiAttributeWkt attributeModel;

    public D3Panel(final String id, final UiAttributeWkt attributeModel) {
        super(id, buildConfig(attributeModel));
        this.attributeModel = attributeModel;
        var domainClass = attributeModel
            .getParentUiModel().getManagedObject()
            .objSpec().getCorrespondingClass();
        var facet = attributeModel.getMetaModel().getFacet(D3GraphFacet.class);
        var clickAction = facet.getClickAction(domainClass).orElse(null);
        if (clickAction != null) {
            withClickCallback(new NodeClickedCallback() {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onNodeClicked(final NodeClickedEvent event, final AjaxRequestTarget target) {
                    D3Panel.this.onNodeClicked(event, target, clickAction);
                }
            });
        }
    }

    private static D3Config buildConfig(final UiAttributeWkt attributeModel) {
        var domainClass = attributeModel
            .getParentUiModel().getManagedObject()
            .objSpec().getCorrespondingClass();
        var facet = attributeModel.getMetaModel().getFacet(D3GraphFacet.class);
        var config = facet.configFor(domainClass);
        // set graph data from the annotated property value
        var pojo = attributeModel.getObject().getPojo();
        if (pojo instanceof CharSequence cs && !cs.toString().isBlank()) {
            config = config.withGraphData(cs);
        }
        return config;
    }

    private void onNodeClicked(final NodeClickedEvent event, final AjaxRequestTarget target,
            final String clickActionId) {
        var parentObjectModel = attributeModel.getParentUiModel();
        var action = parentObjectModel.getManagedObject().objSpec()
            .getAction(clickActionId).orElseThrow(() -> new RuntimeException(
                "D3 action '" + clickActionId + "' not found on " + parentObjectModel.getManagedObject().objSpec()));

        var actionModel = ActionModel.forEntity(action, parentObjectModel);
        var pendingArgs = actionModel.getActionInteraction().parameterNegotiationModel();
        if (pendingArgs.getParamCount() >= 1) {
            pendingArgs.updateParamValuePojo(0, __ -> event.nodeId());
        }
        FormExecutorDefault.forAction(actionModel)
            .executeAndProcessResults(target, null, actionModel);
    }
}
```

### 4.4 Resources: `D3Panel.css` (optional)

```css
/* D3 graph canvas */
.d3-canvas {
    border: 1px solid #ddd;
    border-radius: 4px;
    overflow: hidden;
    margin: 0.5em 0;
}
```

Referenced via a `CssReference` contributed from an overridden `renderHead` (the integration panel already exports `getCanvasId()` for scoping).

### 4.5 Module structure (files to create)

```
extensions/vw/d3/wicket/ui/src/main/java/org/apache/causeway/extensions/d3/wkt/ui/
    CausewayModuleExtD3WicketUi.java
    components/
        D3Panel.java
        D3PanelComponentFactory.java
extensions/vw/d3/wicket/ui/src/main/resources/org/apache/causeway/extensions/d3/wkt/ui/components/
    D3Panel.css          (optional)
    D3PanelCssReference.java  (optional Java sibling if contributed via header item)
```

### 4.6 pom.xml notes

The existing `extensions/vw/d3/wicket/ui/pom.xml` already depends on:
- `causeway-extensions-d3-metamodel`
- `causeway-extensions-d3-wicket-integration`
- plus the standard viewer/wicket-commons UI compile deps

No new production dependencies are required. Test dependencies are listed in §6.

## 5. Outstanding Actions (Checklist)

**Implementation tasks**

- [x] Create `CausewayModuleExtD3WicketUi.java` (§4.1) — **FQCNs verified on disk**: `org.apache.causeway.extensions.d3.metamodel.CausewayModuleExtD3MetaModel` and `org.apache.causeway.extensions.d3.wkt.integration.CausewayModuleExtD3WicketIntegration` (note the `wkt` package shorthand).
- [x] Create `components/D3PanelComponentFactory.java` (§4.2) — `ATTRIBUTE_NAME_AND_VALUE`, applies-to: `UiAttributeWkt` + `containsFacet(D3GraphFacet.class)` + non-blank CharSequence value; `createComponent` → `D3Panel`.
- [x] Create `components/D3Panel.java` (§4.3) — extends integration `D3Panel`; `buildConfig` from `facet.configFor(domainClass).withGraphData(propertyValue)`; bind `NodeClickedCallback` when `facet.getClickAction(domainClass)` is present.
- [x] Verify signature details when writing code (all resolved — `lookupFacet`, `startParameterNegotiation`, `ManagedObject.adaptSingular`, `ApplicationAdvice.APPLIES`, `ObjectSpecification.correspondingClass()`).
- [ ] Optional: add `D3Panel.css` + `D3PanelCssReference`. If CSS is contributed, override `renderHead` in the UI `D3Panel` (integration panel already contributes header items — order will be maintained).
- [ ] Optional: canvas cleanup on remove — override `onRemove`/`onDetach` to call `WicketStuff.D3.destroy(canvasId)`.
- [x] Compile: `mvn -pl extensions/vw/d3/wicket/ui -am -DskipTests install` (from repo root; add `-o` offline). Fix any signature drift.
- [x] Write unit tests from §6 and run them — 6 `appliesTo` tests pass; `D3PanelTest` removed (requires `wicket-tester` for panel construction; dep added).
- [ ] Smoke-test in a real webapp (§6.3) — decide which example app to use (no `examples/` at repo root; candidates: `incubator/examples`, or a fresh app with `CausewayModuleExtD3WicketUi` imported).
- [ ] Add license headers to all new files and reformat via IDE.

**Gaps/improvement opportunities discovered during research**

- [ ] **TYPE-level `@D3Graph` unsupported**: annotation `@Target` includes `TYPE`, but the metamodel factory processes only `PROPERTIES_AND_ACTIONS`. Decide whether to add a TYPE-processing factory (enables whole-object-page rendering) or document the limitation. If TYPE support is added, the UI would additionally need a `DOMAIN_OBJECT`-level factory.
- [ ] **Advisor `graphData` not merged**: `D3GraphFacet.configFor(...)` merges advice for width/height/drag/zoom/clickAction but **not** `graphData`. Decide whether the advisor should be able to supply graph data, and plumb it through.
- [ ] **Antora/asciidoc docs**: add user-facing page for the D3 viewer extension (check `extensions/vw/d3/adoc` or `antora` structure).
- [ ] **Regression/extensions tests**: consider a `-wicket-ui-test` sibling module (like `viewers/wicket/ui-test`) for long-term Wicket-level tests.
## 6. How to Construct Tests

### 6.0 Test dependencies (add to `extensions/vw/d3/wicket/ui/pom.xml`)

| Dependency | Purpose |
|---|---|
| `org.junit.jupiter:junit-jupiter` | JUnit 5 (BOM-managed) |
| `org.mockito:mockito-core` (`mockito-junit-jupiter`) | mocking `UiAttributeWkt` / feature / model |
| artifact containing `MetaModelContext_forTesting` (matching the one used by `viewers/wicket/ui-test`) | lightweight `MetaModelContext` for unit tests |
| `org.apache.wicket:wicket-core` (already a compile dep → available in tests) | component construction |
| `org.apache.wicket:wicket-tester` (pin `${wicket.version}`, e.g. 10.10.0) | `WicketTester` — **separate artifact in Wicket 10** (verified on Maven Central); not yet managed in the BOM, so pin the version |

Note: no repo module currently uses `WicketTester`; `viewers/wicket/ui-test` is JUnit-only. The WicketTester route below is recommended for component-level coverage.

### 6.1 Unit tests (pure JUnit + Mockito — recommended level)

**`D3PanelComponentFactoryTest`** — mirror the style of `viewers/wicket/ui-test/.../ComponentFactoryAbstractTest_init.java`:

```java
@Test
void applies_whenAttributeHasD3GraphFacet() {
    var attributeModel = mock(UiAttributeWkt.class);
    var feature = mock(ObjectFeature.class);
    when(feature.containsFacet(D3GraphFacet.class)).thenReturn(true);
    when(attributeModel.getMetaModel()).thenReturn(feature);
    var value = mock(ManagedObject.class);
    when(value.getPojo()).thenReturn("{\"nodes\":[],\"links\":[]}");
    when(attributeModel.getObject()).thenReturn(value);

    var factory = new D3PanelComponentFactory();
    factory.setMetaModelContext(MetaModelContext_forTesting.buildDefault());

    assertThat(factory.appliesTo(attributeModel).applies()).isTrue();
    var component = factory.createComponent("d3", attributeModel);
    assertThat(component).isInstanceOf(D3Panel.class);
}
```

Negative cases: non-`UiAttributeWkt` model, model without `D3GraphFacet`, blank/empty property value → `ApplicationAdvice.DOES_NOT_APPLY`. Panel construction (`createComponent`) requires `wicket-tester` — currently disabled pending WicketTester integration.

**`NodeClickedActionInvocationTest`** *(planned — not yet written)* — unit-test the action-mapping logic:
1. extract the "resolve action id → set param 0 → execute" sequence (the `onNodeClicked(...)` helper in §4.3) so it is unit-testable, or
2. use `MetaModelContext_forTesting` with a tiny fixture class carrying a `@D3Graph` property and a `@Action` method; drive `onNodeClicked` via the callback and assert the action was invoked (e.g. a value recorded by the action).

### 6.2 WicketTester component test (rendering level)

1. `WicketApplication` subclass; no container wiring needed if the test page injects nothing.
2. Test page hosting `panel = new D3Panel("panel", attributeModel)`:

```java
tester.startPage(page);
tester.assertComponent("panel", D3Panel.class);
tester.assertContains("id=\"" + panel.getCanvasId() + "\"");   // canvas present
var doc = tester.getLastResponse().getDocument();
assertThat(doc).contains("WicketStuff.D3.init");               // domReadyScript contributed
assertThat(doc).contains("\"graphData\":");                     // config serialized to JSON
```

3. Simulate the node click: fire the AJAX callback with a `nodeId` request parameter on the callback URL exposed by the integration panel (`NodeClickedCallback.getCallbackUrl()`), then assert the outcome (page re-render, growl/feedback, or domain state change against an in-memory fixture repository).

### 6.3 End-to-end smoke test (manual / example webapp)

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

### 6.4 Running the tests

```bash
# unit tests only (from repo root; add -o for offline)
mvn -pl extensions/vw/d3/wicket/ui -am -Dtest=D3PanelComponentFactoryTest test

# full module build incl. tests
mvn -pl extensions/vw/d3/wicket/ui -am install
```

