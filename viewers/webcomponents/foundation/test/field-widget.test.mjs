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
  CausewayFieldEditorElement,
  causewayFieldDescriptor,
  causewayFieldWidgetConfiguration,
  configureCausewayFieldWidgets,
  renderCausewayFieldWidget,
  supportsCausewayFieldWidget
} = await import('../src/field-widget.mjs');
const {renderCausewayEditor} = await import('../src/editor-registry.mjs');

const fakeBasicModule = new URL('./fixtures/fake-vaadin-basic.mjs', import.meta.url).href;

function context(overrides = {}) {
  return {
    name: 'value',
    label: 'Value',
    value: 'current',
    controlValue: 'current',
    choices: [],
    enumValues: [],
    inputType: {kind: 'SCALAR', name: 'String'},
    semanticType: 'String',
    codec: {id: 'scalar'},
    required: false,
    multiLine: 0,
    inputId: 'value-input',
    labelId: 'value-label',
    descriptionId: '',
    errorId: '',
    disabled: false,
    ...overrides
  };
}

test('normalizes the explicit family allow-list and rejects unsafe policy', () => {
  assert.deepEqual(configureCausewayFieldWidgets({families: 'numeric,basic'}).families, ['basic', 'numeric']);
  assert.throws(() => configureCausewayFieldWidgets({families: 'basic,basic'}), /must not be repeated/);
  assert.throws(() => configureCausewayFieldWidgets({families: 'unknown'}), /Unknown/);
  assert.throws(() => configureCausewayFieldWidgets({families: ['basic'], moduleUrls: {basic: 'data:text/javascript,'}}), /same-origin|not supported/);
  configureCausewayFieldWidgets({families: []});
});

test('classifies only reversible qualified semantic families', () => {
  configureCausewayFieldWidgets({families: ['basic', 'numeric', 'local-temporal']});
  assert.deepEqual(causewayFieldDescriptor(context()), {
    family: 'basic', control: 'text-field', inputMode: null, debounced: true
  });
  assert.equal(causewayFieldDescriptor(context({multiLine: 4})).control, 'text-area');
  assert.equal(causewayFieldDescriptor(context({codec: {id: 'protected'}, semanticType: 'Password'})).control, 'password-field');
  assert.equal(causewayFieldDescriptor(context({codec: {id: 'boolean'}, semanticType: 'Boolean', required: true})).control, 'checkbox');
  assert.equal(causewayFieldDescriptor(context({codec: {id: 'exact-numeric'}, semanticType: 'BigDecimal'})).control, 'text-field');
  assert.equal(causewayFieldDescriptor(context({codec: {id: 'machine-numeric'}, semanticType: 'Int'})).control, 'integer-field');
  assert.equal(causewayFieldDescriptor(context({codec: {id: 'temporal'}, semanticType: 'LocalDateTime'})).control, 'date-time-picker');
  assert.equal(causewayFieldDescriptor(context({
    codec: {id: 'temporal'}, semanticType: 'LocalDateTime', value: '2026-08-24T13:14:15.123456789'
  })), null);
  assert.equal(causewayFieldDescriptor(context({codec: {id: 'temporal'}, semanticType: 'OffsetDateTime'})), null);
  assert.equal(causewayFieldDescriptor(context({codec: {id: 'unsupported'}, semanticType: 'Blob'})), null);
  assert.equal(causewayFieldDescriptor(context({codec: {id: 'reference'}, inputType: {kind: 'OBJECT', name: 'Thing'}})), null);
  assert.equal(causewayFieldDescriptor(context({autoComplete: true})), null);
});

test('keeps protected values out of adapter markup', () => {
  configureCausewayFieldWidgets({families: ['basic']});
  const html = renderCausewayFieldWidget(context({
    value: 'do-not-render',
    controlValue: '',
    codec: {id: 'protected'},
    semanticType: 'ProtectedValue'
  }));
  assert.doesNotMatch(html, /do-not-render/);
  assert.match(html, /data-sensitive="true"/);
  assert.match(html, /data-control="password-field"/);
});

test('registry prefers qualified fields and retains native fallback', () => {
  configureCausewayFieldWidgets({families: ['numeric']});
  assert.equal(renderCausewayEditor(context({
    codec: undefined,
    semanticType: 'BigInteger',
    inputType: {kind: 'SCALAR', name: 'BigInteger'}
  })).editorId, 'vaadin-field');
  configureCausewayFieldWidgets({families: []});
  assert.equal(renderCausewayEditor(context({
    codec: undefined,
    semanticType: 'BigInteger',
    inputType: {kind: 'SCALAR', name: 'BigInteger'}
  })).editorId, 'exact-number');
});

test('upgrades through a family-local module and ignores disconnected completion', async () => {
  configureCausewayFieldWidgets({families: ['basic'], moduleUrls: {basic: fakeBasicModule}});
  const editor = new CausewayFieldEditorElement();
  editor.setAttribute('id', 'field');
  editor.setAttribute('data-family', 'basic');
  editor.setAttribute('data-control', 'text-field');
  editor.setAttribute('data-causeway-editor', 'name');
  editor.setAttribute('data-value', 'Ada');
  editor.setAttribute('data-labelledby', 'name-label');
  document.body.appendChild(editor);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(editor.dataset.widgetState, 'ready');
  assert.equal(editor.childNodes[0].localName, 'vaadin-text-field');
  assert.equal(editor.childNodes[0].value, 'Ada');
  document.body.removeChild(editor);

  const disconnected = new CausewayFieldEditorElement();
  disconnected.setAttribute('data-family', 'basic');
  disconnected.setAttribute('data-control', 'text-field');
  document.body.appendChild(disconnected);
  document.body.removeChild(disconnected);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.notEqual(disconnected.dataset.widgetState, 'ready');
});

test('fails one family closed with a bounded event', async () => {
  configureCausewayFieldWidgets({families: ['basic'], moduleUrls: {
    basic: new URL('./fixtures/missing-field-module.mjs', import.meta.url).href
  }});
  const editor = new CausewayFieldEditorElement();
  editor.setAttribute('data-family', 'basic');
  editor.setAttribute('data-control', 'text-field');
  let failure;
  editor.addEventListener('causeway-field-load-failed', event => { failure = event.detail; });
  document.body.appendChild(editor);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(editor.dataset.widgetState, 'fallback');
  assert.equal(failure.family, 'basic');
  assert.doesNotMatch(failure.message, /missing-field-module/);
  assert.equal(supportsCausewayFieldWidget(context()), false);
  assert.deepEqual(causewayFieldWidgetConfiguration().families, ['basic']);
  document.body.removeChild(editor);
});
