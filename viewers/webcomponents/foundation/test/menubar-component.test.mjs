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
