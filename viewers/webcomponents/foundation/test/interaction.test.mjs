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
  CausewayEditorRegistry,
  CausewayInteractionControllerElement,
  CausewayPropertyElement,
  CausewayReferenceEditorElement,
  InteractionStatus,
  autoCompleteWindowPlan,
  buildMutationInteractionOperation,
  buildObjectInteractionOperation,
  commandSelection,
  causewayReferenceWidgetConfiguration,
  configureCausewayReferenceWidgets,
  defaultEditorRegistry,
  normalizeAutoCompleteWindow,
  parseCausewayEditorValue,
  renderCausewayEditor
} = await import('../src/index.mjs');

const scalar = name => ({kind: 'SCALAR', name, ofType: null});
const named = name => ({kind: 'OBJECT', name, ofType: null});
const list = typeRef => ({kind: 'LIST', name: null, ofType: typeRef});
const argument = (name, type) => ({name, description: null, defaultValue: null, type});
const field = (name, type, args = []) => ({name, description: null, args, type});
const type = (name, fields) => ({name, kind: 'OBJECT', description: null, fields, inputFields: [], enumValues: []});

function objectDescription() {
  const objectName = 'rich__example_Object';
  const propertyName = `${objectName}__name__gqlv_property`;
  return {
    generatedTypeName: objectName,
    generatedInputTypeName: `${objectName}__gqlv_input`,
    generatedFieldName: 'example_Object',
    types: new Map([
      [objectName, type(objectName, [field('name', named(propertyName))])],
      [propertyName, type(propertyName, [
        field('validate', scalar('String'), [argument('name', scalar('String'))])
      ])]
    ])
  };
}

