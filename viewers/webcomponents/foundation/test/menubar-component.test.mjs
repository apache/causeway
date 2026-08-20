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

import assert from 'node:assert/strict';
import test from 'node:test';

import {installDomShim} from './dom-shim.mjs';

const {document} = installDomShim();
const {
  CausewayElementName,
  CausewayInteractionControllerElement,
  CausewayMenubarPrimaryElement,
  CausewayMenubarsElement,
  CausewaySemanticEvent,
  defineCausewayWebComponents,
  MenuBarsStatus,
  parseCausewayMenuBarsXml,
  applyServiceActionStates,
  CausewayGraphQLClient
} = await import('../src/index.mjs');
const {MENU_ACTION_STATES, MENU_BARS_XML} = await import('./fixtures/menu-layout-fixtures.mjs');
const {createMenuGraphQLExecutor} = await import('./fixtures/menu-graphql-fixture.mjs');
const {waitFor} = await import('./fixtures/rich-schema-fixture.mjs');

defineCausewayWebComponents();
const ONE_SERVICE_XML = MENU_BARS_XML.replaceAll('causeway.webcomponents.sample.AdminMenu', 'causeway.webcomponents.sample.SampleMenu');

test('semantic bar renders labelled navigation, disclosures, text-safe hints, disabled reasons, and no hidden actions', () => {
  const parsed = parseCausewayMenuBarsXml(MENU_BARS_XML);
  const plan = applyServiceActionStates(parsed.plan, MENU_ACTION_STATES);
  const context = fakeMenuContext(plan);
  const bar = new CausewayMenubarPrimaryElement();
  bar.context = context;
  document.body.appendChild(bar);
  const markup = renderMarkup(bar);

  assert.match(markup, /<nav[^>]+aria-label="Primary application menu"/);
  assert.match(markup, /data-causeway-menu-disclosure/);
  assert.match(markup, /aria-expanded="false"/);
  assert.match(markup, /data-icon-hint="fa-building"/);
  assert.match(markup, /Welcome Message/);
  assert.match(markup, /disabled aria-disabled="true"/);
  assert.match(markup, /Available to administrators only/);
  assert.doesNotMatch(markup, /hiddenAction/);
  assert.doesNotMatch(markup, /role="menubar"|role="menuitem"/);
  document.body.removeChild(bar);
});

