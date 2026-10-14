# D3 Wicket Viewer (UI Module) — Implementation Overview

> **Date:** 14 October 2026  
> **Module:** `extensions/vw/d3/wicket/ui` — artifact `causeway-extensions-d3-wicket-ui`  
> **Target package:** `org.apache.causeway.extensions.d3.wkt.ui`

---

## 1. Problem Statement

A domain entity may expose a property annotated with `@D3Graph` whose value is a
JSON string describing a force-layout graph (`{ nodes: [...], links: [...] }`).
The Wicket UI module must:

1. **Render** that property as an interactive D3.js force-layout graph on the
   entity's object page.
2. **Handle node clicks** by invoking a configurable domain action on the owning
   entity, passing the clicked node's id as the action's first `String` parameter.

The solution reuses the existing `extensions/vw/d3/wicket/integration` module
that provides the D3.js canvas, the `d3-integration.mjs` client-side script,
and the AJAX callback infrastructure.

---

## 2. Module Landscape

```mermaid
graph TB
    subgraph "Apache Causeway Extensions — D3"
        APPLIB["applib<br/>(causeway-extensions-d3-applib)"]
        METAMODEL["metamodel<br/>(causeway-extensions-d3-metamodel)"]
        INTEGRATION["wicket/integration<br/>(causeway-extensions-d3-wicket-integration)"]
        UI["wicket/ui ★ NEW<br/>(causeway-extensions-d3-wicket-ui)"]
    end

    subgraph "Viewer Framework"
        WICKET_MODEL["viewer/wicket/model"]
        WICKET_UI["viewer/wicket/ui"]
    end

    APPLIB --> METAMODEL
    METAMODEL --> INTEGRATION
    METAMODEL --> UI
    INTEGRATION --> UI
    WICKET_MODEL --> UI
    WICKET_UI --> UI
```

### Module responsibilities

| Module | Key exports | Role |
|---|---|---|
| **applib** | `@D3Graph`, `D3Config`, `D3GraphAdvisor` | Domain annotation, config DTO, SPI for dynamic config |
| **metamodel** | `D3GraphFacet`, `D3GraphFacetFromAnnotationFactory` | Scans `@D3Graph`, attaches facet to properties/actions |
| **wicket/integration** | `D3Panel`, `NodeClickedCallback`, `D3AjaxCallback`, `d3-integration.mjs` | Generic D3.js canvas, AJAX callback URL wiring, client-side script |
| **wicket/ui** (this module) | `CausewayModuleExtD3WicketUi`, `D3PanelComponentFactory`, UI `D3Panel` | Glue: picks up `D3GraphFacet`, creates panel from property value, wires action invocation on node click |

---

## 3. New Classes — Architecture

### 3.1 Class diagram

```mermaid
classDiagram
    class CausewayModuleExtD3WicketUi {
        <<@Configuration>>
        +CausewayModuleExtD3WicketUi()
    }
    note for CausewayModuleExtD3WicketUi "@Import of metamodel,<br>integration, and factory"

    class ComponentFactoryAbstract {
        <<abstract>>
        +appliesTo(IModel) ApplicationAdvice
        +createComponent(String, IModel) Component
    }

    class D3PanelComponentFactory {
        <<@Component>>
        +appliesTo(IModel) ApplicationAdvice
        +createComponent(String, IModel) Component
    }

    class D3Panel_integration {
        <<wicket/integration>>
        -config: D3Config
        -clickCallback: NodeClickedCallback
        +withClickCallback(NodeClickedCallback) D3Panel
        +renderHead(IHeaderResponse)
        +getCanvasId() CharSequence
    }
    note for D3Panel_integration "Panel + WebComponent(canvas)<br>+ JS/CSS header contributions"

    class D3Panel_ui {
        <<wicket/ui>>
        -attributeModel: UiAttributeWkt
        +D3Panel(String, UiAttributeWkt)
        -buildConfig(UiAttributeWkt) D3Config
        -onNodeClicked(NodeClickedEvent, AjaxRequestTarget, String)
    }
    note for D3Panel_ui "Subclasses integration D3Panel.<br>No own .html needed."

    class NodeClickedCallback {
        <<abstract>>
        #onNodeClicked(NodeClickedEvent, AjaxRequestTarget)
    }

    class FormExecutorDefault {
        +forAction(ActionModel) FormExecutorDefault
        +executeAndProcessResults(target, form, context)
    }

    CausewayModuleExtD3WicketUi --> D3PanelComponentFactory : imports
    D3PanelComponentFactory --|> ComponentFactoryAbstract : extends
    D3PanelComponentFactory --> D3Panel_ui : creates
    D3Panel_ui --|> D3Panel_integration : extends
    D3Panel_ui --> NodeClickedCallback : instantiates
    D3Panel_ui --> FormExecutorDefault : executes action
```

