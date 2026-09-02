/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import {
  ACTION_REQUEST_EVENT,
  ACTION_RESULT_EVENT,
  ACTION_RESULTS_DISMISS_REQUEST_EVENT,
  COMPONENT_STATE_EVENT,
  MENU_BARS_STATE_EVENT,
  NAVIGATION_REQUEST_EVENT,
  OBJECT_CONTEXT_STATE_EVENT,
  requestGraphQLClient
} from '../causeway-webcomponents/context-events.mjs';
import {CausewayGraphQLClientElement} from '../causeway-webcomponents/graphql-client-element.mjs';
import {createFetchGraphQLExecutor} from '../causeway-webcomponents/graphql-executor.mjs';
import {configureCausewayReferenceWidgets} from '../causeway-webcomponents/reference-widget.mjs';
import {
  CAUSEWAY_GRID_WIDGET_POLICY_EVENT,
  configureCausewayGridWidgets
} from '../causeway-webcomponents/grid-widget.mjs';
import {
  CAUSEWAY_MENUBAR_WIDGET_POLICY_EVENT,
  configureCausewayMenubarWidgets
} from '../causeway-webcomponents/menubar-widget.mjs';
import {defineCausewayWebComponents} from '../causeway-webcomponents/register.mjs';
import {
  applyStandaloneCollectionPresentation,
  standaloneCollectionPresentation
} from '../causeway-webcomponents/standalone-collection-presentation.mjs';
import {
  applyAuthenticationMenuPolicy,
  csrfHeaders,
  isExcludedAction,
  isUnsafeMethod,
  loginDestination,
  readAuthenticationMetadata
} from './authentication-policy.mjs';
import {
  canonicalObjectPath,
  homeObjectIdentity,
  resultObjectIdentity
} from './route-policy.mjs';

if (!globalThis.customElements?.get('cw-graphql-client')) {
  globalThis.customElements?.define('cw-graphql-client', CausewayGraphQLClientElement);
}
const shell = document.querySelector('cw-graphql-client');
const routeRegion = document.querySelector('#causeway-route');
const announcement = document.querySelector('#causeway-route-announcement');
const resultRegion = document.querySelector('#causeway-result');
const basePath = document.documentElement.dataset.causewayHtmxBase;
const referenceWidgetMode = document.documentElement.dataset.causewayReferenceWidgets;
const collectionGridMode = document.documentElement.dataset.causewayCollectionGrid;
const collectionGridModuleUrl = document.documentElement.dataset.causewayGridModuleUrl;
const applicationMenubarMode = document.documentElement.dataset.causewayApplicationMenubar;
const applicationMenubarModuleUrl = document.documentElement.dataset.causewayApplicationMenubarUrl;
const resourcePageMode = document.documentElement.dataset.causewayResourcePageMode;
const authentication = readAuthenticationMetadata(document);
if (shell && authentication) {
  const executor = createFetchGraphQLExecutor({
    endpoint: shell.endpoint,
    headers: () => csrfHeaders(authentication)
  });
  shell.executor = async request => {
    try {
      return await executor(request);
    } catch (error) {
      if (error?.status === 401) {
        redirectToLogin();
      }
      throw error;
    }
  };
}
configureCausewayReferenceWidgets({
  enabled: referenceWidgetMode === 'vaadin',
  minimumSearchLength: Number(document.documentElement.dataset.causewayReferenceMinimumSearchLength),
  maximumResults: Number(document.documentElement.dataset.causewayReferenceMaximumResults)
});
document.addEventListener(CAUSEWAY_GRID_WIDGET_POLICY_EVENT, event => {
  const detail = event.detail ?? {};
  document.documentElement.dataset.causewayGridFamily = detail.reason === 'failure'
    ? 'failed'
    : collectionGridMode === 'vaadin' ? 'healthy' : 'native';
  document.documentElement.dataset.causewayGridPolicyRevision = String(detail.revision ?? 0);
  document.documentElement.dataset.causewayGridFailurePhase = detail.reason === 'failure' ? String(detail.phase ?? '') : '';
  document.documentElement.dataset.causewayGridFailureClassification = detail.reason === 'failure'
    ? String(detail.classification ?? '')
    : '';
});
configureCausewayGridWidgets({
  enabled: collectionGridMode === 'vaadin',
  moduleUrl: collectionGridModuleUrl
});
document.addEventListener(CAUSEWAY_MENUBAR_WIDGET_POLICY_EVENT, event => {
  const detail = event.detail ?? {};
  document.documentElement.dataset.causewayMenubarFamily = detail.reason === 'failure'
    ? 'failed'
    : applicationMenubarMode === 'vaadin' ? 'healthy' : 'native';
  document.documentElement.dataset.causewayMenubarPolicyRevision = String(detail.revision ?? 0);
  document.documentElement.dataset.causewayMenubarFailurePhase = detail.reason === 'failure' ? String(detail.phase ?? '') : '';
  document.documentElement.dataset.causewayMenubarFailureClassification = detail.reason === 'failure'
    ? String(detail.classification ?? '')
    : '';
});
configureCausewayMenubarWidgets({
  enabled: applicationMenubarMode === 'vaadin',
  moduleUrl: applicationMenubarModuleUrl,
  excludeAction: detail => Boolean(authentication && isExcludedAction(authentication, detail))
});
let activeRequest = null;
let navigationGeneration = 0;
let pendingVoidRefreshGeneration = null;
const resolvingHomeLandings = new WeakSet();
const actionResultDestinations = new WeakMap();
const collectionPresentationCache = new Map();
let unscopedActionResultDestination = null;

