import { inject as le, onMounted as W, onBeforeUnmount as te, defineComponent as O, ref as L, openBlock as N, createElementBlock as I, createElementVNode as C, toDisplayString as de, computed as P, h as S, defineAsyncComponent as pe, watch as fe, createBlock as ge, resolveDynamicComponent as he, nextTick as ye } from "vue";
import { useRoute as ve } from "vue-router";
const k = "cw-object-context[data-causeway-route-context]", we = "cw-interaction-controller[data-causeway-route-interactions]";
function me(e, t) {
  const a = [...e.querySelectorAll(k)].filter((i) => !i.parentElement?.closest(k));
  if (a.length === 0) return Object.freeze({ valid: !1, classification: "missing-context" });
  if (a.length !== 1) return Object.freeze({ valid: !1, classification: "duplicate-context" });
  const n = a[0];
  return n.getAttribute("logical-type") !== t.logicalTypeName || n.getAttribute("object-id") !== t.objectId ? Object.freeze({ valid: !1, classification: "identity" }) : [...n.querySelectorAll(we)].filter((i) => i.closest(k) === n).length !== 1 ? Object.freeze({ valid: !1, classification: "interactions" }) : Object.freeze({ valid: !0, context: n });
}
function x(e, t) {
  const a = e.querySelectorAll(t);
  return a.length === 1 ? a[0] : null;
}
function ae(e) {
  const t = x(e, "cw-graphql-client[data-causeway-shell-client]"), a = x(e, "[data-causeway-router-view]"), n = x(e, "[data-causeway-route-loading]"), r = x(e, "[data-causeway-route-announcement]"), i = x(e, "cw-action-results[data-causeway-shell-result]");
  if (!t || !a || !n || !r || !i || !t.contains(a) || !t.contains(i))
    throw new Error("The authored Vue application shell is invalid.");
  return Object.freeze({ shell: e, client: t, route: a, loading: n, announcement: r, result: i });
}
const ne = /* @__PURE__ */ Symbol("causeway-vue-viewer"), M = Object.freeze({
  serviceLogicalTypeName: "causeway.security.LogoutMenu",
  actionId: "logout"
});
function G(e) {
  return e?.serviceLogicalTypeName === M.serviceLogicalTypeName && e?.actionId === M.actionId;
}
function F(e) {
  let t = 0;
  for (const a of e.querySelectorAll("[data-causeway-service-action]"))
    G({
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
  const a = t.location ?? window.location, n = typeof e?.path == "string" ? e.path.trim() : "", r = e?.openUrlStrategy;
  if (!n || !["SAME_WINDOW", "NEW_WINDOW"].includes(r ?? ""))
    throw R("LOCAL_RESOURCE_INVALID", "The local-resource result is incomplete.");
  if (/^[a-z][a-z\d+.-]*:/i.test(n) || n.startsWith("//") || n.includes("\\") || /[\u0000-\u001f\u007f]/.test(n))
    throw R("LOCAL_RESOURCE_TARGET_UNSAFE", "The local-resource target is not an application-local path.");
  const i = new URL(a.href), s = _e(t.applicationBase ?? "/", i).pathname.replace(/\/+$/, "") || "/", u = n.startsWith("/") ? n : `/${n}`, h = s !== "/" && (u === s || u.startsWith(`${s}/`) || u.startsWith(`${s}?`) || u.startsWith(`${s}#`)) ? u : `${s === "/" ? "" : s}/${u.replace(/^\/+/, "")}`, y = new URL(h, i.origin);
  if (y.origin !== i.origin || y.username || y.password || s !== "/" && y.pathname !== s && !y.pathname.startsWith(`${s}/`))
    throw R("LOCAL_RESOURCE_TARGET_UNSAFE", "The local-resource target escapes the configured application boundary.");
  return Object.freeze({ url: y, openUrlStrategy: r });
}
function Te(e, t = {}) {
  const a = t.location ?? window.location, n = Ee(e, { location: a, applicationBase: t.applicationBase });
  if (n.openUrlStrategy === "SAME_WINDOW") {
    a.assign(n.url.href);
    return;
  }
  const i = (t.open ?? window.open.bind(window))(n.url.href, "_blank", "noopener,noreferrer");
  i && (i.opener = null);
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
const K = 4096, oe = 4096, Ae = "The requested application route is invalid.";
function T() {
  return new Error(Ae);
}
function Se(e) {
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
function V(e) {
  const t = String(e ?? "");
  if (!t || t.length > K || t === "." || t === ".." || /[\\/\u0000-\u001f\u007f-\u009f]/u.test(t) || Se(t) || new TextEncoder().encode(t).length > K) throw T();
  let a;
  try {
    a = encodeURIComponent(t).replace(/[!'()*]/g, (n) => `%${n.charCodeAt(0).toString(16).toUpperCase()}`);
  } catch {
    throw T();
  }
  if (a.length > oe) throw T();
  return a;
}
function Y(e) {
  if (!e || e.length > oe || !/^(?:[^%]|%[0-9A-F]{2})+$/u.test(e))
    throw T();
  let t;
  try {
    t = decodeURIComponent(e);
  } catch {
    throw T();
  }
  if (V(t) !== e) throw T();
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
  const a = U(e), n = `/object/${V(t?.logicalTypeName)}/${V(t?.id ?? t?.objectId)}`;
  return a === "/" ? n : `${a}${n}`;
}
function z(e) {
  return Ce("/", e);
}
function re(e, t = "/") {
  const a = String(e ?? ""), n = U(t), r = n === "/" ? "/object/" : `${n}/object/`;
  if (!a.startsWith(r)) throw T();
  const d = a.slice(r.length).split("/");
  if (d.length !== 2) throw T();
  return Object.freeze({
    logicalTypeName: Y(d[0]),
    objectId: Y(d[1])
  });
}
function Oe(e) {
  return z(e);
}
const X = "causeway-navigation-request", q = "causeway-action-request", Z = "causeway-action-result", H = "causeway-object-context-state-change", Q = "causeway-menu-bars-state-change";
function $() {
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
function A(e) {
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
function Le(e) {
  const t = e.querySelectorAll("cw-action-results[data-causeway-shell-result]");
  return t.length === 1 ? t[0] : null;
}
function B(e) {
  const t = je(e);
  if (t.length > 1) throw new Error("The active Vue route has duplicate action-result outlets.");
  if (t.length === 1) return t[0];
  const a = Le(e);
  if (!a?.isConnected) throw new Error("The authored Vue shell result outlet is unavailable.");
  return a;
}
function J(e, ...t) {
  const a = e;
  a.replacePresentation ? a.replacePresentation(...t) : e.replaceChildren(...t), e.hidden = t.length === 0;
}
function ie(e, t) {
  const a = t.result, n = document.createElement("h2");
  if (n.textContent = t.actionId ? `${t.actionId} result` : "Action result", a?.kind === "collection") {
    const i = document.createElement("cw-standalone-collection");
    i.setAttribute("data-testid", "causeway-standalone-action-result"), i.setAttribute("named", String(t.resultPresentation?.named ?? n.textContent)), i.result = a, J(e, i);
    return;
  }
  const r = document.createElement("output");
  r.textContent = a?.kind === "scalar" ? String(a.value ?? "") : a?.kind === "void" ? "Completed" : "This result cannot be presented.", J(e, n, r);
}
async function Ne(e, t) {
  const a = e.state.shell, r = a?.querySelector("[data-causeway-router-view]")?.querySelector("cw-object-context[data-causeway-route-context]"), i = r?.context;
  if (!r || !i?.refresh) return;
  const d = e.state.routeGeneration, s = (w) => {
    if (d !== e.state.routeGeneration) return u();
    const h = w.detail?.state;
    if (h?.status !== "terminal-error") {
      (h?.status === "ready" || h?.status === "partial-error") && u();
      return;
    }
    u(), (h.errors?.[0]?.extensions?.classification ?? h.errors?.[0]?.extensions?.code ?? h.error?.code) === "NOT_FOUND" && e.router.replace("/");
  }, u = () => r.removeEventListener(H, s);
  r.addEventListener(H, s), i.refresh(), ie(B(a), t);
}
async function ee(e, t) {
  const a = $();
  await e.policies.navigate?.(t, a, A(e)) === !0 && a.claim(), a.claimed || await e.router.push(z(t));
}
function ce(e, t) {
  const a = /* @__PURE__ */ new WeakMap(), n = /* @__PURE__ */ new WeakSet(), r = t.querySelector("cw-menubars"), i = (l) => G(l);
  r && (r.excludeAction = i);
  let d = null, s = !0;
  const u = () => {
    const l = t.querySelector("[data-causeway-route-announcement]");
    l && (l.textContent = "Logout requires a host authentication integration."), t.dataset.causewayLogoutUnavailable = "true";
  }, w = (l, p) => {
    if (!s || p !== e.state.routeGeneration || !(l.target instanceof EventTarget)) return;
    const o = new CustomEvent(q, {
      bubbles: !0,
      composed: !0,
      cancelable: !0,
      detail: l.detail
    });
    n.add(o), l.target.dispatchEvent(o);
  }, h = (l) => {
    const p = l, o = p.detail;
    try {
      const f = B(t);
      o?.context && typeof o.context == "object" ? a.set(o.context, f) : d = f;
    } catch (f) {
      e.policies.error?.(f, A(e));
    }
    if (n.has(p)) return;
    const c = G(o), v = e.policies.action;
    if (!v) {
      c && (p.preventDefault(), u());
      return;
    }
    const g = $(), _ = e.state.routeGeneration;
    try {
      const f = v(o, g, A(e));
      if (f && typeof f.then == "function") {
        p.preventDefault(), Promise.resolve(f).then((E) => {
          E === !0 && g.claim(), !g.claimed && !c ? w(p, _) : g.claimed || u();
        }).catch((E) => e.policies.error?.(E, A(e)));
        return;
      }
      f === !0 && g.claim(), (g.claimed || c) && p.preventDefault(), c && !g.claimed && u();
    } catch (f) {
      p.preventDefault(), e.policies.error?.(f, A(e));
    }
  }, y = (l) => {
    const p = l, o = p.detail?.target;
    !o?.logicalTypeName || !(o.id ?? o.objectId) || (p.preventDefault(), ee(e, o).catch((c) => e.policies.error?.(c, A(e))));
  }, m = (l) => {
    const p = l, o = p.detail ?? {};
    (async () => {
      const c = $();
      if (await e.policies.result?.(o, c, A(e)) === !0 && c.claim(), c.claimed) return;
      if (o.result?.kind === "local-resource") {
        Te(o.result.value, { applicationBase: e.applicationResourceBase }), p.target?.dismissResult?.();
        return;
      }
      const g = xe(o.result);
      if (g) return ee(e, g);
      const _ = o.context && a.get(o.context) || d || B(t);
      o.result?.kind === "void" ? await Ne(e, o) : ie(_, o), p.target?.dismissResult?.();
    })().catch((c) => e.policies.error?.(c, A(e)));
  }, b = () => queueMicrotask(() => {
    s && F(t);
  });
  return F(t), t.addEventListener(q, h, { capture: !0 }), t.addEventListener(X, y), t.addEventListener(Z, m), t.addEventListener(Q, b), () => {
    s = !1, r?.excludeAction === i && (r.excludeAction = void 0), t.removeEventListener(q, h, { capture: !0 }), t.removeEventListener(X, y), t.removeEventListener(Z, m), t.removeEventListener(Q, b);
  };
}
const Re = /^[A-Za-z_][A-Za-z0-9_$-]*(?:\.[A-Za-z_][A-Za-z0-9_$-]*)*$/u;
function Pe(e) {
  return e ? e instanceof Map ? [...e.entries()] : Array.isArray(e) ? e : Object.entries(e) : [];
}
function Ie(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [a, n] of Pe(e)) {
    const r = String(a ?? "").trim();
    if (!Re.test(r)) throw new Error("A Vue page registration has an invalid logical type.");
    if (!n || typeof n != "object" && typeof n != "function")
      throw new Error(`The Vue page registration for ${r} is unsupported.`);
    if (t.has(r)) throw new Error(`The Vue page registration for ${r} is duplicated.`);
    t.set(r, n);
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
      install(i) {
        i.provide(ne, n);
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
function D() {
  const e = le(ne);
  if (!e) throw new Error("The Causeway Vue viewer plugin is not installed.");
  return e;
}
function Xe(e) {
  const t = D();
  let a = null;
  return W(() => {
    const n = e.value;
    if (!n) throw new Error("The authored Vue application shell is missing.");
    ae(n), t.state.shell = n, a = ce(t, n);
  }), te(() => {
    a?.(), a = null, t.state.shell === e.value && (t.state.shell = null);
  }), e;
}
function Ze(e, t) {
  const a = ae(t);
  e.state.shell = t;
  const n = ce(e, t);
  return {
    landmarks: a,
    dispose() {
      n(), e.state.shell === t && (e.state.shell = null);
    }
  };
}
const ke = ["data-route-state"], qe = /* @__PURE__ */ O({
  __name: "HomePage",
  setup(e) {
    const t = D(), a = L(null), n = L("loading"), r = L("Loading the application home page…");
    function i(d) {
      let s = null;
      return d.dispatchEvent(new CustomEvent("causeway-graphql-client-request", {
        bubbles: !0,
        composed: !0,
        detail: { provide(u) {
          s ??= u;
        } }
      })), s;
    }
    return W(async () => {
      const d = ++t.state.routeGeneration, s = a.value ? i(a.value) : null;
      if (!s) {
        n.value = "unsupported", r.value = "Choose an application action to begin.";
        return;
      }
      try {
        const u = await s.describeApplicationEntry();
        if (d !== t.state.routeGeneration) return;
        if (!u?.supported) {
          n.value = "unsupported", r.value = "Choose an application action to begin.";
          return;
        }
        const w = await s.readApplicationEntry({ description: u });
        if (d !== t.state.routeGeneration) return;
        const h = w?.data?.home, y = h?.object?._meta, m = y?.logicalTypeName ?? h?.logicalTypeName, b = y?.id, l = { claimed: !1, claim() {
          return this.claimed ? !1 : (this.claimed = !0, !0);
        } }, p = { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: d };
        if (await t.policies.home?.(w?.data, l, p) === !0 && l.claim(), d !== t.state.routeGeneration || l.claimed) return;
        if (h?.kind === "OBJECT" && m && b) {
          await t.router.replace(z({ logicalTypeName: m, id: b }));
          return;
        }
        n.value = w?.errors?.length ? "partial-error" : "ready", r.value = "Choose an application action to begin.";
      } catch (u) {
        if (d !== t.state.routeGeneration) return;
        t.policies.error?.(u, { router: t.router, basePath: t.basePath, shell: t.state.shell, routeGeneration: d }), n.value = "partial-error", r.value = "The home page is unavailable; application menus remain available.";
      }
    }), (d, s) => (N(), I("section", {
      ref_key: "page",
      ref: a,
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": n.value,
      tabindex: "-1"
    }, [
      s[0] || (s[0] = C("h1", null, "Welcome", -1)),
      C("p", null, de(r.value), 1)
    ], 8, ke));
  }
}), Ge = {
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
    return (t, a) => (N(), I("section", Ge, [
      C("cw-object-context", {
        "data-causeway-route-context": "",
        "logical-type": e.logicalTypeName,
        "object-id": e.objectId
      }, [...a[0] || (a[0] = [
        C("cw-object", { editable: "" }, null, -1),
        C("cw-interaction-controller", { "data-causeway-route-interactions": "" }, null, -1)
      ])], 8, Ve)
    ]));
  }
}), Be = ["data-route-key"], We = {
  key: 0,
  class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
  "data-route-state": "terminal-error",
  tabindex: "-1",
  role: "alert"
}, ze = /* @__PURE__ */ O({
  __name: "ObjectRoutePage",
  setup(e) {
    const t = ve(), a = D(), n = L(null), r = L(null), i = P(() => re(t.path, "/")), d = O(() => () => S("section", {
      class: "causeway-vue-route-page causeway-vue-status",
      "data-route-state": "loading",
      tabindex: -1,
      role: "status"
    }, [S("h1", "Loading page…")])), s = O(() => () => S("section", {
      class: "causeway-vue-route-page causeway-vue-status causeway-vue-status-danger",
      "data-route-state": "terminal-error",
      tabindex: -1,
      role: "alert"
    }, [S("h1", "Page unavailable"), S("p", "The application page could not be loaded.")])), u = P(() => Oe(i.value)), w = P(() => a.pages.get(i.value.logicalTypeName)), h = P(() => {
      const o = w.value;
      return o ? Ue(o) ? pe({
        loader: async () => {
          const c = await o();
          return "default" in c ? c.default : c;
        },
        delay: 0,
        timeout: 3e4,
        loadingComponent: d,
        errorComponent: s,
        onError(c, v, g) {
          a.policies.error?.(c, {
            router: a.router,
            basePath: a.basePath,
            shell: a.state.shell,
            routeGeneration: a.state.routeGeneration
          }), g();
        }
      }) : o : $e;
    });
    function y() {
      const o = a.state.shell;
      return {
        route: o?.querySelector("[data-causeway-router-view]") ?? null,
        loading: o?.querySelector("[data-causeway-route-loading]") ?? null,
        announcement: o?.querySelector("[data-causeway-route-announcement]") ?? null
      };
    }
    function m(o, c) {
      const v = y().announcement;
      v && (v.textContent = "", requestAnimationFrame(() => {
        c === a.state.routeGeneration && v.isConnected && (v.textContent = o);
      }));
    }
    function b(o) {
      const c = y();
      c.route?.setAttribute("aria-busy", String(o)), c.loading && (c.loading.hidden = !o);
    }
    async function l() {
      const o = a.state.routeGeneration;
      if (r.value = null, await ye(), o !== a.state.routeGeneration || !n.value) return;
      const c = me(n.value, i.value);
      c.valid || (r.value = c.classification ?? "invalid", b(!1), m("Page unavailable", o));
    }
    function p(o) {
      const c = a.state.routeGeneration, v = o, g = v.detail?.state, _ = v.target?.closest("[data-causeway-route-page]");
      if (!g || !_ || !n.value?.contains(_)) return;
      let f = String(g.status ?? "terminal-error");
      if (f === "terminal-error") {
        const E = g.errors?.[0]?.extensions?.classification ?? g.errors?.[0]?.extensions?.code ?? g.error?.code;
        (E === "NOT_FOUND" || E === "ACCESS_DENIED") && (f = "unavailable");
      }
      if (_.dataset.routeState = f, f === "ready" || f === "partial-error") {
        b(!1);
        const E = g.snapshot?.data?._meta?.title;
        E && (document.title = String(E));
        for (const ue of _.querySelectorAll("cw-collection:not([active])"))
          ue.activate?.();
        m(f === "ready" ? "Page ready" : "Page ready with partial information", c);
      } else (f === "terminal-error" || f === "unavailable") && (b(!1), m("Page unavailable", c));
    }
    return fe(() => t.fullPath, () => {
      a.state.routeGeneration += 1, b(!0), m("Loading page", a.state.routeGeneration), l();
    }, { immediate: !0 }), W(async () => {
      n.value?.addEventListener("causeway-object-context-state-change", p), await l();
      const o = n.value?.querySelector(
        "cw-object-context[data-causeway-route-context]"
      );
      o?.context?.state && o.dispatchEvent(new CustomEvent("causeway-object-context-state-change", {
        bubbles: !0,
        composed: !0,
        detail: { state: o.context.state, context: o.context }
      })), n.value?.querySelector("[data-causeway-route-page]")?.focus({ preventScroll: !0 });
    }), te(() => n.value?.removeEventListener("causeway-object-context-state-change", p)), (o, c) => (N(), I("div", {
      ref_key: "host",
      ref: n,
      class: "causeway-vue-object-route",
      "data-route-key": u.value
    }, [
      r.value ? (N(), I("section", We, [...c[0] || (c[0] = [
        C("h1", null, "Page unavailable", -1),
        C("p", null, "The application page has an invalid semantic context boundary.", -1)
      ])])) : (N(), ge(he(h.value), {
        key: u.value,
        "logical-type-name": i.value.logicalTypeName,
        "object-id": i.value.objectId,
        "route-key": u.value,
        onVnodeMounted: l,
        onVnodeUpdated: l
      }, null, 8, ["logical-type-name", "object-id", "route-key"]))
    ], 8, Be));
  }
}), j = Object.freeze({
  home: "causeway-home",
  object: "causeway-object",
  invalid: "causeway-invalid-route",
  notFound: "causeway-not-found"
});
function se(e, t, a, n) {
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
    { path: "/", name: j.home, component: e.homeComponent ?? qe },
    {
      path: "/object/:logicalTypeName/:objectId",
      name: j.object,
      component: ze,
      beforeEnter(t) {
        try {
          return re(t.path, "/"), !0;
        } catch {
          return { name: j.invalid, replace: !0 };
        }
      }
    },
    {
      path: "/invalid-route",
      name: j.invalid,
      component: De
    },
    {
      path: "/:pathMatch(.*)*",
      name: j.notFound,
      component: e.notFoundComponent ?? Me
    }
  ];
}
export {
  q as ACTION_REQUEST_EVENT,
  Z as ACTION_RESULT_EVENT,
  j as CAUSEWAY_ROUTE_NAMES,
  ne as CAUSEWAY_VIEWER_KEY,
  $e as CausewayGenericObjectPage,
  qe as CausewayHomePage,
  De as CausewayInvalidRoutePage,
  Me as CausewayNotFoundPage,
  ze as CausewayObjectRoutePage,
  M as FRAMEWORK_LOGOUT_ACTION,
  Ae as INVALID_ROUTE_MESSAGE,
  be as LocalResourceNavigationError,
  Q as MENU_BARS_STATE_EVENT,
  X as NAVIGATION_REQUEST_EVENT,
  H as OBJECT_CONTEXT_STATE_EVENT,
  k as ROUTE_CONTEXT_SELECTOR,
  we as ROUTE_INTERACTIONS_SELECTOR,
  Ze as bindCausewayShell,
  Ce as canonicalObjectPath,
  Oe as canonicalRouteKey,
  z as canonicalRouterObjectPath,
  He as createCausewayRouteRecords,
  Ye as createCausewayVueViewer,
  Y as decodeRouteSegment,
  V as encodeRouteSegment,
  ce as installSemanticBridge,
  G as isFrameworkLogoutAction,
  Ue as isPageLoader,
  Te as navigateLocalResource,
  U as normalizeBasePath,
  Ie as normalizePageRegistry,
  re as parseCanonicalObjectPath,
  ie as presentSemanticResult,
  F as removeFrameworkLogoutMenuActions,
  Ee as resolveLocalResourceTarget,
  B as resolveResultOutlet,
  Xe as useCausewayShell,
  D as useCausewayViewer,
  me as validateRouteBoundary,
  ae as validateShellBoundary
};
//# sourceMappingURL=causeway-vue.js.map