### 3.2 File layout

```
extensions/vw/d3/wicket/ui/src/main/java/org/apache/causeway/extensions/d3/wkt/ui/
    CausewayModuleExtD3WicketUi.java          -- Spring @Configuration entry point
    components/
        D3Panel.java                          -- UI panel (extends integration D3Panel)
        D3PanelComponentFactory.java           -- Wicket ComponentFactory
```

No additional `.html` markup is needed — the UI `D3Panel` inherits the integration
module's `D3Panel.html`.

---

## 4. End-to-End Scenarios

### 4.1 Page Load — Render the graph

```mermaid
sequenceDiagram
    actor Browser
    participant WicketPage as Wicket Object Page
    participant Factory as D3PanelComponentFactory
    participant Facet as D3GraphFacet
    participant Panel_ui as D3Panel (ui)
    participant Panel_int as D3Panel (integration)
    participant JS as d3-integration.mjs

    Browser->>WicketPage: GET /entity/123

    WicketPage->>WicketPage: iterate attributes
    WicketPage->>Factory: appliesTo(attributeModel)
    Factory->>Factory: instanceof UiAttributeWkt?
    Factory->>Factory: containsFacet(D3GraphFacet.class)?
    Factory->>Factory: value instanceof CharSequence && non-blank?
    Factory-->>WicketPage: ApplicationAdvice.APPLIES

    WicketPage->>Factory: createComponent("d3", attributeModel)
    Factory->>Panel_ui: new D3Panel("d3", attributeModel)

    Panel_ui->>Panel_ui: buildConfig(attributeModel)
    Panel_ui->>Facet: lookupFacet(D3GraphFacet.class)
    Panel_ui->>Facet: configFor(domainClass)
    Panel_ui->>Panel_ui: config.withGraphData(propertyValue)

    Panel_ui->>Facet: getClickAction(domainClass)
    alt clickAction is present
        Panel_ui->>Panel_ui: withClickCallback(callback)
        Panel_ui->>Panel_int: withClickCallback(callback)
    else no clickAction
        Panel_ui->>Panel_int: (no callback attached)
    end

    Note over Panel_int: renderHead() is called
    Panel_int->>Panel_int: config.withClickCallbackUrl(callbackUrl)
    Panel_int->>JS: D3Reference + D3IntegrationReference + domReadyScript(config)

    WicketPage-->>Browser: HTML page

    Browser->>Browser: execute domReadyScript
    Browser->>JS: WicketStuff.D3.init(config)
    JS->>JS: parse config.graphData
    JS->>JS: build D3 force layout
    JS->>Browser: render SVG canvas
```

### 4.2 Node Click — Invoke domain action

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant D3Ajax as D3AjaxCallback (integration)
    participant Callback as NodeClickedCallback (ui)
    participant Panel_ui as D3Panel (ui)
    participant ActionModel as ActionModel
    participant FormExec as FormExecutorDefault
    participant DomainObj as Domain Entity

    User->>Browser: clicks graph node "A"
    Browser->>Browser: d3 event handler fires
    Browser->>D3Ajax: Wicket.Ajax.get(u + "&nodeId=A")

    D3Ajax->>D3Ajax: read "nodeId" param -> "A"
    D3Ajax->>Callback: onNodeClicked(event("A"), target)
    Callback->>Panel_ui: onNodeClicked(event, target, "selectNode")

    Panel_ui->>Panel_ui: objectSpec.getAction("selectNode")
    Panel_ui-->>Panel_ui: ObjectAction

    Panel_ui->>ActionModel: ActionModel.forEntity(action, parentUiModel)
    Panel_ui->>ActionModel: startParameterNegotiation()
    Panel_ui->>ActionModel: updateParamValuePojo(0, __ -> "A")

    Panel_ui->>FormExec: FormExecutorDefault.forAction(actionModel)
    Panel_ui->>FormExec: executeAndProcessResults(target, null, actionModel)

    FormExec->>DomainObj: selectNode("A")
    DomainObj-->>FormExec: return value (e.g. this)

    FormExec->>Browser: Ajax response (partial page update)
    Browser->>Browser: update UI