test('menu action selection, outside activation, Escape, and sibling opening close transient disclosures', () => {
  const parsed = parseCausewayMenuBarsXml(MENU_BARS_XML);
  const plan = applyServiceActionStates(parsed.plan, MENU_ACTION_STATES);
  const bar = new CausewayMenubarPrimaryElement();
  bar.context = fakeMenuContext(plan);
  document.body.appendChild(bar);

  const firstPanel = {id: 'first-panel', hidden: false};
  const secondPanel = {id: 'second-panel', hidden: true};
  const firstDisclosure = fakeDisclosure('first-panel', true);
  const secondDisclosure = fakeDisclosure('second-panel', false);
  const barDisclosure = fakeDisclosure('bar-content', true);
  const action = {
    disabled: false,
    getAttribute(name) {
      return {['data-service-logical-type']: 'causeway.webcomponents.sample.SampleMenu', ['data-action-id']: 'welcomeMessage'}[name] ?? null;
    },
    closest(selector) {
      if (selector === '[data-causeway-service-action]') return this;
      if (selector === '[data-causeway-menu-panel]') return firstPanel;
      return null;
    },
    dispatchEvent(event) {
      event.target = this;
      return bar.dispatchEvent(event);
    }
  };
  const known = new Set([firstDisclosure, secondDisclosure, barDisclosure, firstPanel, secondPanel, action]);
  bar.contains = candidate => known.has(candidate);
  bar.querySelector = selector => {
    if (selector.startsWith('#first-panel')) return firstPanel;
    if (selector.startsWith('#second-panel')) return secondPanel;
    if (selector.includes('aria-controls="first-panel"')) return firstDisclosure;
    if (selector.includes('aria-controls="second-panel"')) return secondDisclosure;
    if (selector.includes('[data-causeway-menu-disclosure]') && selector.includes('aria-expanded="true"')) {
      return [firstDisclosure, secondDisclosure].find(disclosure => disclosure.getAttribute('aria-expanded') === 'true') ?? null;
    }
    if (selector.includes('[data-causeway-bar-disclosure]') && selector.includes('aria-expanded="true"')) {
      return barDisclosure.getAttribute('aria-expanded') === 'true' ? barDisclosure : null;
    }
    return null;
  };
  bar.querySelectorAll = selector => selector.includes('aria-expanded="true"')
    ? [firstDisclosure, secondDisclosure].filter(disclosure => disclosure.getAttribute('aria-expanded') === 'true')
    : [];

  let request;
  let requestCount = 0;
  bar.addEventListener(CausewaySemanticEvent.ACTION_REQUEST, event => {
    request = event.detail;
    requestCount += 1;
  });
  const actionClick = new Event('click');
  actionClick.target = action;
  bar.dispatchEvent(actionClick);
  assert.equal(request.actionId, 'welcomeMessage');
  assert.equal(requestCount, 1);
  assert.equal(Object.hasOwn(request, 'focusTarget'), false);
  assert.equal(firstDisclosure.getAttribute('aria-expanded'), 'false');
  assert.equal(firstPanel.hidden, true);
  assert.equal(firstDisclosure.focusCount, 1);

  firstDisclosure.setAttribute('aria-expanded', 'true');
  firstPanel.hidden = false;
  const outsideClick = new Event('click');
  outsideClick.target = {};
  document.dispatchEvent(outsideClick);
  assert.equal(firstDisclosure.getAttribute('aria-expanded'), 'false');
  assert.equal(firstDisclosure.focusCount, 1);

  firstDisclosure.setAttribute('aria-expanded', 'true');
  firstPanel.hidden = false;
  const siblingClick = new Event('click');
  siblingClick.target = {closest: selector => selector === '[data-causeway-menu-disclosure]' ? secondDisclosure : null};
  bar.dispatchEvent(siblingClick);
  assert.equal(firstDisclosure.getAttribute('aria-expanded'), 'false');
  assert.equal(secondDisclosure.getAttribute('aria-expanded'), 'true');
  assert.equal(secondPanel.hidden, false);

  const escape = new Event('keydown', {cancelable: true});
  escape.key = 'Escape';
  escape.target = {closest: () => null};
  bar.dispatchEvent(escape);
  assert.equal(escape.defaultPrevented, true);
  assert.equal(secondDisclosure.getAttribute('aria-expanded'), 'false');
  assert.equal(secondPanel.hidden, true);
  assert.equal(secondDisclosure.focusCount, 1);
  assert.equal(barDisclosure.getAttribute('aria-expanded'), 'true');
  assert.equal(requestCount, 1);
  document.body.removeChild(bar);
});

test('composite preserves declarative bars, generates only missing effective roles, and shares one generation', async () => {
  const executor = createMenuGraphQLExecutor();
  const composite = new CausewayMenubarsElement();
  composite.client = new CausewayGraphQLClient({executor});
  let fetchCount = 0;
  composite.fetchImpl = async () => {
    fetchCount += 1;
    return xmlResponse(ONE_SERVICE_XML);
  };
  const declarativePrimary = document.createElement(CausewayElementName.MENUBAR_PRIMARY);
  declarativePrimary.setAttribute('data-authored', 'true');
  composite.appendChild(declarativePrimary);
  document.body.appendChild(composite);
  await waitFor(() => [MenuBarsStatus.READY, MenuBarsStatus.PARTIAL_ERROR].includes(composite.context?.state.status));

  const bars = composite.childNodes.filter(child => [
    CausewayElementName.MENUBAR_PRIMARY,
    CausewayElementName.MENUBAR_SECONDARY,
    CausewayElementName.MENUBAR_TERTIARY
  ].includes(child.localName));
  assert.equal(bars.length, 3);
  assert.equal(bars.filter(child => child.localName === CausewayElementName.MENUBAR_PRIMARY).length, 1);
  assert.equal(bars.find(child => child.localName === CausewayElementName.MENUBAR_PRIMARY), declarativePrimary);
  assert.equal(fetchCount, 1);
  assert.equal(executor.applicationCalls.length, 1);
  assert.equal(executor.serviceCalls.filter(call => call.operationName === 'CausewayReadServiceActionStates').length, 1);
  assert.ok(composite.childNodes.some(child => child.localName === CausewayElementName.INTERACTION_CONTROLLER));
  document.body.removeChild(composite);
});