defineCausewayWebComponents();
globalThis.causewayActionResultPresentationResolver = resolveCollectionPresentation;

async function resolveCollectionPresentation({logicalTypeName} = {}) {
  const type = String(logicalTypeName ?? '').trim();
  if (!/^[A-Za-z_][A-Za-z0-9_$-]*(?:\.[A-Za-z_][A-Za-z0-9_$-]*)*$/.test(type)) return null;
  if (resourcePageMode !== 'reload' && collectionPresentationCache.has(type)) {
    return collectionPresentationCache.get(type);
  }
  const pending = fetch(`${basePath}/_collection-presentations/${encodeURIComponent(type)}`, {
    headers: {Accept: 'text/html'}
  }).then(async response => {
    if (response.status === 404) return null;
    if (!response.ok) throw new Error(`Collection presentation lookup failed (${response.status}).`);
    return parseCollectionPresentation(await response.text());
  });
  if (resourcePageMode !== 'reload') collectionPresentationCache.set(type, pending);
  try {
    return await pending;
  } catch (error) {
    if (resourcePageMode !== 'reload') collectionPresentationCache.delete(type);
    document.documentElement.dataset.causewayCollectionPresentationError = 'resolution';
    throw error;
  }
}

function parseCollectionPresentation(html) {
  const template = document.createElement('template');
  template.innerHTML = String(html ?? '');
  const roots = [...template.content.children];
  if (roots.length !== 1 || roots[0].localName !== 'cw-standalone-collection') {
    throw new Error('A collection presentation requires one standalone collection root.');
  }
  const root = roots[0];
  const supportedAttributes = new Set([
    'named', 'described-as', 'description-as', 'resizable-columns', 'reorderable-columns'
  ]);
  if ([...root.attributes].some(attribute => !supportedAttributes.has(attribute.name))
      || [...root.children].some(child => child.localName !== 'cw-collection-column')
      || root.querySelector('script,style,link,iframe,object,embed')) {
    throw new Error('A collection presentation contains unsupported markup.');
  }
  document.documentElement.removeAttribute('data-causeway-collection-presentation-error');
  return standaloneCollectionPresentation(root);
}

function redirectToLogin() {
  if (authentication) {
    activeRequest?.abort?.();
    globalThis.location.assign(loginDestination(authentication, globalThis.location));
  }
}

function announce(message) {
  if (announcement) {
    announcement.textContent = '';
    globalThis.requestAnimationFrame?.(() => {
      announcement.textContent = message;
    });
  }
}

function activePageResultOutlet() {
  const outlets = [...(routeRegion?.querySelectorAll?.('cw-action-results') ?? [])]
    .filter(outlet => outlet.isConnected && outlet.closest?.('[data-route-state]'));
  if (outlets.length === 1) {
    routeRegion?.removeAttribute('data-causeway-action-results-error');
    return outlets[0];
  }
  if (outlets.length > 1) routeRegion?.setAttribute('data-causeway-action-results-error', 'duplicate');
  else routeRegion?.removeAttribute('data-causeway-action-results-error');
  return null;
}

