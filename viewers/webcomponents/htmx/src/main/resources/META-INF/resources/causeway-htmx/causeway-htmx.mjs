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
  ACTION_RESULT_EVENT,
  COMPONENT_STATE_EVENT,
  MENU_BARS_STATE_EVENT,
  NAVIGATION_REQUEST_EVENT,
  OBJECT_CONTEXT_STATE_EVENT,
  defineCausewayWebComponents,
  requestGraphQLClient
} from '../causeway-webcomponents/index.mjs';
import {
  canonicalObjectPath,
  homeObjectIdentity,
  resultObjectIdentity
} from './route-policy.mjs';

const shell = document.querySelector('causeway-graphql-client');
const routeRegion = document.querySelector('#causeway-route');
const announcement = document.querySelector('#causeway-route-announcement');
const resultRegion = document.querySelector('#causeway-result');
const basePath = document.documentElement.dataset.causewayHtmxBase;
let activeRequest = null;
let navigationGeneration = 0;

defineCausewayWebComponents();

function announce(message) {
  if (announcement) {
    announcement.textContent = '';
    globalThis.requestAnimationFrame?.(() => {
      announcement.textContent = message;
    });
  }
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
  for (const collection of routeRegion?.querySelectorAll('causeway-collection:not([active])') ?? []) {
    collection.activate?.();
  }
}

async function navigate(path, {replace = false, preserveResult = false} = {}) {
  const policy = globalThis.causewayHtmxPolicy;
  if (typeof policy?.navigate === 'function' && await policy.navigate({path, replace}) === true) {
    return;
  }
  if (!globalThis.htmx || !routeRegion) {
    globalThis.location.assign(path);
    return;
  }
  const generation = ++navigationGeneration;
  routeRegion.dataset.navigationGeneration = String(generation);
  for (const disclosure of document.querySelectorAll('[data-causeway-menu-disclosure][aria-expanded="true"]')) {
    disclosure.click();
  }
  if (!preserveResult) {
    resultRegion?.replaceChildren();
    if (resultRegion) {
      resultRegion.hidden = true;
    }
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
  if (!resultRegion) {
    return;
  }
  resultRegion.replaceChildren();
  const heading = document.createElement('h2');
  heading.textContent = detail?.actionId ? `${detail.actionId} result` : 'Action result';
  const output = document.createElement('output');
  const result = detail?.result;
  if (result?.kind === 'collection') {
    output.textContent = `${Array.isArray(result.value) ? result.value.length : 0} results`;
    const list = document.createElement('ul');
    list.className = 'causeway-result-list';
    for (const item of result.value ?? []) {
      const metadata = item?._meta;
      const entry = document.createElement('li');
      if (metadata?.logicalTypeName && metadata?.id) {
        const link = document.createElement('a');
        link.href = canonicalObjectPath(basePath, metadata);
        link.dataset.causewayRouteLink = '';
        link.textContent = metadata.title ?? metadata.id;
        entry.append(link);
      } else {
        entry.textContent = 'Result item';
      }
      list.append(entry);
    }
    resultRegion.append(heading, output, list);
  } else if (result?.kind === 'scalar') {
    output.textContent = result.value == null ? '' : String(result.value);
    resultRegion.append(heading, output);
  } else {
    output.textContent = 'Completed';
    resultRegion.append(heading, output);
    const context = routeRegion?.querySelector('causeway-object-context')?.context;
    if (context) {
      globalThis.setTimeout(() => navigate(
        globalThis.location.pathname + globalThis.location.search,
        {replace: true, preserveResult: true}
      ), 0);
    }
  }
  resultRegion.hidden = false;
  announce(`${heading.textContent}: ${output.textContent}`);
}

async function resolveHome() {
  const landing = routeRegion?.querySelector('[data-route-state="landing"]');
  if (!landing || !shell) {
    return;
  }
  const client = requestGraphQLClient(shell);
  if (!client) {
    return;
  }
  try {
    const description = await client.describeApplicationEntry();
    if (!description.supported) {
      landing.dataset.routeState = 'unsupported';
      landing.querySelector('[data-causeway-home-message]').textContent = 'Choose an application action to begin.';
      return;
    }
    const response = await client.readApplicationEntry({description});
    const identity = homeObjectIdentity(response.data);
    const policy = globalThis.causewayHtmxPolicy;
    if (identity && typeof policy?.handleHome === 'function' && await policy.handleHome(identity) === true) {
      return;
    }
    if (identity) {
      await navigate(canonicalObjectPath(basePath, identity), {replace: true});
    } else {
      landing.dataset.routeState = response.errors?.length ? 'partial-error' : 'ready';
      landing.querySelector('[data-causeway-home-message]').textContent = 'Choose an application action to begin.';
    }
  } catch {
    landing.dataset.routeState = 'partial-error';
    landing.querySelector('[data-causeway-home-message]').textContent = 'The home page is unavailable; application menus remain available.';
  }
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

document.addEventListener(MENU_BARS_STATE_EVENT, () => globalThis.setTimeout(collapseNarrowBars, 0));

document.addEventListener(ACTION_RESULT_EVENT, event => {
  presentResult(event.detail);
  queueMicrotask(() => event.target?.dismissResult?.());
});

document.addEventListener(COMPONENT_STATE_EVENT, event => {
  const collection = event.target;
  if (collection?.matches?.('causeway-collection:not([active])')
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
  if (state.status === 'ready' || state.status === 'partial-error') {
    const objectTitle = state.snapshot?.data?._meta?.title;
    const brand = document.querySelector('.causeway-shell-brand > span:last-child')?.textContent?.trim();
    if (objectTitle) {
      document.title = brand ? `${objectTitle} · ${brand}` : objectTitle;
    }
    globalThis.setTimeout(activateRouteCollections, 0);
    announce(state.status === 'ready' ? 'Page ready' : 'Page ready with partial information');
  } else if (state.status === 'terminal-error') {
    const code = state.errors?.[0]?.extensions?.classification ?? state.errors?.[0]?.extensions?.code;
    page.dataset.routeState = code === 'NOT_FOUND' ? 'not-found' : code === 'ACCESS_DENIED' ? 'access-denied' : 'terminal-error';
    announce(page.dataset.routeState === 'access-denied' ? 'Access denied' : 'Page unavailable');
  }
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
  event.detail.target.querySelector('[tabindex="-1"]')?.focus();
});

document.body.addEventListener('htmx:afterRequest', event => {
  if (event.detail?.target?.id === 'causeway-route') {
    activeRequest = null;
    setBusy(false);
  }
});

document.body.addEventListener('htmx:responseError', event => {
  if (event.detail?.target?.id === 'causeway-route') {
    renderRouteFailure('The requested page could not be loaded.');
  }
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
