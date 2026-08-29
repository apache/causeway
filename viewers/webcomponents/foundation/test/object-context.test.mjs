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
import {ObjectContextController, StructuralResourceError} from '../src/object-context-controller.mjs';
import {
  createRichSchemaFixtureExecutor,
  createRichSchemaTypes,
  createVersionlessRichSchemaTypes,
  DEPARTMENT_LOGICAL_TYPE,
  DEPARTMENT_OBJECT_FIELD,
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
  assert.match(executor.readCalls[0].document, /metadata\s*\{/);
  for (const field of ['friendlyName', 'description', 'multiLine', 'labelPosition']) {
    assert.match(executor.readCalls[0].document, new RegExp(`\\b${field}\\b`));
  }
  assert.match(executor.readCalls[0].document, /code \{/);
  assert.equal(headerStates.at(-1).data.title, 'Classics Department');
  assert.equal(nameStates.at(-1).data.get, 'Classics');
  assert.deepEqual(nameStates.at(-1).data.metadata, {
    friendlyName: 'Department name',
    description: 'The department display name.',
    multiLine: null,
    labelPosition: 'LEFT'
  });
  assert.equal(codeStates.at(-1).data.get, 'CLA');
});

test('property reads remain compatible when member metadata is unavailable', async () => {
  const types = createRichSchemaTypes();
  for (const [name, type] of types) {
    if (name.endsWith('__gqlv_property')) {
      types.set(name, {...type, fields: type.fields.filter(field => field.name !== 'metadata')});
    }
  }
  const executor = createRichSchemaFixtureExecutor({types});
  const context = createContext(executor);
  let propertyState;
  context.registerRequirement({kind: 'property', member: 'name'}, state => { propertyState = state; });
  await waitFor(() => context.state.status === 'ready');
  assert.doesNotMatch(executor.readCalls[0].document, /metadata\s*\{/);
  assert.equal(propertyState.data.get, 'Classics');
});

test('coalesces layout metadata with semantic member requirements', async () => {
  const executor = createRichSchemaFixtureExecutor();
  const context = createContext(executor);
  let layoutState;
  context.registerRequirement({kind: 'layout'}, state => { layoutState = state; });
  context.registerRequirement({kind: 'property', member: 'name'});
  await waitFor(() => context.state.status === 'ready');
  assert.equal(executor.readCalls.length, 1);
  assert.match(executor.readCalls[0].document, /_meta\s*\{[\s\S]*?grid/);
  assert.match(executor.readCalls[0].document, /name\s*\{/);
  assert.equal(layoutState.data.grid, '/graphql/object/university.dept.Department:42/_meta/grid');
  assert.equal(layoutState.data.cssClass, 'department');
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

test('object-valued property projection omits an absent metadata version', async () => {
  const response = graphQLObjectResponse();
  delete response.data.rich[DEPARTMENT_OBJECT_FIELD].chair.get._meta.version;
  const executor = createRichSchemaFixtureExecutor({
    types: createVersionlessRichSchemaTypes(),
    readResponses: [response]
  });
  const context = createContext(executor);
  let chairState;
  context.registerRequirement({kind: 'property', member: 'chair'}, state => { chairState = state; });
  await waitFor(() => context.state.status === 'ready');

  const document = executor.readCalls[0].document;
  const chairSelection = document.slice(document.indexOf('chair {'), document.indexOf('prospectus {'));
  assert.match(chairSelection, /_meta\s*\{[\s\S]*?id/);
  assert.match(chairSelection, /logicalTypeName/);
  assert.match(chairSelection, /title/);
  assert.doesNotMatch(chairSelection, /version/);
  assert.deepEqual(chairState.data.get._meta, {
    id: 'staff-1', logicalTypeName: 'university.staff.StaffMember', title: 'Dr Ada'
  });
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

test('loads bounded structural resources with opaque same-origin no-store requests', async () => {
  const requests = [];
  const fetchImpl = async function(path, init) {
    assert.equal(this, undefined);
    requests.push({path, init});
    return {
      ok: true,
      status: 200,
      headers: {get: name => name === 'content-type' ? 'application/xml' : null},
      text: async () => '<bs:grid/>'
    };
  };
  const context = createContext(createRichSchemaFixtureExecutor(), fetchImpl);
  const resource = await context.loadStructuralResource('/graphql/object/type:id/_meta/grid');
  assert.equal(resource.text, '<bs:grid/>');
  assert.equal(resource.mediaType, 'application/xml');
  assert.equal(requests.length, 1);
  assert.equal(requests[0].path, '/graphql/object/type:id/_meta/grid');
  assert.deepEqual(
    {
      method: requests[0].init.method,
      credentials: requests[0].init.credentials,
      cache: requests[0].init.cache,
      redirect: requests[0].init.redirect,
      accept: requests[0].init.headers.accept
    },
    {
      method: 'GET',
      credentials: 'same-origin',
      cache: 'no-store',
      redirect: 'error',
      accept: 'application/xml'
    }
  );
});

test('rejects unsafe paths and reports HTTP failures without reading or disclosing response bodies', async () => {
  let bodyRead = false;
  const fetchImpl = async () => ({
    ok: false,
    status: 403,
    headers: {get: () => null},
    text: async () => {
      bodyRead = true;
      return 'sensitive authorization policy';
    }
  });
  const context = createContext(createRichSchemaFixtureExecutor(), fetchImpl);
  await assert.rejects(
    context.loadStructuralResource('//other.example/grid'),
    error => error instanceof StructuralResourceError && error.code === 'INVALID_RESOURCE_PATH'
  );
  await assert.rejects(
    context.loadStructuralResource('/graphql/object/type:id/_meta/grid'),
    error => error instanceof StructuralResourceError
      && error.code === 'RESOURCE_REQUEST_FAILED'
      && error.status === 403
      && !error.message.includes('sensitive')
  );
  assert.equal(bodyRead, false);
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

function createContext(executor, fetchImpl = globalThis.fetch) {
  return new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42',
    fetchImpl
  });
}