```

### 4.3 Decision tree — `appliesTo`

```mermaid
flowchart TD
    Start["factory.appliesTo(model)"] --> CheckType{"model instanceof<br/>UiAttributeWkt?"}
    CheckType -- No --> NoApply["ApplicationAdvice.DOES_NOT_APPLY"]
    CheckType -- Yes --> CheckFacet{"containsFacet<br/>(D3GraphFacet.class)?"}
    CheckFacet -- No --> NoApply
    CheckFacet -- Yes --> CheckNull{"value != null?"}
    CheckNull -- No --> NoApply
    CheckNull -- Yes --> CheckType2{"value.getPojo() instanceof<br/>CharSequence?"}
    CheckType2 -- No --> NoApply
    CheckType2 -- Yes --> CheckBlank{"!cs.toString().isBlank()?"}
    CheckBlank -- No --> NoApply
    CheckBlank -- Yes --> Apply["ApplicationAdvice.APPLIES"]
```

### 4.4 Callback wiring detail

```mermaid
flowchart LR
    subgraph "Construction (D3Panel ctor)"
        A["facet.getClickAction(domainClass)"] --> HasAction{"Optional present?"}
        HasAction -- Yes --> B["withClickCallback(new NodeClickedCallback() {<br/>  onNodeClicked(event, target) {<br/>    D3Panel.this.onNodeClicked(event, target, actionId)<br/>  }<br/>})"]
        HasAction -- No --> C["(no callback attached)"]
    end

    subgraph "Rendering (integration D3Panel.renderHead)"
        B --> D["config.withClickCallbackUrl(callback.getCallbackUrl())"]
        D --> E["domReadyScript(configWithCallback)"]
        E --> F["Browser receives config<br/>with clickCallbackUrl field"]
    end

    subgraph "Node click (browser)"
        F --> G["Wicket.Ajax.get(callbackUrl + '&nodeId=' + id)"]
        G --> H["D3AjaxCallback.respond(target)"]
        H --> I["NodeClickedCallback.onNodeClicked(...)"]
    end
```

---

## 5. Test Strategy

```mermaid
flowchart TD
    subgraph "Unit Tests (JUnit + Mockito)"
        U1["D3PanelComponentFactoryTest<br/>6 tests ✓ ALL PASS"]:::pass
        U2["appliesTo: correct type + facet + non-blank → APPLIES"]:::pass
        U3["appliesTo: wrong type → DOES_NOT_APPLY"]:::pass
        U4["appliesTo: missing facet → DOES_NOT_APPLY"]:::pass
        U5["appliesTo: blank value → DOES_NOT_APPLY"]:::pass
        U6["appliesTo: null value → DOES_NOT_APPLY"]:::pass
        U7["appliesTo: non-CharSequence value → DOES_NOT_APPLY"]:::pass
    end

    subgraph "Component Tests (WicketTester)"
        C1["D3PanelTest (planned)"]:::blocked
        C2["startPage(D3Panel)"]:::blocked
        C3["assertContains canvas id"]:::blocked
        C4["assertContains domReadyScript"]:::blocked
        C5["assertContains graphData JSON"]:::blocked
    end

    subgraph "E2E Smoke Test (manual / example webapp)"
        E1["GraphOwner entity with<br/>@D3Graph + @Action"]:::todo
        E2["Import CausewayModuleExtD3WicketUi"]:::todo
        E3["Open object page → see graph"]:::todo
        E4["Click node → action invoked"]:::todo
    end

    U1 --> U2 & U3 & U4 & U5 & U6 & U7
    C1 --> C2 & C3 & C4 & C5
    E1 --> E2 --> E3 --> E4

    classDef pass fill:#9f9,stroke:#090
    classDef blocked fill:#fcc,stroke:#c00
    classDef todo fill:#ffc,stroke:#cc0
```

### Test dependency

| Dependency | Version | Used for |
|---|---|---|
| `junit-jupiter` | (BOM-managed) | Test runner |
| `mockito-core` / `mockito-junit-jupiter` | (BOM-managed) | Mocking `UiAttributeWkt`, `ObjectFeature`, `ManagedObject` |
| `MetaModelContext_forTesting` | (project test-jar from `viewers/commons/testsupport`) | Lightweight `MetaModelContext` without full container |
| `wicket-tester` | `${wicket.version}` (10.10.0) | `WicketTester` for component rendering tests |

> **Note:** `wicket-tester` is declared in `pom.xml` but panel-level tests (`D3PanelTest`)
> were removed pending a rewrite. The `createComponent` test in `D3PanelComponentFactoryTest`
> is disabled with `//TODO` because it requires a full Wicket request cycle.

---

## 6. Key Design Decisions

