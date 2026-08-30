/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {installDomShim} from './dom-shim.mjs';

installDomShim();
const {
  CausewayActionControlElement,
  causewayActionWidgetConfiguration,
  configureCausewayActionWidgets,
  renderCausewayActionWidget,
  renderNativeCausewayActionButton,
  useCausewayActionWidget
} = await import('../src/action-widget.mjs');

const fakeActionModule = new URL('./fixtures/fake-vaadin-action.mjs', import.meta.url).href;

test('action widget defaults enabled and preserves a native loading fallback', () => {
  configureCausewayActionWidgets({enabled: true, moduleUrl: fakeActionModule});
  assert.equal(causewayActionWidgetConfiguration().enabled, true);
  assert.equal(useCausewayActionWidget(), true);
  const html = renderCausewayActionWidget({label: 'Inspect', describedBy: 'inspect-description', testId: 'inspect-control'});
  assert.match(html, /<cw-action-control/);
  assert.match(html, /data-label="Inspect"/);
  assert.match(html, /data-control-testid="inspect-control"/);
  assert.match(html, /<button type="button" data-causeway-action-control aria-describedby="inspect-description" data-testid="inspect-control"><span class="causeway-action-label">Inspect<\/span><\/button>/);
});

test('native action markup retains disabled semantics', () => {
  const html = renderNativeCausewayActionButton({label: 'Delete', describedBy: 'delete-reason', disabled: true});
  assert.match(html, /disabled aria-disabled="true"/);
  assert.match(html, /aria-describedby="delete-reason"/);
});

test('action control upgrades lazily and preserves accessible state', async () => {
  configureCausewayActionWidgets({enabled: true, moduleUrl: fakeActionModule});
  const adapter = new CausewayActionControlElement();
  adapter.setAttribute('data-label', 'Inspect');
  adapter.setAttribute('data-describedby', 'inspect-description');
  adapter.setAttribute('data-control-testid', 'inspect-control');
  document.body.focus();
  adapter.focus();
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  const control = adapter.childNodes[0];
  assert.equal(adapter.dataset.widgetState, 'ready');
  assert.equal(control.localName, 'vaadin-button');
  assert.equal(control.textContent, 'Inspect');
  assert.equal(control.getAttribute('aria-describedby'), 'inspect-description');
  assert.equal(control.hasAttribute('data-causeway-action-control'), false);
  assert.equal(control.getAttribute('data-testid'), 'inspect-control');
  await Promise.resolve();
  assert.equal(document.activeElement, control);
  document.body.removeChild(adapter);
});

test('disconnect and reconnect cannot apply stale action work', async () => {
  configureCausewayActionWidgets({enabled: true, moduleUrl: fakeActionModule});
  const adapter = new CausewayActionControlElement();
  document.body.appendChild(adapter);
  document.body.removeChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.notEqual(adapter.dataset.widgetState, 'ready');
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'ready');
  document.body.removeChild(adapter);
});

test('policy revisions cannot complete stale action upgrades', async () => {
  let resolveDelayedModule;
  globalThis.causewayDelayedActionModulePromise = new Promise(resolve => { resolveDelayedModule = resolve; });
  const delayedModule = new URL('./fixtures/fake-vaadin-delayed-action.mjs', import.meta.url).href;
  configureCausewayActionWidgets({enabled: true, moduleUrl: delayedModule});
  const adapter = new CausewayActionControlElement();
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'loading');
  configureCausewayActionWidgets({enabled: false, moduleUrl: fakeActionModule});
  resolveDelayedModule();
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.notEqual(adapter.dataset.widgetState, 'ready');
  document.body.removeChild(adapter);
  delete globalThis.causewayDelayedActionModulePromise;
});

test('native policy avoids action upgrade', async () => {
  configureCausewayActionWidgets({enabled: false, moduleUrl: fakeActionModule});
  const adapter = new CausewayActionControlElement();
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'native');
  assert.equal(adapter.childNodes.length, 0);
  document.body.removeChild(adapter);
});

test('action module failure is bounded and disables only the adapter family', async () => {
  configureCausewayActionWidgets({enabled: true, moduleUrl: new URL('./fixtures/missing-vaadin-action.mjs', import.meta.url).href});
  const adapter = new CausewayActionControlElement();
  let failures = 0;
  adapter.addEventListener('causeway-action-load-failed', () => { failures += 1; });
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'fallback');
  assert.equal(failures, 1);
  assert.equal(useCausewayActionWidget(), false);
  document.body.removeChild(adapter);
  configureCausewayActionWidgets({enabled: true, moduleUrl: fakeActionModule});
});
