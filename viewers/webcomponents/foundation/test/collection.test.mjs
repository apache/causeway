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
import {
  collectionWindowResponse,
  createRichSchemaFixtureExecutor,
  createRichSchemaTypes,
  createVersionlessRichSchemaTypes,
  createWindowedRichSchemaTypes,
  DEPARTMENT_LOGICAL_TYPE,
  DEPARTMENT_OBJECT_FIELD,
  departmentObjectData,
  graphQLObjectResponse,
  STAFF_LOGICAL_TYPE,
  STAFF_OBJECT_FIELD,
  waitFor
} from './fixtures/rich-schema-fixture.mjs';

const {document} = installDomShim();
const {
  captureDeclarativeCollectionColumns,
  CausewayCollectionColumnElement,
  CausewayCollectionElement,
  CausewayGraphQLClient,
  ObjectContextController
} = await import('../src/index.mjs');

function row({id = 'staff-1', name = 'Dr Ada', code = 'ADA', version = '3'} = {}) {
  return {
    _meta: {id, logicalTypeName: STAFF_LOGICAL_TYPE, ...(version == null ? {} : {version}), title: name},
    name: {hidden: false, disabled: null, datatype: 'String', get: name},
    code: {hidden: false, disabled: null, datatype: 'String', get: code}
  };
}

function collectionResponse(rows) {
  return {
    data: {
      rich: {
        [DEPARTMENT_OBJECT_FIELD]: {
          staffMembers: {get: rows}
        }
      }
    }
  };
}

function staffDeltaResponse() {
  return {
    data: {
      rich: {
        [STAFF_OBJECT_FIELD]: {
          code: {hidden: false, disabled: null, datatype: 'String', get: 'ADA'}
        }
      }
    }
  };
}

test('captures declarative columns before browser upgrade reactions replace source children', async () => {
  const collection = new CausewayCollectionElement();
  const column = new CausewayCollectionColumnElement();
  column.member = 'name';
  column.label = 'Name';
  column.setAttribute('data-testid', 'column-name');
  collection.appendChild(column);
  captureDeclarativeCollectionColumns({querySelectorAll: () => [collection]});
  collection.removeChild(column);
  document.body.appendChild(collection);
  await waitFor(() => collection.columns.length === 1);
  assert.deepEqual(collection.columns, [{member: 'name', label: 'Name', testId: 'column-name'}]);
  document.body.removeChild(collection);
});

