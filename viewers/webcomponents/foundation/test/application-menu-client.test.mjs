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

import {CausewayGraphQLClient} from '../src/graphql-client.mjs';
import {ServiceActionContextController} from '../src/service-action-context.mjs';
import {InteractionStatus} from '../src/types.mjs';
import {
  createMenuGraphQLExecutor,
  createMenuGraphQLTypes,
  SAMPLE_SERVICE_LOGICAL_TYPE
} from './fixtures/menu-graphql-fixture.mjs';
import {waitFor} from './fixtures/rich-schema-fixture.mjs';

test('targeted application discovery reads the effective menu descriptor and caches capability types', async () => {
  const executor = createMenuGraphQLExecutor();
  const client = new CausewayGraphQLClient({executor});
  const capability = await client.describeApplicationEntry();

  assert.equal(capability.supported, true);
  assert.equal(capability.menuBarsType.name, 'rich__gqlv_application_menu_bars');
  assert.equal(executor.calls.filter(call => call.operationName === 'CausewayDescribeTypes').every(call => Object.keys(call.variables).length === 1), true);
  const introspectionCount = executor.calls.filter(call => call.operationName === 'CausewayDescribeTypes').length;
  assert.equal(await client.describeApplicationEntry(), capability);
  assert.equal(executor.calls.filter(call => call.operationName === 'CausewayDescribeTypes').length, introspectionCount);

  const result = await client.readApplicationEntry({description: capability});
  assert.equal(result.data.menuBars.href, '/graphql/application/menu-bars');
  assert.equal(result.data.home.object._meta.id, 'home-1');
  assert.match(executor.applicationCalls[0].document, /application\s*\{/);
  assert.match(executor.applicationCalls[0].document, /menuBars\s*\{/);
  assert.match(executor.applicationCalls[0].document, /home\s*\{/);
  assert.match(executor.applicationCalls[0].document, /\.\.\. on rich__sample_Home/);
  assert.match(executor.applicationCalls[0].document, /_meta \{ id logicalTypeName title \}/);
});

test('one GraphQL client serializes initial targeted schema discovery across application and service contexts', async () => {
  const base = createMenuGraphQLExecutor();
  let active = 0;
  let maximumActive = 0;
  const executor = async request => {
    if (request.operationName === 'CausewayDescribeTypes' || request.operationName === 'CausewayDescribeOperationRoots') {
      active += 1;
      maximumActive = Math.max(maximumActive, active);
      await new Promise(resolve => setTimeout(resolve, 2));
      try {
        return await base(request);
      } finally {
        active -= 1;
      }
    }
    return base(request);
  };
  const client = new CausewayGraphQLClient({executor});

  await Promise.all([
    client.describeApplicationEntry(),
    client.describeService(SAMPLE_SERVICE_LOGICAL_TYPE)
  ]);
  assert.equal(maximumActive, 1);
});

test('application discovery reports omitted structural capability without issuing an invalid read', async () => {
  const executor = createMenuGraphQLExecutor({types: createMenuGraphQLTypes({menuBarsAvailable: false})});
  const client = new CausewayGraphQLClient({executor});
  const capability = await client.describeApplicationEntry();

  assert.equal(capability.supported, false);
  assert.equal(capability.reason, 'MENU_BARS_UNAVAILABLE');
  assert.equal(executor.applicationCalls.length, 0);
});

test('service descriptions and current action state use one logical-service query without object identity', async () => {
  const executor = createMenuGraphQLExecutor();
  const client = new CausewayGraphQLClient({executor});
  const context = new ServiceActionContextController({client, logicalTypeName: SAMPLE_SERVICE_LOGICAL_TYPE});
  const state = await context.loadActionStates(['welcomeMessage', 'disabledAction', 'hiddenAction']);

  assert.equal(state.states.get('welcomeMessage').hidden, false);
  assert.equal(state.states.get('disabledAction').disabled, 'Available to administrators only.');
  assert.equal(state.states.get('hiddenAction').hidden, true);
  assert.equal(executor.serviceCalls.length, 1);
  assert.match(executor.serviceCalls[0].document, /causeway_webcomponents_sample_SampleMenu\s*\{/);
  assert.doesNotMatch(executor.serviceCalls[0].document, /\$object|object:/);
  assert.deepEqual(executor.serviceCalls[0].variables, {});
});

test('service action adapter reuses parameter preparation, validation, safe invocation, and typed results', async () => {
  const executor = createMenuGraphQLExecutor();
  const client = new CausewayGraphQLClient({executor});
  const context = new ServiceActionContextController({client, logicalTypeName: SAMPLE_SERVICE_LOGICAL_TYPE});

  const prepared = await context.prepareAction('greet', {});
  assert.equal(prepared.status, InteractionStatus.SUCCESS);
  assert.equal(prepared.data.parameters[0].id, 'name');
  assert.equal(prepared.data.parameters[0].state.default, 'Ada');
  assert.deepEqual(prepared.data.parameters[0].state.choices, ['Ada', 'Grace']);

  const invalid = await context.validateAction('greet', {name: ''});
  assert.equal(invalid.data, 'A name is required.');
  const valid = await context.validateAction('greet', {name: 'Grace'});
  assert.equal(valid.status, InteractionStatus.SUCCESS);
  assert.equal(valid.data, null);

  const invoked = await context.invokeAction('greet', {name: 'Grace'});
  assert.equal(invoked.status, InteractionStatus.SUCCESS);
  assert.deepEqual(invoked.data, {kind: 'scalar', value: 'Hello, Grace!'});
  assert.equal(context.identity, null);
  assert.deepEqual(context.interactionTarget, {kind: 'service', logicalTypeName: SAMPLE_SERVICE_LOGICAL_TYPE});
});

test('service action context executes advertised autocomplete windows', async () => {
  const executor = createMenuGraphQLExecutor();
  const client = new CausewayGraphQLClient({executor});
  const context = new ServiceActionContextController({client, logicalTypeName: SAMPLE_SERVICE_LOGICAL_TYPE});

  const result = await context.autoCompleteActionParameterWindow(
    'greet', 'name', 'Gr', {}, {offset: 2, size: 2});

  assert.equal(result.status, InteractionStatus.SUCCESS);
  assert.deepEqual(result.data.items, ['Grace']);
  assert.equal(result.data.totalCount, 3);
  const call = executor.serviceCalls.find(candidate =>
    candidate.operationName === 'CausewayServiceActionParameterAutoCompleteWindow');
  assert.deepEqual(Object.values(call.variables), ['Gr', 2, 2]);
  assert.match(call.document, /autoCompleteWindow/);
});

test('superseded service parameter preparation cannot replace the latest response', async () => {
  const base = createMenuGraphQLExecutor();
  let releaseFirst;
  let prepareCount = 0;
  const gate = new Promise(resolve => releaseFirst = resolve);
  const executor = async request => {
    if (request.operationName === 'CausewayPrepareServiceAction' && ++prepareCount === 1) {
      await gate;
    }
    return base(request);
  };
  const context = new ServiceActionContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: SAMPLE_SERVICE_LOGICAL_TYPE
  });

  const first = context.prepareAction('greet', {name: 'Ada'});
  await waitFor(() => prepareCount === 1);
  const second = context.prepareAction('greet', {name: 'Grace'});
  assert.equal((await second).status, InteractionStatus.SUCCESS);
  releaseFirst();
  assert.equal((await first).status, InteractionStatus.OBSOLETE);
});

test('mutating service actions use the existing top-level mutation without a manufactured target', async () => {
  const executor = createMenuGraphQLExecutor();
  const client = new CausewayGraphQLClient({executor});
  let changed = 0;
  const context = new ServiceActionContextController({
    client,
    logicalTypeName: SAMPLE_SERVICE_LOGICAL_TYPE,
    onChanged: () => changed += 1
  });

  const invoked = await context.invokeAction('clearNotes', {});
  assert.equal(invoked.status, InteractionStatus.SUCCESS);
  assert.deepEqual(invoked.data, {kind: 'scalar', value: 'Cleared'});
  assert.equal(executor.mutationCalls.length, 1);
  assert.match(executor.mutationCalls[0].document, /^mutation CausewayInvokeServiceAction \{/);
  assert.doesNotMatch(executor.mutationCalls[0].document, /CausewayInvokeServiceAction\(\)|_target|\$object/);
  assert.equal(changed, 1);
});

test('parameterless service mutation returns versionless object metadata without requesting version or target', async () => {
  const executor = createMenuGraphQLExecutor();
  const client = new CausewayGraphQLClient({executor});
  const context = new ServiceActionContextController({client, logicalTypeName: SAMPLE_SERVICE_LOGICAL_TYPE});

  const invoked = await context.invokeAction('openView', {});

  assert.equal(invoked.status, InteractionStatus.SUCCESS);
  assert.deepEqual(invoked.data, {kind: 'object', value: {_meta: {
    id: 'view-1', logicalTypeName: 'sample.VersionlessViewModel', title: 'View one'
  }}});
  const operation = executor.mutationCalls.at(-1);
  assert.match(operation.document, /^mutation CausewayInvokeServiceAction \{/);
  assert.match(operation.document, /_meta \{\s+id\s+logicalTypeName\s+title\s+\}/);
  assert.doesNotMatch(operation.document, /version|target|_target|\$object/);
  assert.deepEqual(operation.variables, {});
});

test('mutating service actions delegate to application-scope serialization', async () => {
  const executor = createMenuGraphQLExecutor();
  let tail = Promise.resolve();
  let active = 0;
  let maximumActive = 0;
  const serializeMutation = execute => {
    const result = tail.then(async () => {
      active += 1;
      maximumActive = Math.max(maximumActive, active);
      await new Promise(resolve => setTimeout(resolve, 5));
      try {
        return await execute();
      } finally {
        active -= 1;
      }
    });
    tail = result.catch(() => undefined);
    return result;
  };
  const context = new ServiceActionContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: SAMPLE_SERVICE_LOGICAL_TYPE,
    serializeMutation
  });

  const results = await Promise.all([
    context.invokeAction('clearNotes', {}),
    context.invokeAction('clearNotes', {})
  ]);
  assert.equal(results.every(result => result.status === InteractionStatus.SUCCESS), true);
  assert.equal(maximumActive, 1);
  assert.equal(executor.mutationCalls.length, 2);
});