function snapshotActionResultDestination(detail, source = null) {
  const snapshot = Object.freeze({
    outlet: activePageResultOutlet(),
    origin: actionResultOrigin(source),
    routeGeneration: navigationGeneration
  });
  if (detail?.context && typeof detail.context === 'object') actionResultDestinations.set(detail.context, snapshot);
  else unscopedActionResultDestination = snapshot;
}

function resultDestinationSnapshot(detail) {
  return detail?.context && typeof detail.context === 'object'
    ? actionResultDestinations.get(detail.context)
    : unscopedActionResultDestination;
}

function resultDestination(detail) {
  const snapshot = resultDestinationSnapshot(detail);
  if (snapshot?.outlet?.isConnected
      && snapshot.routeGeneration === navigationGeneration
      && routeRegion?.contains(snapshot.outlet)) {
    return snapshot.outlet;
  }
  return resultRegion;
}

function resultOrigin(detail) {
  const origin = resultDestinationSnapshot(detail)?.origin;
  return origin?.isConnected && typeof origin.focus === 'function' ? origin : null;
}

function actionResultOrigin(source) {
  if (!source) return null;
  if (source.localName === 'cw-action') {
    return source.querySelector?.('[data-causeway-action-control]') ?? null;
  }
  if (source.matches?.('vaadin-menu-bar-item[data-causeway-key]')) {
    const [, role, menuIndex] = String(source.getAttribute('data-causeway-key') ?? '').split(':');
    const controls = document.querySelectorAll?.(`cw-menubar-${role} cw-menubar-control vaadin-menu-bar-button`) ?? [];
    const stableMenuOrigin = controls[Number(menuIndex)];
    if (stableMenuOrigin && typeof stableMenuOrigin.focus === 'function') return stableMenuOrigin;
  }
  if (source.matches?.('[data-service-logical-type][data-action-id]')) {
    const panelId = source.closest?.('[data-causeway-menu-panel]')?.id;
    const stableMenuOrigin = panelId
      ? document.querySelector?.(`[data-causeway-menu-disclosure][aria-controls="${CSS.escape(panelId)}"]`)
      : null;
    if (stableMenuOrigin && typeof stableMenuOrigin.focus === 'function') return stableMenuOrigin;
  }
  if (source.hasAttribute?.('data-causeway-action-control') || source.matches?.('button,[href],[tabindex]')) {
    return typeof source.focus === 'function' ? source : null;
  }
  return source.closest?.('cw-action')?.querySelector?.('[data-causeway-action-control]') ?? null;
}

function replaceResultPresentation(destination, ...nodes) {
  if (!destination) return;
  if (typeof destination.replacePresentation === 'function') destination.replacePresentation(...nodes);
  else destination.replaceChildren(...nodes);
  destination.hidden = nodes.filter(Boolean).length === 0;
}

function revealResultPresentation(destination) {
  if ((destination?.presentationStyle ?? 'INLINE') !== 'INLINE') return;
  globalThis.requestAnimationFrame?.(() => {
    if (!destination?.isConnected || destination.hidden || destination.children.length === 0) return;
    const reducedMotion = globalThis.matchMedia?.('(prefers-reduced-motion: reduce)').matches === true;
    destination.scrollIntoView?.({block: 'start', behavior: reducedMotion ? 'auto' : 'smooth'});
  });
}

function preserveRouteResultInShell() {
  const outlet = activePageResultOutlet();
  const nodes = outlet?.presentationNodes ?? [...(outlet?.children ?? [])];
  if (!outlet || !resultRegion || nodes.length === 0) return;
  resultRegion.setPresentationContext?.(outlet.presentationContext ?? {});
  replaceResultPresentation(resultRegion, ...nodes);
  replaceResultPresentation(outlet);
}

function rehomePreservedShellResult() {
  const outlet = activePageResultOutlet();
  const nodes = resultRegion?.presentationNodes ?? [...(resultRegion?.children ?? [])];
  if (!outlet || !resultRegion || nodes.length === 0) return;
  outlet.setPresentationContext?.(resultRegion.presentationContext ?? {});
  replaceResultPresentation(outlet, ...nodes);
  replaceResultPresentation(resultRegion);
}

function collapseNarrowBars() {
  if (!globalThis.matchMedia?.('(max-width: 48rem)').matches) {
    return;
  }
  for (const disclosure of document.querySelectorAll('.causeway-shell-header .causeway-menubar-bar-disclosure[aria-expanded="true"]')) {
    disclosure.click();
  }
}