1. **Attribute-level factory, not object-level.**  
   The `D3GraphFacet` is attached to *properties/actions only* (the metamodel factory
   processes `PROPERTIES_AND_ACTIONS`). The UI factory registers for
   `UiComponentType.ATTRIBUTE_NAME_AND_VALUE`, matching the PDF.js extension pattern.

2. **Extends integration `D3Panel`, no new `.html`.**  
   The UI `D3Panel` subclasses the integration module's panel, inheriting its
   `D3Panel.html` markup, D3.js library references, and callback-URL wiring.
   Only the *data* (graph JSON) and *behavior* (action invocation) are added.

3. **Graph data from the property value.**  
   The annotated property's value (a JSON `String`) is pushed into `D3Config`
   via `withGraphData(...)`. This is the primary source; advisor-provided
   `graphData` is not merged by the facet today (documented gap).

4. **Action invocation via `FormExecutorDefault`.**  
   On node click, the panel resolves the domain action by member id, builds an
   `ActionModel`, sets the first parameter to the clicked node id via
   `updateParamValuePojo(0, __ -> event.nodeId())`, and delegates to
   `FormExecutorDefault.forAction(actionModel).executeAndProcessResults(...)`.
   This reuses the standard Wicket viewer's action-execution pipeline (parameter
   validation, result handling, page update).

5. **No separate CSS file (optional).**  
   A small `D3Panel.css` was considered but not implemented. The integration
   panel already contributes D3.js CSS via `D3Reference`.

---

## 7. Gaps & Future Work

| Area | Status | Notes |
|---|---|---|
| **TYPE-level `@D3Graph`** | Unsupported | `@Target` includes `TYPE`, but metamodel factory only processes `PROPERTIES_AND_ACTIONS` |
| **Advisor `graphData` merge** | Not merged | `configFor(...)` merges advisor width/height/drag/zoom/clickAction but not graph data |
| **`D3Panel.css`** | Optional | Not yet created; would require a `CssReference` in `renderHead` |
| **Canvas cleanup** | Optional | Override `onRemove`/`onDetach` to call `WicketStuff.D3.destroy(canvasId)` |
| **Antora docs** | Not started | User-facing page for the D3 viewer extension |
| **`ui-test` module** | Not started | Long-term Wicket-level regression tests (like `viewers/wicket/ui-test`) |

---

## 8. Quick Reference — Key API Signatures

### Source of truth (verified on disk)

| API | File |
|---|---|
| `UiAttributeWkt.getMetaModel()` → `ObjectFeature` (FacetHolder) | `viewer/wicket/model/.../UiAttributeWkt.java` |
| `UiAttributeWkt.getObject()` → `ManagedObject` | same |
| `UiAttributeWkt.getParentUiModel()` → `UiObjectWkt` | same |
| `ManagedObject.getPojo()` → `Object` | `core/metamodel/.../ManagedObject.java` |
| `ManagedObject.adaptSingular(ObjectSpec, Object)` | same |
| `ObjectFeature.containsFacet(Class)` → `boolean` | `core/metamodel/.../ObjectFeature.java` |
| `ObjectFeature.lookupFacet(Class)` → `Optional<? extends Facet>` | same |
| `ObjectSpecification.getAction(String)` → `Optional<ObjectAction>` | `core/metamodel/.../ObjectSpecification.java` |
| `ObjectSpecification.getCorrespondingClass()` → `Class<?>` | same |
| `D3GraphFacet.configFor(Class)` → `D3Config` | `extensions/vw/d3/metamodel/.../D3GraphFacet.java` |
| `D3GraphFacet.getClickAction(Class)` → `Optional<String>` | same |
| `ActionModel.forEntity(ObjectAction, UiObjectWkt)` | `viewer/wicket/model/.../ActionModel.java` |
| `ActionModel.getActionInteraction()` → `ActionInteractionWkt` | same |
| `ActionInteractionWkt.startParameterNegotiation()` → `Optional<ParameterNegotiationModel>` | same |
| `ParameterNegotiationModel.updateParamValuePojo(int, Function)` | `viewer/commons/model/.../ParameterNegotiationModel.java` |
| `FormExecutorDefault.forAction(ActionModel)` | `viewer/wicket/ui/.../FormExecutorDefault.java` |
| `FormExecutorDefault.executeAndProcessResults(AjaxRequestTarget, Form, FormExecutorContext)` | same |

### Build / run commands

```bash
# unit tests only
mvn -pl extensions/vw/d3/wicket/ui -am -Dtest=D3PanelComponentFactoryTest test

# full module build
mvn -pl extensions/vw/d3/wicket/ui -am install
```
