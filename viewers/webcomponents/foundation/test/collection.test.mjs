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
  createCriteriaWindowedRichSchemaTypes,
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
  configureCausewayGridWidgets,
  CausewayCollectionElement,
  CausewayGraphQLClient,
  ObjectContextController
} = await import('../src/index.mjs');

function row({id = 'staff-1', name = 'Dr Ada', code = 'ADA', version = '3'} = {}) {
  return {
    _meta: {id, logicalTypeName: STAFF_LOGICAL_TYPE, ...(version == null ? {} : {version}), title: name},
    name: {
      hidden: false,
      disabled: null,
      datatype: 'String',
      metadata: {friendlyName: 'Staff name', description: null, multiLine: null, labelPosition: 'LEFT'},
      get: name
    },
    code: {
      hidden: false,
      disabled: null,
      datatype: 'String',
      metadata: {friendlyName: 'Staff code', description: null, multiLine: null, labelPosition: 'LEFT'},
      get: code
    }
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
  column.id = 'name';
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

test('collection window criteria are discovered, transported and included in request identity', async () => {
  const rows = [row({id: 'staff-2', name: 'Dr Grace'})];
  const executor = createRichSchemaFixtureExecutor({
    types: createCriteriaWindowedRichSchemaTypes(),
    readResponses: [graphQLObjectResponse()],
    windowResponses: [collectionWindowResponse({
      rows,
      requestedSize: 2,
      totalCount: 1,
      ordering: 'REQUESTED',
      sortableMembers: ['name'],
      searchSupported: true,
      searchPrompt: 'Search staff member'
    })]
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
    offset: 0,
    size: 2,
    sortBy: 'name',
    sortDirection: 'DESCENDING',
    search: 'Grace'
  });

  assert.deepEqual(executor.windowCalls[0].variables, {
    object: {id: '42'},
    offset: 0,
    size: 2,
    sortBy: 'name',
    sortDirection: 'DESCENDING',
    search: 'Grace'
  });
  assert.match(executor.windowCalls[0].document, /sortBy: \$sortBy/);
  assert.match(executor.windowCalls[0].document, /search: \$search/);
  assert.match(executor.windowCalls[0].document, /sortableMembers/);
  assert.deepEqual(loaded.window.sortableMembers, ['name']);
  assert.equal(loaded.window.searchSupported, true);
  assert.equal(loaded.window.searchPrompt, 'Search staff member');
  assert.equal(loaded.window.ordering, 'REQUESTED');
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
  collection.id = 'staffMembers';
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
  assert.match(collection.innerHTML, /<cw-object-link/);
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

test('collection component resolves canonical and HTML headings without unmodifiable noise or rereads', async () => {
  let registrations = 0;
  let publish;
  const context = {
    registerRequirement(_requirement, listener) {
      registrations += 1;
      publish = listener;
      listener({
        status: 'ready',
        descriptor: {id: 'staffMembers'},
        data: {
          hidden: false,
          disabled: 'Cannot edit a mixed-in collection.',
          metadata: {friendlyName: 'Department staff', description: 'Current staff members.'}
        },
        errors: [],
        generation: 1
      });
      return () => {};
    }
  };
  const collection = new CausewayCollectionElement();
  collection.id = 'staffMembers';
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => collection.innerHTML.includes('Department staff'));

  assert.deepEqual(CausewayCollectionElement.observedAttributes, [
    'id',
    'named',
    'described-as',
    'description-as',
    'label',
    'active',
    'paged',
    'sortable',
    'filterable',
    'resizable-columns',
    'reorderable-columns'
  ]);
  for (const [value, expected] of [
    ['1', 1],
    ['100', 100],
    ['0', null],
    ['101', null],
    ['1.5', null],
    ['1e2', null],
    ['many', null],
    [' ', null]
  ]) {
    collection.setAttribute('paged', value);
    assert.equal(collection.paged, expected);
  }
  collection.paged = null;
  assert.equal(collection.hasAttribute('paged'), false);
  assert.equal(collection.resizableColumns, false);
  assert.equal(collection.reorderableColumns, false);
  collection.resizableColumns = true;
  collection.reorderableColumns = true;
  assert.equal(collection.resizableColumns, true);
  assert.equal(collection.reorderableColumns, true);
  collection.resizableColumns = false;
  collection.reorderableColumns = false;

  assert.match(collection.innerHTML, /class="causeway-collection-label causeway-member-tooltip"/);
  assert.match(collection.innerHTML, /class="causeway-collection-description">Current staff members\.<\/p>/);
  assert.match(collection.innerHTML, /data-tooltip="Current staff members\.\s+Cannot edit a mixed-in collection\."/);
  assert.match(collection.innerHTML, /aria-labelledby="causeway-collection-label-/);
  assert.match(collection.innerHTML, /aria-describedby="causeway-collection-description-[^\"]+ causeway-collection-reason-/);
  assert.match(collection.innerHTML, /causeway-visually-hidden">Cannot edit a mixed-in collection\.<\/span>/);

  publish({
    status: 'partial-error',
    descriptor: {id: 'staffMembers'},
    data: {
      hidden: false,
      disabled: null,
      metadata: {friendlyName: 'Department staff', description: null}
    },
    errors: [{message: 'Collection description is unavailable.'}],
    generation: 2
  });
  assert.match(collection.innerHTML, /Department staff/);
  assert.doesNotMatch(collection.innerHTML, /causeway-collection-description/);
  assert.doesNotMatch(collection.innerHTML, /causeway-error/);
  publish({
    status: 'ready',
    descriptor: {id: 'staffMembers'},
    data: {
      hidden: false,
      disabled: null,
      metadata: {friendlyName: 'Department staff', description: 'Current staff members.'}
    },
    errors: [],
    generation: 3
  });

  collection.label = 'Legacy staff label';
  assert.match(collection.innerHTML, /Legacy staff label/);
  collection.named = 'Priority team';
  collection.describedAs = 'People assigned to priority cases.';
  collection.descriptionAs = 'tooltip';
  assert.equal(collection.descriptionAs, 'tooltip');
  assert.match(collection.innerHTML, /class="causeway-collection-label causeway-member-tooltip"[^>]+data-tooltip="People assigned to priority cases\."/);
  assert.match(collection.innerHTML, /class="causeway-collection-description causeway-visually-hidden">People assigned to priority cases\.<\/p>/);

  collection.setAttribute('description-as', 'unsupported');
  assert.equal(collection.descriptionAs, 'label');
  assert.match(collection.innerHTML, /class="causeway-collection-description">People assigned to priority cases\.<\/p>/);
  assert.doesNotMatch(collection.innerHTML, /causeway-member-tooltip/);

  collection.describedAs = ' priority TEAM ';
  assert.doesNotMatch(collection.innerHTML, /causeway-collection-description/);
  assert.doesNotMatch(collection.innerHTML, /aria-describedby/);

  collection.removeAttribute('named');
  collection.removeAttribute('described-as');
  collection.removeAttribute('label');
  assert.match(collection.innerHTML, /Department staff/);
  assert.match(collection.innerHTML, /Current staff members/);
  assert.equal(registrations, 1);
  document.body.removeChild(collection);
});

test('collection description tooltips remain escaped, bounded and available in local states', () => {
  const collection = new CausewayCollectionElement();
  collection.id = 'staffMembers';
  collection.describedAs = 'Current <staff> & contractors.';
  collection.descriptionAs = 'TOOLTIP';
  const disabledReason = `Restricted ${'z'.repeat(280)}`;
  const data = {
    hidden: false,
    disabled: disabledReason,
    metadata: {friendlyName: 'Department staff', description: 'Metadata description'}
  };

  collection.renderComponentState({status: 'idle', data, errors: []});
  assert.match(collection.innerHTML, /class="causeway-collection-label causeway-member-tooltip"/);
  assert.match(collection.innerHTML, /data-tooltip="Current &lt;staff&gt; &amp; contractors\.\s+Restricted z+…"/);
  assert.match(collection.innerHTML, /causeway-collection-description causeway-visually-hidden[^>]*>Current &lt;staff&gt; &amp; contractors\.<\/p>/);
  assert.match(collection.innerHTML, /aria-describedby="causeway-collection-description-[^\"]+ causeway-collection-reason-/);
  assert.ok(collection.innerHTML.match(/data-tooltip="([\s\S]*?)"/)?.[1].length < 520);

  collection.renderComponentState({status: 'terminal-error', data, errors: [{message: 'Unavailable'}]});
  assert.match(collection.innerHTML, /causeway-collection-label causeway-member-tooltip/);
  assert.match(collection.innerHTML, /role="alert">Unavailable/);

  collection.renderComponentState({status: 'ready', data, errors: []});
  assert.match(collection.innerHTML, /causeway-collection-label causeway-member-tooltip/);
  assert.match(collection.innerHTML, /Load Department staff/);
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
  collection.id = 'staffMembers';
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

test('collection host owns server-backed sort and search criteria across native reloads', async () => {
  const requests = [];
  const context = {
    registerRequirement(_requirement, listener) {
      listener({
        status: 'ready',
        descriptor: {id: 'staffMembers'},
        data: {hidden: false, disabled: null},
        errors: [],
        generation: 1
      });
      return () => {};
    },
    async loadCollection(options) {
      requests.push(options);
      const currentRows = [row({id: `staff-${requests.length}`, name: `Dr ${requests.length}`})];
      return {
        descriptor: {id: 'staffMembers'},
        data: {window: {rows: currentRows}},
        rows: currentRows,
        window: {
          offset: 0,
          requestedSize: 2,
          returnedCount: 1,
          totalCount: 1,
          countAvailable: true,
          maximumSize: 100,
          hasPrevious: false,
          hasNext: false,
          previousOffset: null,
          nextOffset: null,
          rangeStart: 1,
          rangeEnd: 1,
          ordering: options.sortBy ? 'REQUESTED' : 'ENCOUNTER',
          sortableMembers: ['name'],
          searchSupported: true,
          searchPrompt: 'Search owners'
        },
        errors: [],
        rowDescription: {members: new Map([['name', {value: {typeRef: {kind: 'SCALAR', name: 'String'}}}]])},
        rowSelection: {_meta: {id: true}, name: {get: true}}
      };
    },
    createHydratedRowContext() {
      return {disconnect() {}};
    }
  };
  const collection = new CausewayCollectionElement();
  collection.id = 'staffMembers';
  collection.columns = [{member: 'name', label: 'Name'}];
  collection.sortable = true;
  collection.filterable = true;
  collection.paged = 2;
  collection.active = true;
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => collection.collectionState.status === 'ready');
  collection.acceptGridResponsiveState(true);

  assert.match(collection.innerHTML, /data-causeway-collection-sort="name"/);
  assert.match(collection.innerHTML, /causeway-collection-sort-indicator[^>]*aria-hidden="true">↕/);
  assert.match(collection.innerHTML, /Search owners/);
  assert.equal(collection.gridQualification.reason, 'ordering-not-deterministic');
  assert.equal(requests[0].sortBy, null);
  assert.equal(requests[0].search, '');

  const sortButton = document.createElement('button');
  sortButton.setAttribute('data-causeway-collection-sort', 'name');
  collection.appendChild(sortButton);
  sortButton.dispatchEvent(new Event('click', {bubbles: true}));
  await waitFor(() => requests.length === 2 && collection.collectionState.status === 'ready');
  assert.equal(requests[1].offset, 0);
  assert.equal(requests[1].sortBy, 'name');
  assert.equal(requests[1].sortDirection, 'ASCENDING');
  assert.equal(collection.gridQualification.reason, 'ordering-not-deterministic');

  sortButton.dispatchEvent(new Event('click', {bubbles: true}));
  await waitFor(() => requests.length === 3 && collection.collectionState.status === 'ready');
  assert.equal(requests[2].sortDirection, 'DESCENDING');
  assert.equal(collection.gridQualification.reason, 'ordering-not-deterministic');

  const search = document.createElement('input');
  search.setAttribute('data-causeway-collection-search', '');
  search.value = '  Ada  ';
  collection.appendChild(search);
  search.dispatchEvent(new Event('input', {bubbles: true}));
  await waitFor(() => requests.length === 4 && collection.collectionState.status === 'ready', 1000);
  assert.equal(requests[3].offset, 0);
  assert.equal(requests[3].search, 'Ada');
  assert.equal(requests[3].sortDirection, 'DESCENDING');

  collection.filterable = false;
  await waitFor(() => requests.length === 5 && collection.collectionState.status === 'ready');
  assert.equal(requests[4].search, null);
  assert.doesNotMatch(collection.innerHTML, /data-causeway-collection-search/);
  document.body.removeChild(collection);
});

test('collection host owns immutable Grid qualification diagnostics policy recovery and bounded ranges', async () => {
  configureCausewayGridWidgets({enabled: true});
  const rangeRequests = [];
  let additionalDisconnects = 0;
  let reportedTotal = 40;
  let reportedOrdering = 'CONFIGURED';
  const context = {
    registerRequirement(_requirement, listener) {
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
      rangeRequests.push(options);
      const offset = options.offset ?? 0;
      const requestedSize = options.size ?? 20;
      const currentRows = [row({id: `staff-${offset + 1}`, name: `Dr ${offset + 1}`})];
      const memberDescriptor = {value: {typeRef: {kind: 'SCALAR', name: 'String', ofType: null}}};
      return {
        descriptor: {id: 'staffMembers'},
        data: {window: {rows: currentRows}},
        rows: currentRows,
        window: {
          offset,
          requestedSize,
          returnedCount: 1,
          totalCount: reportedTotal,
          countAvailable: Number.isSafeInteger(reportedTotal),
          maximumSize: 100,
          hasPrevious: offset > 0,
          hasNext: offset + 1 < 40,
          previousOffset: offset > 0 ? Math.max(0, offset - requestedSize) : null,
          nextOffset: offset + 1 < 40 ? offset + 1 : null,
          rangeStart: offset + 1,
          rangeEnd: offset + 1,
          ordering: reportedOrdering
        },
        errors: [],
        rowDescription: {members: new Map([['name', memberDescriptor]])},
        rowSelection: {_meta: {id: true}, name: {get: true}}
      };
    },
    createHydratedRowContext(hydratedRow) {
      const additional = hydratedRow._meta.id !== 'staff-1';
      return {disconnect() {
        if (additional) additionalDisconnects += 1;
      }};
    }
  };
  const collection = new CausewayCollectionElement();
  const gridAdapter = {presentation: null};
  collection.querySelector = selector => selector.includes('cw-collection-grid') ? gridAdapter : null;
  collection.id = 'staffMembers';
  collection.columns = [{member: 'name', label: 'Name'}];
  collection.active = true;
  collection.acceptGridResponsiveState(true);
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => collection.collectionState.status === 'ready');

  assert.equal(collection.gridQualification.qualified, true);
  assert.equal(collection.gridQualification.presentation, 'grid-virtual');
  assert.equal(Object.isFrozen(collection.gridQualification), true);
  assert.equal(collection.dataset.causewayGridOrdering, 'CONFIGURED');
  assert.equal(collection.dataset.causewayGridCount, 'available');
  assert.equal(collection.dataset.causewayGridResponsive, 'wide');
  assert.equal(gridAdapter.presentation.mode, 'virtual');
  assert.deepEqual(gridAdapter.presentation.columns.map(column => column.member), ['_meta', 'name']);
  assert.equal(gridAdapter.presentation.rows[0].identity.id, 'staff-1');
  assert.equal(gridAdapter.presentation.totalCount, 40);
  assert.equal(gridAdapter.presentation.resizableColumns, false);
  assert.equal(gridAdapter.presentation.reorderableColumns, false);
  assert.equal(rangeRequests.length, 1);

  const range = await collection.requestCollectionRange({offset: 20, size: 20});
  const repeated = await collection.requestCollectionRange({offset: 20, size: 20});
  assert.equal(range, repeated);
  assert.equal(range.window.offset, 20);
  assert.equal(rangeRequests.length, 2);
  assert.equal(rangeRequests[1].cache, false);
  assert.notEqual(rangeRequests[1].requestKey, collection);

  reportedTotal = 41;
  await assert.rejects(
    gridAdapter.presentation.rangeProvider({offset: 30, size: 10}),
    {name: 'AbortError'}
  );
  await waitFor(() => collection.collectionState.status === 'ready'
    && collection.collectionState.window.totalCount === 41);
  assert.equal(rangeRequests.length, 4, 'changed range metadata must trigger one authoritative first-window refresh');
  assert.equal(rangeRequests[3].offset, 0);
  assert.equal(rangeRequests[3].force, true);

  collection.acceptGridResponsiveState(false);
  assert.equal(additionalDisconnects, 2);
  await assert.rejects(collection.requestCollectionRange({offset: 0, size: 20}), /no current bounded range broker/);
  collection.acceptGridResponsiveState(true);
  assert.equal(collection.gridQualification.presentation, 'grid-virtual');

  reportedOrdering = 'ENCOUNTER';
  await collection.load({force: true});
  assert.equal(collection.gridQualification.reason, 'ordering-not-deterministic');
  await assert.rejects(collection.requestCollectionRange({offset: 0, size: 20}), /no current bounded range broker/);
  reportedOrdering = 'CONFIGURED';
  reportedTotal = null;
  await collection.load({force: true});
  assert.equal(collection.gridQualification.presentation, 'grid-bounded');
  assert.equal(gridAdapter.presentation.mode, 'bounded');
  assert.match(collection.innerHTML, /data-causeway-grid-next/);
  assert.doesNotMatch(collection.innerHTML, /Items 1–1 of/);

  configureCausewayGridWidgets({enabled: false});
  assert.equal(collection.gridQualification.reason, 'policy-native');
  configureCausewayGridWidgets({enabled: true});
  assert.equal(collection.gridQualification.presentation, 'grid-bounded');

  collection.resizableColumns = true;
  collection.reorderableColumns = true;
  assert.equal(gridAdapter.presentation.resizableColumns, true);
  assert.equal(gridAdapter.presentation.reorderableColumns, true);

  const requestsBeforePaging = rangeRequests.length;
  collection.paged = 10;
  await waitFor(() => rangeRequests.length === requestsBeforePaging + 1
    && collection.collectionState.status === 'ready'
    && rangeRequests.at(-1).size === 10);
  assert.equal(collection.paged, 10);
  assert.equal(rangeRequests.at(-1).offset, 0);
  assert.equal(rangeRequests.at(-1).size, 10);
  assert.equal(rangeRequests.at(-1).force, true);
  assert.equal(collection.gridQualification.presentation, 'grid-bounded');
  assert.match(collection.innerHTML, /data-causeway-grid-next/);

  collection.acceptGridResponsiveState(false);
  assert.equal(collection.gridQualification.reason, 'narrow');
  assert.match(collection.innerHTML, /data-causeway-grid-next/, 'declarative paging remains available with native presentation');
  collection.acceptGridResponsiveState(true);

  const requestsBeforeInvalidPaging = rangeRequests.length;
  collection.paged = 101;
  await waitFor(() => rangeRequests.length === requestsBeforeInvalidPaging + 1
    && collection.collectionState.status === 'ready'
    && rangeRequests.at(-1).size == null);
  assert.equal(collection.paged, null);
  collection.resizableColumns = false;
  collection.reorderableColumns = false;
  assert.equal(gridAdapter.presentation.resizableColumns, false);
  assert.equal(gridAdapter.presentation.reorderableColumns, false);
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
  collection.id = 'staffMembers';
  collection.active = true;
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => capturedSignal);
  document.body.removeChild(collection);
  assert.equal(capturedSignal.aborted, true);
});

test('collection component renders default object links, empty and partial-error states', () => {
  const collection = new CausewayCollectionElement();
  collection.id = 'staffMembers';
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
  assert.match(collection.innerHTML, /cw-object-link/);

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