function setBusy(busy) {
  routeRegion?.setAttribute('aria-busy', String(busy));
  document.body.dataset.routeLoading = String(busy);
}

function activateRouteCollections() {
  for (const collection of routeRegion?.querySelectorAll('cw-collection:not([active])') ?? []) {
    collection.activate?.();
  }
}

async function navigate(path, {replace = false, preserveResult = false, recoverMissingAfterVoid = false} = {}) {
  pendingVoidRefreshGeneration = null;
  const policy = globalThis.causewayHtmxPolicy;
  if (typeof policy?.navigate === 'function' && await policy.navigate({path, replace}) === true) {
    return;
  }
  if (!globalThis.htmx || !routeRegion) {
    globalThis.location.assign(path);
    return;
  }
  const generation = ++navigationGeneration;
  pendingVoidRefreshGeneration = recoverMissingAfterVoid ? generation : null;
  routeRegion.dataset.navigationGeneration = String(generation);
  for (const disclosure of document.querySelectorAll('[data-causeway-menu-disclosure][aria-expanded="true"]')) {
    disclosure.click();
  }
  if (!preserveResult) {
    replaceResultPresentation(resultRegion);
  } else {
    preserveRouteResultInShell();
  }
  setBusy(true);
  try {
    await globalThis.htmx.ajax('GET', path, {
      target: routeRegion,
      swap: 'innerHTML',
      push: replace ? undefined : path,
      replace: replace ? path : undefined,
      headers: {'X-Causeway-Route-Generation': String(generation)}
    });
  } catch {
    if (generation === navigationGeneration) {
      renderRouteFailure('The requested page could not be loaded.');
    }
  } finally {
    if (generation === navigationGeneration) {
      setBusy(false);
    }
  }
}

function renderRouteFailure(message) {
  if (!routeRegion) {
    return;
  }
  routeRegion.replaceChildren(statusCard('Page unavailable', message, 'danger'));
  routeRegion.querySelector('[tabindex="-1"]')?.focus();
  announce(message);
}

function statusCard(title, message, tone = 'info') {
  const section = document.createElement('section');
  section.className = `causeway-route-page causeway-route-error causeway-status-${tone}`;
  section.dataset.routeState = 'terminal-error';
  section.tabIndex = -1;
  const card = document.createElement('div');
  card.className = 'causeway-status-card';
  const heading = document.createElement('h1');
  heading.textContent = title;
  const paragraph = document.createElement('p');
  paragraph.textContent = message;
  card.append(heading, paragraph);
  section.append(card);
  return section;
}

function presentResult(detail) {
  const policy = globalThis.causewayHtmxPolicy;
  if (typeof policy?.handleResult === 'function' && policy.handleResult(detail) === true) {
    return;
  }
  const identity = resultObjectIdentity(detail?.result);
  if (identity) {
    navigate(canonicalObjectPath(basePath, identity));
    return;
  }
  const destination = resultDestination(detail);
  if (!destination) return;
  replaceResultPresentation(destination);
  destination.setPresentationContext?.({origin: resultOrigin(detail)});
  const heading = document.createElement('h2');
  heading.textContent = detail?.actionId ? `${detail.actionId} result` : 'Action result';
  const output = document.createElement('output');
  const result = detail?.result;
  if (result?.kind === 'collection') {
    const count = Array.isArray(result.value) ? result.value.length : 0;
    const collection = document.createElement('cw-standalone-collection');
    applyStandaloneCollectionPresentation(collection, detail?.resultPresentation);
    if (!collection.named) collection.named = heading.textContent;
    collection.setAttribute('data-testid', 'causeway-standalone-action-result');
    replaceResultPresentation(destination, resultDismissButton(destination, detail), collection);
    collection.result = result;
    output.textContent = `${count} result${count === 1 ? '' : 's'}`;
  } else if (result?.kind === 'scalar') {
    output.textContent = result.value == null ? '' : String(result.value);
    replaceResultPresentation(destination, resultDismissButton(destination, detail), heading, output);
  } else {
    output.textContent = 'Completed';
    replaceResultPresentation(destination, resultDismissButton(destination, detail), heading, output);
    const context = routeRegion?.querySelector('cw-object-context')?.context;
    if (context) {
      globalThis.setTimeout(() => navigate(
        globalThis.location.pathname + globalThis.location.search,
        {replace: true, preserveResult: true, recoverMissingAfterVoid: result?.kind === 'void'}
      ), 0);
    }
  }
  destination.hidden = false;
  revealResultPresentation(destination);
  announce(`${collectionHeading(detail, heading)}: ${output.textContent}`);
}

