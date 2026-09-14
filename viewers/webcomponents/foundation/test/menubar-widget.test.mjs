/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {installDomShim} from './dom-shim.mjs';
import {projectCausewayMenuBar} from '../src/menubar-projection.mjs';

installDomShim();
const {
  CausewayMenubarControlElement,
  causewayMenubarWidgetConfiguration,
  configureCausewayMenubarWidgets,
  failCausewayMenubarWidget,
  useCausewayMenubarWidget
} = await import('../src/menubar-widget.mjs');

const fakeModule = new URL('./fixtures/fake-vaadin-menubar.mjs', import.meta.url).href;

function projection(generation = 1) {
  return projectCausewayMenuBar({
    role: 'primary',
    menus: [{label: 'Administration', sections: [{label: 'People', actions: [
      {serviceLogicalTypeName: 'demo.People', actionId: 'find', label: 'Find people'},
      {serviceLogicalTypeName: 'demo.People', actionId: 'remove', label: 'Remove', disabled: 'Not permitted'}
    ]}]}]
  }, {
    generation,
    actionAppearance: action => action.actionId === 'find' ? 'sign-out' : undefined
  });
}

async function connect(adapter) {
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
}

test('Menu Bar family configuration is bounded and value-free', () => {
  const events = [];
  const listener = event => events.push(event.detail);
  document.addEventListener('causeway-menubar-widget-policy', listener);
  configureCausewayMenubarWidgets({enabled: true, moduleUrl: fakeModule, definitionTimeoutMs: 100});
  const configuration = causewayMenubarWidgetConfiguration();
  assert.equal(configuration.enabled, true);
  assert.equal(configuration.failed, false);
  assert.equal(configuration.definitionTimeoutMs, 100);
  assert.equal(useCausewayMenubarWidget(), true);
  assert.equal(events.at(-1).family, 'menubar');
  assert.equal(events.at(-1).reason, 'configuration');
  assert.throws(() => configureCausewayMenubarWidgets({moduleUrl: 'javascript:alert(1)'}), /approved module protocol/);
  assert.throws(() => configureCausewayMenubarWidgets({definitionTimeoutMs: 0}), /definition timeout/);
  document.removeEventListener('causeway-menubar-widget-policy', listener);
});

test('qualified adapter loads once and selects only current enabled leaves', async () => {
  configureCausewayMenubarWidgets({enabled: true, moduleUrl: fakeModule});
  const adapter = new CausewayMenubarControlElement();
  const selected = [];
  adapter.presentation = {projection: projection(), overflowLabel: 'Weitere Optionen', activate: descriptor => selected.push(descriptor)};
  document.body.appendChild(adapter);
  adapter.focus({preventScroll: true});
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'ready');
  const control = adapter.childNodes[0];
  assert.equal(document.activeElement, control);
  assert.equal(control.localName, 'vaadin-menu-bar');
  assert.equal(control.i18n.moreOptions, 'Weitere Optionen');
  assert.equal(globalThis.Vaadin.featureFlags.accessibleDisabledMenuItems, true);
  assert.equal(control.items[0].text, 'Administration');
  const section = control.items[0].children[0];
  const enabled = control.items[0].children[1];
  const disabled = control.items[0].children[2];
  assert.equal(section.children, undefined);
  assert.equal(section.component.getAttribute('role'), 'separator');
  assert.equal(section.component.getAttribute('aria-label'), 'People');
  assert.equal(enabled.component.localName, 'vaadin-menu-bar-item');
  assert.equal(enabled.component.dataset.causewayActionAppearance, 'sign-out');
  assert.equal(enabled.component.childNodes[0].dataset.causewayActionAppearance, 'sign-out');
  assert.equal(disabled.component.localName, 'vaadin-menu-bar-item');
  assert.equal(disabled.component.childNodes[0].getAttribute('aria-label'), 'Remove. Unavailable: Not permitted');
  assert.equal(disabled.component.childNodes[0].dataset.disabledReason, 'Not permitted');
  control.dispatchEvent(new CustomEvent('item-selected', {detail: {value: enabled}}));
  control.dispatchEvent(new CustomEvent('item-selected', {detail: {value: disabled}}));
  control.dispatchEvent(new CustomEvent('item-selected', {detail: {value: {causewayKey: 'stale'}}}));
  assert.equal(selected.length, 1);
  assert.equal(selected[0].actionId, 'find');
  document.body.removeChild(adapter);
  assert.equal(control.items.length, 0);
});

test('native policy requests no internal control', async () => {
  configureCausewayMenubarWidgets({enabled: false, moduleUrl: fakeModule});
  const adapter = new CausewayMenubarControlElement();
  adapter.presentation = {projection: projection(), activate() { assert.fail('must not activate'); }};
  await connect(adapter);
  assert.equal(adapter.dataset.widgetState, 'native');
  assert.equal(adapter.childNodes.length, 0);
  document.body.removeChild(adapter);
});

test('module failures are family scoped and recoverable', async () => {
  configureCausewayMenubarWidgets({enabled: true, moduleUrl: new URL('./fixtures/missing-vaadin-menubar.mjs', import.meta.url).href});
  const missing = new CausewayMenubarControlElement();
  missing.presentation = {projection: projection(), activate() {}};
  await connect(missing);
  assert.equal(missing.dataset.widgetState, 'fallback');
  assert.equal(causewayMenubarWidgetConfiguration().failure.classification, 'MENUBAR_MODULE_UNAVAILABLE');
  document.body.removeChild(missing);
  configureCausewayMenubarWidgets({enabled: true, moduleUrl: fakeModule});
});

test('event translation failure is bounded and family scoped', async () => {
  configureCausewayMenubarWidgets({enabled: true, moduleUrl: fakeModule});
  const adapter = new CausewayMenubarControlElement();
  adapter.presentation = {projection: projection(), activate() { throw new Error('private value-bearing failure'); }};
  let failure;
  adapter.addEventListener('causeway-menubar-load-failed', event => { failure = event.detail; });
  await connect(adapter);
  const control = adapter.childNodes[0];
  const enabled = control.items[0].children[1];
  control.dispatchEvent(new CustomEvent('item-selected', {detail: {value: enabled}}));
  assert.equal(adapter.dataset.widgetState, 'fallback');
  assert.deepEqual(failure, {
    family: 'menubar',
    phase: 'event',
    classification: 'MENUBAR_EVENT_UNAVAILABLE',
    revision: causewayMenubarWidgetConfiguration().revision
  });
  assert.equal(JSON.stringify(failure).includes('private'), false);
  assert.equal(useCausewayMenubarWidget(), false);
  document.body.removeChild(adapter);
  configureCausewayMenubarWidgets({enabled: true, moduleUrl: fakeModule});
});

test('disconnect and policy revision reject stale work', async () => {
  configureCausewayMenubarWidgets({enabled: true, moduleUrl: fakeModule});
  const adapter = new CausewayMenubarControlElement();
  adapter.presentation = {projection: projection(), activate() {}};
  document.body.appendChild(adapter);
  document.body.removeChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.notEqual(adapter.dataset.widgetState, 'ready');
  assert.equal(failCausewayMenubarWidget({phase: 'event', classification: 'MENUBAR_EVENT_UNAVAILABLE'}), true);
  assert.equal(failCausewayMenubarWidget({phase: 'again', classification: 'IGNORED'}), false);
  configureCausewayMenubarWidgets({enabled: true, moduleUrl: fakeModule});
});
