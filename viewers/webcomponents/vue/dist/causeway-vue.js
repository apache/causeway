import { inject as ye, onMounted as F, onBeforeUnmount as ce, defineComponent as j, ref as P, openBlock as I, createElementBlock as G, createElementVNode as x, toDisplayString as ve, computed as k, h as C, defineAsyncComponent as we, watch as me, createBlock as be, resolveDynamicComponent as Ee, nextTick as Ae } from "vue";
import { useRoute as Te } from "vue-router";
const V = "cw-object-context[data-causeway-route-context]", _e = "cw-interaction-controller[data-causeway-route-interactions]";
function Se(e, t) {
  const a = [...e.querySelectorAll(V)].filter((c) => !c.parentElement?.closest(V));
  if (a.length === 0) return Object.freeze({ valid: !1, classification: "missing-context" });
  if (a.length !== 1) return Object.freeze({ valid: !1, classification: "duplicate-context" });
  const n = a[0];
  return n.getAttribute("logical-type") !== t.logicalTypeName || n.getAttribute("object-id") !== t.objectId ? Object.freeze({ valid: !1, classification: "identity" }) : [...n.querySelectorAll(_e)].filter((c) => c.closest(V) === n).length !== 1 ? Object.freeze({ valid: !1, classification: "interactions" }) : Object.freeze({ valid: !0, context: n });
}
function N(e, t) {
  const a = e.querySelectorAll(t);
  return a.length === 1 ? a[0] : null;
}
function ie(e) {
  const t = N(e, "cw-graphql-client[data-causeway-shell-client]"), a = N(e, "[data-causeway-router-view]"), n = N(e, "[data-causeway-route-loading]"), o = N(e, "[data-causeway-route-announcement]"), c = N(e, "cw-action-results[data-causeway-shell-result]");
  if (!t || !a || !n || !o || !c || !t.contains(a) || !t.contains(c))
    throw new Error("The authored Vue application shell is invalid.");
  return Object.freeze({ shell: e, client: t, route: a, loading: n, announcement: o, result: c });
}
const se = /* @__PURE__ */ Symbol("causeway-vue-viewer"), H = Object.freeze({
  serviceLogicalTypeName: "causeway.security.LogoutMenu",
  actionId: "logout"
});
function $(e) {
  return e?.serviceLogicalTypeName === H.serviceLogicalTypeName && e?.actionId === H.actionId;
}
function Q(e) {
  let t = 0;
  for (const a of e.querySelectorAll("[data-causeway-service-action]"))
    $({
      serviceLogicalTypeName: a.getAttribute("data-service-logical-type") ?? void 0,
      actionId: a.getAttribute("data-action-id") ?? ""
    }) && ((a.closest("[data-causeway-service-action-region]") ?? a).remove(), t += 1);
  return t;
}
class Ce extends Error {
  code;
  constructor(t, a) {
    super(a), this.name = "LocalResourceNavigationError", this.code = t;
  }
}
function Le(e, t = {}) {
  const a = t.location ?? window.location, n = typeof e?.path == "string" ? e.path.trim() : "", o = e?.openUrlStrategy;
  if (!n || !["SAME_WINDOW", "NEW_WINDOW"].includes(o ?? ""))
    throw U("LOCAL_RESOURCE_INVALID", "The local-resource result is incomplete.");
  if (/^[a-z][a-z\d+.-]*:/i.test(n) || n.startsWith("//") || n.includes("\\") || /[\u0000-\u001f\u007f]/.test(n))
    throw U("LOCAL_RESOURCE_TARGET_UNSAFE", "The local-resource target is not an application-local path.");
  const c = new URL(a.href), u = Oe(t.applicationBase ?? "/", c).pathname.replace(/\/+$/, "") || "/", d = n.startsWith("/") ? n : `/${n}`, g = u !== "/" && (d === u || d.startsWith(`${u}/`) || d.startsWith(`${u}?`) || d.startsWith(`${u}#`)) ? d : `${u === "/" ? "" : u}/${d.replace(/^\/+/, "")}`, h = new URL(g, c.origin);
  if (h.origin !== c.origin || h.username || h.password || u !== "/" && h.pathname !== u && !h.pathname.startsWith(`${u}/`))
    throw U("LOCAL_RESOURCE_TARGET_UNSAFE", "The local-resource target escapes the configured application boundary.");
  return Object.freeze({ url: h, openUrlStrategy: o });
}
function xe(e, t = {}) {
  const a = t.location ?? window.location, n = Le(e, { location: a, applicationBase: t.applicationBase });
  if (n.openUrlStrategy === "SAME_WINDOW") {
    a.assign(n.url.href);
    return;
  }
  const c = (t.open ?? window.open.bind(window))(n.url.href, "_blank", "noopener,noreferrer");
  c && (c.opener = null);
}
function Oe(e, t) {
  const a = String(e).trim() || "/";
  if (a.startsWith("//") || /[\u0000-\u001f\u007f]/.test(a))
    throw U("LOCAL_RESOURCE_BASE_INVALID", "The application-local resource base is invalid.");
  const n = new URL(a.endsWith("/") ? a : `${a}/`, t.origin);
  if (n.origin !== t.origin || n.username || n.password)
    throw U("LOCAL_RESOURCE_BASE_INVALID", "The application-local resource base must be same-origin.");
  return n;
}
function U(e, t) {
  return new Ce(e, t);
}
const J = 4096, ue = 4096, je = "The requested application route is invalid.";
function S() {
  return new Error(je);
}
function Ne(e) {
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
function W(e) {
  const t = String(e ?? "");
  if (!t || t.length > J || t === "." || t === ".." || /[\\/\u0000-\u001f\u007f-\u009f]/u.test(t) || Ne(t) || new TextEncoder().encode(t).length > J) throw S();
  let a;
  try {
    a = encodeURIComponent(t).replace(/[!'()*]/g, (n) => `%${n.charCodeAt(0).toString(16).toUpperCase()}`);
  } catch {
    throw S();
  }
  if (a.length > ue) throw S();
  return a;
}
function ee(e) {
  if (!e || e.length > ue || !/^(?:[^%]|%[0-9A-F]{2})+$/u.test(e))
    throw S();
  let t;
  try {
    t = decodeURIComponent(e);
  } catch {
    throw S();
  }
  if (W(t) !== e) throw S();
  return t;
}
function q(e) {
  let t = String(e ?? "").trim();
  if (!t.startsWith("/") || t.startsWith("//") || /[?#\\\u0000-\u001f]/u.test(t))
    throw new Error("The Vue viewer base path is invalid.");
  for (; t.length > 1 && t.endsWith("/"); ) t = t.slice(0, -1);
  return t;
}
function Re(e, t) {
  const a = q(e), n = `/object/${W(t?.logicalTypeName)}/${W(t?.id ?? t?.objectId)}`;
  return a === "/" ? n : `${a}${n}`;
}
function K(e) {
  return Re("/", e);
}
function le(e, t = "/") {
  const a = String(e ?? ""), n = q(t), o = n === "/" ? "/object/" : `${n}/object/`;
  if (!a.startsWith(o)) throw S();
  const p = a.slice(o.length).split("/");
  if (p.length !== 2) throw S();
  return Object.freeze({
    logicalTypeName: ee(p[0]),
    objectId: ee(p[1])
  });
}
function Pe(e) {
  return K(e);
}
const te = "causeway-navigation-request", B = "causeway-action-request", ae = "causeway-action-result", ne = "causeway-object-context-state-change", oe = "causeway-menu-bars-state-change";
function z() {
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
function m(e) {
  return Object.freeze({
    router: e.router,
    basePath: e.basePath,
    shell: e.state.shell,
    routeGeneration: e.state.routeGeneration
  });
}
function Ie(e) {
  if (e?.kind !== "object" || !e.value || typeof e.value != "object") return null;
  const t = e.value._meta, a = t?.logicalTypeName, n = t?.id;
  return typeof a != "string" || typeof n != "string" || !a || !n ? null : Object.freeze({ logicalTypeName: a, id: n, title: String(t?.title ?? n) });
}
function Ue(e) {
  return [...e.querySelector("[data-causeway-router-view]")?.querySelectorAll("cw-action-results[data-causeway-page-result]") ?? []].filter((a) => a.isConnected);
}
function ke(e) {
  const t = e.querySelectorAll("cw-action-results[data-causeway-shell-result]");
  return t.length === 1 ? t[0] : null;
}
function D(e) {
  const t = Ue(e);
  if (t.length > 1) throw new Error("The active Vue route has duplicate action-result outlets.");
  if (t.length === 1) return t[0];
  const a = ke(e);
  if (!a?.isConnected) throw new Error("The authored Vue shell result outlet is unavailable.");
  return a;
}
function M(e, ...t) {
  const a = e;
  a.replacePresentation ? a.replacePresentation(...t) : e.replaceChildren(...t), e.hidden = t.length === 0;
}
function Ge(e) {
  const t = e.querySelectorAll(
    "cw-action-results[data-causeway-page-result], cw-action-results[data-causeway-shell-result]"
  );
  for (const a of t)
    a.isConnected && M(a);
}
function de(e, t) {
  const a = t.result, n = document.createElement("h2");
  if (n.textContent = t.actionId ? `${t.actionId} result` : "Action result", a?.kind === "collection") {
    const c = document.createElement("cw-standalone-collection");
    c.setAttribute("data-testid", "causeway-standalone-action-result"), c.setAttribute("named", String(t.resultPresentation?.named ?? n.textContent)), c.result = a, M(e, c);
    return;
  }
  const o = document.createElement("output");
  o.textContent = a?.kind === "scalar" ? String(a.value ?? "") : a?.kind === "void" ? "Completed" : "This result cannot be presented.", M(e, n, o);
}
async function qe(e, t) {
  const a = e.state.shell, o = a?.querySelector("[data-causeway-router-view]")?.querySelector("cw-object-context[data-causeway-route-context]"), c = o?.context;
  if (!o || !c?.refresh) return;
  const p = e.state.routeGeneration, u = (b) => {
    if (p !== e.state.routeGeneration) return d();
    const g = b.detail?.state;
    if (g?.status !== "terminal-error") {
      (g?.status === "ready" || g?.status === "partial-error") && d();
      return;
    }
    d(), (g.errors?.[0]?.extensions?.classification ?? g.errors?.[0]?.extensions?.code ?? g.error?.code) === "NOT_FOUND" && e.router.replace("/");
  }, d = () => o.removeEventListener(ne, u);
  o.addEventListener(ne, u), c.refresh(), de(D(a), t);
}
async function re(e, t) {
  const a = z();
  await e.policies.navigate?.(t, a, m(e)) === !0 && a.claim(), a.claimed || await e.router.push(K(t));
}
function pe(e, t) {
  const a = /* @__PURE__ */ new WeakMap(), n = /* @__PURE__ */ new WeakSet(), o = t.querySelector("cw-menubars"), c = (i) => {
    try {
      const r = e.policies.menuLabel?.(i, m(e));
      return typeof r == "string" && r.trim() ? r : void 0;
    } catch (r) {
      e.policies.error?.(r, m(e));
      return;
    }
  }, p = (i) => {
    try {
      const r = e.policies.menuActionLabel?.(i, m(e));
      return typeof r == "string" && r.trim() ? r : void 0;
    } catch (r) {
      e.policies.error?.(r, m(e));
      return;
    }
  }, u = (i) => {
    try {
      const r = e.policies.menuActionAppearance?.(i, m(e));
      return typeof r == "string" && r.trim() ? r : void 0;
    } catch (r) {
      e.policies.error?.(r, m(e));
      return;
    }
  }, d = () => !!p({
    serviceLogicalTypeName: "causeway.security.LogoutMenu",
    actionId: "logout"
  }), b = (i) => $(i) && !p(i);
  o && (o.excludeAction = b, o.menuLabel = c, o.actionLabel = p, o.actionAppearance = u);
  let g = null, h = !0;
  const E = () => {
    const i = t.querySelector("[data-causeway-route-announcement]");
    i && (i.textContent = "Logout requires a host authentication integration."), t.dataset.causewayLogoutUnavailable = "true";
  }, _ = (i, r) => {
    if (!h || r !== e.state.routeGeneration || !(i.target instanceof EventTarget)) return;
    const s = new CustomEvent(B, {
      bubbles: !0,
      composed: !0,
      cancelable: !0,
      detail: i.detail
    });
    n.add(s), i.target.dispatchEvent(s);
  }, A = (i) => {
    const r = i, s = r.detail;
    try {
      const w = D(t);
      s?.context && typeof s.context == "object" ? a.set(s.context, w) : g = w;
    } catch (w) {
      e.policies.error?.(w, m(e));
    }
    if (n.has(r)) return;
    const y = $(s), T = e.policies.action;
    if (!T) {
      y && (r.preventDefault(), E());
      return;
    }
    const v = z(), X = e.state.routeGeneration;
    try {
      const w = T(s, v, m(e));
      if (w && typeof w.then == "function") {
        r.preventDefault(), Promise.resolve(w).then((O) => {
          O === !0 && v.claim(), !v.claimed && !y ? _(r, X) : v.claimed || E();
        }).catch((O) => e.policies.error?.(O, m(e)));
        return;
      }
      w === !0 && v.claim(), (v.claimed || y) && r.preventDefault(), y && !v.claimed && E();
    } catch (w) {
      r.preventDefault(), e.policies.error?.(w, m(e));
    }
  }, L = (i) => {
    const r = i, s = r.detail?.target;
    !s?.logicalTypeName || !(s.id ?? s.objectId) || (r.preventDefault(), re(e, s).catch((y) => e.policies.error?.(y, m(e))));
  }, l = (i) => {
    const r = i, s = r.detail ?? {}, y = e.state.routeGeneration, T = r.target;
    (async () => {
      const v = z();
      if (await e.policies.result?.(s, v, m(e)) === !0 && v.claim(), v.claimed || !h || y !== e.state.routeGeneration) return;
      const w = () => {
        try {
          T?.dismissResult?.();
        } catch (he) {
          e.policies.error?.(he, m(e));
        }
      };
      if (Ge(t), s.result?.kind === "local-resource") {
        xe(s.result.value, { applicationBase: e.applicationResourceBase }), w();
        return;
      }
      const O = Ie(s.result);
      if (O) {
        w(), await re(e, O);
        return;
      }
      const Z = s.context && a.get(s.context) || g, ge = Z?.isConnected ? Z : D(t);
      s.context && a.delete(s.context), g = null, s.result?.kind === "void" ? await qe(e, s) : de(ge, s), w();
    })().catch((v) => e.policies.error?.(v, m(e)));
  }, f = () => queueMicrotask(() => {
    h && !d() && Q(t);
  });
  return d() || Q(t), t.addEventListener(B, A, { capture: !0 }), t.addEventListener(te, L), t.addEventListener(ae, l), t.addEventListener(oe, f), () => {
    h = !1, o?.excludeAction === b && (o.excludeAction = void 0), o?.menuLabel === c && (o.menuLabel = void 0), o?.actionLabel === p && (o.actionLabel = void 0), o?.actionAppearance === u && (o.actionAppearance = void 0), t.removeEventListener(B, A, { capture: !0 }), t.removeEventListener(te, L), t.removeEventListener(ae, l), t.removeEventListener(oe, f);
  };
}
const Ve = /^[A-Za-z_][A-Za-z0-9_$-]*(?:\.[A-Za-z_][A-Za-z0-9_$-]*)*$/u;
function Be(e) {
  return e ? e instanceof Map ? [...e.entries()] : Array.isArray(e) ? e : Object.entries(e) : [];
}
function $e(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [a, n] of Be(e)) {
    const o = String(a ?? "").trim();
    if (!Ve.test(o)) throw new Error("A Vue page registration has an invalid logical type.");
    if (!n || typeof n != "object" && typeof n != "function")
      throw new Error(`The Vue page registration for ${o} is unsupported.`);
    if (t.has(o)) throw new Error(`The Vue page registration for ${o} is duplicated.`);
    t.set(o, n);
  }
  return t;
}
function We(e) {
  return typeof e == "function";
}
function tt(e) {
  if (!e?.router) throw new Error("The Vue viewer requires the application router.");
  const t = String(e.endpoint ?? "").trim();
  if (!t) throw new Error("The Vue viewer GraphQL endpoint is required.");
  const a = { shell: null, routeGeneration: 0 };
  let n;
  return n = Object.freeze({
    plugin: {
      install(c) {
        c.provide(se, n);
      }
    },
    router: e.router,
    endpoint: t,
    basePath: q(e.basePath ?? (e.router.options.history.base || "/")),
    applicationResourceBase: q(e.applicationResourceBase ?? "/"),
    pages: $e(e.pages),
    policies: Object.freeze({ ...e.policies }),
    developmentDiagnostics: e.developmentDiagnostics ?? !0,
    state: a
  }), n;
}
function Y() {
  const e = ye(se);
  if (!e) throw new Error("The Causeway Vue viewer plugin is not installed.");
  return e;
}
function at(e) {
  const t = Y();
  let a = null;
  return F(() => {
    const n = e.value;
    if (!n) throw new Error("The authored Vue application shell is missing.");
    ie(n), t.state.shell = n, a = pe(t, n);
  }), ce(() => {
    a?.(), a = null, t.state.shell === e.value && (t.state.shell = null);
  }), e;
}
function nt(e, t) {
  const a = ie(t);
  e.state.shell = t;
  const n = pe(e, t);
  return {
    landmarks: a,
    dispose() {
      n(), e.state.shell === t && (e.state.shell = null);
    }
  };
}
const ze = ["data-route-state"], De = /* @__PURE__ */ j({
  __name: "HomePage",
  setup(e) {
    const t = Y(), a = P(null), n = P("loading"), o = P("Loading the application home page…");
    function c(p) {
      let u = null;
      return p.dispatchEvent(new CustomEvent("causeway-graphql-client-request", {
        bubbles: !0,
        composed: !0,
        detail: { provide(d) {
          u ??= d;
        } }
      })), u;
    }
    return F(async () => {
      const p = ++t.state.routeGeneration, u = a.value ? c(a.value) : null;
      if (!u) {
        n.value = "unsupported", o.value = "Choose an application action to begin.";
        return;
      }
      try {
        const d = await u.describeApplicationEntry();
        if (p !== t.state.routeGeneration) return;
        if (!d?.supported) {
          n.value = "unsupported", o.value = "Choose an application action to begin.";
          return;
        }
        const b = await u.readApplicationEntry({ description: d });
        if (p !== t.state.routeGeneration) return;
        const g = b?.data?.home, h = g?.object?._meta, E = h?.logicalTypeName ?? g?.logicalTypeName, _ = h?.id, A = { claimed: !1, claim() {
          return this.claimed ? !1 : (this.claimed = !0, !0);
        } }, L = { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: p };
        if (await t.policies.home?.(b?.data, A, L) === !0 && A.claim(), p !== t.state.routeGeneration || A.claimed) return;
        if (g?.kind === "OBJECT" && E && _) {
          await t.router.replace(K({ logicalTypeName: E, id: _ }));
          return;
        }
        n.value = b?.errors?.length ? "partial-error" : "ready", o.value = "Choose an application action to begin.";
      } catch (d) {
        if (p !== t.state.routeGeneration) return;
        t.policies.error?.(d, { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: p }), n.value = "partial-error", o.value = "The home page is unavailable; application menus remain available.";
      }
    }), (p, u) => (I(), G("section", {
      ref_key: "page",
      ref: a,
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": n.value,
      tabindex: "-1"
    }, [
      u[0] || (u[0] = x("h1", null, "Welcome", -1)),
      x("p", null, ve(o.value), 1)
    ], 8, ze));
  }
}), Me = {
  class: "causeway-vue-route-page causeway-vue-route-object",
  "data-causeway-route-page": "",
  "data-page-kind": "generic",
  "data-route-state": "loading",
  tabindex: "-1",
  "aria-label": "Object page"
}, Fe = ["logical-type", "object-id"], Ke = /* @__PURE__ */ j({
  __name: "GenericObjectPage",
  props: {
    logicalTypeName: {},
    objectId: {},
    routeKey: {}
  },
  setup(e) {
    return (t, a) => (I(), G("section", Me, [
      x("cw-object-context", {
        "data-causeway-route-context": "",
        "logical-type": e.logicalTypeName,
        "object-id": e.objectId
      }, [...a[0] || (a[0] = [
        x("cw-object", { editable: "" }, null, -1),
        x("cw-interaction-controller", { "data-causeway-route-interactions": "" }, null, -1)
      ])], 8, Fe)
    ]));
  }
}), Ye = ["data-route-key"], Xe = {
  key: 0,
  class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
  "data-route-state": "terminal-error",
  tabindex: "-1",
  role: "alert"
}, Ze = /* @__PURE__ */ j({
  __name: "ObjectRoutePage",
  setup(e) {
    const t = Te(), a = Y(), n = P(null), o = P(null), c = k(() => le(t.path, "/")), p = j(() => () => C("section", {
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": "loading",
      tabindex: -1,
      role: "status"
    }, [C("h1", "Loading page…")])), u = j(() => () => C("section", {
      class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
      "data-route-state": "terminal-error",
      tabindex: -1,
      role: "alert"
    }, [C("h1", "Page unavailable"), C("p", "The application page could not be loaded.")])), d = k(() => Pe(c.value)), b = k(() => a.pages.get(c.value.logicalTypeName)), g = k(() => {
      const l = b.value;
      return l ? We(l) ? we({
        loader: async () => {
          const f = await l();
          return "default" in f ? f.default : f;
        },
        delay: 0,
        timeout: 3e4,
        loadingComponent: p,
        errorComponent: u,
        onError(f, i, r) {
          a.policies.error?.(f, {
            router: a.router,
            basePath: a.basePath,
            shell: a.state.shell,
            routeGeneration: a.state.routeGeneration
          }), r();
        }
      }) : l : Ke;
    });
    function h() {
      const l = a.state.shell;
      return {
        route: l?.querySelector("[data-causeway-router-view]") ?? null,
        loading: l?.querySelector("[data-causeway-route-loading]") ?? null,
        announcement: l?.querySelector("[data-causeway-route-announcement]") ?? null
      };
    }
    function E(l, f) {
      const i = h().announcement;
      i && (i.textContent = "", requestAnimationFrame(() => {
        f === a.state.routeGeneration && i.isConnected && (i.textContent = l);
      }));
    }
    function _(l) {
      const f = h();
      f.route?.setAttribute("aria-busy", String(l)), f.loading && (f.loading.hidden = !l);
    }
    async function A() {
      const l = a.state.routeGeneration;
      if (o.value = null, await Ae(), l !== a.state.routeGeneration || !n.value) return;
      const f = Se(n.value, c.value);
      f.valid || (o.value = f.classification ?? "invalid", _(!1), E("Page unavailable", l));
    }
    function L(l) {
      const f = a.state.routeGeneration, i = l, r = i.detail?.state, s = i.target?.closest("[data-causeway-route-page]");
      if (!r || !s || !n.value?.contains(s)) return;
      let y = String(r.status ?? "terminal-error");
      if (y === "terminal-error") {
        const T = r.errors?.[0]?.extensions?.classification ?? r.errors?.[0]?.extensions?.code ?? r.error?.code;
        (T === "NOT_FOUND" || T === "ACCESS_DENIED") && (y = "unavailable");
      }
      if (s.dataset.routeState = y, y === "ready" || y === "partial-error") {
        _(!1);
        const T = r.snapshot?.data?._meta?.title;
        T && (document.title = String(T));
        for (const v of s.querySelectorAll("cw-collection:not([active])"))
          v.activate?.();
        E(y === "ready" ? "Page ready" : "Page ready with partial information", f);
      } else (y === "terminal-error" || y === "unavailable") && (_(!1), E("Page unavailable", f));
    }
    return me(() => t.fullPath, () => {
      a.state.routeGeneration += 1, _(!0), E("Loading page", a.state.routeGeneration), A();
    }, { immediate: !0 }), F(async () => {
      n.value?.addEventListener("causeway-object-context-state-change", L), await A();
      const l = n.value?.querySelector(
        "cw-object-context[data-causeway-route-context]"
      );
      l?.context?.state && l.dispatchEvent(new CustomEvent("causeway-object-context-state-change", {
        bubbles: !0,
        composed: !0,
        detail: { state: l.context.state, context: l.context }
      })), n.value?.querySelector("[data-causeway-route-page]")?.focus({ preventScroll: !0 });
    }), ce(() => n.value?.removeEventListener("causeway-object-context-state-change", L)), (l, f) => (I(), G("div", {
      ref_key: "host",
      ref: n,
      class: "causeway-vue-object-route",
      "data-route-key": d.value
    }, [
      o.value ? (I(), G("section", Xe, [...f[0] || (f[0] = [
        x("h1", null, "Page unavailable", -1),
        x("p", null, "The application page has an invalid semantic context boundary.", -1)
      ])])) : (I(), be(Ee(g.value), {
        key: d.value,
        "logical-type-name": c.value.logicalTypeName,
        "object-id": c.value.objectId,
        "route-key": d.value,
        onVnodeMounted: A,
        onVnodeUpdated: A
      }, null, 8, ["logical-type-name", "object-id", "route-key"]))
    ], 8, Ye));
  }
}), R = Object.freeze({
  home: "causeway-home",
  object: "causeway-object",
  invalid: "causeway-invalid-route",
  notFound: "causeway-not-found"
});
function fe(e, t, a, n) {
  return j({
    name: `Causeway${e.replace(/\W/gu, "")}Page`,
    setup() {
      return () => C("section", {
        class: ["causeway-vue-route-page", "causeway-vue-status", a === "invalid-route" && "causeway-vue-status-danger"],
        "data-route-state": a,
        tabindex: -1,
        role: n
      }, [C("h1", e), C("p", t)]);
    }
  });
}
const He = fe(
  "Invalid route",
  "The requested application route is invalid.",
  "invalid-route",
  "alert"
), Qe = fe(
  "Page unavailable",
  "The requested page is unavailable.",
  "not-found",
  "alert"
);
function ot(e = {}) {
  return [
    { path: "/", name: R.home, component: e.homeComponent ?? De },
    {
      path: "/object/:logicalTypeName/:objectId",
      name: R.object,
      component: Ze,
      beforeEnter(t) {
        try {
          return le(t.path, "/"), !0;
        } catch {
          return { name: R.invalid, replace: !0 };
        }
      }
    },
    {
      path: "/invalid-route",
      name: R.invalid,
      component: He
    },
    {
      path: "/:pathMatch(.*)*",
      name: R.notFound,
      component: e.notFoundComponent ?? Qe
    }
  ];
}
export {
  B as ACTION_REQUEST_EVENT,
  ae as ACTION_RESULT_EVENT,
  R as CAUSEWAY_ROUTE_NAMES,
  se as CAUSEWAY_VIEWER_KEY,
  Ke as CausewayGenericObjectPage,
  De as CausewayHomePage,
  He as CausewayInvalidRoutePage,
  Qe as CausewayNotFoundPage,
  Ze as CausewayObjectRoutePage,
  H as FRAMEWORK_LOGOUT_ACTION,
  je as INVALID_ROUTE_MESSAGE,
  Ce as LocalResourceNavigationError,
  oe as MENU_BARS_STATE_EVENT,
  te as NAVIGATION_REQUEST_EVENT,
  ne as OBJECT_CONTEXT_STATE_EVENT,
  V as ROUTE_CONTEXT_SELECTOR,
  _e as ROUTE_INTERACTIONS_SELECTOR,
  nt as bindCausewayShell,
  Re as canonicalObjectPath,
  Pe as canonicalRouteKey,
  K as canonicalRouterObjectPath,
  ot as createCausewayRouteRecords,
  tt as createCausewayVueViewer,
  ee as decodeRouteSegment,
  W as encodeRouteSegment,
  pe as installSemanticBridge,
  $ as isFrameworkLogoutAction,
  We as isPageLoader,
  xe as navigateLocalResource,
  q as normalizeBasePath,
  $e as normalizePageRegistry,
  le as parseCanonicalObjectPath,
  de as presentSemanticResult,
  Q as removeFrameworkLogoutMenuActions,
  Le as resolveLocalResourceTarget,
  D as resolveResultOutlet,
  at as useCausewayShell,
  Y as useCausewayViewer,
  Se as validateRouteBoundary,
  ie as validateShellBoundary
};
//# sourceMappingURL=causeway-vue.js.map