function resultDismissButton(destination, detail) {
  const button = document.createElement('button');
  button.type = 'button';
  button.className = 'causeway-button causeway-button-secondary causeway-result-dismiss';
  button.setAttribute('data-causeway-result-dismiss', '');
  button.setAttribute('aria-label', 'Dismiss action result');
  button.textContent = 'Dismiss';
  button.addEventListener('click', () => {
    if (typeof destination.requestDismiss === 'function') {
      destination.requestDismiss('control');
      return;
    }
    replaceResultPresentation(destination);
    const actionId = String(detail?.actionId ?? '').trim();
    if (actionId) {
      routeRegion?.querySelector(`cw-action[id="${CSS.escape(actionId)}"] [data-causeway-action-control]`)?.focus?.();
    }
  });
  return button;
}

function collectionHeading(detail, fallbackHeading) {
  return detail?.resultPresentation?.named || fallbackHeading.textContent;
}

async function resolveHome() {
  const landing = routeRegion?.querySelector('[data-route-state="landing"]');
  if (!landing || !shell || resolvingHomeLandings.has(landing)) {
    return;
  }
  const client = requestGraphQLClient(shell);
  if (!client) {
    return;
  }
  resolvingHomeLandings.add(landing);
  try {
    const description = await client.describeApplicationEntry();
    if (!isCurrentLanding(landing)) {
      return;
    }
    if (!description.supported) {
      landing.dataset.routeState = 'unsupported';
      landing.querySelector('[data-causeway-home-message]').textContent = 'Choose an application action to begin.';
      return;
    }
    const response = await client.readApplicationEntry({description});
    if (!isCurrentLanding(landing)) {
      return;
    }
    const identity = homeObjectIdentity(response.data);
    const policy = globalThis.causewayHtmxPolicy;
    if (identity && typeof policy?.handleHome === 'function' && await policy.handleHome(identity) === true) {
      return;
    }
    if (!isCurrentLanding(landing)) {
      return;
    }
    if (identity) {
      await navigate(canonicalObjectPath(basePath, identity), {replace: true});
    } else {
      landing.dataset.routeState = response.errors?.length ? 'partial-error' : 'ready';
      landing.querySelector('[data-causeway-home-message]').textContent = 'Choose an application action to begin.';
    }
  } catch {
    if (isCurrentLanding(landing)) {
      landing.dataset.routeState = 'partial-error';
      landing.querySelector('[data-causeway-home-message]').textContent = 'The home page is unavailable; application menus remain available.';
    }
  } finally {
    resolvingHomeLandings.delete(landing);
  }
}

function isCurrentLanding(landing) {
  return landing.isConnected && routeRegion?.contains(landing) === true;
}

document.addEventListener('click', event => {
  const link = event.target.closest?.('a[data-causeway-route-link]');
  if (!link || event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey
      || link.target === '_blank' || link.origin !== globalThis.location.origin) {
    return;
  }
  event.preventDefault();
  navigate(link.pathname + link.search);
});

document.addEventListener(NAVIGATION_REQUEST_EVENT, event => {
  const target = event.detail?.target;
  if (!target?.logicalTypeName || !target?.id) {
    return;
  }
  event.preventDefault();
  navigate(canonicalObjectPath(basePath, target));
});

document.addEventListener(ACTION_REQUEST_EVENT, event => {
  snapshotActionResultDestination(event.detail, event.target);
}, {capture: true});

document.addEventListener(ACTION_REQUEST_EVENT, event => {
  if (!authentication) {
    return;
  }
  if (!isExcludedAction(authentication, event.detail)) {
    return;
  }
  event.preventDefault();
  event.stopImmediatePropagation();
  document.querySelector('[data-causeway-logout-form]')?.requestSubmit?.();
}, {capture: true});

document.addEventListener(MENU_BARS_STATE_EVENT, () => globalThis.setTimeout(() => {
  collapseNarrowBars();
  applyAuthenticationMenuPolicy(authentication, document);
}, 0));

document.addEventListener(ACTION_RESULT_EVENT, event => {
  presentResult(event.detail);
  queueMicrotask(() => event.target?.dismissResult?.());
});