test('object context keeps action and inactive collection state in the primary projection', async () => {
  const executor = createRichSchemaFixtureExecutor({readResponses: [graphQLObjectResponse()]});
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  let actionState;
  let collectionState;
  context.registerRequirement({kind: 'action', member: 'changeName'}, state => { actionState = state; });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'}, state => { collectionState = state; });
  await waitFor(() => context.state.status === 'ready');
  assert.equal(actionState.data.hidden, false);
  assert.equal(collectionState.data.hidden, false);
  assert.equal(executor.readCalls.length, 1);
  assert.doesNotMatch(executor.readCalls[0].document, /staffMembers\s*\{\s*get/);
});

test('collection secondary reads are lazy, cached and hydrate row contexts', async () => {
  const staff = row();
  const executor = createRichSchemaFixtureExecutor({
    readResponses: [graphQLObjectResponse(), collectionResponse([staff]), staffDeltaResponse()]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');
  assert.equal(executor.readCalls.length, 1);

  const loaded = await context.loadCollection({member: 'staffMembers', columns: ['name']});
  assert.equal(executor.readCalls.length, 2);
  assert.equal(loaded.data.get[0]._meta.id, 'staff-1');
  assert.equal(loaded.rowDescription.logicalTypeName, STAFF_LOGICAL_TYPE);
  assert.match(executor.readCalls[1].document, /staffMembers/);
  assert.match(executor.readCalls[1].document, /name\s*\{/);
  await context.loadCollection({member: 'staffMembers', columns: ['name']});
  assert.equal(executor.readCalls.length, 2);

  const rowContext = context.createHydratedRowContext(loaded.data.get[0], loaded.rowSelection);
  let nameState;
  rowContext.registerRequirement({kind: 'property', member: 'name'}, state => { nameState = state; });
  await waitFor(() => rowContext.state.status === 'ready');
  assert.equal(nameState.data.get, 'Dr Ada');
  assert.equal(executor.readCalls.length, 2, 'hydrated name must not repeat an object read');

  let codeState;
  rowContext.registerRequirement({kind: 'property', member: 'code'}, state => { codeState = state; });
  await waitFor(() => executor.readCalls.length === 3 && codeState?.status === 'ready');
  assert.equal(codeState.data.get, 'ADA');
});

test('versionless concrete collection rows retain identity, columns and hydration', async () => {
  const staff = row({version: null});
  const executor = createRichSchemaFixtureExecutor({
    types: createVersionlessRichSchemaTypes(),
    readResponses: [graphQLObjectResponse(), collectionResponse([staff])]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');

  const loaded = await context.loadCollection({member: 'staffMembers', columns: ['name']});
  const document = executor.readCalls[1].document;
  assert.match(document, /_meta\s*\{[\s\S]*?id/);
  assert.match(document, /logicalTypeName/);
  assert.match(document, /title/);
  assert.doesNotMatch(document, /version/);
  assert.match(document, /name\s*\{/);
  assert.equal(loaded.rows[0]._meta.version, undefined);
  const rowContext = context.createHydratedRowContext(loaded.rows[0], loaded.rowSelection);
  assert.equal(rowContext.identity.id, 'staff-1');
  rowContext.disconnect();
});

test('collection secondary reads prefer bounded windows and expose semantic range state', async () => {
  const rows = [row({id: 'staff-6', name: 'Dr Six'}), row({id: 'staff-7', name: 'Dr Seven'})];
  const executor = createRichSchemaFixtureExecutor({
    types: createWindowedRichSchemaTypes(),
    readResponses: [graphQLObjectResponse()],
    windowResponses: [collectionWindowResponse({rows, offset: 5, requestedSize: 2, totalCount: 8})]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');

  const loaded = await context.loadCollection({
    member: 'staffMembers',
    columns: ['name'],
    offset: 5,
    size: 2
  });
  assert.equal(executor.windowCalls.length, 1);
  assert.deepEqual(loaded.rows, rows);
  assert.deepEqual(loaded.window, {
    offset: 5,
    requestedSize: 2,
    returnedCount: 2,
    totalCount: 8,
    countAvailable: true,
    maximumSize: 100,
    hasPrevious: true,
    hasNext: true,
    previousOffset: 3,
    nextOffset: 7,
    rangeStart: 6,
    rangeEnd: 7,
    ordering: 'ENCOUNTER'
  });
  assert.deepEqual(executor.windowCalls[0].variables, {object: {id: '42'}, offset: 5, size: 2});
  assert.match(executor.windowCalls[0].document, /requestedSize/);
  assert.match(executor.windowCalls[0].document, /maximumSize/);

  await context.loadCollection({member: 'staffMembers', columns: ['name'], offset: 5, size: 2});
  assert.equal(executor.windowCalls.length, 1, 'an identical window should use the secondary cache');
});

test('small abstract collection selects advertised fragments without a probe', async () => {
  const types = createRichSchemaTypes();
  const unionTypeName = 'rich__university_staff_SmallUnion';
  const collectionType = types.get('rich__university_dept_Department__staffMembers__gqlv_collection');
  collectionType.fields.find(candidate => candidate.name === 'get').type = {
    kind: 'LIST', name: null, ofType: {kind: 'UNION', name: unionTypeName, ofType: null}
  };
  types.set(unionTypeName, {
    kind: 'UNION', name: unionTypeName, description: null, fields: [],
    possibleTypes: [{kind: 'OBJECT', name: 'rich__university_staff_StaffMember'}]
  });
  const staff = {...row(), __typename: 'rich__university_staff_StaffMember'};
  const executor = createRichSchemaFixtureExecutor({
    types,
    readResponses: [graphQLObjectResponse(), collectionResponse([staff])]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');

  const loaded = await context.loadCollection({member: 'staffMembers'});

  assert.equal(executor.readCalls.length, 2);
  assert.equal(loaded.probeOperation, null);
  assert.match(loaded.operation.document, /\.\.\. on rich__university_staff_StaffMember/);
  assert.equal(loaded.rows[0]._meta.id, 'staff-1');
});

test('abstract collection rows probe typenames then replay concrete fragments', async () => {
  const types = createRichSchemaTypes();
  const unionTypeName = 'rich__university_staff_StaffUnion';
  const collectionType = types.get('rich__university_dept_Department__staffMembers__gqlv_collection');
  collectionType.fields.find(candidate => candidate.name === 'get').type = {
    kind: 'LIST', name: null, ofType: {kind: 'UNION', name: unionTypeName, ofType: null}
  };
  types.set(unionTypeName, {
    kind: 'UNION', name: unionTypeName, description: null, fields: [],
    possibleTypes: [
      {kind: 'OBJECT', name: 'rich__university_dept_Department'},
      {kind: 'OBJECT', name: 'rich__university_staff_StaffMember'},
      ...Array.from({length: 7}, (_, index) => ({kind: 'OBJECT', name: `rich__university_staff_Unobserved${index}`}))
    ]
  });
  const staff = {...row(), __typename: 'rich__university_staff_StaffMember'};
  const department = {...departmentObjectData(), __typename: 'rich__university_dept_Department'};
  const executor = createRichSchemaFixtureExecutor({
    types,
    readResponses: [
      graphQLObjectResponse(),
      collectionResponse([{__typename: staff.__typename}, {__typename: department.__typename}]),
      collectionResponse([staff, department])
    ]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');

  const loaded = await context.loadCollection({member: 'staffMembers', columns: ['name', 'status']});

  assert.equal(executor.readCalls.length, 3);
  assert.match(loaded.probeOperation.document, /__typename/);
  assert.doesNotMatch(loaded.probeOperation.document, /\.\.\. on/);
  assert.match(loaded.operation.document, /\.\.\. on rich__university_dept_Department/);
  assert.match(loaded.operation.document, /\.\.\. on rich__university_staff_StaffMember/);
  assert.match(loaded.operation.document, /_meta/);
  assert.match(loaded.operation.document, /name\s*\{/);
  const staffFragment = loaded.operation.document.slice(
    loaded.operation.document.indexOf('... on rich__university_staff_StaffMember'));
  assert.doesNotMatch(staffFragment, /status\s*\{/);
  assert.equal(loaded.rows[0]._meta.id, 'staff-1');
  assert.equal(loaded.rows[1]._meta.id, '42');
  const rowContext = context.createHydratedRowContext(loaded.rows[0], loaded.rowSelection);
  assert.equal(rowContext.identity.id, 'staff-1');
  rowContext.disconnect();
});

test('abstract bounded windows probe and replay identical arguments once', async () => {
  const types = createWindowedRichSchemaTypes();
  const unionTypeName = 'rich__university_staff_StaffWindowUnion';
  const unionRef = {kind: 'UNION', name: unionTypeName, ofType: null};
  types.get('rich__university_dept_Department__staffMembers__gqlv_collection')
    .fields.find(candidate => candidate.name === 'get').type = {kind: 'LIST', name: null, ofType: unionRef};
  types.get('rich__university_dept_Department__staffMembers__gqlv_collection_window')
    .fields.find(candidate => candidate.name === 'rows').type = {kind: 'LIST', name: null, ofType: unionRef};
  types.set(unionTypeName, {
    kind: 'UNION', name: unionTypeName, description: null, fields: [],
    possibleTypes: [
      {kind: 'OBJECT', name: 'rich__university_staff_StaffMember'},
      ...Array.from({length: 8}, (_, index) => ({kind: 'OBJECT', name: `rich__university_staff_WindowUnobserved${index}`}))
    ]
  });
  const staff = {...row(), __typename: 'rich__university_staff_StaffMember'};
  const executor = createRichSchemaFixtureExecutor({
    types,
    readResponses: [graphQLObjectResponse()],
    windowResponses: [
      collectionWindowResponse({rows: [{__typename: staff.__typename}], offset: 4, requestedSize: 2, totalCount: 5}),
      collectionWindowResponse({rows: [staff], offset: 4, requestedSize: 2, totalCount: 5})
    ]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');

  const loaded = await context.loadCollection({member: 'staffMembers', columns: ['name'], offset: 4, size: 2});

  assert.equal(executor.windowCalls.length, 2);
  assert.deepEqual(executor.windowCalls[0].variables, executor.windowCalls[1].variables);
  assert.match(executor.windowCalls[0].document, /__typename/);
  assert.match(executor.windowCalls[1].document, /\.\.\. on rich__university_staff_StaffMember/);
  assert.equal(loaded.window.offset, 4);
  assert.equal(loaded.rows[0]._meta.id, 'staff-1');
});

test('abstract collection reports a replay type not observed by its bounded probe', async () => {
  const types = createRichSchemaTypes();
  const unionTypeName = 'rich__university_staff_ChangingUnion';
  const collectionType = types.get('rich__university_dept_Department__staffMembers__gqlv_collection');
  collectionType.fields.find(candidate => candidate.name === 'get').type = {
    kind: 'LIST', name: null, ofType: {kind: 'UNION', name: unionTypeName, ofType: null}
  };
  types.set(unionTypeName, {
    kind: 'UNION', name: unionTypeName, description: null, fields: [],
    possibleTypes: [
      {kind: 'OBJECT', name: 'rich__university_dept_Department'},
      {kind: 'OBJECT', name: 'rich__university_staff_StaffMember'},
      ...Array.from({length: 7}, (_, index) => ({kind: 'OBJECT', name: `rich__university_staff_Changing${index}`}))
    ]
  });
  const executor = createRichSchemaFixtureExecutor({
    types,
    readResponses: [
      graphQLObjectResponse(),
      collectionResponse([{__typename: 'rich__university_staff_StaffMember'}]),
      collectionResponse([{...departmentObjectData(), __typename: 'rich__university_dept_Department'}])
    ]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');

  const loaded = await context.loadCollection({member: 'staffMembers'});

  assert.equal(executor.readCalls.length, 3);
  assert.equal(loaded.errors.length, 1);
  assert.match(loaded.errors[0].message, /unprojected concrete type/);
});

test('abstract collection rejects a probe typename not advertised by its union', async () => {
  const types = createRichSchemaTypes();
  const unionTypeName = 'rich__university_staff_AdvertisedUnion';
  const collectionType = types.get('rich__university_dept_Department__staffMembers__gqlv_collection');
  collectionType.fields.find(candidate => candidate.name === 'get').type = {
    kind: 'LIST', name: null, ofType: {kind: 'UNION', name: unionTypeName, ofType: null}
  };
  types.set(unionTypeName, {
    kind: 'UNION', name: unionTypeName, description: null, fields: [],
    possibleTypes: [
      {kind: 'OBJECT', name: 'rich__university_staff_StaffMember'},
      ...Array.from({length: 8}, (_, index) => ({kind: 'OBJECT', name: `rich__university_staff_Advertised${index}`}))
    ]
  });
  const executor = createRichSchemaFixtureExecutor({
    types,
    readResponses: [graphQLObjectResponse(), collectionResponse([{__typename: 'rich__university_staff_Other'}])]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');

  await assert.rejects(context.loadCollection({member: 'staffMembers'}), /not advertised/);
  assert.equal(executor.readCalls.length, 2);
});

test('abstract collection aborts an in-flight fragment replay', async () => {
  const types = createRichSchemaTypes();
  const unionTypeName = 'rich__university_staff_CancellableUnion';
  const collectionType = types.get('rich__university_dept_Department__staffMembers__gqlv_collection');
  collectionType.fields.find(candidate => candidate.name === 'get').type = {
    kind: 'LIST', name: null, ofType: {kind: 'UNION', name: unionTypeName, ofType: null}
  };
  types.set(unionTypeName, {
    kind: 'UNION', name: unionTypeName, description: null, fields: [],
    possibleTypes: [
      {kind: 'OBJECT', name: 'rich__university_staff_StaffMember'},
      ...Array.from({length: 8}, (_, index) => ({kind: 'OBJECT', name: `rich__university_staff_Cancellable${index}`}))
    ]
  });
  const executor = createRichSchemaFixtureExecutor({
    types,
    readResponses: [
      graphQLObjectResponse(),
      collectionResponse([{__typename: 'rich__university_staff_StaffMember'}]),
      request => new Promise((resolve, reject) => request.signal.addEventListener('abort', () => {
        const error = new Error('Aborted');
        error.name = 'AbortError';
        reject(error);
      }, {once: true}))
    ]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');
  const abortController = new AbortController();

  const loading = context.loadCollection({member: 'staffMembers', signal: abortController.signal});
  await waitFor(() => executor.readCalls.length === 3);
  abortController.abort();

  await assert.rejects(loading, error => error?.name === 'AbortError');
});

test('collection secondary reads discard responses superseded for the same consumer', async () => {
  const resolvers = new Map();
  const executor = createRichSchemaFixtureExecutor({
    types: createWindowedRichSchemaTypes(),
    readResponses: [graphQLObjectResponse()],
    windowResponses: [request => new Promise(resolve => resolvers.set(request.variables.offset, resolve))]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');
  const requestKey = {};

  const first = context.loadCollection({member: 'staffMembers', offset: 0, size: 2, requestKey});
  await waitFor(() => resolvers.has(0));
  const second = context.loadCollection({member: 'staffMembers', offset: 2, size: 2, requestKey});
  await waitFor(() => resolvers.has(2));
  resolvers.get(2)(collectionWindowResponse({rows: [row({id: 'staff-3'})], offset: 2, requestedSize: 2, totalCount: 3}));
  const latest = await second;
  assert.equal(latest.window.offset, 2);

  const third = context.loadCollection({member: 'staffMembers', offset: 4, size: 2, requestKey});
  await waitFor(() => resolvers.has(4));
  resolvers.get(4)(collectionWindowResponse({rows: [], offset: 4, requestedSize: 2, totalCount: 3}));
  assert.equal((await third).window.offset, 4);

  resolvers.get(0)(collectionWindowResponse({rows: [row()], offset: 0, requestedSize: 2, totalCount: 3}));
  await assert.rejects(first, error => error?.name === 'AbortError');
});

test('collection component does not read until activated and renders declared columns', async () => {
  let requirementListener;
  let loadCount = 0;
  const context = {
    identity: {logicalTypeName: DEPARTMENT_LOGICAL_TYPE, id: '42'},
    registerRequirement(requirement, listener) {
      assert.deepEqual(requirement, {kind: 'collection', member: 'staffMembers'});
      requirementListener = listener;
      listener({
        status: 'ready',
        descriptor: {id: 'staffMembers', description: 'Staff members'},
        data: {hidden: false, disabled: null},
        errors: [],
        generation: 1
      });
      return () => {};
    },
    async loadCollection() {
      loadCount += 1;
      return {
        descriptor: {id: 'staffMembers'},
        data: {get: [row()]},
        errors: [],
        rowSelection: {_meta: {id: true}, name: {get: true}}
      };
    },
    createHydratedRowContext() {
      return {disconnect() {}};
    }
  };
  const collection = new CausewayCollectionElement();
  collection.member = 'staffMembers';
  collection.columns = [{member: 'name', label: 'Name'}, {member: 'code', label: 'Code'}];
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => collection.innerHTML.includes('Load Staff Members'));
  assert.equal(loadCount, 0);
  assert.match(collection.innerHTML, /Load Staff Members/);

  assert.equal(collection.activate(), true);
  await waitFor(() => collection.collectionState.status === 'ready');
  assert.equal(loadCount, 1);
  assert.match(collection.innerHTML, /causeway-collection-table/);
  assert.match(collection.innerHTML, /<th scope="col">Item<\/th>/);
  assert.match(collection.innerHTML, /<causeway-object-link/);
  assert.match(collection.innerHTML, /Dr Ada/);
  assert.match(collection.innerHTML, /ADA/);

  requirementListener({
    status: 'ready',
    descriptor: {id: 'staffMembers', description: 'Staff members'},
    data: {hidden: true, disabled: null},
    errors: [],
    generation: 2
  });
  assert.equal(collection.hidden, true);
});

test('collection component forwards window requests and publishes semantic window state', async () => {
  let request;
  const context = {
    registerRequirement(requirement, listener) {
      listener({
        status: 'ready',
        descriptor: {id: 'staffMembers', description: 'Staff members'},
        data: {hidden: false, disabled: null},
        errors: [],
        generation: 1
      });
      return () => {};
    },
    async loadCollection(options) {
      request = options;
      return {
        descriptor: {id: 'staffMembers'},
        data: {window: {rows: [row()]}},
        rows: [row()],
        window: {
          offset: 20,
          requestedSize: 10,
          returnedCount: 1,
          totalCount: null,
          countAvailable: false,
          maximumSize: 100,
          hasPrevious: true,
          hasNext: false,
          previousOffset: 10,
          nextOffset: null,
          rangeStart: 21,
          rangeEnd: 21,
          ordering: 'ENCOUNTER'
        },
        errors: [],
        rowSelection: {_meta: {id: true}}
      };
    },
    createHydratedRowContext() {
      return {disconnect() {}};
    }
  };
  const collection = new CausewayCollectionElement();
  collection.member = 'staffMembers';
  collection.active = true;
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => collection.collectionState.status === 'ready');

  await collection.load({offset: 20, size: 10, force: true});

  assert.equal(request.offset, 20);
  assert.equal(request.size, 10);
  assert.equal(request.requestKey, collection);
  assert.equal(collection.collectionState.window.countAvailable, false);
  assert.equal(collection.collectionState.window.rangeStart, 21);
  assert.match(collection.innerHTML, /Dr Ada/);
  document.body.removeChild(collection);
});

test('collection component cancels an in-flight secondary read when disconnected', async () => {
  let capturedSignal;
  const context = {
    registerRequirement(requirement, listener) {
      listener({
        status: 'ready',
        descriptor: {id: 'staffMembers', description: 'Staff members'},
        data: {hidden: false, disabled: null},
        errors: [],
        generation: 1
      });
      return () => {};
    },
    loadCollection({signal}) {
      capturedSignal = signal;
      return new Promise((resolve, reject) => signal.addEventListener('abort', () => {
        const error = new Error('Aborted');
        error.name = 'AbortError';
        reject(error);
      }, {once: true}));
    }
  };
  const collection = new CausewayCollectionElement();
  collection.member = 'staffMembers';
  collection.active = true;
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => capturedSignal);
  document.body.removeChild(collection);
  assert.equal(capturedSignal.aborted, true);
});

test('collection component renders default object links, empty and partial-error states', () => {
  const collection = new CausewayCollectionElement();
  collection.member = 'staffMembers';
  collection.active = true;
  const baseState = {
    status: 'ready',
    descriptor: {id: 'staffMembers', description: 'Staff members'},
    data: {hidden: false, disabled: null},
    errors: [],
    generation: 1
  };
  collection.collectionState = {status: 'ready', data: {get: [row()]}, errors: []};
  collection.renderComponentState(baseState);
  assert.match(collection.innerHTML, /causeway-object-link/);

  collection.collectionState = {status: 'ready', data: {get: []}, errors: []};
  collection.renderComponentState(baseState);
  assert.match(collection.innerHTML, /No items/);

  collection.collectionState = {
    status: 'partial-error',
    data: {get: [row()]},
    errors: [{message: 'One row failed', path: ['staffMembers', 'get', 0]}]
  };
  collection.renderComponentState(baseState);
  assert.match(collection.innerHTML, /One row failed/);
});