test('interaction operations derive typed variables from introspected fields', () => {
  const description = objectDescription();
  const operation = buildObjectInteractionOperation({
    description,
    identity: {id: '42'},
    selection: {name: {validate: commandSelection({name: 'Updated'})}},
    schemaNames: {richRootField: 'rich', lookupArgumentName: 'object'},
    operationName: 'CausewayValidateProperty'
  });
  assert.match(operation.document, /\$input\d+: String/);
  assert.match(operation.document, /validate\(name: \$input\d+\)/);
  assert.deepEqual(operation.variables.object, {id: '42'});
  assert.ok(Object.values(operation.variables).includes('Updated'));

  const mutationType = type('Mutation', [
    field('example_Object__name', named(description.generatedTypeName), [
      argument('_target', named(description.generatedInputTypeName)),
      argument('name', scalar('String'))
    ])
  ]);
  const mutation = buildMutationInteractionOperation({
    mutationType,
    fieldName: 'example_Object__name',
    args: {_target: {id: '42'}, name: 'Updated'},
    resultSelection: {__typename: true},
    types: new Map([...description.types, ['Mutation', mutationType]]),
    operationName: 'CausewayUpdateProperty'
  });
  assert.match(mutation.document, /^mutation CausewayUpdateProperty/);
  assert.match(mutation.document, /example_Object__name\(_target:/);
  assert.ok(Object.values(mutation.variables).some(value => value?.id === '42'));
  assert.ok(Object.values(mutation.variables).includes('Updated'));
});

test('interaction operations render and validate inline fragments', () => {
  const union = {...type('ActionResultUnion', []), kind: 'UNION', possibleTypes: [{kind: 'OBJECT', name: 'ResultType'}]};
  const resultType = type('ResultType', [field('_meta', named('ResultMeta'))]);
  const resultMeta = type('ResultMeta', [field('id', scalar('ID'))]);
  const mutationType = type('Mutation', [field('run', {kind: 'UNION', name: union.name, ofType: null})]);
  const types = new Map([
    ['Mutation', mutationType], [union.name, union], [resultType.name, resultType], [resultMeta.name, resultMeta]
  ]);

  const operation = buildMutationInteractionOperation({
    mutationType,
    fieldName: 'run',
    args: {},
    resultSelection: {
      __typename: true,
      __fragments: {ResultType: {_meta: {id: true}}}
    },
    types,
    operationName: 'CausewayRun'
  });

  assert.match(operation.document, /__typename/);
  assert.match(operation.document, /\.\.\. on ResultType/);
  assert.throws(() => buildMutationInteractionOperation({
    mutationType,
    fieldName: 'run',
    args: {},
    resultSelection: {__fragments: {OtherType: {__typename: true}}},
    types,
    operationName: 'CausewayInvalidRun'
  }), /not advertised/);
});

test('autocomplete windows derive advertised selections and normalize paging metadata', () => {
  const itemType = type('Choice', [field('_meta', named('ChoiceMeta'))]);
  const metadataType = type('ChoiceMeta', [field('id', scalar('ID')), field('logicalTypeName', scalar('String'))]);
  const windowType = type('ChoiceWindow', [
    field('items', list(named('Choice'))), field('offset', scalar('Int')),
    field('requestedSize', scalar('Int')), field('returnedCount', scalar('Int')),
    field('totalCount', scalar('Int')), field('maximumSize', scalar('Int')),
    field('hasPrevious', scalar('Boolean')), field('hasNext', scalar('Boolean')),
    field('ordering', scalar('String'))
  ]);
  const windowField = field('autoCompleteWindow', named('ChoiceWindow'), [
    {...argument('offset', scalar('Int')), defaultValue: '0'},
    {...argument('size', scalar('Int')), defaultValue: '5'}
  ]);
  const plan = autoCompleteWindowPlan(windowField, new Map([
    ['ChoiceWindow', windowType], ['Choice', itemType], ['ChoiceMeta', metadataType]
  ]));

  assert.equal(plan.offsetDefault, 0);
  assert.equal(plan.sizeDefault, 5);
  assert.deepEqual(plan.selection.items, {_meta: {id: true, logicalTypeName: true}});
  const normalized = normalizeAutoCompleteWindow({
    items: [{_meta: {id: '6', logicalTypeName: 'example.Choice'}}], offset: 5,
    requestedSize: 5, returnedCount: 1, totalCount: 6, maximumSize: 5,
    hasPrevious: true, hasNext: false, ordering: 'APPLICATION'
  });
  assert.equal(normalized.windowed, true);
  assert.equal(normalized.totalCount, 6);
  assert.equal(normalized.items.length, 1);
});

test('editor registry selects standard inputs and supports application overrides', () => {
  const text = renderCausewayEditor({
    name: 'name', value: 'Classics', choices: [], enumValues: [], inputType: scalar('String'),
    inputId: 'name-input', labelId: 'name-label', descriptionId: '', errorId: '', testId: 'name-editor'
  });
  assert.equal(text.editorId, 'vaadin-field');
  assert.match(text.html, /data-family="basic" data-control="text-field"/);

  const multiline = renderCausewayEditor({
    name: 'notes', value: 'First line\nSecond line', choices: [], enumValues: [], inputType: scalar('String'), multiLine: 5,
    inputId: 'notes-input', labelId: 'notes-label', descriptionId: 'notes-description', errorId: '', testId: 'notes-editor'
  });
  assert.equal(multiline.editorId, 'vaadin-field');
  assert.match(multiline.html, /data-family="basic" data-control="text-area"/);
  assert.match(multiline.html, /data-rows="5"/);
  assert.match(multiline.html, /data-value="First line\nSecond line"/);
  assert.equal(parseCausewayEditorValue(multiline.editor, {value: 'Updated\nnotes'}), 'Updated\nnotes');

  const number = renderCausewayEditor({
    name: 'capacity', value: 24, choices: [], enumValues: [], inputType: scalar('Int'),
    inputId: 'capacity-input', labelId: 'capacity-label', descriptionId: '', errorId: '', testId: ''
  });
  assert.equal(number.editorId, 'vaadin-field');
  assert.equal(parseCausewayEditorValue(number.editor, {value: '25', inputType: scalar('Int')}), 25);

  const dateTime = renderCausewayEditor({
    name: 'visitAt', value: '2026-08-20T09:00:00', choices: [], enumValues: [], inputType: scalar('LocalDateTime'),
    inputId: 'visit-at-input', labelId: 'visit-at-label', descriptionId: '', errorId: '', testId: ''
  });
  assert.equal(dateTime.editorId, 'vaadin-field');
  assert.match(dateTime.html, /data-family="local-temporal" data-control="date-time-picker"/);
  assert.equal(parseCausewayEditorValue(dateTime.editor, {value: '2026-08-21T10:15:00'}), '2026-08-21T10:15:00');

  const choices = renderCausewayEditor({
    name: 'status', value: 'ACTIVE', choices: ['ACTIVE', 'PAUSED'], enumValues: [], inputType: {kind: 'ENUM', name: 'Status'},
    inputId: 'status-input', labelId: 'status-label', descriptionId: '', errorId: '', testId: ''
  });
  assert.equal(choices.editorId, 'vaadin-field');
  assert.match(choices.html, /data-family="basic" data-control="select"/);

  const registry = new CausewayEditorRegistry(defaultEditorRegistry.registrations);
  registry.register({id: 'custom-name', priority: 1000, supports: context => context.name === 'name', render: () => '<textarea></textarea>'});
  assert.equal(registry.select({name: 'name'}).id, 'custom-name');
});

test('property interactions preserve exact decimal lexical state and map codec errors locally', async () => {
  let stateListener;
  const calls = [];
  const context = {
    registerRequirement(requirement, listener) {
      stateListener = listener;
      return () => {};
    },
    async prepareProperty() {
      return {
        status: 'success', errors: [], data: {
          capabilities: {validate: true, inputType: scalar('String'), enumValues: []},
          choices: []
        }
      };
    },
    async validateProperty(member, value) {
      calls.push(`validate:${value}`);
      return {status: 'success', data: null, errors: []};
    },
    async updateProperty(member, value) {
      calls.push(`update:${value}`);
      return {status: 'success', data: {_meta: {id: '42'}}, errors: []};
    }
  };
  const property = new CausewayPropertyElement();
  property.id = 'amount';
  property.editable = true;
  property.context = context;
  document.body.appendChild(property);
  stateListener({
    status: 'ready',
    descriptor: {id: 'amount', value: {typeRef: scalar('String')}},
    data: {hidden: false, disabled: null, datatype: 'rich__java_math_BigDecimal', get: '1.2300'},
    errors: [], generation: 1
  });
  assert.equal(await property.beginEdit(), true);
  assert.equal(property.getAttribute('data-editor'), 'vaadin-field');
  assert.match(property.innerHTML, /value="1.2300"/);

  const invalid = new Event('input');
  invalid.target = {
    getAttribute: name => name === 'data-causeway-editor' ? 'amount' : null,
    value: '1.2.3',
    checked: false
  };
  property.dispatchEvent(invalid);
  assert.equal(property.interactionState.status, 'failed');
  assert.match(property.interactionState.error, /exact decimal/);
  assert.equal(property.interactionState.pendingValue, '1.2.3');
  assert.deepEqual(calls, []);

  const corrected = new Event('input');
  corrected.target = {...invalid.target, value: '9007199254740993.1200'};
  property.dispatchEvent(corrected);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(property.interactionState.pendingValue, '9007199254740993.1200');
  assert.ok(calls.includes('validate:9007199254740993.1200'));
  property.cancelEdit();
  assert.equal(calls.some(call => call.startsWith('update:')), false);
});

test('default reference widgets preserve identities, bounds and explicit native fallback', () => {
  assert.equal(causewayReferenceWidgetConfiguration().enabled, true);
  const references = [
    {_meta: {id: 'owner-1', logicalTypeName: 'example.Owner', title: 'Owner One'}},
    {_meta: {id: 'owner-2', logicalTypeName: 'example.Owner', title: 'Owner Two'}}
  ];
  configureCausewayReferenceWidgets({enabled: true, minimumSearchLength: 3, maximumResults: 2});
  const single = renderCausewayEditor({
    name: 'owner', label: 'Owner', value: references[0], choices: references, suggestions: [], autoComplete: false,
    enumValues: [], inputType: named('example_Owner'), inputId: 'owner-input', labelId: 'owner-label',
    descriptionId: '', errorId: '', testId: 'owner-editor', required: true, disabled: false
  });
  assert.equal(single.editorId, 'vaadin-reference');
  assert.match(single.html, /<cw-reference-editor/);
  assert.match(single.html, /data-minimum-search-length="3"/);
  assert.match(single.html, /required/);
  assert.deepEqual(parseCausewayEditorValue(single.editor, {value: {id: 'owner-2'}}), {id: 'owner-2'});

  const multiple = renderCausewayEditor({
    name: 'owners', label: 'Owners', value: references, choices: references, suggestions: [], autoComplete: false,
    enumValues: [], inputType: list(named('example_Owner')), inputId: 'owners-input', labelId: 'owners-label',
    descriptionId: '', errorId: '', testId: 'owners-editor', required: false, disabled: false
  });
  assert.equal(multiple.editorId, 'vaadin-reference');
  assert.match(multiple.html, / multiple/);
  assert.deepEqual(parseCausewayEditorValue(multiple.editor, {value: [{id: 'owner-1'}]}), [{id: 'owner-1'}]);

  const overBound = renderCausewayEditor({
    name: 'owner', value: null, choices: [...references, {_meta: {id: 'owner-3', title: 'Owner Three'}}],
    suggestions: [], autoComplete: false, enumValues: [], inputType: named('example_Owner'),
    inputId: 'owner-input', labelId: 'owner-label', descriptionId: '', errorId: '', testId: ''
  });
  assert.equal(overBound.editorId, 'choice');
  configureCausewayReferenceWidgets({enabled: false});
  const nativeFallback = renderCausewayEditor({
    name: 'owner', value: references[0], choices: references, suggestions: [], autoComplete: false,
    enumValues: [], inputType: named('example_Owner'), inputId: 'owner-input', labelId: 'owner-label',
    descriptionId: '', errorId: '', testId: ''
  });
  assert.equal(nativeFallback.editorId, 'choice');
});

test('editable properties support prepare, validation, cancel and authoritative save', async () => {
  let stateListener;
  let resolveDeferredValidation;
  const calls = [];
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    registerRequirement(requirement, listener) {
      assert.deepEqual(requirement, {kind: 'property', member: 'name'});
      stateListener = listener;
      return () => {};
    },
    async prepareProperty() {
      calls.push('prepare');
      return {status: 'success', data: {capabilities: {validate: true, inputType: scalar('String'), enumValues: []}, choices: []}, errors: []};
    },
    async validateProperty(member, value) {
      calls.push(`validate:${value}`);
      if (value === 'Deferred') {
        return new Promise(resolve => {
          resolveDeferredValidation = () => resolve({status: 'success', data: 'Obsolete validation result.', errors: []});
        });
      }
      return {status: 'success', data: value.length < 3 ? 'Use at least three characters.' : null, errors: []};
    },
    async updateProperty(member, value) {
      calls.push(`update:${value}`);
      return {status: 'success', data: {_meta: {id: '42'}}, errors: []};
    }
  };
  const property = new CausewayPropertyElement();
  property.id = 'name';
  property.editable = true;
  property.multiLine = 5;
  property.setAttribute('data-testid', 'property-name');
  property.context = context;
  document.body.appendChild(property);
  stateListener({
    status: 'ready',
    descriptor: {id: 'name', description: "Owner's full name", value: {typeRef: scalar('String')}},
    data: {hidden: false, disabled: 'Locked by policy', get: 'Classics'}, errors: [], generation: 1
  });
  assert.doesNotMatch(property.innerHTML, /property-name-edit/);
  assert.match(property.innerHTML, /class="causeway-property-label causeway-member-tooltip causeway-property-disabled-tooltip"/);
  assert.doesNotMatch(property.innerHTML, /title="Owner&#39;s full name"/);
  assert.match(property.innerHTML, /tabindex="0"/);
  assert.match(property.innerHTML, /data-tooltip="Owner&#39;s full name\s+Locked by policy"/);
  assert.match(property.innerHTML, /aria-describedby="causeway-property-description-[^"]+ causeway-property-reason-[^"]+"/);
  assert.match(property.innerHTML, /causeway-visually-hidden">Locked by policy/);
  assert.doesNotMatch(property.innerHTML, /causeway-property-disabled-indicator|&#9432;/);
  assert.match(property.innerHTML, /class="causeway-property-value causeway-property-value-string"/);
  stateListener({
    status: 'ready',
    descriptor: {id: 'name', description: "Owner's full name", value: {typeRef: scalar('Int')}},
    data: {hidden: false, disabled: null, get: 18}, errors: [], generation: 2
  });
  assert.doesNotMatch(property.innerHTML, /causeway-property-value-string/);
  stateListener({
    status: 'ready',
    descriptor: {id: 'name', description: "Owner's full name", value: {typeRef: scalar('String')}},
    data: {hidden: false, disabled: null, get: 'Classics'}, errors: [], generation: 3
  });
  assert.match(property.innerHTML, /property-name-edit/);
  assert.match(property.innerHTML, /data-multi-line="5"/);
  assert.match(property.innerHTML, /causeway-property-value-string/);
  assert.doesNotMatch(property.innerHTML, /causeway-property-disabled-tooltip|data-tooltip="Locked by policy"|tabindex="0"/);
  assert.match(property.innerHTML, /class="causeway-property-edit"[^>]+data-causeway-action="edit"/);
  property.descriptionAs = 'tooltip';
  assert.match(property.innerHTML, /causeway-property-label causeway-member-tooltip/);
  assert.match(property.innerHTML, /causeway-property-description causeway-visually-hidden/);
  property.descriptionAs = 'label';
  assert.doesNotMatch(property.innerHTML, /causeway-member-tooltip/);
  assert.deepEqual(calls, []);
  assert.match(property.innerHTML, /aria-label="Edit Name"/);
  assert.match(property.innerHTML, /title="Edit Name"/);
  assert.match(property.innerHTML, /<svg class="causeway-property-edit-icon"[^>]+aria-hidden="true"[^>]+focusable="false"/);
  assert.doesNotMatch(property.innerHTML, />Edit Name</);
  assert.equal(await property.beginEdit(), true);
  assert.match(property.innerHTML, /property-name-editor/);
  assert.match(property.innerHTML, /causeway-property-editing[^>]+data-multi-line="5"/);
  assert.match(property.innerHTML, /data-control="text-area"[^>]+data-rows="5"/);
  assert.doesNotMatch(property.innerHTML, /causeway-property-interaction-status/);
  assert.doesNotMatch(property.innerHTML, />Editing</);
  assert.match(property.innerHTML, /data-causeway-action="save"[^>]+aria-label="Save Name"[^>]+title="Save Name"/);
  assert.match(property.innerHTML, /data-causeway-action="cancel"[^>]+aria-label="Cancel editing Name"[^>]+title="Cancel editing Name"/);
  assert.equal([...property.innerHTML.matchAll(/<svg class="causeway-property-editor-action-icon"[^>]+aria-hidden="true"[^>]+focusable="false"/g)].length, 2);
  assert.doesNotMatch(property.innerHTML, />(Save|Cancel)</);
  property.setPendingValue('Deferred');
  const pendingValidation = property.validatePending();
  const editorTarget = {
    getAttribute: name => name === 'data-causeway-editor' ? 'true' : null,
    value: 'Deferred',
    checked: false
  };
  const focusout = new Event('focusout');
  focusout.target = editorTarget;
  property.dispatchEvent(focusout);
  await Promise.resolve();
  assert.equal(calls.filter(call => call === 'validate:Deferred').length, 1);
  property.setPendingValue('Superseding value');
  resolveDeferredValidation();
  await pendingValidation;
  assert.equal(property.interactionState.pendingValue, 'Superseding value');
  assert.equal(property.interactionState.error, null);
  property.setPendingValue('No');
  await property.validatePending();
  assert.match(property.innerHTML, /Use at least three characters/);
  const correctedInput = new Event('input');
  correctedInput.target = {...editorTarget, value: 'Updated name'};
  property.dispatchEvent(correctedInput);
  assert.equal(property.interactionState.pendingValue, 'Updated name');
  assert.equal(property.interactionState.status, 'editing');
  assert.equal(await property.saveEdit(), true);
  assert.ok(calls.includes('update:Updated name'));
  assert.equal(property.interactionState, null);

  await property.beginEdit();
  property.setPendingValue('Cancelled');
  assert.equal(property.cancelEdit(), true);
  assert.equal(calls.includes('update:Cancelled'), false);
});

test('property validation preserves Save and Cancel focus across consecutive renders', async () => {
  for (const actionName of ['save', 'cancel']) {
    let stateListener;
    let resolveValidation;
    const context = {
      registerRequirement(requirement, listener) {
        stateListener = listener;
        return () => {};
      },
      async prepareProperty() {
        return {status: 'success', data: {capabilities: {validate: true, inputType: scalar('String'), enumValues: []}, choices: []}, errors: []};
      },
      async validateProperty() {
        return new Promise(resolve => { resolveValidation = resolve; });
      }
    };
    const property = new CausewayPropertyElement();
    property.id = 'name';
    property.editable = true;
    property.context = context;
    document.body.appendChild(property);
    stateListener({
      status: 'ready',
      descriptor: {id: 'name', description: '', value: {typeRef: scalar('String')}},
      data: {hidden: false, disabled: null, get: 'Original'}, errors: [], generation: 1
    });
    assert.equal(await property.beginEdit(), true);
    await Promise.resolve();

    const oldAction = document.createElement('button');
    oldAction.setAttribute('data-causeway-action', actionName);
    property.appendChild(oldAction);
    let replacementAction = document.createElement('button');
    replacementAction.setAttribute('data-causeway-action', actionName);
    property.appendChild(replacementAction);
    property.querySelector = selector => selector === `[data-causeway-action="${actionName}"]` ? replacementAction : null;
    oldAction.focus();
    property.setPendingValue('Changed');

    const validation = property.validatePending();
    assert.equal(document.activeElement, replacementAction);
    assert.match(property.innerHTML, /data-causeway-action="save"[^>]+aria-disabled="true"/);
    assert.doesNotMatch(property.innerHTML, /data-causeway-action="save"[^>]+ disabled/);
    const finalAction = document.createElement('button');
    finalAction.setAttribute('data-causeway-action', actionName);
    property.appendChild(finalAction);
    replacementAction = finalAction;
    resolveValidation({status: 'success', data: null, errors: []});
    await validation;
    assert.equal(document.activeElement, finalAction);
    assert.match(property.innerHTML, /data-causeway-action="save"[^>]+aria-disabled="false"/);
    document.body.removeChild(property);
  }
});

test('property preserves clear focus intent across validation renders but not external departure', async () => {
  let stateListener;
  const validationResolvers = [];
  const context = {
    registerRequirement(requirement, listener) {
      stateListener = listener;
      return () => {};
    },
    async prepareProperty() {
      return {status: 'success', data: {capabilities: {validate: true, inputType: scalar('String'), enumValues: []}, choices: []}, errors: []};
    },
    async validateProperty() {
      return new Promise(resolve => validationResolvers.push(resolve));
    }
  };
  const property = new CausewayPropertyElement();
  property.id = 'knownAs';
  property.editable = true;
  property.context = context;
  document.body.appendChild(property);
  stateListener({
    status: 'ready',
    descriptor: {id: 'knownAs', description: '', value: {typeRef: scalar('String')}},
    data: {hidden: false, disabled: null, get: 'Original'}, errors: [], generation: 1
  });
  assert.equal(await property.beginEdit(), true);
  await Promise.resolve();

  const oldClear = document.createElement('button');
  oldClear.setAttribute('data-causeway-field-clear', '');
  property.appendChild(oldClear);
  const firstAdapter = {requests: 0, focusClear() { this.requests += 1; }};
  let replacementAdapter = firstAdapter;
  property.querySelector = selector => selector === 'cw-field-editor' ? replacementAdapter : null;
  oldClear.focus();
  property.setPendingValue('Changed');

  const validation = property.validatePending();
  assert.equal(firstAdapter.requests, 1);
  document.body.focus();
  const secondAdapter = {requests: 0, focusClear() { this.requests += 1; }};
  replacementAdapter = secondAdapter;
  validationResolvers.shift()({status: 'success', data: null, errors: []});
  await validation;
  assert.equal(secondAdapter.requests, 1);

  const external = document.createElement('button');
  external.focus();
  const departure = new Event('focusout', {bubbles: true});
  departure.relatedTarget = external;
  property.dispatchEvent(departure);
  const thirdAdapter = {requests: 0, focusClear() { this.requests += 1; }};
  replacementAdapter = thirdAdapter;
  property.setPendingValue('Again');
  const laterValidation = property.validatePending();
  assert.equal(thirdAdapter.requests, 0);
  validationResolvers.shift()({status: 'success', data: null, errors: []});
  await laterValidation;
  assert.equal(thirdAdapter.requests, 0);
  document.body.removeChild(property);
});

test('windowed Vaadin reference adapter requests authoritative later pages', async () => {
  const moduleSource = `
    if (!globalThis.customElements.get('vaadin-combo-box')) globalThis.customElements.define('vaadin-combo-box', class extends HTMLElement {});
    if (!globalThis.customElements.get('vaadin-multi-select-combo-box')) globalThis.customElements.define('vaadin-multi-select-combo-box', class extends HTMLElement {});
  `;
  globalThis.customElements.whenDefined ??= async () => {};
  configureCausewayReferenceWidgets({
    enabled: true,
    minimumSearchLength: 1,
    maximumResults: 50,
    moduleUrl: `data:text/javascript,${encodeURIComponent(moduleSource)}`
  });
  const editor = new CausewayReferenceEditorElement();
  editor.dataset ??= {};
  editor.childNodes ??= [];
  editor.replaceChildren = (...children) => {
    editor.childNodes = children;
    for (const child of children) child.parentNode = editor;
  };
  editor.id = 'owner-editor';
  editor.setAttribute('data-causeway-editor', 'owner');
  editor.setAttribute('data-label', 'Owner');
  editor.setAttribute('data-items', '[]');
  editor.setAttribute('data-value', 'null');
  editor.setAttribute('data-autocomplete', 'true');
  editor.setAttribute('data-autocomplete-window', 'true');
  editor.setAttribute('data-autocomplete-page-size', '2');
  Object.assign(editor.dataset, {
    autocomplete: 'true',
    autocompleteWindow: 'true',
    autocompletePageSize: '2',
    items: '[]',
    value: 'null',
    label: 'Owner'
  });
  let requested;
  const pendingRequests = new Map();
  editor.addEventListener('causeway-reference-search', event => {
    requested = event.detail;
    pendingRequests.set(event.detail.search, event.detail);
    if (event.detail.search === 'Ow') {
      event.detail.respond({
        status: 'success',
        data: {
          items: [{_meta: {id: 'owner-3', logicalTypeName: 'example.Owner', title: 'Owner 3'}}],
          offset: 2,
          requestedSize: 2,
          returnedCount: 1,
          totalCount: 3,
          maximumSize: 2,
          hasPrevious: true,
          hasNext: false,
          ordering: 'APPLICATION',
          windowed: true
        }
      });
    }
  });
  document.body.appendChild(editor);
  editor.focus({preventScroll: true});
  for (let index = 0; index < 20 && editor.dataset.widgetState !== 'ready'; index += 1) {
    await new Promise(resolve => setTimeout(resolve, 0));
  }
  assert.equal(editor.dataset.widgetState, 'ready', editor.dataset.widgetError);
  const control = editor.childNodes[0];
  assert.equal(editor._focusRequested, false);
  assert.equal(editor._focusOptions, undefined);
  assert.equal(control.pageSize, 2);
  let callbackResult;

  control.dataProvider({filter: 'Ow', page: 1, pageSize: 2}, (items, total) => {
    callbackResult = {items, total};
  });
  await new Promise(resolve => setTimeout(resolve, 0));

  assert.equal(requested.offset, 2);
  assert.equal(requested.size, 2);
  assert.deepEqual(callbackResult, {
    items: [{id: 'owner-3', logicalTypeName: 'example.Owner', title: 'Owner 3'}],
    total: 3
  });

  let staleCallback;
  let currentCallback;
  control.dataProvider({filter: 'Old', page: 0, pageSize: 2}, (items, total) => staleCallback = {items, total});
  control.dataProvider({filter: 'New', page: 0, pageSize: 2}, (items, total) => currentCallback = {items, total});
  pendingRequests.get('Old').respond({status: 'success', data: {
    items: [{_meta: {id: 'old', title: 'Old'}}], totalCount: 1, windowed: true
  }});
  pendingRequests.get('New').respond({status: 'success', data: {
    items: [{_meta: {id: 'new', title: 'New'}}], totalCount: 1, windowed: true
  }});
  assert.equal(staleCallback, undefined);
  assert.deepEqual(currentCallback, {items: [{id: 'new', title: 'New'}], total: 1});
  document.body.removeChild(editor);

  const staleEditor = new CausewayReferenceEditorElement();
  staleEditor.setAttribute('data-causeway-editor', 'stale-owner');
  staleEditor.setAttribute('data-items', '[]');
  staleEditor.setAttribute('data-value', 'null');
  document.body.appendChild(staleEditor);
  staleEditor.focus();
  document.body.removeChild(staleEditor);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(staleEditor._focusRequested, false);
  assert.equal(staleEditor._control, null);
});

test('default reference module failure is bounded and falls back natively', async () => {
  configureCausewayReferenceWidgets({
    enabled: true,
    moduleUrl: new URL('./fixtures/missing-reference-module.mjs', import.meta.url).href
  });
  const editor = new CausewayReferenceEditorElement();
  editor.id = 'failed-reference';
  editor.setAttribute('data-causeway-editor', 'owner');
  editor.setAttribute('data-label', 'Owner');
  editor.setAttribute('data-items', JSON.stringify([{id: 'owner-1', title: 'Owner One'}]));
  editor.setAttribute('data-value', 'null');
  let failure;
  editor.addEventListener('causeway-reference-load-failed', event => { failure = event.detail; });

  document.body.appendChild(editor);
  editor.focus();
  await new Promise(resolve => setTimeout(resolve, 0));
  await new Promise(resolve => setTimeout(resolve, 0));

  assert.equal(editor.dataset.widgetState, 'fallback');
  assert.equal(editor.childNodes[0].localName, 'select');
  assert.equal(editor._focusRequested, false);
  assert.equal(editor._focusOptions, undefined);
  assert.equal(failure.message, 'The configured reference adapter could not be loaded.');
  assert.doesNotMatch(failure.message, /missing-reference-module/);
  document.body.removeChild(editor);
});

test('reference autocomplete cancels stale work and rejects over-bound results', async () => {
  configureCausewayReferenceWidgets({enabled: true, minimumSearchLength: 2, maximumResults: 2});
  let stateListener;
  const pending = new Map();
  const context = {
    registerRequirement(requirement, listener) {
      stateListener = listener;
      return () => {};
    },
    async prepareProperty() {
      return {status: 'success', data: {capabilities: {autoComplete: true, validate: false, inputType: named('example_Owner')}, choices: []}, errors: []};
    },
    autoCompleteProperty(member, search, {signal}) {
      return new Promise(resolve => {
        pending.set(search, resolve);
        signal.addEventListener('abort', () => resolve({status: 'obsolete', data: null, errors: []}), {once: true});
      });
    }
  };
  const property = new CausewayPropertyElement();
  property.id = 'owner';
  property.editable = true;
  property.context = context;
  document.body.appendChild(property);
  stateListener({
    status: 'ready', descriptor: {id: 'owner', description: 'Owner'},
    data: {hidden: false, disabled: null, get: {_meta: {id: 'owner-1', logicalTypeName: 'example.Owner', title: 'Owner One'}}},
    errors: [], generation: 1
  });
  assert.equal(await property.beginEdit(), true);
  assert.equal(property.interactionState.editor.id, 'vaadin-reference');
  const first = property.loadAutoComplete('Ow');
  const second = property.loadAutoComplete('Own');
  pending.get('Own')({status: 'success', data: [
    {_meta: {id: 'owner-1', title: 'Owner One'}},
    {_meta: {id: 'owner-2', title: 'Owner Two'}}
  ], errors: []});
  assert.equal((await first).status, 'obsolete');
  assert.equal((await second).status, 'success');
  assert.equal(property.interactionState.suggestions.length, 2);

  const overBound = property.loadAutoComplete('Owner');
  pending.get('Owner')({status: 'success', data: [
    {_meta: {id: 'owner-1'}}, {_meta: {id: 'owner-2'}}, {_meta: {id: 'owner-3'}}
  ], errors: []});
  assert.equal((await overBound).status, 'failed');
  assert.match(property.interactionState.error, /More than 2 references/);
  configureCausewayReferenceWidgets({enabled: false});
  property.dispatchEvent(new CustomEvent('causeway-reference-load-failed', {bubbles: true, detail: {message: 'load failed'}}));
  assert.equal(property.interactionState.editor.id, 'autocomplete');
  property.cancelEdit();
});

test('standard action controller renders prompts, blocks invalid input, invokes and publishes results', async () => {
  const calls = [];
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    async prepareAction(actionId, values) {
      calls.push(`prepare:${JSON.stringify(values)}`);
      return {
        status: 'success', errors: [], data: {
          parameters: [{
            id: 'newName', description: 'New name', inputType: scalar('String'), enumValues: [], fields: new Map(),
            state: {hidden: false, disabled: null, default: 'Classics', validity: null}
          }]
        }
      };
    },
    async validateAction(actionId, values) {
      calls.push(`validate:${values.newName}`);
      return {status: 'success', data: values.newName === 'Invalid' ? 'That name is invalid.' : null, errors: []};
    },
    async invokeAction(actionId, values) {
      calls.push(`invoke:${values.newName}`);
      return {status: 'success', data: {kind: 'scalar', value: values.newName}, errors: []};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);
  let semanticResult;
  controller.addEventListener('causeway-action-result', event => { semanticResult = event.detail; });
  assert.equal(await controller.beginAction('changeName', context, null, {
    name: 'Rename this object',
    description: 'Changes the display name.'
  }), true);
  assert.match(controller.innerHTML, /data-testid="action-prompt"/);
  assert.match(controller.innerHTML, /<h2[^>]*>Rename this object<\/h2>/);
  assert.match(controller.innerHTML, /causeway-action-prompt-description">Changes the display name\.<\/p>/);
  assert.match(controller.innerHTML, /aria-describedby="causeway-action-prompt-description-\d+"/);
  assert.match(controller.innerHTML, /action-prompt-parameter-newName/);
  assert.equal((controller.innerHTML.match(/>New name</gi) ?? []).length, 1);
  await controller.setParameterValue('newName', 'Invalid', {recompute: false});
  assert.equal(await controller.submitPrompt(), false);
  assert.match(controller.innerHTML, /That name is invalid/);
  await controller.setParameterValue('newName', 'Updated', {recompute: false});
  assert.equal(await controller.submitPrompt(), true);
  assert.equal(semanticResult.actionId, 'changeName');
  assert.deepEqual(semanticResult.result, {kind: 'scalar', value: 'Updated'});
  assert.match(controller.innerHTML, /Action result/);
  assert.ok(calls.includes('invoke:Updated'));
});

test('action prompts render modal and sidebar surfaces with safe style normalization', async () => {
  const parameter = {
    id: 'name', description: 'Name', inputType: scalar('String'), enumValues: [], fields: new Map(),
    state: {hidden: false, disabled: null, default: null, validity: null}
  };
  const context = {
    async prepareAction() {
      return {status: 'success', errors: [], data: {parameters: [parameter]}};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);

  assert.equal(await controller.beginAction('rename', context, null, {promptStyle: 'DIALOG_SIDEBAR'}), true);
  assert.match(controller.innerHTML, /<dialog open class="causeway-action-prompt causeway-action-prompt-sidebar"/);
  assert.match(controller.innerHTML, /data-prompt-style="DIALOG_SIDEBAR"/);
  assert.doesNotMatch(controller.innerHTML, /data-causeway-dialog-drag-handle/);
  controller.cancelPrompt();

  assert.equal(await controller.beginAction('rename', context, null, {promptStyle: 'unexpected'}), true);
  assert.match(controller.innerHTML, /<dialog open class="causeway-action-prompt causeway-action-prompt-modal"/);
  assert.match(controller.innerHTML, /data-prompt-style="DIALOG_MODAL"/);
  assert.match(controller.innerHTML, /data-causeway-dialog-drag-handle/);
  controller.cancelPrompt();
});

test('inline prompts reversibly replace direct and effective-grid property associations', async () => {
  const parameter = {
    id: 'name', description: 'Name', inputType: scalar('String'), enumValues: [], fields: new Map(),
    state: {hidden: false, disabled: null, default: 'Mary', validity: null}
  };
  const context = {
    async prepareAction() {
      return {status: 'success', errors: [], data: {parameters: [parameter]}};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);

  const property = document.createElement('cw-property');
  const directAction = document.createElement('cw-action');
  property.appendChild(directAction);
  document.body.appendChild(property);
  const primary = property.children.find(child => child.hasAttribute('data-causeway-member-primary'));

  assert.equal(await controller.beginAction('rename', context, directAction, {promptStyle: 'INLINE'}, directAction), true);
  assert.equal(property.hasAttribute('data-causeway-inline-action-prompt'), true,
    `${controller.promptState.presentation.promptStyle}: ${controller.innerHTML} / ${property.innerHTML}`);
  assert.equal(primary.hidden, true);
  assert.equal(directAction.hidden, true);
  assert.doesNotMatch(controller.innerHTML, /data-testid="action-prompt"/);
  assert.match(property.innerHTML, /role="region"[\s\S]*data-prompt-style="INLINE"/);
  controller.cancelPrompt();
  assert.equal(property.hasAttribute('data-causeway-inline-action-prompt'), false);
  assert.equal(primary.hidden, false);
  assert.equal(directAction.hidden, false);

  const composition = document.createElement('section');
  composition.setAttribute('data-causeway-associated-member', 'name');
  const generatedProperty = document.createElement('cw-property');
  const actionGroup = document.createElement('div');
  actionGroup.setAttribute('data-causeway-action-group', '');
  const generatedAction = document.createElement('cw-action');
  actionGroup.appendChild(generatedAction);
  composition.appendChild(generatedProperty);
  composition.appendChild(actionGroup);
  document.body.appendChild(composition);

  assert.equal(await controller.beginAction('rename', context, generatedAction, {promptStyle: 'INLINE'}, generatedAction), true);
  assert.equal(composition.hasAttribute('data-causeway-inline-action-prompt'), true);
  assert.equal(generatedProperty.hidden, true);
  assert.equal(actionGroup.hidden, true);
  controller.cancelPrompt();
  assert.equal(generatedProperty.hidden, false);
  assert.equal(actionGroup.hidden, false);

  assert.equal(await controller.beginAction('rename', context, null, {promptStyle: 'INLINE'}), true);
  assert.match(controller.innerHTML, /data-prompt-style="DIALOG_MODAL"/);
  controller.cancelPrompt();
});

test('action prompt applies partial authored parameter presentation without changing authority', async () => {
  const parameters = [
    {id: 'notes', description: 'Canonical notes', inputType: scalar('String'), enumValues: [], fields: new Map(), state: {hidden: false, disabled: null, validity: null}},
    {id: 'reason', description: 'Canonical reason', inputType: scalar('String'), enumValues: [], fields: new Map(), state: {hidden: false, disabled: null, validity: null}},
    {id: 'species', description: 'Species', inputType: scalar('String'), enumValues: [], fields: new Map(), state: {hidden: false, disabled: null, validity: null}}
  ];
  const context = {
    async prepareAction() {
      return {status: 'success', errors: [], data: {parameters}};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);
  assert.equal(await controller.beginAction('customize', context, null, {
    parameters: [
      {parameter: 'notes', named: 'Visit notes', describedAs: 'Private <clinical> context', descriptionAs: 'tooltip', multiLine: 4},
      {parameter: 'reason', named: null, describedAs: 'Explain the appointment', descriptionAs: 'label', multiLine: null},
      {parameter: 'unknown', named: 'Must not render', describedAs: null, descriptionAs: null, multiLine: null}
    ]
  }), true);

  assert.match(controller.innerHTML, /data-parameter="notes" data-multi-line="4"/);
  assert.match(controller.innerHTML, /class="causeway-action-parameter-label causeway-member-tooltip"[^>]+data-tooltip="Private &lt;clinical&gt; context"[^>]*>Visit notes<\/label>/);
  assert.match(controller.innerHTML, /causeway-action-parameter-description causeway-visually-hidden">Private &lt;clinical&gt; context<\/span>/);
  assert.match(controller.innerHTML, /data-control="text-area"[^>]+data-rows="4"/);
  assert.match(controller.innerHTML, /data-parameter="reason"[\s\S]*?>Reason<\/label>[\s\S]*?causeway-action-parameter-description">Explain the appointment<\/span>/);
  assert.match(controller.innerHTML, /data-parameter="species"[\s\S]*?>Species<\/label>/);
  assert.doesNotMatch(controller.innerHTML, /Must not render|data-parameter="unknown"/);
  assert.deepEqual(controller.promptState.parameters, parameters);
  controller.cancelPrompt();
});

test('action prompt defers prepared validity and recomputation until focus completion', async () => {
  const calls = [];
  const target = (id, value) => ({
    getAttribute: name => name === 'data-causeway-editor' ? id : null,
    value,
    checked: false
  });
  const parameter = (id, values) => ({
    id, description: id, inputType: scalar('String'), enumValues: [], fields: new Map(),
    state: {
      hidden: false,
      disabled: null,
      choices: id === 'species' ? ['DOG', 'CAT'] : [],
      validity: values[id] ? null : `'${id}' is mandatory`
    }
  });
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    async prepareAction(actionId, values) {
      calls.push(`prepare:${JSON.stringify(values)}`);
      return {status: 'success', errors: [], data: {parameters: [
        parameter('name', values),
        parameter('species', values)
      ]}};
    },
    async validateAction(actionId, values) {
      calls.push(`validate:${JSON.stringify(values)}`);
      return {status: 'success', data: values.name && values.species ? null : 'Complete mandatory parameters.', errors: []};
    },
    async invokeAction() {
      throw new Error('Invalid prompt must not invoke.');
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);
  assert.equal(await controller.beginAction('addPet', context), true);
  assert.doesNotMatch(controller.innerHTML, /is mandatory/);
  const initialPreparationCount = calls.filter(call => call.startsWith('prepare:')).length;

  const nameTarget = target('name', 'Turing');
  const nameInput = new Event('input');
  nameInput.target = nameTarget;
  controller.dispatchEvent(nameInput);
  await new Promise(resolve => setTimeout(resolve, 300));
  assert.equal(controller.promptState.values.name, 'Turing');
  assert.equal(calls.filter(call => call.startsWith('prepare:')).length, initialPreparationCount);
  assert.doesNotMatch(controller.innerHTML, /is mandatory/);

  const nameFocusout = new Event('focusout');
  nameFocusout.target = nameTarget;
  controller.dispatchEvent(nameFocusout);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(calls.filter(call => call.startsWith('prepare:')).length, initialPreparationCount + 1);
  assert.doesNotMatch(controller.innerHTML, /name&#39; is mandatory/);
  assert.doesNotMatch(controller.innerHTML, /species&#39; is mandatory/);

  const speciesFocusout = new Event('focusout');
  speciesFocusout.target = target('species', '');
  controller.dispatchEvent(speciesFocusout);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.match(controller.innerHTML, /species&#39; is mandatory/);
  assert.doesNotMatch(controller.innerHTML, /name&#39; is mandatory/);

  const unblurredName = new Event('input');
  unblurredName.target = target('name', '');
  controller.dispatchEvent(unblurredName);
  assert.equal(await controller.submitPrompt(), false);
  assert.match(calls.find(call => call.startsWith('validate:')), /"name":null/);
  assert.match(controller.innerHTML, /name&#39; is mandatory/);
  assert.match(controller.innerHTML, /Complete mandatory parameters/);
});

test('action parameters preserve exact integer defaults and defer malformed-value feedback until focus completion', async () => {
  const calls = [];
  const parameter = value => ({
    id: 'amount', description: 'Amount', inputType: scalar('String'), enumValues: [], fields: new Map(),
    state: {
      hidden: false,
      disabled: null,
      datatype: 'rich__java_math_BigInteger',
      default: value,
      validity: null
    }
  });
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    async prepareAction(actionId, values) {
      calls.push(`prepare:${JSON.stringify(values)}`);
      return {status: 'success', errors: [], data: {parameters: [parameter(values.amount ?? '9007199254740993')]}};
    },
    async validateAction(actionId, values) {
      calls.push(`validate:${values.amount}`);
      return {status: 'success', data: null, errors: []};
    },
    async invokeAction(actionId, values) {
      calls.push(`invoke:${values.amount}`);
      return {status: 'success', data: {kind: 'scalar', value: values.amount}, errors: []};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);
  assert.equal(await controller.beginAction('changeAmount', context), true);
  assert.match(controller.innerHTML, /value="9007199254740993"/);
  const preparationCount = calls.filter(call => call.startsWith('prepare:')).length;

  const invalid = new Event('input');
  invalid.target = {
    getAttribute: name => name === 'data-causeway-editor' ? 'amount' : null,
    value: '12.5',
    checked: false
  };
  controller.dispatchEvent(invalid);
  assert.equal(controller.promptState.status, 'editing');
  assert.equal(controller.promptState.error, null);
  assert.doesNotMatch(controller.innerHTML, /whole number/);
  assert.equal(calls.filter(call => call.startsWith('prepare:')).length, preparationCount);

  const invalidFocusout = new Event('focusout');
  invalidFocusout.target = invalid.target;
  controller.dispatchEvent(invalidFocusout);
  assert.equal(controller.promptState.status, 'failed');
  assert.match(controller.promptState.error, /whole number/);
  assert.match(controller.innerHTML, /whole number/);
  assert.equal(calls.filter(call => call.startsWith('prepare:')).length, preparationCount);

  const corrected = new Event('input');
  corrected.target = {...invalid.target, value: '123456789012345678901234567890'};
  controller.dispatchEvent(corrected);
  const correctedFocusout = new Event('focusout');
  correctedFocusout.target = corrected.target;
  controller.dispatchEvent(correctedFocusout);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(controller.promptState.values.amount, '123456789012345678901234567890');
  assert.equal(await controller.submitPrompt(), true);
  assert.ok(calls.includes('validate:123456789012345678901234567890'));
  assert.ok(calls.includes('invoke:123456789012345678901234567890'));
});

test('protected action values remain write-only in markup and semantic prompt events', async () => {
  const published = [];
  const parameter = validity => ({
    id: 'password', description: 'Password', inputType: scalar('String'), enumValues: [], fields: new Map(),
    state: {
      hidden: false,
      disabled: null,
      datatype: 'rich__causeway_applib_value_Password',
      default: null,
      validity
    }
  });
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    async prepareAction(actionId, values) {
      const secret = values.password;
      return {status: 'success', errors: [], data: {parameters: [parameter(secret ? `Rejected ${secret}` : null)]}};
    },
    async validateAction() {
      return {status: 'success', data: null, errors: []};
    },
    async invokeAction() {
      return {status: 'success', data: {kind: 'void'}, errors: []};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  controller.addEventListener('causeway-action-prompt-state-change', event => published.push(event.detail));
  document.body.appendChild(controller);
  assert.equal(await controller.beginAction('changePassword', context), true);
  assert.match(controller.innerHTML, /data-control="password-field"/);

  const input = new Event('input');
  input.target = {
    getAttribute: name => name === 'data-causeway-editor' ? 'password' : null,
    value: 'top secret',
    checked: false
  };
  controller.dispatchEvent(input);
  await new Promise(resolve => setTimeout(resolve, 300));
  assert.doesNotMatch(controller.innerHTML, /top secret|Rejected/);
  const focusout = new Event('focusout');
  focusout.target = input.target;
  controller.dispatchEvent(focusout);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.doesNotMatch(controller.innerHTML, /top secret/);
  assert.match(controller.innerHTML, /Rejected \[protected\]/);
  assert.equal(published.at(-1).values.password, null);
  assert.doesNotMatch(JSON.stringify(published), /top secret/);
  controller.cancelPrompt();
});

test('action prompts recompute autocomplete suggestions and cancel without invoking', async () => {
  let invoked = false;
  const autoCompleteField = field('autoComplete', {kind: 'LIST', name: null, ofType: scalar('String')}, [argument('search', scalar('String'))]);
  const parameter = {
    id: 'search', description: 'Search', inputType: scalar('String'), enumValues: [],
    fields: new Map([['autoComplete', autoCompleteField]]),
    state: {hidden: false, disabled: null, validity: null}
  };
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    async prepareAction() {
      return {status: 'success', data: {parameters: [parameter]}, errors: []};
    },
    async autoCompleteActionParameter(actionId, parameterId, search) {
      assert.equal(search, 'Frame');
      return {status: 'success', data: ['Framework-neutral composition'], errors: []};
    },
    async validateAction() {
      return {status: 'success', data: null, errors: []};
    },
    async invokeAction() {
      invoked = true;
      return {status: 'success', data: {kind: 'collection', value: []}, errors: []};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);
  assert.equal(await controller.beginAction('findRelated', context), true);
  assert.equal(await controller.setParameterValue('search', 'Frame'), true);
  assert.deepEqual(controller.promptState.parameters[0].state.suggestions, ['Framework-neutral composition']);
  assert.match(controller.innerHTML, /Framework-neutral composition/);
  assert.equal(controller.cancelPrompt(), true);
  assert.equal(invoked, false);
});

test('claimed action requests bypass the standard controller', async () => {
  let prepared = false;
  const context = {
    async prepareAction() {
      prepared = true;
      return {status: 'success', data: {parameters: []}, errors: []};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);
  const source = document.createElement('div');
  document.body.appendChild(source);
  const claim = event => event.preventDefault();
  document.body.addEventListener('causeway-action-request', claim);
  source.dispatchEvent(new CustomEvent('causeway-action-request', {
    bubbles: true,
    composed: true,
    cancelable: true,
    detail: {actionId: 'claimed', context}
  }));
  await new Promise(resolve => queueMicrotask(resolve));
  document.body.removeEventListener('causeway-action-request', claim);
  assert.equal(prepared, false);
});

test('are-you-sure parameterless actions require explicit confirmation and restore focus when declined', async () => {
  let invocationCount = 0;
  let resolveInvocation;
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    async prepareAction() {
      return {status: 'success', data: {parameters: []}, errors: []};
    },
    async invokeAction() {
      invocationCount += 1;
      return new Promise(resolve => { resolveInvocation = resolve; });
    }
  };
  const controller = new CausewayInteractionControllerElement();
  const source = document.createElement('button');
  source.matches = selector => selector.includes('button');
  document.body.appendChild(source);
  document.body.appendChild(controller);

  assert.equal(await controller.beginAction('delete', context, source, {
    name: 'Delete this object',
    areYouSure: true
  }), true);
  assert.equal(controller.promptState.status, InteractionStatus.CONFIRMING);
  assert.equal(invocationCount, 0);
  assert.match(controller.innerHTML, /data-testid="action-confirmation"/);
  assert.match(controller.innerHTML, /role="alertdialog"/);
  assert.match(controller.innerHTML, /Confirm Delete this object/);
  assert.match(controller.innerHTML, /This action cannot be undone/);
  assert.match(controller.innerHTML, /data-testid="action-confirmation-confirm"/);
  assert.match(controller.innerHTML, /data-testid="action-confirmation-cancel"/);

  assert.equal(controller.cancelPrompt(), true);
  await Promise.resolve();
  assert.equal(controller.promptState, null);
  assert.equal(invocationCount, 0);
  assert.equal(document.activeElement, source);

  assert.equal(await controller.beginAction('delete', context, source, {
    name: 'Delete this object',
    areYouSure: true
  }), true);
  const invocation = controller.confirmPrompt();
  assert.equal(controller.promptState.status, InteractionStatus.INVOKING);
  assert.equal(invocationCount, 1);
  assert.equal(controller.confirmPrompt(), false);
  resolveInvocation({status: 'success', data: {kind: 'void', value: null}, errors: []});
  assert.equal(await invocation, true);
  assert.equal(invocationCount, 1);
  assert.equal(controller.resultState.result.kind, 'void');
});

test('are-you-sure parameterized actions confirm after validation and retain values when declined', async () => {
  const calls = [];
  const parameter = {
    id: 'reason', description: 'Reason', inputType: scalar('String'), enumValues: [], fields: new Map(),
    state: {hidden: false, disabled: null, validity: null}
  };
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    async prepareAction(actionId, values) {
      calls.push(`prepare:${JSON.stringify(values)}`);
      return {status: 'success', data: {parameters: [parameter]}, errors: []};
    },
    async validateAction(actionId, values) {
      calls.push(`validate:${values.reason}`);
      return {status: 'success', data: null, errors: []};
    },
    async invokeAction(actionId, values) {
      calls.push(`invoke:${values.reason}`);
      return {status: 'success', data: {kind: 'scalar', value: values.reason}, errors: []};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);
  const property = document.createElement('cw-property');
  const primary = document.createElement('div');
  primary.setAttribute('data-causeway-member-primary', '');
  const action = document.createElement('cw-action');
  property.appendChild(primary);
  property.appendChild(action);
  document.body.appendChild(property);
  assert.equal(await controller.beginAction('purge', context, action, {
    name: 'Purge records',
    areYouSure: true,
    promptStyle: 'INLINE'
  }, action), true);
  assert.equal(property.hasAttribute('data-causeway-inline-action-prompt'), true);
  await controller.setParameterValue('reason', 'Obsolete', {recompute: false});

  assert.equal(await controller.submitPrompt(), true);
  assert.equal(controller.promptState.status, InteractionStatus.CONFIRMING);
  assert.equal(property.hasAttribute('data-causeway-inline-action-prompt'), false);
  assert.match(controller.innerHTML, /role="alertdialog"[\s\S]*data-prompt-style="DIALOG_MODAL"/);
  assert.equal(controller.promptState.values.reason, 'Obsolete');
  assert.equal(calls.some(call => call.startsWith('invoke:')), false);
  assert.equal(controller.cancelPrompt(), true);
  assert.equal(controller.promptState.status, InteractionStatus.EDITING);
  assert.equal(controller.promptState.values.reason, 'Obsolete');
  assert.equal(property.hasAttribute('data-causeway-inline-action-prompt'), true);

  assert.equal(await controller.submitPrompt(), true);
  assert.equal(controller.promptState.status, InteractionStatus.CONFIRMING);
  assert.equal(await controller.confirmPrompt(), true);
  assert.ok(calls.includes('invoke:Obsolete'));
});

test('parameterless actions invoke without opening a parameter form', async () => {
  const context = {
    identity: {logicalTypeName: 'example.Object', id: '42'},
    async prepareAction() {
      return {status: 'success', data: {parameters: []}, errors: []};
    },
    async invokeAction() {
      return {status: 'success', data: {kind: 'void', value: null}, errors: []};
    }
  };
  const controller = new CausewayInteractionControllerElement();
  document.body.appendChild(controller);
  assert.equal(await controller.beginAction('reset', context), true);
  assert.equal(controller.promptState, null);
  assert.equal(controller.resultState.result.kind, 'void');
  assert.doesNotMatch(controller.innerHTML, /<dialog/);
  assert.equal(controller.dismissResult(), true);
  assert.equal(controller.resultState, null);
  assert.equal(controller.dismissResult(), false);
});
