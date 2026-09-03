import { inject as le, onMounted as W, onBeforeUnmount as ee, defineComponent as C, ref as L, openBlock as N, createElementBlock as I, createElementVNode as A, toDisplayString as de, computed as P, h as S, defineAsyncComponent as pe, watch as fe, createBlock as ge, resolveDynamicComponent as he, nextTick as ye } from "vue";
import { useRoute as ve } from "vue-router";
const k = "cw-object-context[data-causeway-route-context]", we = "cw-interaction-controller[data-causeway-route-interactions]";
function me(e, t) {
  const a = [...e.querySelectorAll(k)].filter((r) => !r.parentElement?.closest(k));
  if (a.length === 0) return Object.freeze({ valid: !1, classification: "missing-context" });
  if (a.length !== 1) return Object.freeze({ valid: !1, classification: "duplicate-context" });
  const n = a[0];
  return n.getAttribute("logical-type") !== t.logicalTypeName || n.getAttribute("object-id") !== t.objectId ? Object.freeze({ valid: !1, classification: "identity" }) : [...n.querySelectorAll(we)].filter((r) => r.closest(k) === n).length !== 1 ? Object.freeze({ valid: !1, classification: "interactions" }) : Object.freeze({ valid: !0, context: n });
}
function j(e, t) {
  const a = e.querySelectorAll(t);
  return a.length === 1 ? a[0] : null;
}
function te(e) {
  const t = j(e, "cw-graphql-client[data-causeway-shell-client]"), a = j(e, "[data-causeway-router-view]"), n = j(e, "[data-causeway-route-loading]"), o = j(e, "[data-causeway-route-announcement]"), r = j(e, "cw-action-results[data-causeway-shell-result]");
  if (!t || !a || !n || !o || !r || !t.contains(a) || !t.contains(r))
    throw new Error("The authored Vue application shell is invalid.");
  return Object.freeze({ shell: e, client: t, route: a, loading: n, announcement: o, result: r });
}
const ae = /* @__PURE__ */ Symbol("causeway-vue-viewer"), D = Object.freeze({
  serviceLogicalTypeName: "causeway.security.LogoutMenu",
  actionId: "logout"
});
function ne(e) {
  return e?.serviceLogicalTypeName === D.serviceLogicalTypeName && e?.actionId === D.actionId;
}
function M(e) {
  let t = 0;
  for (const a of e.querySelectorAll("[data-causeway-service-action]"))
    ne({
      serviceLogicalTypeName: a.getAttribute("data-service-logical-type") ?? void 0,
      actionId: a.getAttribute("data-action-id") ?? ""
    }) && ((a.closest("[data-causeway-service-action-region]") ?? a).remove(), t += 1);
  return t;
}
class be extends Error {
  code;
  constructor(t, a) {
    super(a), this.name = "LocalResourceNavigationError", this.code = t;
  }
}
function Ee(e, t = {}) {
  const a = t.location ?? window.location, n = typeof e?.path == "string" ? e.path.trim() : "", o = e?.openUrlStrategy;
  if (!n || !["SAME_WINDOW", "NEW_WINDOW"].includes(o ?? ""))
    throw R("LOCAL_RESOURCE_INVALID", "The local-resource result is incomplete.");
  if (/^[a-z][a-z\d+.-]*:/i.test(n) || n.startsWith("//") || n.includes("\\") || /[\u0000-\u001f\u007f]/.test(n))
    throw R("LOCAL_RESOURCE_TARGET_UNSAFE", "The local-resource target is not an application-local path.");
  const r = new URL(a.href), u = _e(t.applicationBase ?? "/", r).pathname.replace(/\/+$/, "") || "/", l = n.startsWith("/") ? n : `/${n}`, h = u !== "/" && (l === u || l.startsWith(`${u}/`) || l.startsWith(`${u}?`) || l.startsWith(`${u}#`)) ? l : `${u === "/" ? "" : u}/${l.replace(/^\/+/, "")}`, v = new URL(h, r.origin);
  if (v.origin !== r.origin || v.username || v.password || u !== "/" && v.pathname !== u && !v.pathname.startsWith(`${u}/`))
    throw R("LOCAL_RESOURCE_TARGET_UNSAFE", "The local-resource target escapes the configured application boundary.");
  return Object.freeze({ url: v, openUrlStrategy: o });
}
function Te(e, t = {}) {
  const a = t.location ?? window.location, n = Ee(e, { location: a, applicationBase: t.applicationBase });
  if (n.openUrlStrategy === "SAME_WINDOW") {
    a.assign(n.url.href);
    return;
  }
  const r = (t.open ?? window.open.bind(window))(n.url.href, "_blank", "noopener,noreferrer");
  r && (r.opener = null);
}
function _e(e, t) {
  const a = String(e).trim() || "/";
  if (a.startsWith("//") || /[\u0000-\u001f\u007f]/.test(a))
    throw R("LOCAL_RESOURCE_BASE_INVALID", "The application-local resource base is invalid.");
  const n = new URL(a.endsWith("/") ? a : `${a}/`, t.origin);
  if (n.origin !== t.origin || n.username || n.password)
    throw R("LOCAL_RESOURCE_BASE_INVALID", "The application-local resource base must be same-origin.");
  return n;
}
function R(e, t) {
  return new be(e, t);
}
const F = 4096, oe = 4096, Se = "The requested application route is invalid.";
function b() {
  return new Error(Se);
}
function Ae(e) {
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
function q(e) {
  const t = String(e ?? "");
  if (!t || t.length > F || t === "." || t === ".." || /[\\/\u0000-\u001f\u007f-\u009f]/u.test(t) || Ae(t) || new TextEncoder().encode(t).length > F) throw b();
  let a;
  try {
    a = encodeURIComponent(t).replace(/[!'()*]/g, (n) => `%${n.charCodeAt(0).toString(16).toUpperCase()}`);
  } catch {
    throw b();
  }
  if (a.length > oe) throw b();
  return a;
}
function K(e) {
  if (!e || e.length > oe || !/^(?:[^%]|%[0-9A-F]{2})+$/u.test(e))
    throw b();
  let t;
  try {
    t = decodeURIComponent(e);
  } catch {
    throw b();
  }
  if (q(t) !== e) throw b();
  return t;
}
function U(e) {
  let t = String(e ?? "").trim();
  if (!t.startsWith("/") || t.startsWith("//") || /[?#\\\u0000-\u001f]/u.test(t))
    throw new Error("The Vue viewer base path is invalid.");
  for (; t.length > 1 && t.endsWith("/"); ) t = t.slice(0, -1);
  return t;
}
function Ce(e, t) {
  const a = U(e), n = `/object/${q(t?.logicalTypeName)}/${q(t?.id ?? t?.objectId)}`;
  return a === "/" ? n : `${a}${n}`;
}
function B(e) {
  return Ce("/", e);
}
function re(e, t = "/") {
  const a = String(e ?? ""), n = U(t), o = n === "/" ? "/object/" : `${n}/object/`;
  if (!a.startsWith(o)) throw b();
  const p = a.slice(o.length).split("/");
  if (p.length !== 2) throw b();
  return Object.freeze({
    logicalTypeName: K(p[0]),
    objectId: K(p[1])
  });
}
function Oe(e) {
  return B(e);
}
const Y = "causeway-navigation-request", G = "causeway-action-request", X = "causeway-action-result", Z = "causeway-object-context-state-change", H = "causeway-menu-bars-state-change";
function V() {
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
function _(e) {
  return Object.freeze({
    router: e.router,
    basePath: e.basePath,
    shell: e.state.shell,
    routeGeneration: e.state.routeGeneration
  });
}
function je(e) {
  if (e?.kind !== "object" || !e.value || typeof e.value != "object") return null;
  const t = e.value._meta, a = t?.logicalTypeName, n = t?.id;
  return typeof a != "string" || typeof n != "string" || !a || !n ? null : Object.freeze({ logicalTypeName: a, id: n, title: String(t?.title ?? n) });
}
function xe(e) {
  return [...e.querySelector("[data-causeway-router-view]")?.querySelectorAll("cw-action-results[data-causeway-page-result]") ?? []].filter((a) => a.isConnected);
}
function Le(e) {
  const t = e.querySelectorAll("cw-action-results[data-causeway-shell-result]");
  return t.length === 1 ? t[0] : null;
}
function $(e) {
  const t = xe(e);
  if (t.length > 1) throw new Error("The active Vue route has duplicate action-result outlets.");
  if (t.length === 1) return t[0];
  const a = Le(e);
  if (!a?.isConnected) throw new Error("The authored Vue shell result outlet is unavailable.");
  return a;
}
function Q(e, ...t) {
  const a = e;
  a.replacePresentation ? a.replacePresentation(...t) : e.replaceChildren(...t), e.hidden = t.length === 0;
}
function ie(e, t) {
  const a = t.result, n = document.createElement("h2");
  if (n.textContent = t.actionId ? `${t.actionId} result` : "Action result", a?.kind === "collection") {
    const r = document.createElement("cw-standalone-collection");
    r.setAttribute("data-testid", "causeway-standalone-action-result"), r.setAttribute("named", String(t.resultPresentation?.named ?? n.textContent)), r.result = a, Q(e, r);
    return;
  }
  const o = document.createElement("output");
  o.textContent = a?.kind === "scalar" ? String(a.value ?? "") : a?.kind === "void" ? "Completed" : "This result cannot be presented.", Q(e, n, o);
}
async function Ne(e, t) {
  const a = e.state.shell, o = a?.querySelector("[data-causeway-router-view]")?.querySelector("cw-object-context[data-causeway-route-context]"), r = o?.context;
  if (!o || !r?.refresh) return;
  const p = e.state.routeGeneration, u = (m) => {
    if (p !== e.state.routeGeneration) return l();
    const h = m.detail?.state;
    if (h?.status !== "terminal-error") {
      (h?.status === "ready" || h?.status === "partial-error") && l();
      return;
    }
    l(), (h.errors?.[0]?.extensions?.classification ?? h.errors?.[0]?.extensions?.code ?? h.error?.code) === "NOT_FOUND" && e.router.replace("/");
  }, l = () => o.removeEventListener(Z, u);
  o.addEventListener(Z, u), r.refresh(), ie($(a), t);
}
async function J(e, t) {
  const a = V();
  await e.policies.navigate?.(t, a, _(e)) === !0 && a.claim(), a.claimed || await e.router.push(B(t));
}
function ce(e, t) {
  const a = /* @__PURE__ */ new WeakMap(), n = /* @__PURE__ */ new WeakSet();
  let o = null, r = !0;
  const p = () => {
    const f = t.querySelector("[data-causeway-route-announcement]");
    f && (f.textContent = "Logout requires a host authentication integration."), t.dataset.causewayLogoutUnavailable = "true";
  }, u = (f, d) => {
    if (!r || d !== e.state.routeGeneration || !(f.target instanceof EventTarget)) return;
    const i = new CustomEvent(G, {
      bubbles: !0,
      composed: !0,
      cancelable: !0,
      detail: f.detail
    });
    n.add(i), f.target.dispatchEvent(i);
  }, l = (f) => {
    const d = f, i = d.detail;
    try {
      const g = $(t);
      i?.context && typeof i.context == "object" ? a.set(i.context, g) : o = g;
    } catch (g) {
      e.policies.error?.(g, _(e));
    }
    if (n.has(d)) return;
    const y = ne(i), s = e.policies.action;
    if (!s) {
      y && (d.preventDefault(), p());
      return;
    }
    const c = V(), w = e.state.routeGeneration;
    try {
      const g = s(i, c, _(e));
      if (g && typeof g.then == "function") {
        d.preventDefault(), Promise.resolve(g).then((E) => {
          E === !0 && c.claim(), !c.claimed && !y ? u(d, w) : c.claimed || p();
        }).catch((E) => e.policies.error?.(E, _(e)));
        return;
      }
      g === !0 && c.claim(), (c.claimed || y) && d.preventDefault(), y && !c.claimed && p();
    } catch (g) {
      d.preventDefault(), e.policies.error?.(g, _(e));
    }
  }, m = (f) => {
    const d = f, i = d.detail?.target;
    !i?.logicalTypeName || !(i.id ?? i.objectId) || (d.preventDefault(), J(e, i).catch((y) => e.policies.error?.(y, _(e))));
  }, h = (f) => {
    const d = f, i = d.detail ?? {};
    (async () => {
      const y = V();
      if (await e.policies.result?.(i, y, _(e)) === !0 && y.claim(), y.claimed) return;
      if (i.result?.kind === "local-resource") {
        Te(i.result.value, { applicationBase: e.applicationResourceBase }), d.target?.dismissResult?.();
        return;
      }
      const c = je(i.result);
      if (c) return J(e, c);
      const w = i.context && a.get(i.context) || o || $(t);
      i.result?.kind === "void" ? await Ne(e, i) : ie(w, i), d.target?.dismissResult?.();
    })().catch((y) => e.policies.error?.(y, _(e)));
  }, v = () => queueMicrotask(() => {
    r && M(t);
  });
  return M(t), t.addEventListener(G, l, { capture: !0 }), t.addEventListener(Y, m), t.addEventListener(X, h), t.addEventListener(H, v), () => {
    r = !1, t.removeEventListener(G, l, { capture: !0 }), t.removeEventListener(Y, m), t.removeEventListener(X, h), t.removeEventListener(H, v);
  };
}
const Re = /^[A-Za-z_][A-Za-z0-9_$-]*(?:\.[A-Za-z_][A-Za-z0-9_$-]*)*$/u;
function Pe(e) {
  return e ? e instanceof Map ? [...e.entries()] : Array.isArray(e) ? e : Object.entries(e) : [];
}
function Ie(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [a, n] of Pe(e)) {
    const o = String(a ?? "").trim();
    if (!Re.test(o)) throw new Error("A Vue page registration has an invalid logical type.");
    if (!n || typeof n != "object" && typeof n != "function")
      throw new Error(`The Vue page registration for ${o} is unsupported.`);
    if (t.has(o)) throw new Error(`The Vue page registration for ${o} is duplicated.`);
    t.set(o, n);
  }
  return t;
}
function Ue(e) {
  return typeof e == "function";
}
function Ye(e) {
  if (!e?.router) throw new Error("The Vue viewer requires the application router.");
  const t = String(e.endpoint ?? "").trim();
  if (!t) throw new Error("The Vue viewer GraphQL endpoint is required.");
  const a = { shell: null, routeGeneration: 0 };
  let n;
  return n = Object.freeze({
    plugin: {
      install(r) {
        r.provide(ae, n);
      }
    },
    router: e.router,
    endpoint: t,
    basePath: U(e.basePath ?? (e.router.options.history.base || "/")),
    applicationResourceBase: U(e.applicationResourceBase ?? "/"),
    pages: Ie(e.pages),
    policies: Object.freeze({ ...e.policies }),
    developmentDiagnostics: e.developmentDiagnostics ?? !0,
    state: a
  }), n;
}
function z() {
  const e = le(ae);
  if (!e) throw new Error("The Causeway Vue viewer plugin is not installed.");
  return e;
}
function Xe(e) {
  const t = z();
  let a = null;
  return W(() => {
    const n = e.value;
    if (!n) throw new Error("The authored Vue application shell is missing.");
    te(n), t.state.shell = n, a = ce(t, n);
  }), ee(() => {
    a?.(), a = null, t.state.shell === e.value && (t.state.shell = null);
  }), e;
}
function Ze(e, t) {
  const a = te(t);
  e.state.shell = t;
  const n = ce(e, t);
  return {
    landmarks: a,
    dispose() {
      n(), e.state.shell === t && (e.state.shell = null);
    }
  };
}
const ke = ["data-route-state"], Ge = /* @__PURE__ */ C({
  __name: "HomePage",
  setup(e) {
    const t = z(), a = L(null), n = L("loading"), o = L("Loading the application home page…");
    function r(p) {
      let u = null;
      return p.dispatchEvent(new CustomEvent("causeway-graphql-client-request", {
        bubbles: !0,
        composed: !0,
        detail: { provide(l) {
          u ??= l;
        } }
      })), u;
    }
    return W(async () => {
      const p = ++t.state.routeGeneration, u = a.value ? r(a.value) : null;
      if (!u) {
        n.value = "unsupported", o.value = "Choose an application action to begin.";
        return;
      }
      try {
        const l = await u.describeApplicationEntry();
        if (p !== t.state.routeGeneration) return;
        if (!l?.supported) {
          n.value = "unsupported", o.value = "Choose an application action to begin.";
          return;
        }
        const m = await u.readApplicationEntry({ description: l });
        if (p !== t.state.routeGeneration) return;
        const h = m?.data?.home, v = h?.object?._meta, f = v?.logicalTypeName ?? h?.logicalTypeName, d = v?.id, i = { claimed: !1, claim() {
          return this.claimed ? !1 : (this.claimed = !0, !0);
        } }, y = { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: p };
        if (await t.policies.home?.(m?.data, i, y) === !0 && i.claim(), p !== t.state.routeGeneration || i.claimed) return;
        if (h?.kind === "OBJECT" && f && d) {
          await t.router.replace(B({ logicalTypeName: f, id: d }));
          return;
        }
        n.value = m?.errors?.length ? "partial-error" : "ready", o.value = "Choose an application action to begin.";
      } catch (l) {
        if (p !== t.state.routeGeneration) return;
        t.policies.error?.(l, { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: p }), n.value = "partial-error", o.value = "The home page is unavailable; application menus remain available.";
      }
    }), (p, u) => (N(), I("section", {
      ref_key: "page",
      ref: a,
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": n.value,
      tabindex: "-1"
    }, [
      u[0] || (u[0] = A("h1", null, "Welcome", -1)),
      A("p", null, de(o.value), 1)
    ], 8, ke));
  }
}), qe = {
  class: "causeway-vue-route-page causeway-vue-route-object",
  "data-causeway-route-page": "",
  "data-page-kind": "generic",
  "data-route-state": "loading",
  tabindex: "-1",
  "aria-label": "Object page"
}, Ve = ["logical-type", "object-id"], $e = /* @__PURE__ */ C({
  __name: "GenericObjectPage",
  props: {
    logicalTypeName: {},
    objectId: {},
    routeKey: {}
  },
  setup(e) {
    return (t, a) => (N(), I("section", qe, [
      A("cw-object-context", {
        "data-causeway-route-context": "",
        "logical-type": e.logicalTypeName,
        "object-id": e.objectId
      }, [...a[0] || (a[0] = [
        A("cw-object", { editable: "" }, null, -1),
        A("cw-interaction-controller", { "data-causeway-route-interactions": "" }, null, -1)
      ])], 8, Ve)
    ]));
  }
}), We = ["data-route-key"], Be = {
  key: 0,
  class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
  "data-route-state": "terminal-error",
  tabindex: "-1",
  role: "alert"
}, ze = /* @__PURE__ */ C({
  __name: "ObjectRoutePage",
  setup(e) {
    const t = ve(), a = z(), n = L(null), o = L(null), r = P(() => re(t.path, "/")), p = C(() => () => S("section", {
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": "loading",
      tabindex: -1,
      role: "status"
    }, [S("h1", "Loading page…")])), u = C(() => () => S("section", {
      class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
      "data-route-state": "terminal-error",
      tabindex: -1,
      role: "alert"
    }, [S("h1", "Page unavailable"), S("p", "The application page could not be loaded.")])), l = P(() => Oe(r.value)), m = P(() => a.pages.get(r.value.logicalTypeName)), h = P(() => {
      const s = m.value;
      return s ? Ue(s) ? pe({
        loader: async () => {
          const c = await s();
          return "default" in c ? c.default : c;
        },
        delay: 0,
        timeout: 3e4,
        loadingComponent: p,
        errorComponent: u,
        onError(c, w, g) {
          a.policies.error?.(c, {
            router: a.router,
            basePath: a.basePath,
            shell: a.state.shell,
            routeGeneration: a.state.routeGeneration
          }), g();
        }
      }) : s : $e;
    });
    function v() {
      const s = a.state.shell;
      return {
        route: s?.querySelector("[data-causeway-router-view]") ?? null,
        loading: s?.querySelector("[data-causeway-route-loading]") ?? null,
        announcement: s?.querySelector("[data-causeway-route-announcement]") ?? null
      };
    }
    function f(s, c) {
      const w = v().announcement;
      w && (w.textContent = "", requestAnimationFrame(() => {
        c === a.state.routeGeneration && w.isConnected && (w.textContent = s);
      }));
    }
    function d(s) {
      const c = v();
      c.route?.setAttribute("aria-busy", String(s)), c.loading && (c.loading.hidden = !s);
    }
    async function i() {
      const s = a.state.routeGeneration;
      if (o.value = null, await ye(), s !== a.state.routeGeneration || !n.value) return;
      const c = me(n.value, r.value);
      c.valid || (o.value = c.classification ?? "invalid", d(!1), f("Page unavailable", s));
    }
    function y(s) {
      const c = a.state.routeGeneration, w = s, g = w.detail?.state, E = w.target?.closest("[data-causeway-route-page]");
      if (!g || !E || !n.value?.contains(E)) return;
      let T = String(g.status ?? "terminal-error");
      if (T === "terminal-error") {
        const O = g.errors?.[0]?.extensions?.classification ?? g.errors?.[0]?.extensions?.code ?? g.error?.code;
        (O === "NOT_FOUND" || O === "ACCESS_DENIED") && (T = "unavailable");
      }
      if (E.dataset.routeState = T, T === "ready" || T === "partial-error") {
        d(!1);
        const O = g.snapshot?.data?._meta?.title;
        O && (document.title = String(O));
        for (const ue of E.querySelectorAll("cw-collection:not([active])"))
          ue.activate?.();
        f(T === "ready" ? "Page ready" : "Page ready with partial information", c);
      } else (T === "terminal-error" || T === "unavailable") && (d(!1), f("Page unavailable", c));
    }
    return fe(() => t.fullPath, () => {
      a.state.routeGeneration += 1, d(!0), f("Loading page", a.state.routeGeneration), i();
    }, { immediate: !0 }), W(async () => {
      n.value?.addEventListener("causeway-object-context-state-change", y), await i();
      const s = n.value?.querySelector(
        "cw-object-context[data-causeway-route-context]"
      );
      s?.context?.state && s.dispatchEvent(new CustomEvent("causeway-object-context-state-change", {
        bubbles: !0,
        composed: !0,
        detail: { state: s.context.state, context: s.context }
      })), n.value?.querySelector("[data-causeway-route-page]")?.focus({ preventScroll: !0 });
    }), ee(() => n.value?.removeEventListener("causeway-object-context-state-change", y)), (s, c) => (N(), I("div", {
      ref_key: "host",
      ref: n,
      class: "causeway-vue-object-route",
      "data-route-key": l.value
    }, [
      o.value ? (N(), I("section", Be, [...c[0] || (c[0] = [
        A("h1", null, "Page unavailable", -1),
        A("p", null, "The application page has an invalid semantic context boundary.", -1)
      ])])) : (N(), ge(he(h.value), {
        key: l.value,
        "logical-type-name": r.value.logicalTypeName,
        "object-id": r.value.objectId,
        "route-key": l.value,
        onVnodeMounted: i,
        onVnodeUpdated: i
      }, null, 8, ["logical-type-name", "object-id", "route-key"]))
    ], 8, We));
  }
}), x = Object.freeze({
  home: "causeway-home",
  object: "causeway-object",
  invalid: "causeway-invalid-route",
  notFound: "causeway-not-found"
});
function se(e, t, a, n) {
  return C({
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
const De = se(
  "Invalid route",
  "The requested application route is invalid.",
  "invalid-route",
  "alert"
), Me = se(
  "Page unavailable",
  "The requested page is unavailable.",
  "not-found",
  "alert"
);
function He(e = {}) {
  return [
    { path: "/", name: x.home, component: e.homeComponent ?? Ge },
    {
      path: "/object/:logicalTypeName/:objectId",
      name: x.object,
      component: ze,
      beforeEnter(t) {
        try {
          return re(t.path, "/"), !0;
        } catch {
          return { name: x.invalid, replace: !0 };
        }
      }
    },
    {
      path: "/invalid-route",
      name: x.invalid,
      component: De
    },
    {
      path: "/:pathMatch(.*)*",
      name: x.notFound,
      component: e.notFoundComponent ?? Me
    }
  ];
}
export {
  G as ACTION_REQUEST_EVENT,
  X as ACTION_RESULT_EVENT,
  x as CAUSEWAY_ROUTE_NAMES,
  ae as CAUSEWAY_VIEWER_KEY,
  $e as CausewayGenericObjectPage,
  Ge as CausewayHomePage,
  De as CausewayInvalidRoutePage,
  Me as CausewayNotFoundPage,
  ze as CausewayObjectRoutePage,
  D as FRAMEWORK_LOGOUT_ACTION,
  Se as INVALID_ROUTE_MESSAGE,
  be as LocalResourceNavigationError,
  H as MENU_BARS_STATE_EVENT,
  Y as NAVIGATION_REQUEST_EVENT,
  Z as OBJECT_CONTEXT_STATE_EVENT,
  k as ROUTE_CONTEXT_SELECTOR,
  we as ROUTE_INTERACTIONS_SELECTOR,
  Ze as bindCausewayShell,
  Ce as canonicalObjectPath,
  Oe as canonicalRouteKey,
  B as canonicalRouterObjectPath,
  He as createCausewayRouteRecords,
  Ye as createCausewayVueViewer,
  K as decodeRouteSegment,
  q as encodeRouteSegment,
  ce as installSemanticBridge,
  ne as isFrameworkLogoutAction,
  Ue as isPageLoader,
  Te as navigateLocalResource,
  U as normalizeBasePath,
  Ie as normalizePageRegistry,
  re as parseCanonicalObjectPath,
  ie as presentSemanticResult,
  M as removeFrameworkLogoutMenuActions,
  Ee as resolveLocalResourceTarget,
  $ as resolveResultOutlet,
  Xe as useCausewayShell,
  z as useCausewayViewer,
  me as validateRouteBoundary,
  te as validateShellBoundary
};
//# sourceMappingURL=causeway-vue.js.map
