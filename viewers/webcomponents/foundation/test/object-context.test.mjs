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
import {ObjectContextController} from '../src/object-context-controller.mjs';
import {
  createRichSchemaFixtureExecutor,
  DEPARTMENT_LOGICAL_TYPE,
  graphQLObjectResponse,
  partialPropertyErrorResponse,
  waitFor
} from './fixtures/rich-schema-fixture.mjs';

test('coalesces header and property requirements into one initial object read', async () => {
  const executor = createRichSchemaFixtureExecutor();
  const context = createContext(executor);
  const headerStates = [];
  const nameStates = [];
  const codeStates = [];
  context.registerRequirement({kind: 'header'}, state => headerStates.push(state));
  context.registerRequirement({kind: 'property', member: 'name'}, state => nameStates.push(state));
  context.registerRequirement({kind: 'property', member: 'code'}, state => codeStates.push(state));
  await waitFor(() => context.state.status === 'ready');
  assert.equal(executor.readCalls.length, 1);
  assert.match(executor.readCalls[0].document, /_meta \{/);
  assert.match(executor.readCalls[0].document, /name \{/);
  assert.match(executor.readCalls[0].document, /code \{/);
  assert.equal(headerStates.at(-1).data.title, 'Classics Department');
  assert.equal(nameStates.at(-1).data.get, 'Classics');
  assert.equal(codeStates.at(-1).data.get, 'CLA');
});

test('coordinates enum, object-reference, LOB and unsupported property projections', async () => {
  const executor = createRichSchemaFixtureExecutor();
  const context = createContext(executor);
  let statusState;
  let chairState;
  let blobState;
  let clobState;
  let unsupportedState;
  context.registerRequirement({kind: 'property', member: 'status'}, state => { statusState = state; });
  context.registerRequirement({kind: 'property', member: 'chair'}, state => { chairState = state; });
  context.registerRequirement({kind: 'property', member: 'prospectus'}, state => { blobState = state; });
  context.registerRequirement({kind: 'property', member: 'history'}, state => { clobState = state; });
  context.registerRequirement({kind: 'property', member: 'unsupportedValue'}, state => { unsupportedState = state; });
  await waitFor(() => context.state.status === 'ready');

  assert.equal(executor.readCalls.length, 1);
  const document = executor.readCalls[0].document;
  assert.match(document, /status\s*\{[^}]*get/s);
  assert.match(document, /chair\s*\{[\s\S]*?_meta\s*\{/);
  assert.match(document, /prospectus\s*\{[\s\S]*?bytes/);
  assert.match(document, /history\s*\{[\s\S]*?chars/);
  assert.match(document, /unsupportedValue\s*\{[\s\S]*?__typename/);
  assert.equal(statusState.data.get, 'ACTIVE');
  assert.equal(chairState.data.get._meta.id, 'staff-1');
  assert.equal(blobState.data.get.name, 'prospectus.pdf');
  assert.equal(clobState.data.get.chars, '/graphql/object/history/clobChars');
  assert.deepEqual(unsupportedState.data.get, {nested: {value: 'unknown'}});
});

test('loads a later requirement as a delta and refreshes only active requirements', async () => {
  const executor = createRichSchemaFixtureExecutor({
    readResponses: [graphQLObjectResponse(), graphQLObjectResponse({version: '8'}), graphQLObjectResponse({version: '9'})]
  });
  const context = createContext(executor);
  context.registerRequirement({kind: 'header'});
  const releaseName = context.registerRequirement({kind: 'property', member: 'name'});
  await waitFor(() => executor.readCalls.length === 1 && context.state.status === 'ready');
  context.registerRequirement({kind: 'property', member: 'code'});
  await waitFor(() => executor.readCalls.length === 2 && context.state.status === 'ready');
  assert.doesNotMatch(executor.readCalls[1].document, /name \{/);
  assert.match(executor.readCalls[1].document, /code \{/);
  releaseName();
  context.refresh();
  await waitFor(() => executor.readCalls.length === 3 && context.state.status === 'ready');
  assert.doesNotMatch(executor.readCalls[2].document, /name \{/);
  assert.match(executor.readCalls[2].document, /code \{/);
  assert.match(executor.readCalls[2].document, /_meta \{/);
});

test('preserves successful data and maps partial errors to one property', async () => {
  const executor = createRichSchemaFixtureExecutor({readResponses: [partialPropertyErrorResponse()]});
  const context = createContext(executor);
  let nameState;
  let codeState;
  context.registerRequirement({kind: 'property', member: 'name'}, state => { nameState = state; });
  context.registerRequirement({kind: 'property', member: 'code'}, state => { codeState = state; });
  await waitFor(() => context.state.status === 'partial-error');
  assert.equal(nameState.status, 'ready');
  assert.equal(nameState.data.get, 'Classics');
  assert.equal(codeState.status, 'partial-error');
  assert.equal(codeState.errors[0].message, 'Code is not readable.');
});

test('reports invalid identity and unsupported members without an object read', async () => {
  const executor = createRichSchemaFixtureExecutor();
  const invalid = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: ''
  });
  assert.equal(invalid.state.status, 'terminal-error');
  assert.equal(executor.readCalls.length, 0);

  const context = createContext(executor);
  let state;
  context.registerRequirement({kind: 'property', member: 'missing'}, next => { state = next; });
  await waitFor(() => state?.status === 'unsupported');
  assert.match(state.errors[0].message, /Property 'missing'/);
  assert.equal(executor.readCalls.length, 0);
});

test('aborts superseded reads and ignores their late result', async () => {
  const fixture = createRichSchemaFixtureExecutor();
  const baseClient = new CausewayGraphQLClient({executor: fixture});
  const description = await baseClient.describeObject(DEPARTMENT_LOGICAL_TYPE);
  let reads = 0;
  let firstAborted = false;
  const client = {
    describeObject: async () => description,
    readObject: ({signal}) => {
      reads += 1;
      if (reads === 1) {
        return new Promise((resolve, reject) => {
          signal.addEventListener('abort', () => {
            firstAborted = true;
            const error = new Error('Aborted');
            error.name = 'AbortError';
            reject(error);
          });
        });
      }
      return Promise.resolve({data: graphQLObjectResponse({name: 'New'}).data.rich.university_dept_Department, errors: []});
    }
  };
  const context = new ObjectContextController({client, logicalTypeName: DEPARTMENT_LOGICAL_TYPE, objectId: '42'});
  let nameState;
  context.registerRequirement({kind: 'property', member: 'name'}, state => { nameState = state; });
  await waitFor(() => reads === 1);
  context.refresh();
  await waitFor(() => reads === 2 && context.state.status === 'ready');
  assert.equal(firstAborted, true);
  assert.equal(nameState.data.get, 'New');
});

test('invokes a supplied scheduler without binding the object context as its receiver', async () => {
  const executor = createRichSchemaFixtureExecutor();
  const client = new CausewayGraphQLClient({executor});
  const schedule = function(callback) {
    assert.equal(this, undefined);
    queueMicrotask(callback);
  };
  const context = new ObjectContextController({
    client,
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42',
    schedule
  });
  context.registerRequirement({kind: 'property', member: 'name'});
  await waitFor(() => context.state.status === 'ready');
  assert.equal(executor.readCalls.length, 1);
});

function createContext(executor) {
  return new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
}
