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
  InteractionStatus,
  buildMutationInteractionOperation,
  buildObjectInteractionOperation,
  commandSelection,
  defaultEditorRegistry,
  parseCausewayEditorValue,
  renderCausewayEditor
} = await import('../src/index.mjs');

const scalar = name => ({kind: 'SCALAR', name, ofType: null});
const named = name => ({kind: 'OBJECT', name, ofType: null});
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

test('editor registry selects standard inputs and supports application overrides', () => {
  const text = renderCausewayEditor({
    name: 'name', value: 'Classics', choices: [], enumValues: [], inputType: scalar('String'),
    inputId: 'name-input', labelId: 'name-label', descriptionId: '', errorId: '', testId: 'name-editor'
  });
  assert.equal(text.editorId, 'text');
  assert.match(text.html, /type="text"/);

  const number = renderCausewayEditor({
    name: 'capacity', value: 24, choices: [], enumValues: [], inputType: scalar('Int'),
    inputId: 'capacity-input', labelId: 'capacity-label', descriptionId: '', errorId: '', testId: ''
  });
  assert.equal(number.editorId, 'number');
  assert.equal(parseCausewayEditorValue(number.editor, {value: '25', inputType: scalar('Int')}), 25);

  const dateTime = renderCausewayEditor({
    name: 'visitAt', value: '2026-08-20T09:00:00', choices: [], enumValues: [], inputType: scalar('LocalDateTime'),
    inputId: 'visit-at-input', labelId: 'visit-at-label', descriptionId: '', errorId: '', testId: ''
  });
  assert.equal(dateTime.editorId, 'temporal');
  assert.match(dateTime.html, /type="datetime-local"/);
  assert.match(dateTime.html, /step="1"/);
  assert.equal(parseCausewayEditorValue(dateTime.editor, {value: '2026-08-21T10:15:00'}), '2026-08-21T10:15:00');

  const choices = renderCausewayEditor({
    name: 'status', value: 'ACTIVE', choices: ['ACTIVE', 'PAUSED'], enumValues: [], inputType: {kind: 'ENUM', name: 'Status'},
    inputId: 'status-input', labelId: 'status-label', descriptionId: '', errorId: '', testId: ''
  });
  assert.equal(choices.editorId, 'choice');
  assert.match(choices.html, /<select/);

  const registry = new CausewayEditorRegistry(defaultEditorRegistry.registrations);
  registry.register({id: 'custom-name', priority: 1000, supports: context => context.name === 'name', render: () => '<textarea></textarea>'});
  assert.equal(registry.select({name: 'name'}).id, 'custom-name');
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
  property.member = 'name';
  property.editable = true;
  property.setAttribute('data-testid', 'property-name');
  property.context = context;
  document.body.appendChild(property);
  stateListener({
    status: 'ready',
    descriptor: {id: 'name', description: 'Name', value: {typeRef: scalar('String')}},
    data: {hidden: false, disabled: 'Locked', get: 'Classics'}, errors: [], generation: 1
  });
  assert.doesNotMatch(property.innerHTML, /property-name-edit/);
  stateListener({
    status: 'ready',
    descriptor: {id: 'name', description: 'Name', value: {typeRef: scalar('String')}},
    data: {hidden: false, disabled: null, get: 'Classics'}, errors: [], generation: 2
  });
  assert.match(property.innerHTML, /property-name-edit/);
  assert.equal(await property.beginEdit(), true);
  assert.match(property.innerHTML, /property-name-editor/);
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
  assert.equal(await controller.beginAction('changeName', context), true);
  assert.match(controller.innerHTML, /data-testid="action-prompt"/);
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
