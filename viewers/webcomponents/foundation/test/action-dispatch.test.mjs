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

import {
  ActionDispatchPlanError,
  ActionInvocationPlacement,
  actionInvocationArguments,
  actionInvocationResultPlan,
  createActionInvocationPlan,
  extractActionInvocationResult,
  secureActionInvocationResult
} from '../src/action-dispatch.mjs';
import {resultSelectionForType} from '../src/interaction-operations.mjs';

const scalar = name => ({kind: 'SCALAR', name, ofType: null});
const object = name => ({kind: 'OBJECT', name, ofType: null});
const nonNull = type => ({kind: 'NON_NULL', name: null, ofType: type});
const argument = (name, type) => ({name, type});
const field = (name, type, args = []) => ({name, type, args});
const type = (name, fields, kind = 'OBJECT') => ({name, kind, fields, enumValues: [], possibleTypes: []});
const descriptor = fields => ({fields: new Map(fields.map(candidate => [candidate.name, candidate]))});
const description = (types = new Map()) => ({generatedInputTypeName: 'rich__sample_Target__gqlv_input', types});

function mutationType(...fields) {
  return type('Mutation', fields);
}

test('safe nested invocation takes its advertised query placement', () => {
  const invoke = field('invokeIdempotent', object('SafeEnvelope'), [argument('name', scalar('String'))]);
  const mutation = field('sample_Target__greet', scalar('String'), [argument('_target', object('rich__sample_Target__gqlv_input'))]);
  const plan = createActionInvocationPlan({
    targetKind: 'object',
    description: description(),
    descriptor: descriptor([invoke]),
    mutationType: mutationType(mutation),
    mutationFieldName: mutation.name
  });

  assert.equal(plan.placement, ActionInvocationPlacement.NESTED_QUERY);
  assert.equal(plan.fieldName, 'invokeIdempotent');
  assert.equal(plan.mutating, false);
  assert.equal(plan.targetArgumentName, null);
  assert.deepEqual(actionInvocationArguments(plan, {name: 'Ada', ignored: 'not-advertised'}, {id: 'target-1'}), {name: 'Ada'});
});

test('top-level mutation is preferred over legacy nested mutation', () => {
  const legacy = field('invokeNonIdempotent', object('LegacyEnvelope'));
  const mutation = field('sample_Target__update', object('rich__sample_Target'), [
    argument('_target', nonNull(object('rich__sample_Target__gqlv_input'))),
    argument('value', nonNull(scalar('String')))
  ]);
  const plan = createActionInvocationPlan({
    targetKind: 'object',
    description: description(),
    descriptor: descriptor([legacy]),
    mutationType: mutationType(mutation),
    mutationFieldName: mutation.name
  });

  assert.equal(plan.placement, ActionInvocationPlacement.ROOT_MUTATION);
  assert.equal(plan.fieldName, mutation.name);
  assert.equal(plan.targetArgumentName, '_target');
  assert.deepEqual(actionInvocationArguments(plan, {value: 'updated', ignored: 'not-advertised'}, {id: 'target-1'}), {
    _target: {id: 'target-1'},
    value: 'updated'
  });
});

test('service mutation never manufactures an object target', () => {
  const mutation = field('sample_Service__clear', scalar('String'));
  const plan = createActionInvocationPlan({
    targetKind: 'service',
    description: description(),
    descriptor: descriptor([]),
    mutationType: mutationType(mutation),
    mutationFieldName: mutation.name
  });

  assert.equal(plan.placement, ActionInvocationPlacement.ROOT_MUTATION);
  assert.deepEqual(actionInvocationArguments(plan, {}, {id: 'must-not-be-used'}), {});
});

test('legacy nested mutation remains the bounded fallback without a root mutation', () => {
  const legacy = field('invokeNonIdempotent', object('LegacyEnvelope'));
  const plan = createActionInvocationPlan({
    targetKind: 'service',
    description: description(),
    descriptor: descriptor([legacy]),
    mutationType: mutationType(),
    mutationFieldName: 'sample_Service__clear'
  });

  assert.equal(plan.placement, ActionInvocationPlacement.LEGACY_NESTED_MUTATION);
  assert.equal(plan.mutating, true);
});

test('missing executable capability produces an unsupported immutable plan', () => {
  const plan = createActionInvocationPlan({
    targetKind: 'service',
    description: description(),
    descriptor: descriptor([]),
    mutationType: mutationType(),
    mutationFieldName: 'sample_Service__missing'
  });

  assert.equal(plan.supported, false);
  assert.equal(Object.isFrozen(plan), true);
  assert.throws(() => actionInvocationArguments(plan), ActionDispatchPlanError);
});