test('standalone bar owns a private generation and generated interaction controller', async () => {
  const executor = createMenuGraphQLExecutor();
  const bar = new CausewayMenubarPrimaryElement();
  bar.client = new CausewayGraphQLClient({executor});
  let fetchCount = 0;
  bar.fetchImpl = async () => {
    fetchCount += 1;
    return xmlResponse(ONE_SERVICE_XML);
  };
  document.body.appendChild(bar);
  await waitFor(() => bar.getAttribute('data-menu-state') === MenuBarsStatus.READY);

  assert.equal(fetchCount, 1);
  assert.ok(bar.childNodes.some(child => child.localName === CausewayElementName.INTERACTION_CONTROLLER));
  assert.match(renderMarkup(bar), /Welcome Message/);
  document.body.removeChild(bar);
});

test('standard interaction result adds public service target detail', async () => {
  const controller = new CausewayInteractionControllerElement();
  const source = document.createElement('button');
  source.focus = () => {};
  source.matches = () => true;
  const context = {
    identity: null,
    interactionTarget: {kind: 'service', logicalTypeName: 'causeway.webcomponents.sample.SampleMenu'},
    async prepareAction() {
      return {status: 'success', data: {parameters: []}, errors: []};
    },
    async invokeAction() {
      return {status: 'success', data: {kind: 'scalar', value: 'Welcome!'}, errors: []};
    }
  };
  let detail;
  controller.addEventListener(CausewaySemanticEvent.ACTION_RESULT, event => detail = event.detail);
  document.body.appendChild(controller);
  await controller.beginAction('welcomeMessage', context, source);
  await new Promise(resolve => queueMicrotask(resolve));

  assert.equal(detail.identity, null);
  assert.deepEqual(detail.target, {kind: 'service', logicalTypeName: 'causeway.webcomponents.sample.SampleMenu'});
  assert.equal(detail.serviceLogicalTypeName, 'causeway.webcomponents.sample.SampleMenu');
  assert.equal(detail.result.value, 'Welcome!');
  document.body.removeChild(controller);
});

function fakeDisclosure(panelId, expanded) {
  const attributes = new Map([
    ['aria-controls', panelId],
    ['aria-expanded', String(expanded)]
  ]);
  return {
    focusCount: 0,
    getAttribute: name => attributes.get(name) ?? null,
    setAttribute: (name, value) => attributes.set(name, String(value)),
    focus() {
      this.focusCount += 1;
    },
    closest(selector) {
      return selector === '[data-causeway-menu-disclosure]' ? this : null;
    }
  };
}

function fakeMenuContext(plan) {
  const state = Object.freeze({
    status: MenuBarsStatus.READY,
    generation: 1,
    plan,
    diagnostics: Object.freeze([]),
    error: null,
    resource: null
  });
  return {
    state,
    subscribe(listener) {
      listener(state);
      return () => {};
    },
    serviceContext(logicalTypeName) {
      return {logicalTypeName};
    },
    refresh: async () => state
  };
}

function renderMarkup(bar) {
  return bar.childNodes.find(child => child.getAttribute?.('data-causeway-menubar-render') === 'true')?.innerHTML ?? '';
}

function xmlResponse(text) {
  return {
    ok: true,
    status: 200,
    headers: {get: name => name === 'content-type' ? 'application/xml' : null},
    text: async () => text
  };
}
