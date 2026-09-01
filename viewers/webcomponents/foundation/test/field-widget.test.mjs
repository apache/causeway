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
  causewayDatePickerI18n,
  causewayFieldDescriptor,
  causewayReadOnlyFieldDescriptor,
  causewayFieldWidgetConfiguration,
  configureCausewayFieldWidgets,
  qualifyCausewayCalendarTrigger,
  qualifyCausewayTimeTrigger,
  renderCausewayFieldWidget,
  renderCausewayReadOnlyField,
  resolveCausewayDateLocale,
  supportsCausewayFieldWidget,
  supportsCausewayReadOnlyField
} = await import('../src/field-widget.mjs');
const {renderCausewayEditor} = await import('../src/editor-registry.mjs');

const fakeBasicModule = new URL('./fixtures/fake-vaadin-basic.mjs', import.meta.url).href;
const fakeLocalTemporalModule = new URL('./fixtures/fake-vaadin-local-temporal.mjs', import.meta.url).href;

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

test('defaults to every qualified field family', () => {
  assert.deepEqual(causewayFieldWidgetConfiguration().families, ['basic', 'numeric', 'local-temporal']);
});

test('normalizes the deprecated-compatible family allow-list and rejects unsafe policy', () => {
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

test('qualifies read-only families without exposing protected values', () => {
  configureCausewayFieldWidgets({families: ['basic', 'numeric', 'local-temporal'], presentation: true});
  assert.equal(causewayReadOnlyFieldDescriptor(context()).control, 'text-field');
  assert.equal(causewayReadOnlyFieldDescriptor(context({multiLine: 5})).control, 'text-area');
  assert.equal(causewayReadOnlyFieldDescriptor(context({codec: {id: 'boolean'}, semanticType: 'Boolean'})).control, 'checkbox');
  assert.equal(causewayReadOnlyFieldDescriptor(context({enumValues: ['NEW', 'DONE'], inputType: {kind: 'ENUM', name: 'State'}})).control, 'select');
  assert.equal(causewayReadOnlyFieldDescriptor(context({choices: ['one', 'two']})).control, 'select');
  assert.equal(causewayReadOnlyFieldDescriptor(context({codec: {id: 'exact-numeric'}, semanticType: 'BigDecimal'})).family, 'numeric');
  assert.equal(causewayReadOnlyFieldDescriptor(context({codec: {id: 'machine-numeric'}, semanticType: 'Int'})).control, 'integer-field');
  assert.equal(causewayReadOnlyFieldDescriptor(context({codec: {id: 'machine-numeric'}, semanticType: 'Double'})).control, 'number-field');
  assert.equal(causewayReadOnlyFieldDescriptor(context({codec: {id: 'temporal'}, semanticType: 'LocalDate'})).control, 'date-picker');
  assert.equal(causewayReadOnlyFieldDescriptor(context({codec: {id: 'temporal'}, semanticType: 'LocalTime', value: '13:14:15.123'})).control, 'time-picker');
  assert.equal(causewayReadOnlyFieldDescriptor(context({codec: {id: 'temporal'}, semanticType: 'LocalDateTime', value: '2026-08-24T13:14:15.123'})).control, 'date-time-picker');
  for (const excluded of [
    context({codec: {id: 'protected'}, semanticType: 'Password'}),
    context({codec: {id: 'reference'}, inputType: {kind: 'OBJECT', name: 'Thing'}}),
    context({codec: {id: 'unsupported'}, semanticType: 'Blob'}),
    context({codec: {id: 'temporal'}, semanticType: 'OffsetDateTime'}),
    context({codec: {id: 'temporal'}, semanticType: 'ZonedDateTime'}),
    context({codec: {id: 'temporal'}, semanticType: 'DateTime'}),
    context({codec: {id: 'temporal'}, semanticType: 'LocalTime', value: '13:14:15.123456'}),
    context({value: null})
  ]) assert.equal(causewayReadOnlyFieldDescriptor(excluded), null);
  assert.equal(supportsCausewayReadOnlyField(context()), true);
  const html = renderCausewayReadOnlyField(context({descriptionId: 'value-description'}));
  assert.match(html, /data-mode="view"/);
  assert.match(html, /data-control="text-field"/);
  assert.match(html, /data-labelledby="value-label"/);
  assert.match(html, /data-describedby="value-description"/);
  configureCausewayFieldWidgets({families: ['basic'], presentation: false});
  assert.equal(supportsCausewayReadOnlyField(context()), false);
});

test('local date presentation follows bounded document and browser locales reversibly', () => {
  assert.equal(resolveCausewayDateLocale('en-GB', 'en-US'), 'en-GB');
  assert.equal(resolveCausewayDateLocale('%%%', 'de-DE'), 'de-DE');
  assert.equal(resolveCausewayDateLocale('', ''), 'en');

  const british = causewayDatePickerI18n('en-GB');
  assert.equal(british.formatDate({year: 2026, month: 7, day: 30}), '30/08/2026');
  assert.deepEqual(british.parseDate('30/08/2026'), {year: 2026, month: 7, day: 30});
  assert.deepEqual(british.parseDate('2026-08-30'), {year: 2026, month: 7, day: 30});
  assert.equal(british.parseDate('31/02/2026'), undefined);
  assert.equal(british.firstDayOfWeek, 1);

  const american = causewayDatePickerI18n('en-US');
  assert.equal(american.formatDate({year: 2026, month: 7, day: 30}), '8/30/2026');
  assert.deepEqual(american.parseDate('8/30/2026'), {year: 2026, month: 7, day: 30});
  assert.equal(american.firstDayOfWeek, 0);

  const arabic = causewayDatePickerI18n('ar-EG');
  const localized = arabic.formatDate({year: 2026, month: 7, day: 30});
  assert.deepEqual(arabic.parseDate(localized), {year: 2026, month: 7, day: 30});
  assert.equal(arabic.monthNames.length, 12);
  assert.equal(arabic.weekdays.length, 7);
  assert.equal(arabic.weekdaysShort.length, 7);
  assert.equal(causewayDatePickerI18n('%%%').formatDate({year: 2026, month: 7, day: 30}), '8/30/2026');
});

test('calendar trigger gains bounded button semantics and synchronous keyboard activation only when operable', () => {
  const trigger = document.createElement('div');
  trigger.setAttribute('aria-hidden', 'true');
  trigger.isConnected = true;
  let activations = 0;
  let openCalls = 0;
  trigger.click = () => { activations += 1; };
  const input = document.createElement('input');
  const picker = {
    disabled: false,
    readOnly: false,
    opened: false,
    isConnected: true,
    inputElement: input,
    open() { this.opened = true; openCalls += 1; },
    shadowRoot: {querySelector: selector => selector === '[part~="toggle-button"]' ? trigger : null}
  };
  assert.equal(qualifyCausewayCalendarTrigger(picker, 'Last Visit'), trigger);
  assert.equal(trigger.getAttribute('aria-hidden'), null);
  assert.equal(trigger.getAttribute('role'), 'button');
  assert.equal(trigger.getAttribute('tabindex'), '0');
  assert.equal(trigger.getAttribute('aria-label'), 'Open Last Visit calendar');
  assert.equal(trigger.hasAttribute('data-causeway-calendar-trigger'), true);
  input.focus();
  const forwardTab = new Event('keydown', {cancelable: true});
  forwardTab.key = 'Tab';
  input.dispatchEvent(forwardTab);
  assert.equal(forwardTab.defaultPrevented, true);
  assert.equal(document.activeElement, trigger);
  const reverseTab = new Event('keydown', {cancelable: true});
  reverseTab.key = 'Tab';
  reverseTab.shiftKey = true;
  trigger.dispatchEvent(reverseTab);
  assert.equal(reverseTab.defaultPrevented, true);
  assert.equal(document.activeElement, input);
  for (const key of ['Enter', ' ']) {
    const keydown = new Event('keydown', {cancelable: true});
    keydown.key = key;
    trigger.dispatchEvent(keydown);
    assert.equal(keydown.defaultPrevented, true);
    const keyup = new Event('keyup', {cancelable: true});
    keyup.key = key;
    trigger.dispatchEvent(keyup);
    assert.equal(keyup.defaultPrevented, true);
    assert.equal(picker.opened, true);
    assert.equal(document.activeElement, input);
    picker.opened = false;
  }
  assert.equal(openCalls, 2);
  assert.equal(activations, 0);
  trigger.click();
  assert.equal(activations, 1);
  assert.equal(qualifyCausewayCalendarTrigger({...picker, readOnly: true}, 'Last Visit'), null);
  assert.equal(qualifyCausewayCalendarTrigger({...picker, disabled: true}, 'Last Visit'), null);
});

test('time trigger gains bounded button semantics and synchronous keyboard and pointer activation', () => {
  const trigger = document.createElement('div');
  trigger.setAttribute('aria-hidden', 'true');
  trigger.isConnected = true;
  let activations = 0;
  let openCalls = 0;
  trigger.click = () => { activations += 1; };
  const input = document.createElement('input');
  const picker = {
    disabled: false,
    readOnly: false,
    opened: false,
    isConnected: true,
    inputElement: input,
    open() { this.opened = true; openCalls += 1; },
    shadowRoot: {querySelector: selector => selector === '[part~="toggle-button"]' ? trigger : null}
  };
  assert.equal(qualifyCausewayTimeTrigger(picker, 'Visit At'), trigger);
  assert.equal(trigger.getAttribute('aria-hidden'), null);
  assert.equal(trigger.getAttribute('role'), 'button');
  assert.equal(trigger.getAttribute('tabindex'), '0');
  assert.equal(trigger.getAttribute('aria-label'), 'Open Visit At time picker');
  assert.equal(trigger.hasAttribute('data-causeway-time-trigger'), true);

  input.focus();
  const forwardTab = new Event('keydown', {cancelable: true});
  forwardTab.key = 'Tab';
  input.dispatchEvent(forwardTab);
  assert.equal(forwardTab.defaultPrevented, true);
  assert.equal(document.activeElement, trigger);

  const reverseTab = new Event('keydown', {cancelable: true});
  reverseTab.key = 'Tab';
  reverseTab.shiftKey = true;
  trigger.dispatchEvent(reverseTab);
  assert.equal(reverseTab.defaultPrevented, true);
  assert.equal(document.activeElement, input);

  for (const key of ['Enter', ' ']) {
    const keydown = new Event('keydown', {cancelable: true});
    keydown.key = key;
    trigger.dispatchEvent(keydown);
    assert.equal(keydown.defaultPrevented, true);
    const keyup = new Event('keyup', {cancelable: true});
    keyup.key = key;
    trigger.dispatchEvent(keyup);
    assert.equal(keyup.defaultPrevented, true);
    assert.equal(picker.opened, true);
    assert.equal(document.activeElement, input);
    picker.opened = false;
  }
  assert.equal(openCalls, 2);
  assert.equal(activations, 0);
  trigger.click();
  assert.equal(activations, 1);
  trigger.isConnected = false;
  picker.isConnected = false;
  const staleKeyup = new Event('keyup', {cancelable: true});
  staleKeyup.key = 'Enter';
  trigger.dispatchEvent(staleKeyup);
  assert.equal(picker.opened, false);
  assert.equal(openCalls, 2);
  assert.equal(qualifyCausewayTimeTrigger({...picker, readOnly: true}, 'Visit At'), null);
  assert.equal(qualifyCausewayTimeTrigger({...picker, disabled: true}, 'Visit At'), null);
  assert.equal(qualifyCausewayTimeTrigger({...picker, shadowRoot: {querySelector: () => null}}, 'Visit At'), null);
});

test('editable temporal adapters use supported quarter-hour dropdown steps and qualify clock triggers', async () => {
  configureCausewayFieldWidgets({
    families: ['local-temporal'],
    moduleUrls: {'local-temporal': fakeLocalTemporalModule}
  });
  const timeEditor = new CausewayFieldEditorElement();
  timeEditor.setAttribute('id', 'visit-time');
  timeEditor.setAttribute('data-family', 'local-temporal');
  timeEditor.setAttribute('data-control', 'time-picker');
  timeEditor.setAttribute('data-causeway-editor', 'visitAt');
  timeEditor.setAttribute('data-label', 'Visit At');
  timeEditor.setAttribute('data-value', '13:14:15.123');
  document.body.appendChild(timeEditor);
  await new Promise(resolve => setTimeout(resolve, 0));
  const timeControl = timeEditor.childNodes[0];
  assert.equal(timeControl.step, 900);
  assert.equal(timeControl.value, '13:14:15.123');
  assert.equal(timeControl._trigger.getAttribute('aria-label'), 'Open Visit At time picker');
  assert.equal(timeControl._trigger.hasAttribute('data-causeway-time-trigger'), true);

  const dateTimeEditor = new CausewayFieldEditorElement();
  dateTimeEditor.setAttribute('id', 'visit-date-time');
  dateTimeEditor.setAttribute('data-family', 'local-temporal');
  dateTimeEditor.setAttribute('data-control', 'date-time-picker');
  dateTimeEditor.setAttribute('data-causeway-editor', 'visitAt');
  dateTimeEditor.setAttribute('data-label', 'Visit At');
  dateTimeEditor.setAttribute('data-value', '2026-08-24T13:14:15.123');
  document.body.appendChild(dateTimeEditor);
  await new Promise(resolve => setTimeout(resolve, 0));
  const dateTimeControl = dateTimeEditor.childNodes[0];
  assert.equal(dateTimeControl.step, 900);
  assert.equal(dateTimeControl.value, '2026-08-24T13:14:15.123');
  assert.equal(dateTimeControl._datePicker._trigger.hasAttribute('data-causeway-calendar-trigger'), true);
  assert.equal(dateTimeControl._timePicker._trigger.hasAttribute('data-causeway-time-trigger'), true);
  assert.equal(dateTimeControl._timePicker._trigger.getAttribute('aria-label'), 'Open Visit At time picker');

  document.body.removeChild(timeEditor);
  document.body.removeChild(dateTimeEditor);
});

test('editable temporal adapters apply matching date time and date-time ranges before values', async () => {
  configureCausewayFieldWidgets({
    families: ['local-temporal'],
    moduleUrls: {'local-temporal': fakeLocalTemporalModule}
  });
  const cases = [
    ['date-picker', '2026-01-01', '2026-12-31', '2026-08-31'],
    ['time-picker', '08:00', '18:00', '13:14:15.123'],
    ['date-time-picker', '2026-08-31T08:00', '2026-08-31T18:00', '2026-08-31T13:14:15.123']
  ];
  for (const [controlName, min, max, value] of cases) {
    const editor = new CausewayFieldEditorElement();
    editor.setAttribute('data-family', 'local-temporal');
    editor.setAttribute('data-control', controlName);
    editor.setAttribute('data-causeway-editor', 'bounded');
    editor.setAttribute('data-label', 'Bounded value');
    editor.setAttribute('data-value', value);
    editor.setAttribute('data-min', min);
    editor.setAttribute('data-max', max);
    document.body.appendChild(editor);
    await new Promise(resolve => setTimeout(resolve, 0));
    const control = editor.childNodes[0];
    assert.equal(control.min, min);
    assert.equal(control.max, max);
    assert.equal(control.value, value);
    if (['time-picker', 'date-time-picker'].includes(controlName)) assert.equal(control.step, 900);
    document.body.removeChild(editor);
  }

  const html = renderCausewayFieldWidget(context({
    codec: {id: 'temporal'}, semanticType: 'LocalTime', controlValue: '09:00', min: '08:00', max: '18:00'
  }));
  assert.match(html, /data-min="08:00"/);
  assert.match(html, /data-max="18:00"/);
});

test('native temporal fallback receives the same escaped range attributes', () => {
  configureCausewayFieldWidgets({families: []});
  const rendered = renderCausewayEditor(context({
    codec: undefined,
    semanticType: 'LocalDate',
    inputType: {kind: 'SCALAR', name: 'LocalDate'},
    controlValue: '2026-08-31',
    min: '2026-01-01',
    max: '2026-12-31'
  }));
  assert.equal(rendered.editorId, 'temporal');
  assert.match(rendered.html, /type="date"[^>]+min="2026-01-01" max="2026-12-31"/);
});

test('read-only temporal adapters preserve millisecond steps and hide operable triggers', async () => {
  configureCausewayFieldWidgets({
    families: ['local-temporal'],
    presentation: true,
    moduleUrls: {'local-temporal': fakeLocalTemporalModule}
  });
  const view = new CausewayFieldEditorElement();
  view.setAttribute('id', 'visit-time-view');
  view.setAttribute('data-mode', 'view');
  view.setAttribute('data-family', 'local-temporal');
  view.setAttribute('data-control', 'time-picker');
  view.setAttribute('data-label', 'Visit At');
  view.setAttribute('data-value', '13:14:15.123');
  view.setAttribute('data-min', '08:00');
  view.setAttribute('data-max', '18:00');
  document.body.appendChild(view);
  await new Promise(resolve => setTimeout(resolve, 0));
  const control = view.childNodes[0];
  assert.equal(control.step, 0.001);
  assert.equal(control.value, '13:14:15.123');
  assert.equal(control.min, undefined);
  assert.equal(control.max, undefined);
  assert.equal(control._trigger.getAttribute('aria-hidden'), 'true');
  assert.equal(control._trigger.hasAttribute('data-causeway-time-trigger'), false);
  document.body.removeChild(view);
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
  document.body.appendChild(disconnected);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(disconnected.dataset.widgetState, 'ready');
  document.body.removeChild(disconnected);
});

test('read-only adapters upgrade without editor behavior or duplicate labels', async () => {
  configureCausewayFieldWidgets({families: ['basic'], presentation: true, moduleUrls: {basic: fakeBasicModule}});
  const view = new CausewayFieldEditorElement();
  view.setAttribute('id', 'field-view');
  view.setAttribute('data-mode', 'view');
  view.setAttribute('data-family', 'basic');
  view.setAttribute('data-control', 'text-field');
  view.setAttribute('data-label', 'Name');
  view.setAttribute('data-value', 'Ada');
  view.setAttribute('data-labelledby', 'name-label');
  view.setAttribute('data-describedby', 'name-description');
  document.body.appendChild(view);
  await new Promise(resolve => setTimeout(resolve, 0));
  const control = view.childNodes[0];
  assert.equal(view.dataset.widgetState, 'ready');
  assert.equal(control.localName, 'vaadin-text-field');
  assert.equal(control.value, 'Ada');
  assert.equal(control.hasAttribute('readonly'), true);
  assert.equal(control.accessibleNameRef, 'name-label');
  assert.equal(control.inputElement.getAttribute('aria-describedby'), 'name-description');
  assert.equal(control.getAttribute('data-causeway-field-view'), '');
  assert.equal(control.hasAttribute('data-causeway-editor'), false);
  assert.equal(control.childNodes.length, 0);
  document.body.removeChild(view);
});

test('policy revisions cannot complete stale field upgrades', async () => {
  let resolveDelayedModule;
  globalThis.causewayDelayedBasicModulePromise = new Promise(resolve => { resolveDelayedModule = resolve; });
  const delayedModule = new URL('./fixtures/fake-vaadin-delayed-basic.mjs', import.meta.url).href;
  configureCausewayFieldWidgets({families: ['basic'], presentation: true, moduleUrls: {basic: delayedModule}});
  const view = new CausewayFieldEditorElement();
  view.setAttribute('data-mode', 'view');
  view.setAttribute('data-family', 'basic');
  view.setAttribute('data-control', 'delayed-text-field');
  document.body.appendChild(view);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(view.dataset.widgetState, 'loading');
  configureCausewayFieldWidgets({families: [], presentation: false});
  resolveDelayedModule();
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.notEqual(view.dataset.widgetState, 'ready');
  document.body.removeChild(view);
  delete globalThis.causewayDelayedBasicModulePromise;
});

test('qualified optional fields expose a labelled keyboard clear suffix', async () => {
  configureCausewayFieldWidgets({families: ['basic'], moduleUrls: {basic: fakeBasicModule}});
  const editor = new CausewayFieldEditorElement();
  editor.setAttribute('id', 'field-clear');
  editor.setAttribute('data-family', 'basic');
  editor.setAttribute('data-control', 'text-field');
  editor.setAttribute('data-causeway-editor', 'knownAs');
  editor.setAttribute('data-label', 'Known As');
  editor.setAttribute('data-value', 'Mary');
  editor.setAttribute('data-testid', 'known-as-editor');
  let inputEvents = 0;
  let commitEvents = 0;
  editor.addEventListener('input', () => { inputEvents += 1; });
  editor.addEventListener('causeway-editor-commit', () => { commitEvents += 1; });
  editor.focusClear();
  document.body.appendChild(editor);
  await new Promise(resolve => setTimeout(resolve, 0));

  const control = editor.childNodes[0];
  const clearButton = control.childNodes[0];
  assert.equal(control.clearButtonVisible, false);
  assert.equal(clearButton.localName, 'button');
  assert.equal(clearButton.getAttribute('slot'), 'suffix');
  assert.equal(clearButton.getAttribute('tabindex'), '0');
  assert.equal(clearButton.getAttribute('aria-label'), 'Clear Known As');
  assert.equal(clearButton.getAttribute('data-testid'), 'known-as-editor-clear');
  assert.equal(clearButton.hidden, false);
  await Promise.resolve();
  assert.equal(document.activeElement, clearButton);

  const internalFocusout = new Event('focusout', {bubbles: true, composed: true});
  internalFocusout.relatedTarget = clearButton;
  control.dispatchEvent(internalFocusout);
  assert.equal(commitEvents, 0);
  const externalFocusout = new Event('focusout', {bubbles: true, composed: true});
  externalFocusout.relatedTarget = document.createElement('button');
  control.dispatchEvent(externalFocusout);
  assert.equal(commitEvents, 1);

  control.value = '';
  control.dispatchEvent(new Event('change', {bubbles: true, composed: true}));
  assert.equal(clearButton.hidden, true);
  editor.value = 'Ada';
  assert.equal(clearButton.hidden, false);
  document.body.focus();
  editor.focusClear();
  await Promise.resolve();
  assert.equal(document.activeElement, clearButton);

  clearButton.focus();
  clearButton.dispatchEvent(new Event('click', {bubbles: true, cancelable: true}));
  assert.equal(editor.value, '');
  assert.equal(inputEvents, 1);
  assert.equal(clearButton.hidden, true);
  assert.equal(document.activeElement, control);
  document.body.removeChild(editor);
});

test('an inapplicable pre-upgrade clear focus request expires safely', async () => {
  configureCausewayFieldWidgets({families: ['basic'], moduleUrls: {basic: fakeBasicModule}});
  const editor = new CausewayFieldEditorElement();
  editor.setAttribute('data-family', 'basic');
  editor.setAttribute('data-control', 'text-field');
  editor.setAttribute('data-causeway-editor', 'knownAs');
  editor.setAttribute('data-label', 'Known As');
  editor.setAttribute('data-value', '');
  document.body.focus();
  editor.focusClear();
  document.body.appendChild(editor);
  await new Promise(resolve => setTimeout(resolve, 0));
  const clearButton = editor.childNodes[0].childNodes[0];
  assert.equal(clearButton.hidden, true);
  assert.equal(document.activeElement, document.body);
  editor.value = 'Later';
  await Promise.resolve();
  assert.notEqual(document.activeElement, clearButton);
  document.body.removeChild(editor);
});

test('required disabled and protected fields omit the clear suffix', async () => {
  configureCausewayFieldWidgets({families: ['basic'], moduleUrls: {basic: fakeBasicModule}});
  for (const attributes of [
    {control: 'text-field', required: true},
    {control: 'text-field', disabled: true},
    {control: 'password-field', sensitive: true}
  ]) {
    const editor = new CausewayFieldEditorElement();
    editor.setAttribute('data-family', 'basic');
    editor.setAttribute('data-control', attributes.control);
    editor.setAttribute('data-causeway-editor', 'value');
    editor.setAttribute('data-label', 'Value');
    editor.setAttribute('data-value', attributes.sensitive ? '' : 'current');
    if (attributes.required) editor.setAttribute('required', '');
    if (attributes.disabled) editor.setAttribute('disabled', '');
    if (attributes.sensitive) editor.setAttribute('data-sensitive', 'true');
    document.body.appendChild(editor);
    await new Promise(resolve => setTimeout(resolve, 0));
    assert.equal(editor.childNodes[0].childNodes.length, 0);
    assert.equal(editor.childNodes[0].clearButtonVisible, false);
    document.body.removeChild(editor);
  }
});

test('read-only failure emits a bounded value-free signal', async () => {
  configureCausewayFieldWidgets({families: ['basic'], presentation: true, moduleUrls: {
    basic: new URL('./fixtures/missing-field-view-module.mjs', import.meta.url).href
  }});
  const view = new CausewayFieldEditorElement();
  view.setAttribute('data-mode', 'view');
  view.setAttribute('data-family', 'basic');
  view.setAttribute('data-control', 'text-field');
  view.setAttribute('data-value', 'must-not-leak');
  let failure;
  view.addEventListener('causeway-field-view-load-failed', event => { failure = event.detail; });
  document.body.appendChild(view);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(view.dataset.widgetState, 'fallback');
  assert.deepEqual(Object.keys(failure).sort(), ['family', 'message']);
  assert.doesNotMatch(JSON.stringify(failure), /must-not-leak|missing-field-view-module/);
  assert.equal(supportsCausewayReadOnlyField(context()), false);
  document.body.removeChild(view);
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