test('required arguments and object targets fail before operation rendering', () => {
  const mutation = field('sample_Target__update', object('rich__sample_Target'), [
    argument('_target', nonNull(object('rich__sample_Target__gqlv_input'))),
    argument('secret', nonNull(scalar('Password')))
  ]);
  const plan = createActionInvocationPlan({
    targetKind: 'object',
    description: description(),
    descriptor: descriptor([]),
    mutationType: mutationType(mutation),
    mutationFieldName: mutation.name
  });

  assert.throws(
    () => actionInvocationArguments(plan, {}, {id: 'target-1'}),
    error => error.code === 'ACTION_REQUIRED_ARGUMENT_UNAVAILABLE' && !error.message.includes('submitted-secret'));
  assert.throws(
    () => actionInvocationArguments(plan, {secret: 'submitted-secret'}),
    error => error.code === 'ACTION_TARGET_IDENTITY_UNAVAILABLE' && !error.message.includes('submitted-secret'));
});

test('enveloped and direct action results use their described shapes', () => {
  const targetRef = object('rich__sample_Target');
  const envelopeRef = object('SafeEnvelope');
  const types = new Map([
    ['SafeEnvelope', type('SafeEnvelope', [field('target', targetRef), field('results', targetRef)])],
    ['rich__sample_Target', type('rich__sample_Target', [field('_meta', object('rich__sample_Target__gqlv_meta'))])],
    ['rich__sample_Target__gqlv_meta', type('rich__sample_Target__gqlv_meta', [
      field('id', scalar('ID')),
      field('logicalTypeName', scalar('String')),
      field('title', scalar('String'))
    ])]
  ]);
  const envelopePlan = createActionInvocationPlan({
    targetKind: 'service',
    description: description(types),
    descriptor: descriptor([field('invoke', envelopeRef)]),
    mutationType: mutationType(),
    mutationFieldName: 'sample_Service__open'
  });
  const envelopeResult = actionInvocationResultPlan(envelopePlan, types);

  assert.deepEqual(envelopeResult.selection, {results: {_meta: {id: true, logicalTypeName: true, title: true}}});
  assert.deepEqual(extractActionInvocationResult({target: {ignored: true}, results: {_meta: {id: '1'}}}, envelopeResult), {_meta: {id: '1'}});

  const directPlan = createActionInvocationPlan({
    targetKind: 'service',
    description: description(types),
    descriptor: descriptor([]),
    mutationType: mutationType(field('sample_Service__open', targetRef)),
    mutationFieldName: 'sample_Service__open'
  });
  const directResult = actionInvocationResultPlan(directPlan, types);
  assert.deepEqual(directResult.selection, {_meta: {id: true, logicalTypeName: true, title: true}});
  assert.deepEqual(directResult.extractionPath, []);
});

test('protected action results redact errors and operation variables', () => {
  const result = {
    data: 'submitted-secret',
    errors: [{message: 'Rejected submitted-secret', path: ['changePassword'], extensions: {classification: 'BAD_REQUEST'}}],
    operation: {document: 'mutation Change($input0: Password!) { change(password: $input0) }', variables: {input0: 'submitted-secret'}}
  };

  const secured = secureActionInvocationResult(result, [{inputType: scalar('Password')}]);

  assert.equal(secured.errors[0].message, 'Protected action input was not accepted.');
  assert.equal(secured.operation.variables.input0, '<redacted>');
  assert.equal(JSON.stringify(secured).includes('submitted-secret'), false);
  assert.equal(secured.operation.document.includes('Password'), true);
});

test('metadata selection requests only fields advertised by the effective type', () => {
  const targetRef = object('rich__sample_VersionlessViewModel');
  const types = new Map([
    ['rich__sample_VersionlessViewModel', type('rich__sample_VersionlessViewModel', [
      field('_meta', object('rich__sample_VersionlessViewModel__gqlv_meta'))
    ])],
    ['rich__sample_VersionlessViewModel__gqlv_meta', type('rich__sample_VersionlessViewModel__gqlv_meta', [
      field('id', scalar('ID')),
      field('logicalTypeName', scalar('String')),
      field('title', scalar('String'))
    ])]
  ]);

  const selection = resultSelectionForType(targetRef, types);
  assert.deepEqual(selection, {_meta: {id: true, logicalTypeName: true, title: true}});
  assert.equal(JSON.stringify(selection).includes('version'), false);
});
