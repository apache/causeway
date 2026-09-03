import { inject as de, onMounted as D, onBeforeUnmount as ne, defineComponent as O, ref as N, openBlock as R, createElementBlock as U, createElementVNode as L, toDisplayString as pe, computed as I, h as S, defineAsyncComponent as fe, watch as ge, createBlock as he, resolveDynamicComponent as ye, nextTick as ve } from "vue";
import { useRoute as we } from "vue-router";
const G = "cw-object-context[data-causeway-route-context]", me = "cw-interaction-controller[data-causeway-route-interactions]";
function be(e, t) {
  const a = [...e.querySelectorAll(G)].filter((c) => !c.parentElement?.closest(G));
  if (a.length === 0) return Object.freeze({ valid: !1, classification: "missing-context" });
  if (a.length !== 1) return Object.freeze({ valid: !1, classification: "duplicate-context" });
  const n = a[0];
  return n.getAttribute("logical-type") !== t.logicalTypeName || n.getAttribute("object-id") !== t.objectId ? Object.freeze({ valid: !1, classification: "identity" }) : [...n.querySelectorAll(me)].filter((c) => c.closest(G) === n).length !== 1 ? Object.freeze({ valid: !1, classification: "interactions" }) : Object.freeze({ valid: !0, context: n });
}
function x(e, t) {
  const a = e.querySelectorAll(t);
  return a.length === 1 ? a[0] : null;
}
function oe(e) {
  const t = x(e, "cw-graphql-client[data-causeway-shell-client]"), a = x(e, "[data-causeway-router-view]"), n = x(e, "[data-causeway-route-loading]"), o = x(e, "[data-causeway-route-announcement]"), c = x(e, "cw-action-results[data-causeway-shell-result]");
  if (!t || !a || !n || !o || !c || !t.contains(a) || !t.contains(c))
    throw new Error("The authored Vue application shell is invalid.");
  return Object.freeze({ shell: e, client: t, route: a, loading: n, announcement: o, result: c });
}
const re = /* @__PURE__ */ Symbol("causeway-vue-viewer"), K = Object.freeze({
  serviceLogicalTypeName: "causeway.security.LogoutMenu",
  actionId: "logout"
});
function V(e) {
  return e?.serviceLogicalTypeName === K.serviceLogicalTypeName && e?.actionId === K.actionId;
}
function Y(e) {
  let t = 0;
  for (const a of e.querySelectorAll("[data-causeway-service-action]"))
    V({
      serviceLogicalTypeName: a.getAttribute("data-service-logical-type") ?? void 0,
      actionId: a.getAttribute("data-action-id") ?? ""
    }) && ((a.closest("[data-causeway-service-action-region]") ?? a).remove(), t += 1);
  return t;
}
class Ee extends Error {
  code;
  constructor(t, a) {
    super(a), this.name = "LocalResourceNavigationError", this.code = t;
  }
}
function Te(e, t = {}) {
  const a = t.location ?? window.location, n = typeof e?.path == "string" ? e.path.trim() : "", o = e?.openUrlStrategy;
  if (!n || !["SAME_WINDOW", "NEW_WINDOW"].includes(o ?? ""))
    throw P("LOCAL_RESOURCE_INVALID", "The local-resource result is incomplete.");
  if (/^[a-z][a-z\d+.-]*:/i.test(n) || n.startsWith("//") || n.includes("\\") || /[\u0000-\u001f\u007f]/.test(n))
    throw P("LOCAL_RESOURCE_TARGET_UNSAFE", "The local-resource target is not an application-local path.");
  const c = new URL(a.href), u = Ae(t.applicationBase ?? "/", c).pathname.replace(/\/+$/, "") || "/", l = n.startsWith("/") ? n : `/${n}`, g = u !== "/" && (l === u || l.startsWith(`${u}/`) || l.startsWith(`${u}?`) || l.startsWith(`${u}#`)) ? l : `${u === "/" ? "" : u}/${l.replace(/^\/+/, "")}`, h = new URL(g, c.origin);
  if (h.origin !== c.origin || h.username || h.password || u !== "/" && h.pathname !== u && !h.pathname.startsWith(`${u}/`))
    throw P("LOCAL_RESOURCE_TARGET_UNSAFE", "The local-resource target escapes the configured application boundary.");
  return Object.freeze({ url: h, openUrlStrategy: o });
}
function _e(e, t = {}) {
  const a = t.location ?? window.location, n = Te(e, { location: a, applicationBase: t.applicationBase });
  if (n.openUrlStrategy === "SAME_WINDOW") {
    a.assign(n.url.href);
    return;
  }
  const c = (t.open ?? window.open.bind(window))(n.url.href, "_blank", "noopener,noreferrer");
  c && (c.opener = null);
}
function Ae(e, t) {
  const a = String(e).trim() || "/";
  if (a.startsWith("//") || /[\u0000-\u001f\u007f]/.test(a))
    throw P("LOCAL_RESOURCE_BASE_INVALID", "The application-local resource base is invalid.");
  const n = new URL(a.endsWith("/") ? a : `${a}/`, t.origin);
  if (n.origin !== t.origin || n.username || n.password)
    throw P("LOCAL_RESOURCE_BASE_INVALID", "The application-local resource base must be same-origin.");
  return n;
}
function P(e, t) {
  return new Ee(e, t);
}
const X = 4096, ie = 4096, Se = "The requested application route is invalid.";
function _() {
  return new Error(Se);
}
function Ce(e) {
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
function $(e) {
  const t = String(e ?? "");
  if (!t || t.length > X || t === "." || t === ".." || /[\\/\u0000-\u001f\u007f-\u009f]/u.test(t) || Ce(t) || new TextEncoder().encode(t).length > X) throw _();
  let a;
  try {
    a = encodeURIComponent(t).replace(/[!'()*]/g, (n) => `%${n.charCodeAt(0).toString(16).toUpperCase()}`);
  } catch {
    throw _();
  }
  if (a.length > ie) throw _();
  return a;
}
function Z(e) {
  if (!e || e.length > ie || !/^(?:[^%]|%[0-9A-F]{2})+$/u.test(e))
    throw _();
  let t;
  try {
    t = decodeURIComponent(e);
  } catch {
    throw _();
  }
  if ($(t) !== e) throw _();
  return t;
}
function k(e) {
  let t = String(e ?? "").trim();
  if (!t.startsWith("/") || t.startsWith("//") || /[?#\\\u0000-\u001f]/u.test(t))
    throw new Error("The Vue viewer base path is invalid.");
  for (; t.length > 1 && t.endsWith("/"); ) t = t.slice(0, -1);
  return t;
}
function Le(e, t) {
  const a = k(e), n = `/object/${$(t?.logicalTypeName)}/${$(t?.id ?? t?.objectId)}`;
  return a === "/" ? n : `${a}${n}`;
}
function M(e) {
  return Le("/", e);
}
function ce(e, t = "/") {
  const a = String(e ?? ""), n = k(t), o = n === "/" ? "/object/" : `${n}/object/`;
  if (!a.startsWith(o)) throw _();
  const d = a.slice(o.length).split("/");
  if (d.length !== 2) throw _();
  return Object.freeze({
    logicalTypeName: Z(d[0]),
    objectId: Z(d[1])
  });
}
function Oe(e) {
  return M(e);
}
const H = "causeway-navigation-request", B = "causeway-action-request", Q = "causeway-action-result", J = "causeway-object-context-state-change", ee = "causeway-menu-bars-state-change";
function W() {
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
function E(e) {
  return Object.freeze({
    router: e.router,
    basePath: e.basePath,
    shell: e.state.shell,
    routeGeneration: e.state.routeGeneration
  });
}
function xe(e) {
  if (e?.kind !== "object" || !e.value || typeof e.value != "object") return null;
  const t = e.value._meta, a = t?.logicalTypeName, n = t?.id;
  return typeof a != "string" || typeof n != "string" || !a || !n ? null : Object.freeze({ logicalTypeName: a, id: n, title: String(t?.title ?? n) });
}
function je(e) {
  return [...e.querySelector("[data-causeway-router-view]")?.querySelectorAll("cw-action-results[data-causeway-page-result]") ?? []].filter((a) => a.isConnected);
}
function Ne(e) {
  const t = e.querySelectorAll("cw-action-results[data-causeway-shell-result]");
  return t.length === 1 ? t[0] : null;
}
function z(e) {
  const t = je(e);
  if (t.length > 1) throw new Error("The active Vue route has duplicate action-result outlets.");
  if (t.length === 1) return t[0];
  const a = Ne(e);
  if (!a?.isConnected) throw new Error("The authored Vue shell result outlet is unavailable.");
  return a;
}
function te(e, ...t) {
  const a = e;
  a.replacePresentation ? a.replacePresentation(...t) : e.replaceChildren(...t), e.hidden = t.length === 0;
}
function se(e, t) {
  const a = t.result, n = document.createElement("h2");
  if (n.textContent = t.actionId ? `${t.actionId} result` : "Action result", a?.kind === "collection") {
    const c = document.createElement("cw-standalone-collection");
    c.setAttribute("data-testid", "causeway-standalone-action-result"), c.setAttribute("named", String(t.resultPresentation?.named ?? n.textContent)), c.result = a, te(e, c);
    return;
  }
  const o = document.createElement("output");
  o.textContent = a?.kind === "scalar" ? String(a.value ?? "") : a?.kind === "void" ? "Completed" : "This result cannot be presented.", te(e, n, o);
}
async function Re(e, t) {
  const a = e.state.shell, o = a?.querySelector("[data-causeway-router-view]")?.querySelector("cw-object-context[data-causeway-route-context]"), c = o?.context;
  if (!o || !c?.refresh) return;
  const d = e.state.routeGeneration, u = (v) => {
    if (d !== e.state.routeGeneration) return l();
    const g = v.detail?.state;
    if (g?.status !== "terminal-error") {
      (g?.status === "ready" || g?.status === "partial-error") && l();
      return;
    }
    l(), (g.errors?.[0]?.extensions?.classification ?? g.errors?.[0]?.extensions?.code ?? g.error?.code) === "NOT_FOUND" && e.router.replace("/");
  }, l = () => o.removeEventListener(J, u);
  o.addEventListener(J, u), c.refresh(), se(z(a), t);
}
async function ae(e, t) {
  const a = W();
  await e.policies.navigate?.(t, a, E(e)) === !0 && a.claim(), a.claimed || await e.router.push(M(t));
}
function ue(e, t) {
  const a = /* @__PURE__ */ new WeakMap(), n = /* @__PURE__ */ new WeakSet(), o = t.querySelector("cw-menubars"), c = (r) => {
    try {
      const i = e.policies.menuActionLabel?.(r, E(e));
      return typeof i == "string" && i.trim() ? i : void 0;
    } catch (i) {
      e.policies.error?.(i, E(e));
      return;
    }
  }, d = () => !!c({
    serviceLogicalTypeName: "causeway.security.LogoutMenu",
    actionId: "logout"
  }), u = (r) => V(r) && !c(r);
  o && (o.excludeAction = u, o.actionLabel = c);
  let l = null, v = !0;
  const g = () => {
    const r = t.querySelector("[data-causeway-route-announcement]");
    r && (r.textContent = "Logout requires a host authentication integration."), t.dataset.causewayLogoutUnavailable = "true";
  }, h = (r, i) => {
    if (!v || i !== e.state.routeGeneration || !(r.target instanceof EventTarget)) return;
    const s = new CustomEvent(B, {
      bubbles: !0,
      composed: !0,
      cancelable: !0,
      detail: r.detail
    });
    n.add(s), r.target.dispatchEvent(s);
  }, m = (r) => {
    const i = r, s = i.detail;
    try {
      const y = z(t);
      s?.context && typeof s.context == "object" ? a.set(s.context, y) : l = y;
    } catch (y) {
      e.policies.error?.(y, E(e));
    }
    if (n.has(i)) return;
    const p = V(s), A = e.policies.action;
    if (!A) {
      p && (i.preventDefault(), g());
      return;
    }
    const f = W(), T = e.state.routeGeneration;
    try {
      const y = A(s, f, E(e));
      if (y && typeof y.then == "function") {
        i.preventDefault(), Promise.resolve(y).then((q) => {
          q === !0 && f.claim(), !f.claimed && !p ? h(i, T) : f.claimed || g();
        }).catch((q) => e.policies.error?.(q, E(e)));
        return;
      }
      y === !0 && f.claim(), (f.claimed || p) && i.preventDefault(), p && !f.claimed && g();
    } catch (y) {
      i.preventDefault(), e.policies.error?.(y, E(e));
    }
  }, b = (r) => {
    const i = r, s = i.detail?.target;
    !s?.logicalTypeName || !(s.id ?? s.objectId) || (i.preventDefault(), ae(e, s).catch((p) => e.policies.error?.(p, E(e))));
  }, w = (r) => {
    const i = r, s = i.detail ?? {};
    (async () => {
      const p = W();
      if (await e.policies.result?.(s, p, E(e)) === !0 && p.claim(), p.claimed) return;
      if (s.result?.kind === "local-resource") {
        _e(s.result.value, { applicationBase: e.applicationResourceBase }), i.target?.dismissResult?.();
        return;
      }
      const f = xe(s.result);
      if (f) return ae(e, f);
      const T = s.context && a.get(s.context) || l || z(t);
      s.result?.kind === "void" ? await Re(e, s) : se(T, s), i.target?.dismissResult?.();
    })().catch((p) => e.policies.error?.(p, E(e)));
  }, C = () => queueMicrotask(() => {
    v && !d() && Y(t);
  });
  return d() || Y(t), t.addEventListener(B, m, { capture: !0 }), t.addEventListener(H, b), t.addEventListener(Q, w), t.addEventListener(ee, C), () => {
    v = !1, o?.excludeAction === u && (o.excludeAction = void 0), o?.actionLabel === c && (o.actionLabel = void 0), t.removeEventListener(B, m, { capture: !0 }), t.removeEventListener(H, b), t.removeEventListener(Q, w), t.removeEventListener(ee, C);
  };
}
const Pe = /^[A-Za-z_][A-Za-z0-9_$-]*(?:\.[A-Za-z_][A-Za-z0-9_$-]*)*$/u;
function Ie(e) {
  return e ? e instanceof Map ? [...e.entries()] : Array.isArray(e) ? e : Object.entries(e) : [];
}
function Ue(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [a, n] of Ie(e)) {
    const o = String(a ?? "").trim();
    if (!Pe.test(o)) throw new Error("A Vue page registration has an invalid logical type.");
    if (!n || typeof n != "object" && typeof n != "function")
      throw new Error(`The Vue page registration for ${o} is unsupported.`);
    if (t.has(o)) throw new Error(`The Vue page registration for ${o} is duplicated.`);
    t.set(o, n);
  }
  return t;
}
function ke(e) {
  return typeof e == "function";
}
function Xe(e) {
  if (!e?.router) throw new Error("The Vue viewer requires the application router.");
  const t = String(e.endpoint ?? "").trim();
  if (!t) throw new Error("The Vue viewer GraphQL endpoint is required.");
  const a = { shell: null, routeGeneration: 0 };
  let n;
  return n = Object.freeze({
    plugin: {
      install(c) {
        c.provide(re, n);
      }
    },
    router: e.router,
    endpoint: t,
    basePath: k(e.basePath ?? (e.router.options.history.base || "/")),
    applicationResourceBase: k(e.applicationResourceBase ?? "/"),
    pages: Ue(e.pages),
    policies: Object.freeze({ ...e.policies }),
    developmentDiagnostics: e.developmentDiagnostics ?? !0,
    state: a
  }), n;
}
function F() {
  const e = de(re);
  if (!e) throw new Error("The Causeway Vue viewer plugin is not installed.");
  return e;
}
function Ze(e) {
  const t = F();
  let a = null;
  return D(() => {
    const n = e.value;
    if (!n) throw new Error("The authored Vue application shell is missing.");
    oe(n), t.state.shell = n, a = ue(t, n);
  }), ne(() => {
    a?.(), a = null, t.state.shell === e.value && (t.state.shell = null);
  }), e;
}
function He(e, t) {
  const a = oe(t);
  e.state.shell = t;
  const n = ue(e, t);
  return {
    landmarks: a,
    dispose() {
      n(), e.state.shell === t && (e.state.shell = null);
    }
  };
}
const qe = ["data-route-state"], Ge = /* @__PURE__ */ O({
  __name: "HomePage",
  setup(e) {
    const t = F(), a = N(null), n = N("loading"), o = N("Loading the application home page…");
    function c(d) {
      let u = null;
      return d.dispatchEvent(new CustomEvent("causeway-graphql-client-request", {
        bubbles: !0,
        composed: !0,
        detail: { provide(l) {
          u ??= l;
        } }
      })), u;
    }
    return D(async () => {
      const d = ++t.state.routeGeneration, u = a.value ? c(a.value) : null;
      if (!u) {
        n.value = "unsupported", o.value = "Choose an application action to begin.";
        return;
      }
      try {
        const l = await u.describeApplicationEntry();
        if (d !== t.state.routeGeneration) return;
        if (!l?.supported) {
          n.value = "unsupported", o.value = "Choose an application action to begin.";
          return;
        }
        const v = await u.readApplicationEntry({ description: l });
        if (d !== t.state.routeGeneration) return;
        const g = v?.data?.home, h = g?.object?._meta, m = h?.logicalTypeName ?? g?.logicalTypeName, b = h?.id, w = { claimed: !1, claim() {
          return this.claimed ? !1 : (this.claimed = !0, !0);
        } }, C = { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: d };
        if (await t.policies.home?.(v?.data, w, C) === !0 && w.claim(), d !== t.state.routeGeneration || w.claimed) return;
        if (g?.kind === "OBJECT" && m && b) {
          await t.router.replace(M({ logicalTypeName: m, id: b }));
          return;
        }
        n.value = v?.errors?.length ? "partial-error" : "ready", o.value = "Choose an application action to begin.";
      } catch (l) {
        if (d !== t.state.routeGeneration) return;
        t.policies.error?.(l, { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: d }), n.value = "partial-error", o.value = "The home page is unavailable; application menus remain available.";
      }
    }), (d, u) => (R(), U("section", {
      ref_key: "page",
      ref: a,
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": n.value,
      tabindex: "-1"
    }, [
      u[0] || (u[0] = L("h1", null, "Welcome", -1)),
      L("p", null, pe(o.value), 1)
    ], 8, qe));
  }
}), Be = {
  class: "causeway-vue-route-page causeway-vue-route-object",
  "data-causeway-route-page": "",
  "data-page-kind": "generic",
  "data-route-state": "loading",
  tabindex: "-1",
  "aria-label": "Object page"
}, Ve = ["logical-type", "object-id"], $e = /* @__PURE__ */ O({
  __name: "GenericObjectPage",
  props: {
    logicalTypeName: {},
    objectId: {},
    routeKey: {}
  },
  setup(e) {
    return (t, a) => (R(), U("section", Be, [
      L("cw-object-context", {
        "data-causeway-route-context": "",
        "logical-type": e.logicalTypeName,
        "object-id": e.objectId
      }, [...a[0] || (a[0] = [
        L("cw-object", { editable: "" }, null, -1),
        L("cw-interaction-controller", { "data-causeway-route-interactions": "" }, null, -1)
      ])], 8, Ve)
    ]));
  }
}), We = ["data-route-key"], ze = {
  key: 0,
  class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
  "data-route-state": "terminal-error",
  tabindex: "-1",
  role: "alert"
}, De = /* @__PURE__ */ O({
  __name: "ObjectRoutePage",
  setup(e) {
    const t = we(), a = F(), n = N(null), o = N(null), c = I(() => ce(t.path, "/")), d = O(() => () => S("section", {
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": "loading",
      tabindex: -1,
      role: "status"
    }, [S("h1", "Loading page…")])), u = O(() => () => S("section", {
      class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
      "data-route-state": "terminal-error",
      tabindex: -1,
      role: "alert"
    }, [S("h1", "Page unavailable"), S("p", "The application page could not be loaded.")])), l = I(() => Oe(c.value)), v = I(() => a.pages.get(c.value.logicalTypeName)), g = I(() => {
      const r = v.value;
      return r ? ke(r) ? fe({
        loader: async () => {
          const i = await r();
          return "default" in i ? i.default : i;
        },
        delay: 0,
        timeout: 3e4,
        loadingComponent: d,
        errorComponent: u,
        onError(i, s, p) {
          a.policies.error?.(i, {
            router: a.router,
            basePath: a.basePath,
            shell: a.state.shell,
            routeGeneration: a.state.routeGeneration
          }), p();
        }
      }) : r : $e;
    });
    function h() {
      const r = a.state.shell;
      return {
        route: r?.querySelector("[data-causeway-router-view]") ?? null,
        loading: r?.querySelector("[data-causeway-route-loading]") ?? null,
        announcement: r?.querySelector("[data-causeway-route-announcement]") ?? null
      };
    }
    function m(r, i) {
      const s = h().announcement;
      s && (s.textContent = "", requestAnimationFrame(() => {
        i === a.state.routeGeneration && s.isConnected && (s.textContent = r);
      }));
    }
    function b(r) {
      const i = h();
      i.route?.setAttribute("aria-busy", String(r)), i.loading && (i.loading.hidden = !r);
    }
    async function w() {
      const r = a.state.routeGeneration;
      if (o.value = null, await ve(), r !== a.state.routeGeneration || !n.value) return;
      const i = be(n.value, c.value);
      i.valid || (o.value = i.classification ?? "invalid", b(!1), m("Page unavailable", r));
    }
    function C(r) {
      const i = a.state.routeGeneration, s = r, p = s.detail?.state, A = s.target?.closest("[data-causeway-route-page]");
      if (!p || !A || !n.value?.contains(A)) return;
      let f = String(p.status ?? "terminal-error");
      if (f === "terminal-error") {
        const T = p.errors?.[0]?.extensions?.classification ?? p.errors?.[0]?.extensions?.code ?? p.error?.code;
        (T === "NOT_FOUND" || T === "ACCESS_DENIED") && (f = "unavailable");
      }
      if (A.dataset.routeState = f, f === "ready" || f === "partial-error") {
        b(!1);
        const T = p.snapshot?.data?._meta?.title;
        T && (document.title = String(T));
        for (const y of A.querySelectorAll("cw-collection:not([active])"))
          y.activate?.();
        m(f === "ready" ? "Page ready" : "Page ready with partial information", i);
      } else (f === "terminal-error" || f === "unavailable") && (b(!1), m("Page unavailable", i));
    }
    return ge(() => t.fullPath, () => {
      a.state.routeGeneration += 1, b(!0), m("Loading page", a.state.routeGeneration), w();
    }, { immediate: !0 }), D(async () => {
      n.value?.addEventListener("causeway-object-context-state-change", C), await w();
      const r = n.value?.querySelector(
        "cw-object-context[data-causeway-route-context]"
      );
      r?.context?.state && r.dispatchEvent(new CustomEvent("causeway-object-context-state-change", {
        bubbles: !0,
        composed: !0,
        detail: { state: r.context.state, context: r.context }
      })), n.value?.querySelector("[data-causeway-route-page]")?.focus({ preventScroll: !0 });
    }), ne(() => n.value?.removeEventListener("causeway-object-context-state-change", C)), (r, i) => (R(), U("div", {
      ref_key: "host",
      ref: n,
      class: "causeway-vue-object-route",
      "data-route-key": l.value
    }, [
      o.value ? (R(), U("section", ze, [...i[0] || (i[0] = [
        L("h1", null, "Page unavailable", -1),
        L("p", null, "The application page has an invalid semantic context boundary.", -1)
      ])])) : (R(), he(ye(g.value), {
        key: l.value,
        "logical-type-name": c.value.logicalTypeName,
        "object-id": c.value.objectId,
        "route-key": l.value,
        onVnodeMounted: w,
        onVnodeUpdated: w
      }, null, 8, ["logical-type-name", "object-id", "route-key"]))
    ], 8, We));
  }
}), j = Object.freeze({
  home: "causeway-home",
  object: "causeway-object",
  invalid: "causeway-invalid-route",
  notFound: "causeway-not-found"
});
function le(e, t, a, n) {
  return O({
    name: `Causeway${e.replace(/\W/gu, "")}Page`,
    setup() {
      return () => S("section", {
        class: ["causeway-vue-route-page", "causeway-vue-status", a === "invalid-route" && "causeway-vue-status-danger"],
        "data-route-state": a,
        tabindex: -1,
        role: n
      }, [S("h1", e), S("p", t)]);
    }
  });
}
const Me = le(
  "Invalid route",
  "The requested application route is invalid.",
  "invalid-route",
  "alert"
), Fe = le(
  "Page unavailable",
  "The requested page is unavailable.",
  "not-found",
  "alert"
);
function Qe(e = {}) {
  return [
    { path: "/", name: j.home, component: e.homeComponent ?? Ge },
    {
      path: "/object/:logicalTypeName/:objectId",
      name: j.object,
      component: De,
      beforeEnter(t) {
        try {
          return ce(t.path, "/"), !0;
        } catch {
          return { name: j.invalid, replace: !0 };
        }
      }
    },
    {
      path: "/invalid-route",
      name: j.invalid,
      component: Me
    },
    {
      path: "/:pathMatch(.*)*",
      name: j.notFound,
      component: e.notFoundComponent ?? Fe
    }
  ];
}
export {
  B as ACTION_REQUEST_EVENT,
  Q as ACTION_RESULT_EVENT,
  j as CAUSEWAY_ROUTE_NAMES,
  re as CAUSEWAY_VIEWER_KEY,
  $e as CausewayGenericObjectPage,
  Ge as CausewayHomePage,
  Me as CausewayInvalidRoutePage,
  Fe as CausewayNotFoundPage,
  De as CausewayObjectRoutePage,
  K as FRAMEWORK_LOGOUT_ACTION,
  Se as INVALID_ROUTE_MESSAGE,
  Ee as LocalResourceNavigationError,
  ee as MENU_BARS_STATE_EVENT,
  H as NAVIGATION_REQUEST_EVENT,
  J as OBJECT_CONTEXT_STATE_EVENT,
  G as ROUTE_CONTEXT_SELECTOR,
  me as ROUTE_INTERACTIONS_SELECTOR,
  He as bindCausewayShell,
  Le as canonicalObjectPath,
  Oe as canonicalRouteKey,
  M as canonicalRouterObjectPath,
  Qe as createCausewayRouteRecords,
  Xe as createCausewayVueViewer,
  Z as decodeRouteSegment,
  $ as encodeRouteSegment,
  ue as installSemanticBridge,
  V as isFrameworkLogoutAction,
  ke as isPageLoader,
  _e as navigateLocalResource,
  k as normalizeBasePath,
  Ue as normalizePageRegistry,
  ce as parseCanonicalObjectPath,
  se as presentSemanticResult,
  Y as removeFrameworkLogoutMenuActions,
  Te as resolveLocalResourceTarget,
  z as resolveResultOutlet,
  Ze as useCausewayShell,
  F as useCausewayViewer,
  be as validateRouteBoundary,
  oe as validateShellBoundary
};
//# sourceMappingURL=causeway-vue.js.map