document.addEventListener(ACTION_RESULTS_DISMISS_REQUEST_EVENT, event => {
  const outlet = event.detail?.outlet;
  if (!outlet?.matches?.('cw-action-results')) return;
  event.preventDefault();
  outlet.dismiss?.();
});

document.addEventListener(COMPONENT_STATE_EVENT, event => {
  const collection = event.target;
  if (collection?.matches?.('cw-collection:not([active])')
      && collection.closest?.('#causeway-route')
      && ['ready', 'partial-error'].includes(event.detail?.state?.status)) {
    collection.activate?.();
  }
});

document.addEventListener(OBJECT_CONTEXT_STATE_EVENT, event => {
  const contextElement = event.target;
  if (!contextElement?.closest?.('#causeway-route')) {
    return;
  }
  const page = contextElement.closest('[data-route-state]');
  const state = event.detail?.state;
  if (!page || !state) {
    return;
  }
  page.dataset.routeState = state.status;
  const routeGeneration = Number(routeRegion?.dataset.navigationGeneration);
  const awaitingVoidRefresh = pendingVoidRefreshGeneration === routeGeneration;
  if (state.status === 'ready' || state.status === 'partial-error') {
    if (awaitingVoidRefresh) {
      pendingVoidRefreshGeneration = null;
    }
    const objectTitle = state.snapshot?.data?._meta?.title;
    const brand = document.querySelector('.causeway-shell-brand > span:last-child')?.textContent?.trim();
    if (objectTitle) {
      document.title = brand ? `${objectTitle} · ${brand}` : objectTitle;
    }
    globalThis.setTimeout(activateRouteCollections, 0);
    announce(state.status === 'ready' ? 'Page ready' : 'Page ready with partial information');
  } else if (state.status === 'terminal-error') {
    const code = state.errors?.[0]?.extensions?.classification
      ?? state.errors?.[0]?.extensions?.code
      ?? state.error?.code;
    if (awaitingVoidRefresh) {
      pendingVoidRefreshGeneration = null;
      if (code === 'NOT_FOUND') {
        void navigate(basePath, {replace: true, preserveResult: true});
        return;
      }
    }
    page.dataset.routeState = code === 'NOT_FOUND' ? 'not-found' : code === 'ACCESS_DENIED' ? 'access-denied' : 'terminal-error';
    announce(page.dataset.routeState === 'access-denied' ? 'Access denied' : 'Page unavailable');
  }
});

document.body.addEventListener('htmx:configRequest', event => {
  if (!authentication || !isUnsafeMethod(event.detail?.verb)) {
    return;
  }
  Object.assign(event.detail.headers, csrfHeaders(authentication));
});

document.body.addEventListener('htmx:beforeRequest', event => {
  if (event.detail?.target?.id !== 'causeway-route') {
    return;
  }
  activeRequest?.abort?.();
  activeRequest = event.detail.xhr;
  setBusy(true);
});

document.body.addEventListener('htmx:afterSwap', event => {
  if (event.detail?.target?.id !== 'causeway-route') {
    return;
  }
  activeRequest = null;
  setBusy(false);
  const preservingScroll = pendingVoidRefreshGeneration === navigationGeneration;
  if (!preservingScroll) globalThis.scrollTo?.({top: 0, left: 0, behavior: 'auto'});
  event.detail.target.querySelector('[tabindex="-1"]')?.focus?.({preventScroll: true});
  if (preservingScroll) rehomePreservedShellResult();
  void resolveHome();
});

document.body.addEventListener('htmx:afterRequest', event => {
  if (event.detail?.target?.id === 'causeway-route') {
    activeRequest = null;
    setBusy(false);
  }
});

document.body.addEventListener('htmx:responseError', event => {
  if (event.detail?.target?.id !== 'causeway-route') {
    return;
  }
  if (authentication && event.detail?.xhr?.status === 401) {
    redirectToLogin();
    return;
  }
  renderRouteFailure('The requested page could not be loaded.');
});

document.body.addEventListener('htmx:historyCacheMiss', () => setBusy(true));
document.body.addEventListener('htmx:historyRestore', () => {
  setBusy(false);
  queueMicrotask(() => routeRegion?.querySelector('[tabindex="-1"]')?.focus());
  globalThis.setTimeout(activateRouteCollections, 0);
});
globalThis.matchMedia?.('(max-width: 48rem)').addEventListener?.('change', collapseNarrowBars);

resolveHome();

export {navigate, presentResult, resolveHome};
