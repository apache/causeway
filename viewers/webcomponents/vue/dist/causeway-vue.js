import { inject as re, onMounted as V, onBeforeUnmount as X, defineComponent as j, ref as A, openBlock as O, createElementBlock as q, createElementVNode as T, toDisplayString as ie, computed as R, h as b, defineAsyncComponent as ce, watch as se, createBlock as ue, resolveDynamicComponent as le, nextTick as de } from "vue";
import { useRoute as pe } from "vue-router";
const G = "cw-object-context[data-causeway-route-context]", fe = "cw-interaction-controller[data-causeway-route-interactions]";
function ge(e, t) {
  const a = [...e.querySelectorAll(G)].filter((r) => !r.parentElement?.closest(G));
  if (a.length === 0) return Object.freeze({ valid: !1, classification: "missing-context" });
  if (a.length !== 1) return Object.freeze({ valid: !1, classification: "duplicate-context" });
  const n = a[0];
  return n.getAttribute("logical-type") !== t.logicalTypeName || n.getAttribute("object-id") !== t.objectId ? Object.freeze({ valid: !1, classification: "identity" }) : [...n.querySelectorAll(fe)].filter((r) => r.closest(G) === n).length !== 1 ? Object.freeze({ valid: !1, classification: "interactions" }) : Object.freeze({ valid: !0, context: n });
}
function C(e, t) {
  const a = e.querySelectorAll(t);
  return a.length === 1 ? a[0] : null;
}
function Z(e) {
  const t = C(e, "cw-graphql-client[data-causeway-shell-client]"), a = C(e, "[data-causeway-router-view]"), n = C(e, "[data-causeway-route-loading]"), o = C(e, "[data-causeway-route-announcement]"), r = C(e, "cw-action-results[data-causeway-shell-result]");
  if (!t || !a || !n || !o || !r || !t.contains(a) || !t.contains(r))
    throw new Error("The authored Vue application shell is invalid.");
  return Object.freeze({ shell: e, client: t, route: a, loading: n, announcement: o, result: r });
}
const H = /* @__PURE__ */ Symbol("causeway-vue-viewer"), U = 4096, Q = 4096, he = "The requested application route is invalid.";
function h() {
  return new Error(he);
}
function ye(e) {
  for (let t = 0; t < e.length; t += 1) {
    const a = e.charCodeAt(t);
    if (a >= 55296 && a <= 56319) {
      const n = e.charCodeAt(t + 1);
      if (!(n >= 56320 && n <= 57343)) return !0;
      t += 1;
    } else if (a >= 56320 && a <= 57343)
      return !0;
  }
  return !1;
}
function I(e) {
  const t = String(e ?? "");
  if (!t || t.length > U || t === "." || t === ".." || /[\\/\u0000-\u001f\u007f-\u009f]/u.test(t) || ye(t) || new TextEncoder().encode(t).length > U) throw h();
  let a;
  try {
    a = encodeURIComponent(t).replace(/[!'()*]/g, (n) => `%${n.charCodeAt(0).toString(16).toUpperCase()}`);
  } catch {
    throw h();
  }
  if (a.length > Q) throw h();
  return a;
}
function B(e) {
  if (!e || e.length > Q || !/^(?:[^%]|%[0-9A-F]{2})+$/u.test(e))
    throw h();
  let t;
  try {
    t = decodeURIComponent(e);
  } catch {
    throw h();
  }
  if (I(t) !== e) throw h();
  return t;
}
function L(e) {
  let t = String(e ?? "").trim();
  if (!t.startsWith("/") || t.startsWith("//") || /[?#\\\u0000-\u001f]/u.test(t))
    throw new Error("The Vue viewer base path is invalid.");
  for (; t.length > 1 && t.endsWith("/"); ) t = t.slice(0, -1);
  return t;
}
function ve(e, t) {
  const a = L(e), n = `/object/${I(t?.logicalTypeName)}/${I(t?.id ?? t?.objectId)}`;
  return a === "/" ? n : `${a}${n}`;
}
function z(e) {
  return ve("/", e);
}
function J(e, t = "/") {
  const a = String(e ?? ""), n = L(t), o = n === "/" ? "/object/" : `${n}/object/`;
  if (!a.startsWith(o)) throw h();
  const u = a.slice(o.length).split("/");
  if (u.length !== 2) throw h();
  return Object.freeze({
    logicalTypeName: B(u[0]),
    objectId: B(u[1])
  });
}
function we(e) {
  return z(e);
}
const D = "causeway-navigation-request", M = "causeway-action-request", W = "causeway-action-result", F = "causeway-object-context-state-change";
function ee() {
  let e = !1;
  return {
    get claimed() {
      return e;
    },
    claim() {
      return e ? !1 : (e = !0, !0);
    }
  };
}
function S(e) {
  return Object.freeze({
    router: e.router,
    basePath: e.basePath,
    shell: e.state.shell,
    routeGeneration: e.state.routeGeneration
  });
}
function me(e) {
  if (e?.kind !== "object" || !e.value || typeof e.value != "object") return null;
  const t = e.value._meta, a = t?.logicalTypeName, n = t?.id;
  return typeof a != "string" || typeof n != "string" || !a || !n ? null : Object.freeze({ logicalTypeName: a, id: n, title: String(t?.title ?? n) });
}
function be(e) {
  return [...e.querySelector("[data-causeway-router-view]")?.querySelectorAll("cw-action-results[data-causeway-page-result]") ?? []].filter((a) => a.isConnected);
}
function Ee(e) {
  const t = e.querySelectorAll("cw-action-results[data-causeway-shell-result]");
  return t.length === 1 ? t[0] : null;
}
function k(e) {
  const t = be(e);
  if (t.length > 1) throw new Error("The active Vue route has duplicate action-result outlets.");
  if (t.length === 1) return t[0];
  const a = Ee(e);
  if (!a?.isConnected) throw new Error("The authored Vue shell result outlet is unavailable.");
  return a;
}
function K(e, ...t) {
  const a = e;
  a.replacePresentation ? a.replacePresentation(...t) : e.replaceChildren(...t), e.hidden = t.length === 0;
}
function te(e, t) {
  const a = t.result, n = document.createElement("h2");
  if (n.textContent = t.actionId ? `${t.actionId} result` : "Action result", a?.kind === "collection") {
    const r = document.createElement("cw-standalone-collection");
    r.setAttribute("data-testid", "causeway-standalone-action-result"), r.setAttribute("named", String(t.resultPresentation?.named ?? n.textContent)), r.result = a, K(e, r);
    return;
  }
  const o = document.createElement("output");
  o.textContent = a?.kind === "scalar" ? String(a.value ?? "") : a?.kind === "void" ? "Completed" : "This result cannot be presented.", K(e, n, o);
}
async function Te(e, t) {
  const a = e.state.shell, o = a?.querySelector("[data-causeway-router-view]")?.querySelector("cw-object-context[data-causeway-route-context]"), r = o?.context;
  if (!o || !r?.refresh) return;
  const u = e.state.routeGeneration, l = (i) => {
    if (u !== e.state.routeGeneration) return s();
    const d = i.detail?.state;
    if (d?.status !== "terminal-error") {
      (d?.status === "ready" || d?.status === "partial-error") && s();
      return;
    }
    s(), (d.errors?.[0]?.extensions?.classification ?? d.errors?.[0]?.extensions?.code ?? d.error?.code) === "NOT_FOUND" && e.router.replace("/");
  }, s = () => o.removeEventListener(F, l);
  o.addEventListener(F, l), r.refresh(), te(k(a), t);
}
async function Y(e, t) {
  const a = ee();
  await e.policies.navigate?.(t, a, S(e)) === !0 && a.claim(), a.claimed || await e.router.push(z(t));
}
function ae(e, t) {
  const a = /* @__PURE__ */ new WeakMap();
  let n = null;
  const o = (l) => {
    const s = l.detail;
    try {
      const i = k(t);
      s?.context && typeof s.context == "object" ? a.set(s.context, i) : n = i;
    } catch (i) {
      e.policies.error?.(i, S(e));
    }
  }, r = (l) => {
    const s = l, i = s.detail?.target;
    !i?.logicalTypeName || !(i.id ?? i.objectId) || (s.preventDefault(), Y(e, i).catch((d) => e.policies.error?.(d, S(e))));
  }, u = (l) => {
    const s = l, i = s.detail ?? {};
    (async () => {
      const d = ee();
      if (await e.policies.result?.(i, d, S(e)) === !0 && d.claim(), d.claimed) return;
      const f = me(i.result);
      if (f) return Y(e, f);
      const g = i.context && a.get(i.context) || n || k(t);
      i.result?.kind === "void" ? await Te(e, i) : te(g, i), s.target?.dismissResult?.();
    })().catch((d) => e.policies.error?.(d, S(e)));
  };
  return t.addEventListener(M, o, { capture: !0 }), t.addEventListener(D, r), t.addEventListener(W, u), () => {
    t.removeEventListener(M, o, { capture: !0 }), t.removeEventListener(D, r), t.removeEventListener(W, u);
  };
}
const je = /^[A-Za-z_][A-Za-z0-9_$-]*(?:\.[A-Za-z_][A-Za-z0-9_$-]*)*$/u;
function _e(e) {
  return e ? e instanceof Map ? [...e.entries()] : Array.isArray(e) ? e : Object.entries(e) : [];
}
function Ce(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [a, n] of _e(e)) {
    const o = String(a ?? "").trim();
    if (!je.test(o)) throw new Error("A Vue page registration has an invalid logical type.");
    if (!n || typeof n != "object" && typeof n != "function")
      throw new Error(`The Vue page registration for ${o} is unsupported.`);
    if (t.has(o)) throw new Error(`The Vue page registration for ${o} is duplicated.`);
    t.set(o, n);
  }
  return t;
}
function xe(e) {
  return typeof e == "function";
}
function ze(e) {
  if (!e?.router) throw new Error("The Vue viewer requires the application router.");
  const t = String(e.endpoint ?? "").trim();
  if (!t) throw new Error("The Vue viewer GraphQL endpoint is required.");
  const a = { shell: null, routeGeneration: 0 };
  let n;
  return n = Object.freeze({
    plugin: {
      install(r) {
        r.provide(H, n);
      }
    },
    router: e.router,
    endpoint: t,
    basePath: L(e.basePath ?? (e.router.options.history.base || "/")),
    pages: Ce(e.pages),
    policies: Object.freeze({ ...e.policies }),
    developmentDiagnostics: e.developmentDiagnostics ?? !0,
    state: a
  }), n;
}
function $() {
  const e = re(H);
  if (!e) throw new Error("The Causeway Vue viewer plugin is not installed.");
  return e;
}
function $e(e) {
  const t = $();
  let a = null;
  return V(() => {
    const n = e.value;
    if (!n) throw new Error("The authored Vue application shell is missing.");
    Z(n), t.state.shell = n, a = ae(t, n);
  }), X(() => {
    a?.(), a = null, t.state.shell === e.value && (t.state.shell = null);
  }), e;
}
function Ue(e, t) {
  const a = Z(t);
  e.state.shell = t;
  const n = ae(e, t);
  return {
    landmarks: a,
    dispose() {
      n(), e.state.shell === t && (e.state.shell = null);
    }
  };
}
const Se = ["data-route-state"], Ae = /* @__PURE__ */ j({
  __name: "HomePage",
  setup(e) {
    const t = $(), a = A(null), n = A("loading"), o = A("Loading the application home page…");
    function r(u) {
      let l = null;
      return u.dispatchEvent(new CustomEvent("causeway-graphql-client-request", {
        bubbles: !0,
        composed: !0,
        detail: { provide(s) {
          l ??= s;
        } }
      })), l;
    }
    return V(async () => {
      const u = ++t.state.routeGeneration, l = a.value ? r(a.value) : null;
      if (!l) {
        n.value = "unsupported", o.value = "Choose an application action to begin.";
        return;
      }
      try {
        const s = await l.describeApplicationEntry();
        if (u !== t.state.routeGeneration) return;
        if (!s?.supported) {
          n.value = "unsupported", o.value = "Choose an application action to begin.";
          return;
        }
        const i = await l.readApplicationEntry({ description: s });
        if (u !== t.state.routeGeneration) return;
        const d = i?.data?.home, E = d?.object?._meta, f = E?.logicalTypeName ?? d?.logicalTypeName, g = E?.id, y = { claimed: !1, claim() {
          return this.claimed ? !1 : (this.claimed = !0, !0);
        } }, P = { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: u };
        if (await t.policies.home?.(i?.data, y, P) === !0 && y.claim(), u !== t.state.routeGeneration || y.claimed) return;
        if (d?.kind === "OBJECT" && f && g) {
          await t.router.replace(z({ logicalTypeName: f, id: g }));
          return;
        }
        n.value = i?.errors?.length ? "partial-error" : "ready", o.value = "Choose an application action to begin.";
      } catch (s) {
        if (u !== t.state.routeGeneration) return;
        t.policies.error?.(s, { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: u }), n.value = "partial-error", o.value = "The home page is unavailable; application menus remain available.";
      }
    }), (u, l) => (O(), q("section", {
      ref_key: "page",
      ref: a,
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": n.value,
      tabindex: "-1"
    }, [
      l[0] || (l[0] = T("h1", null, "Welcome", -1)),
      T("p", null, ie(o.value), 1)
    ], 8, Se));
  }
}), Oe = {
  class: "causeway-vue-route-page causeway-vue-route-object",
  "data-causeway-route-page": "",
  "data-page-kind": "generic",
  "data-route-state": "loading",
  tabindex: "-1",
  "aria-label": "Object page"
}, Pe = ["logical-type", "object-id"], Ne = /* @__PURE__ */ j({
  __name: "GenericObjectPage",
  props: {
    logicalTypeName: {},
    objectId: {},
    routeKey: {}
  },
  setup(e) {
    return (t, a) => (O(), q("section", Oe, [
      T("cw-object-context", {
        "data-causeway-route-context": "",
        "logical-type": e.logicalTypeName,
        "object-id": e.objectId
      }, [...a[0] || (a[0] = [
        T("cw-object", { editable: "" }, null, -1),
        T("cw-interaction-controller", { "data-causeway-route-interactions": "" }, null, -1)
      ])], 8, Pe)
    ]));
  }
}), Re = ["data-route-key"], qe = {
  key: 0,
  class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
  "data-route-state": "terminal-error",
  tabindex: "-1",
  role: "alert"
}, Ge = /* @__PURE__ */ j({
  __name: "ObjectRoutePage",
  setup(e) {
    const t = pe(), a = $(), n = A(null), o = A(null), r = R(() => J(t.path, "/")), u = j(() => () => b("section", {
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": "loading",
      tabindex: -1,
      role: "status"
    }, [b("h1", "Loading page…")])), l = j(() => () => b("section", {
      class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
      "data-route-state": "terminal-error",
      tabindex: -1,
      role: "alert"
    }, [b("h1", "Page unavailable"), b("p", "The application page could not be loaded.")])), s = R(() => we(r.value)), i = R(() => a.pages.get(r.value.logicalTypeName)), d = R(() => {
      const c = i.value;
      return c ? xe(c) ? ce({
        loader: async () => {
          const p = await c();
          return "default" in p ? p.default : p;
        },
        delay: 0,
        timeout: 3e4,
        loadingComponent: u,
        errorComponent: l,
        onError(p, v, w) {
          a.policies.error?.(p, {
            router: a.router,
            basePath: a.basePath,
            shell: a.state.shell,
            routeGeneration: a.state.routeGeneration
          }), w();
        }
      }) : c : Ne;
    });
    function E() {
      const c = a.state.shell;
      return {
        route: c?.querySelector("[data-causeway-router-view]") ?? null,
        loading: c?.querySelector("[data-causeway-route-loading]") ?? null,
        announcement: c?.querySelector("[data-causeway-route-announcement]") ?? null
      };
    }
    function f(c, p) {
      const v = E().announcement;
      v && (v.textContent = "", requestAnimationFrame(() => {
        p === a.state.routeGeneration && v.isConnected && (v.textContent = c);
      }));
    }
    function g(c) {
      const p = E();
      p.route?.setAttribute("aria-busy", String(c)), p.loading && (p.loading.hidden = !c);
    }
    async function y() {
      const c = a.state.routeGeneration;
      if (o.value = null, await de(), c !== a.state.routeGeneration || !n.value) return;
      const p = ge(n.value, r.value);
      p.valid || (o.value = p.classification ?? "invalid", g(!1), f("Page unavailable", c));
    }
    function P(c) {
      const p = a.state.routeGeneration, v = c, w = v.detail?.state, N = v.target?.closest("[data-causeway-route-page]");
      if (!w || !N || !n.value?.contains(N)) return;
      let m = String(w.status ?? "terminal-error");
      if (m === "terminal-error") {
        const _ = w.errors?.[0]?.extensions?.classification ?? w.errors?.[0]?.extensions?.code ?? w.error?.code;
        (_ === "NOT_FOUND" || _ === "ACCESS_DENIED") && (m = "unavailable");
      }
      if (N.dataset.routeState = m, m === "ready" || m === "partial-error") {
        g(!1);
        const _ = w.snapshot?.data?._meta?.title;
        _ && (document.title = String(_));
        for (const oe of N.querySelectorAll("cw-collection:not([active])"))
          oe.activate?.();
        f(m === "ready" ? "Page ready" : "Page ready with partial information", p);
      } else (m === "terminal-error" || m === "unavailable") && (g(!1), f("Page unavailable", p));
    }
    return se(() => t.fullPath, () => {
      a.state.routeGeneration += 1, g(!0), f("Loading page", a.state.routeGeneration), y();
    }, { immediate: !0 }), V(async () => {
      n.value?.addEventListener("causeway-object-context-state-change", P), await y();
      const c = n.value?.querySelector(
        "cw-object-context[data-causeway-route-context]"
      );
      c?.context?.state && c.dispatchEvent(new CustomEvent("causeway-object-context-state-change", {
        bubbles: !0,
        composed: !0,
        detail: { state: c.context.state, context: c.context }
      })), n.value?.querySelector("[data-causeway-route-page]")?.focus({ preventScroll: !0 });
    }), X(() => n.value?.removeEventListener("causeway-object-context-state-change", P)), (c, p) => (O(), q("div", {
      ref_key: "host",
      ref: n,
      class: "causeway-vue-object-route",
      "data-route-key": s.value
    }, [
      o.value ? (O(), q("section", qe, [...p[0] || (p[0] = [
        T("h1", null, "Page unavailable", -1),
        T("p", null, "The application page has an invalid semantic context boundary.", -1)
      ])])) : (O(), ue(le(d.value), {
        key: s.value,
        "logical-type-name": r.value.logicalTypeName,
        "object-id": r.value.objectId,
        "route-key": s.value,
        onVnodeMounted: y,
        onVnodeUpdated: y
      }, null, 8, ["logical-type-name", "object-id", "route-key"]))
    ], 8, Re));
  }
}), x = Object.freeze({
  home: "causeway-home",
  object: "causeway-object",
  invalid: "causeway-invalid-route",
  notFound: "causeway-not-found"
});
function ne(e, t, a, n) {
  return j({
    name: `Causeway${e.replace(/\W/gu, "")}Page`,
    setup() {
      return () => b("section", {
        class: ["causeway-vue-route-page", "causeway-vue-status", a === "invalid-route" && "causeway-vue-status-danger"],
        "data-route-state": a,
        tabindex: -1,
        role: n
      }, [b("h1", e), b("p", t)]);
    }
  });
}
const Ie = ne(
  "Invalid route",
  "The requested application route is invalid.",
  "invalid-route",
  "alert"
), ke = ne(
  "Page unavailable",
  "The requested page is unavailable.",
  "not-found",
  "alert"
);
function Be(e = {}) {
  return [
    { path: "/", name: x.home, component: e.homeComponent ?? Ae },
    {
      path: "/object/:logicalTypeName/:objectId",
      name: x.object,
      component: Ge,
      beforeEnter(t) {
        try {
          return J(t.path, "/"), !0;
        } catch {
          return { name: x.invalid, replace: !0 };
        }
      }
    },
    {
      path: "/invalid-route",
      name: x.invalid,
      component: Ie
    },
    {
      path: "/:pathMatch(.*)*",
      name: x.notFound,
      component: e.notFoundComponent ?? ke
    }
  ];
}
export {
  M as ACTION_REQUEST_EVENT,
  W as ACTION_RESULT_EVENT,
  x as CAUSEWAY_ROUTE_NAMES,
  H as CAUSEWAY_VIEWER_KEY,
  Ne as CausewayGenericObjectPage,
  Ae as CausewayHomePage,
  Ie as CausewayInvalidRoutePage,
  ke as CausewayNotFoundPage,
  Ge as CausewayObjectRoutePage,
  he as INVALID_ROUTE_MESSAGE,
  D as NAVIGATION_REQUEST_EVENT,
  F as OBJECT_CONTEXT_STATE_EVENT,
  G as ROUTE_CONTEXT_SELECTOR,
  fe as ROUTE_INTERACTIONS_SELECTOR,
  Ue as bindCausewayShell,
  ve as canonicalObjectPath,
  we as canonicalRouteKey,
  z as canonicalRouterObjectPath,
  Be as createCausewayRouteRecords,
  ze as createCausewayVueViewer,
  B as decodeRouteSegment,
  I as encodeRouteSegment,
  ae as installSemanticBridge,
  xe as isPageLoader,
  L as normalizeBasePath,
  Ce as normalizePageRegistry,
  J as parseCanonicalObjectPath,
  te as presentSemanticResult,
  k as resolveResultOutlet,
  $e as useCausewayShell,
  $ as useCausewayViewer,
  ge as validateRouteBoundary,
  Z as validateShellBoundary
};
//# sourceMappingURL=causeway-vue.js.map
